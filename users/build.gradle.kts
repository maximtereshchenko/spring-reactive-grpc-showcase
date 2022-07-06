dependencies {
    testCompileOnly(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
}

tasks.test {
    useJUnitPlatform()
}