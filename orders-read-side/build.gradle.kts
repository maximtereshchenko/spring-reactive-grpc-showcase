plugins {
    application
}

application {
    mainClass.set("com.github.xini1.orders.read.Main")
}

dependencies {
    implementation(project("::common"))
    implementation(libs.spring.actuator)
    implementation(libs.spring.aws.messaging)
    implementation(libs.aws.dynamodb)

    testCompileOnly(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.localstack)
    testImplementation(testFixtures(project("::common")))
}

tasks.test {
    useJUnitPlatform()
}