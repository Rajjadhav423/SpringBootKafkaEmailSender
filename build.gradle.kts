import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("java")
}

group = "com.example.email"
version = "0.0.1-SNAPSHOT"
val lombokVersion = "1.18.32"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
    }
}

project(":common") {
    apply(plugin = "java-library")

    dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.5"))
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("com.fasterxml.jackson.core:jackson-databind")
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    }
}

project(":email-producer") {
    apply(plugin = "org.springframework.boot")

    dependencies {
        implementation(project(":common"))
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.kafka:spring-kafka")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
        runtimeOnly("com.mysql:mysql-connector-j")
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.kafka:spring-kafka-test")
        testImplementation("com.h2database:h2")
    }
}

project(":email-consumer") {
    apply(plugin = "org.springframework.boot")

    dependencies {
        implementation(project(":common"))
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.kafka:spring-kafka")
        implementation("org.springframework.boot:spring-boot-starter-mail")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
        runtimeOnly("com.mysql:mysql-connector-j")
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.kafka:spring-kafka-test")
        testImplementation("com.h2database:h2")
    }
}
