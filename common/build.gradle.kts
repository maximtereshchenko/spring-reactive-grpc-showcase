import com.google.protobuf.gradle.*

plugins {
    alias(libs.plugins.protobuf)
    idea
    `java-library`
    `java-test-fixtures`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.aws.dynamodb)
    api(libs.grpc)
    implementation(libs.annotation.api)
}

protobuf {
    protoc {
        artifact = libs.protoc.compiler.get().toString()
    }
    plugins {
        id("grpc") {
            artifact = libs.protoc.plugin.get().toString()
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}