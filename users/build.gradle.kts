plugins {
    application
}

application {
    mainClass.set("com.github.xini1.users.Main")
}

dependencies {
    implementation(project("::common"))
    implementation(libs.spring.mongodb)
    implementation(libs.jwt)
    implementation(libs.spring.actuator)
    implementation(libs.spring.aws)

    testCompileOnly(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.localstack)
}

tasks.test {
    useJUnitPlatform()
}