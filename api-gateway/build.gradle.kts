plugins {
    application
}

application {
    mainClass.set("com.github.xini1.apigateway.Main")
}

dependencies {
    implementation(project("::common"))
    implementation(libs.spring.webflux)
    implementation(libs.spring.actuator)

    testCompileOnly(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.assertj)
    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(testFixtures(project("::common")))
}

tasks.test {
    useJUnitPlatform()
}