FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

COPY src src
# Versión con caché (descomentar más adelante para acelerar builds):
RUN --mount=type=secret,id=gradle_properties,target=/root/.gradle/gradle.properties \
     --mount=type=cache,target=/root/.gradle/caches \
     ./gradlew bootJar --no-daemon -x test



FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
