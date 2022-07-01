plugins {
    java
}

group = "com.github.xini1"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.assertj:assertj-core:3.23.1")
}

tasks.test {
    useJUnitPlatform()
}