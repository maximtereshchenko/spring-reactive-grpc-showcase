rootProject.name = "spring-reactive-grpc-showcase"

include("common")
include("orders-write-side")
include("orders-read-side")
include("users")
include("api-gateway")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("junit", "5.8.2")
            version("grpc", "1.47.0")
            version("spring-boot-starter", "2.7.1")
            version("testcontainers", "1.17.3")
            version("aws", "1.11.792")

            library("junit-api", "org.junit.jupiter", "junit-jupiter-api")
                .versionRef("junit")
            library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine")
                .versionRef("junit")

            library("grpc", "io.grpc", "grpc-all").versionRef("grpc")
            library("protoc-plugin", "io.grpc", "protoc-gen-grpc-java").versionRef("grpc")

            library("spring-webflux", "org.springframework.boot", "spring-boot-starter-webflux")
                .versionRef("spring-boot-starter")
            library("spring-actuator", "org.springframework.boot", "spring-boot-starter-actuator")
                .versionRef("spring-boot-starter")
            library("spring-test", "org.springframework.boot", "spring-boot-starter-test")
                .versionRef("spring-boot-starter")

            library("testcontainers", "org.testcontainers", "testcontainers")
                .versionRef("testcontainers")
            library("testcontainers-junit", "org.testcontainers", "junit-jupiter")
                .versionRef("testcontainers")
            library("testcontainers-localstack", "org.testcontainers", "localstack")
                .versionRef("testcontainers")

            library("aws-core", "com.amazonaws", "aws-java-sdk-core").versionRef("aws")
            library("aws-dynamodb", "com.amazonaws", "aws-java-sdk-dynamodb").versionRef("aws")

            library(
                "spring-aws-messaging",
                "org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE"
            )
            library("assertj", "org.assertj:assertj-core:3.23.1")
            library("protoc-compiler", "com.google.protobuf:protoc:3.0.0")
            library("annotation-api", "javax.annotation:javax.annotation-api:1.3.2")
            library("jwt", "com.auth0:java-jwt:4.0.0")

            plugin("protobuf", "com.google.protobuf").version("0.8.19")
        }
    }
}