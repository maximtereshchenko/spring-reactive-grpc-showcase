import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    alias(libs.plugins.protobuf)
    idea
    `java-library`
}

repositories {
    gradlePluginPortal()
}

dependencies {
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