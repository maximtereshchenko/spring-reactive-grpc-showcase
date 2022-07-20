plugins {
    java
}

dependencies {
    implementation(project("::common"))
    implementation(libs.spring.mongodb)
    implementation(libs.jwt)

    testCompileOnly(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mongodb)
}

tasks.test {
    useJUnitPlatform()
}