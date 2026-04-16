plugins {
    java
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
