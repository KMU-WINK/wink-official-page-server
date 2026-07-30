FROM --platform=$BUILDPLATFORM gradle:9.6.1-jdk25-alpine AS builder

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN gradle clean :bootstrap:bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine

ENV SPRING_PROFILES_ACTIVE=prod

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /home/gradle/project/bootstrap/build/libs/*.jar /app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
