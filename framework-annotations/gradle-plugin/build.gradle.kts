plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.dokka)
    `java-gradle-plugin`
    `maven-publish`
    signing
}

group = "com.grarcht.shuttle"
val archivesName = "framework-annotations-gradle-plugin"
version = libs.versions.shuttle.get()

val publishToBuildLocal = true

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

java {
    withSourcesJar()
    withJavadocJar()
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

// java-gradle-plugin auto-creates a pluginMaven publication; configure it with POM metadata
// rather than creating a second publication that duplicates from(components["java"]).
afterEvaluate {
    publishing {
        publications {
            named<MavenPublication>("pluginMaven") {
                artifactId = archivesName

                pom {
                    name.set("Shuttle Annotations Gradle Plugin")
                    description.set("Gradle plugin for the Shuttle framework.")
                    url.set("https://github.com/grarcht/Shuttle")

                    scm {
                        connection.set("scm:git@github.com:grarcht/Shuttle.git")
                        developerConnection.set("scm:git@github.com:grarcht/Shuttle.git")
                        url.set("https://github.com/grarcht/Shuttle")
                    }

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://github.com/grarcht/Shuttle/blob/main/LICENSE.md")
                        }
                    }

                    developers {
                        developer {
                            id.set(System.getenv("developerId"))
                            name.set(System.getenv("developerName"))
                            email.set(System.getenv("developerEmail"))
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                name = "mavencentral"

                url = if (publishToBuildLocal) {
                    uri("${layout.buildDirectory.get()}/repos/distribution")
                } else {
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                if (!publishToBuildLocal) {
                    credentials {
                        username = System.getenv("ossrhUsername")
                        password = System.getenv("ossrhPassword")
                    }
                }
            }
        }
    }

    signing {
        setRequired(provider { !publishToBuildLocal && gradle.taskGraph.hasTask("publish") })

        val signingKeyId = System.getenv("signingKeyId")
        val signingKey = System.getenv("signingSecretKeyRingFile")
        val signingPassword = System.getenv("signingPassword")

        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

        sign(publishing.publications["pluginMaven"])
    }
}
