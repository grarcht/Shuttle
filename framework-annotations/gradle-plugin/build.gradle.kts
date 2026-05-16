plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    `java-gradle-plugin`
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
    sourceSets {
        main {
            kotlin.srcDirs("src/main/java")
        }
    }
}

val generateVersionFile by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/version")
    val shuttleVersion = libs.versions.shuttle.get()
    outputs.dir(outputDir)
    doLast {
        val versionFile = outputDir.get()
            .file("com/grarcht/shuttle/framework/gradle/ShuttleVersion.kt").asFile
        versionFile.parentFile.mkdirs()
        versionFile.writeText(
            "package com.grarcht.shuttle.framework.gradle\n\n" +
                "internal const val PLUGIN_VERSION = \"$shuttleVersion\"\n"
        )
    }
}

sourceSets.main {
    kotlin.srcDir(generateVersionFile)
}

gradlePlugin {
    plugins {
        create("shuttleCargo") {
            id = "com.grarcht.shuttle.cargo"
            implementationClass = "com.grarcht.shuttle.framework.gradle.ShuttleCargoGradlePlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.classpathDeps.kotlinGradlePlugin)
}
