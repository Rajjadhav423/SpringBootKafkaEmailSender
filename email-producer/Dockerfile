FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

COPY gradlew gradle settings.gradle.kts build.gradle.kts /workspace/
COPY common /workspace/common
COPY email-producer /workspace/email-producer
COPY email-consumer /workspace/email-consumer

RUN ./gradlew :email-producer:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/email-producer/build/libs/email-producer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
