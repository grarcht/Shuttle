plugins {
    `java-platform`
    `maven-publish`
    signing
}

group = "com.grarcht.shuttle"
val archivesName = "framework-bom"
version = libs.versions.shuttle.get()

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":framework"))
        api(project(":framework-integrations-persistence"))
        api(project(":framework-integrations-extensions-room"))
        api(project(":framework-addons-navigation-component"))
        api(project(":framework-annotations"))
        api(project(":framework-annotations-processor"))
        api(project(":framework-annotations-compiler-plugin"))
        api(project(":framework-annotations-gradle-plugin"))
    }
}

val publishToBuildLocal = true

publishing {
    publications {
        create<MavenPublication>("distribution") {
            artifactId = archivesName
            from(components["javaPlatform"])

            pom {
                name.set("Shuttle BOM")
                description.set("Shuttle Bill of Materials to manage versions of Shuttle artifacts.")
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

