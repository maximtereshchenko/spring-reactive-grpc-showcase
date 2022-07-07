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
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api")
                .versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine")
                .versionRef("junit")
            library("assertj-core", "org.assertj:assertj-core:3.23.1")
        }
    }
}