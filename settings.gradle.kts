pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "Shuttle"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":framework-annotations")
include(":framework")

// Demo Modules
include(":demos-core-lib")
include(":demo-mvc")
include(":demo-mvvm")
include(":demo-mvvm-with-a-service")
include(":demo-mvi-with-compose")
include(":demo-mvvm-with-compose")
include(":demo-mvvm-with-navigation")
include(":demo-mvi-with-compose")

project(":demos-core-lib").projectDir = File(settingsDir, "demos/core")
project(":demo-mvc").projectDir = File(settingsDir, "demos/mvc")
project(":demo-mvvm").projectDir = File(settingsDir, "demos/mvvm")
project(":demo-mvvm-with-a-service").projectDir = File(settingsDir, "demos/mvvm-with-a-service")
project(":demo-mvi-with-compose").projectDir = File(settingsDir, "demos/mvi-with-compose")
project(":demo-mvvm-with-compose").projectDir = File(settingsDir, "demos/mvvm-with-compose")
project(":demo-mvvm-with-navigation").projectDir = File(settingsDir, "demos/mvvm-with-navigation")

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
