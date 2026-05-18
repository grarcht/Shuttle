pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Shuttle"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":framework-annotations")
project(":framework-annotations").projectDir = File(settingsDir, "framework-annotations/annotations")
include(":framework-annotations-processor")
project(":framework-annotations-processor").projectDir = File(settingsDir, "framework-annotations/annotations-processor")
include(":framework-annotations-compiler-plugin")
project(":framework-annotations-compiler-plugin").projectDir = File(settingsDir, "framework-annotations/compiler-plugin")
include(":framework-annotations-gradle-plugin")
project(":framework-annotations-gradle-plugin").projectDir = File(settingsDir, "framework-annotations/gradle-plugin")
include(":framework")

// Demo Modules
include(":demos-core-foundation")
include(":demos-core-compose")
include(":demos-core-di")
include(":demo-mvc")
include(":demo-mvvm")
include(":demo-mvi-with-compose")
include(":demo-mvvm-with-a-service")
include(":demo-mvvm-with-compose")
include(":demo-mvvm-with-compose-and-navigation")
include(":demo-mvvm-with-process-death")

project(":demos-core-foundation").projectDir = File(settingsDir, "demos/core/foundation")
project(":demos-core-compose").projectDir = File(settingsDir, "demos/core/compose")
project(":demos-core-di").projectDir = File(settingsDir, "demos/core/di")
project(":demo-mvc").projectDir = File(settingsDir, "demos/mvc")
project(":demo-mvi-with-compose").projectDir = File(settingsDir, "demos/mvi-with-compose")
project(":demo-mvvm").projectDir = File(settingsDir, "demos/mvvm")
project(":demo-mvvm-with-a-service").projectDir = File(settingsDir, "demos/mvvm-with-a-service")
project(":demo-mvvm-with-compose").projectDir = File(settingsDir, "demos/mvvm-with-compose")
project(":demo-mvvm-with-compose-and-navigation").projectDir = File(settingsDir, "demos/mvvm-with-compose-and-navigation")
project(":demo-mvvm-with-process-death").projectDir = File(settingsDir, "demos/mvvm-with-process-death")

// Integration Modules
include(":framework-integrations-persistence")
project(":framework-integrations-persistence").projectDir =
        File(settingsDir, "framework-integrations/persistence")

// Extension Modules
include(":framework-integrations-extensions-room")
project(":framework-integrations-extensions-room").projectDir =
        File(settingsDir, "framework-integrations-extensions/room")

// Add-On Modules
include(":framework-addons-navigation-component")
project(":framework-addons-navigation-component").projectDir =
        File(settingsDir, "framework-addons/navigation-component")
