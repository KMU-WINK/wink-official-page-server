plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.sentry)
}

dependencies {
    implementation(project(":presentation"))
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    implementation(project(":shared"))

    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.sentry)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    developmentOnly(platform(libs.spring.boot.dependencies))
    developmentOnly(libs.spring.boot.devtools)
}

tasks.bootJar {
    archiveFileName.set("wink-official-page.jar")
}

sentry {
    includeSourceContext = listOf("SENTRY_ORG", "SENTRY_PROJECT", "SENTRY_AUTH_TOKEN").all {
        !System.getenv(it).isNullOrBlank()
    }

    org = System.getenv("SENTRY_ORG")
    projectName = System.getenv("SENTRY_PROJECT")
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}
