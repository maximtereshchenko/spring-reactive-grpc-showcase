allprojects {
    group = "com.github.xini1"
    version = "1.0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}

task("version") {
    doLast {
        println(project.version)
    }
}