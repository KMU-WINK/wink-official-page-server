# WINK backend

Kotlin, Spring Boot, MongoDB, and Redis power the WINK API. The build uses a
Gradle version catalog and explicit modules so dependencies point toward the
domain instead of toward framework implementations.

## Modules

| Module | Responsibility | Depends on |
| --- | --- | --- |
| `shared` | Framework-neutral response models and small shared contracts | — |
| `domain` | Framework-neutral domain entities, rules, and error codes | `shared` |
| `application` | Use cases and outbound ports | `domain`, `shared` |
| `infrastructure` | Mongo/Redis documents and adapters, S3, mail, SMS, and JWT | `application`, `domain`, `shared` |
| `presentation` | HTTP controllers and exception translation | `application` |
| `bootstrap` | Spring Boot composition root and runtime configuration | all modules |

Dependency and plugin versions live in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml).

The domain module has no Spring Data, MongoDB, Redis, BSON, or Jackson
dependency. Repository contracts live in the application module as outbound
ports. Mongo/Redis documents, indexes, keyspaces, DBRefs, converters, and
adapter implementations live in infrastructure, preserving the existing
persisted data shape while dependencies continue to point inward.

## Consistency model

MongoDB remains the system of record. Normal local development works with a
standalone Mongo instance and does not require replica-set transactions.

External notifications use Mongo outboxes:

1. The use case writes deterministic, unique outbox messages before advancing
   the recruitment step.
2. A worker atomically claims pending messages with `findAndModify`, but only
   sends them after the corresponding recruitment step is visible in Mongo.
   This state barrier keeps partially enqueued messages dormant when a
   standalone-Mongo write fails between the outbox and aggregate updates.
3. Final-pass registration invites are created by the worker after the
   `INTERVIEW_END` state barrier is visible, so `PreUser` rows are not committed
   ahead of the recruitment aggregate.
4. Messages that wait too long for an aggregate state transition are canceled,
   and failed deliveries return to `PENDING` with exponential backoff before they
   move to `FAILED`. Terminal SMS records retain delivery audit metadata while
   scrubbing the phone number, rendered message, and form reference.

This prevents duplicate enqueueing when an admin retries a finalization request.
Member invites, recruit edit-link emails, and password-reset emails use a
separate mail outbox. The mail worker stores frozen recipient/title/html, waits
until the token it references is visible in Mongo or Redis, and then calls the
SMTP adapter synchronously so `PROCESSED` is written only after the send call
returns. The same finite wait, lock recovery, and backoff rules apply. SMTP
connect/read/write timeouts are bounded below the outbox lock timeout, and
terminal mail records scrub the recipient, rendered HTML, and barrier token so
expired invitation and reset links are not retained in Mongo.

Delivery is at-least-once because neither the SMS provider nor SMTP accepts an
application idempotency key: a process failure after the provider accepts a
message but before Mongo records `PROCESSED` can still cause one duplicate
delivery.

S3 uploads omit object ACLs by default so they work with
`BucketOwnerEnforced`. Serve public objects through a bucket policy or CDN. Set
`AWS_S3_PUBLIC_READ_ACL_ENABLED=true` only for a legacy ACL-enabled bucket that
still relies on per-object `public-read`.

## Authentication

Access and refresh tokens are issued as `HttpOnly`, `SameSite=Lax` cookies.
Unsafe browser requests use Spring Security's `XSRF-TOKEN` /
`X-XSRF-TOKEN` double-submit protection. Route authorization is default-deny;
only the explicitly listed public endpoints are anonymous.

Public form and one-time-token endpoints are rate-limited in Redis. Application
startup verifies both MongoDB and Redis before the readiness endpoint can
return success. When Redis runs on the Docker host, map
`host.docker.internal` to the custom Docker network gateway and set
`REDIS_HOST=host.docker.internal`.

Set
`RECRUIT_PROXY_CLIENT_HMAC_KEY` to a separate Base64-encoded 32-byte key and
configure the exact same value in the frontend. It authenticates the client
address forwarded by the frontend BFF; it must not reuse the PII encryption
key. Generate it independently with `openssl rand -base64 32`.

## Recruitment personal data

Recruitment forms and pending notification outboxes encrypt personal data with
AES-256-GCM. Set `RECRUIT_PII_ENCRYPTION_KEY` to a secret, Base64-encoded
32-byte key before starting the application. Generate one with:

```bash
openssl rand -base64 32
```

Store this key in the deployment secret manager; never commit it. The
application intentionally fails to start when the key is missing or malformed.
The key is part of the persisted data format: losing it makes existing
recruitment data unrecoverable, and replacing it makes that data unreadable.
Do not rotate it without first implementing and running a dedicated,
old-key-to-new-key migration.

At startup, `APP_RECRUIT_PRIVACY_MIGRATION_ENABLED=true` runs an idempotent
migration in batches of `APP_RECRUIT_PRIVACY_MIGRATION_BATCH_SIZE` (default
`100`). It encrypts legacy form and pending-outbox payloads, creates blind
indexes for exact-match lookups, replaces edit tokens with keyed digests, and
scrubs terminal outbox payloads. The application does not become ready if a
legacy document cannot be migrated safely. Leave this migration enabled until
every deployed instance uses the encrypted schema.

Retention is disabled by default because enabling it is destructive.
`APP_RECRUIT_PRIVACY_RETENTION_ENABLED=true` explicitly enables deletion of
recruitment forms after
`max(recruitEndDate, interviewEndDate) + APP_RECRUIT_PRIVACY_RETENTION_DAYS`
(default `90`) has elapsed. It runs at `APP_RECRUIT_PRIVACY_RETENTION_CRON`
(default `03:30` daily) in `APP_RECRUIT_PRIVACY_RETENTION_ZONE` (default
`Asia/Seoul`). Recruitment schedules and SMS configuration remain intact.
The startup migration uses the same zone when converting legacy date values.

Terminal mail and SMS outboxes receive a `purgeAt` timestamp 30 days after
completion and are deleted by MongoDB TTL indexes. Keep
`MONGODB_AUTO_INDEX_CREATION=true`, or create the declared `purgeAt` TTL indexes
explicitly before deployment if automatic index creation is disabled. MongoDB
TTL deletion is asynchronous, so records may remain briefly after `purgeAt`.

## Run

Copy `.env.template`, provide the required secrets, and run the composition
module:

```bash
./gradlew :bootstrap:bootRun
```

Create the production artifact with:

```bash
./gradlew :bootstrap:bootJar
```

The runnable JAR is written to `bootstrap/build/libs/wink-official-page.jar`.
