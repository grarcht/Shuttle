plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.dokka)
    `maven-publish`
    signing
}

group = "com.grarcht.shuttle"
val archivesName = "framework-annotations"
version = libs.versions.shuttle.get()

val publishToBuildLocal = true

kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("distribution") {
            artifactId = archivesName
            from(components["java"])

            pom {
                name.set("Shuttle Annotations")
                description.set("Annotations for the Shuttle framework.")
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

    sign(publishing.publications["distribution"])
}
