plugins {
    application
}

application {
    mainClass.set("com.github.xini1.orders.write.Main")
}

dependencies {
    implementation(project("::common"))
    implementation(libs.spring.mongodb)
    implementation(libs.spring.kafka)
    implementation(libs.reactor.kafka)
    implementation(libs.spring.actuator)

    testCompileOnly(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.kafka)
}

tasks.test {
    useJUnitPlatform()
}