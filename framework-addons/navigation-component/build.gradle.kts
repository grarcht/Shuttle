import org.gradle.api.publish.maven.MavenPublication
import java.io.File

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.signing)
    alias(libs.plugins.maven.publish)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("documentation/kotlin"))
    }
    dokkaSourceSets.register("main") {
        sourceRoots.from(file("src/main/java"))
    }
}

android {
    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    testOptions.unitTests.isReturnDefaultValues = true
    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    buildFeatures {
        buildConfig = true
    }

    namespace = "com.grarcht.shuttle.framework.addons.navigation"
}

dependencies {
    implementation(libs.jetbrainsKotlinDeps.stdlib)
    implementation(libs.android.navigationFragmentKtx)
    implementation(libs.android.annotationJvm)
    implementation(libs.android.coreKtx)
    implementation(project(":framework"))

    androidTestImplementation(libs.testingDeps.junit.junit5AndroidTestCore)
    androidTestRuntimeOnly(libs.testingDeps.junit.junit5AndroidTestRunner)
    androidTestImplementation(libs.testingDeps.mockito.core)
    androidTestImplementation(libs.testingDeps.mockito.kotlin)
    androidTestImplementation(libs.testingDeps.junit.jupiterApi)
    androidTestImplementation(libs.testingDeps.kotlin.coroutines)
    androidTestImplementation(libs.testingDeps.junit.androidCore)
    androidTestImplementation(libs.testingDeps.androidTest.monitor)
    androidTestRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    androidTestRuntimeOnly(libs.testingDeps.junit.platformCommons) // enables a package of tests to be run

    testImplementation(libs.testingDeps.mockito.core)
    testImplementation(libs.testingDeps.mockito.kotlin)
    testImplementation(libs.testingDeps.mockito.inline)
    testImplementation(libs.testingDeps.androidTest.monitor)
    testImplementation(libs.testingDeps.junit.jupiterApi)
    testImplementation(libs.testingDeps.kotlin.coroutines)
    testImplementation(libs.testingDeps.junit.androidCore)

    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    testRuntimeOnly(libs.testingDeps.junit.platformCommons) // enables a package of tests to be run
}

group = "com.grarcht.shuttle"
val archivesName = "framework-addons-navigation-component"
extensions.getByType<BasePluginExtension>().archivesName.set(archivesName)
version = libs.versions.shuttle.get()
val testPublish = true
val isReleaseVersion = true // the opposite is snapshot
val releaseAARFilePath = if (isReleaseVersion)
    "${projectDir}/build/outputs/aar/${archivesName}-release.aar" else
    "${projectDir}/build/outputs/aar/${archivesName}-release-SNAPSHOT.aar"
val debugAARFilePath = if (isReleaseVersion)
    "${projectDir}/build/outputs/aar/${archivesName}-debug.aar" else
    "${projectDir}/build/outputs/aar/${archivesName}-debug-SNAPSHOT.aar"
val javadocJarFileName = "${archivesName}-javadoc.jar"
val sourcesJarFileName = "${archivesName}-sources.jar"

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    archiveFileName.set(javadocJarFileName)
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    archiveFileName.set(sourcesJarFileName)
    from(android.sourceSets.getByName("main").java.srcDirs)
}

// rename the aar files
tasks.register("renameArtifacts") {
    doLast {
        val aarsDir = file("${projectDir}/build/outputs/aar/")
        val debugSuffix = "debug.aar"
        val releaseSuffix = "release.aar"
        aarsDir.listFiles()?.forEach { outputFile ->
            if (outputFile.name.endsWith(debugSuffix)) {
                val newName = if (isReleaseVersion) "${archivesName}-$debugSuffix" else "${archivesName}-debug-SNAPSHOT.aar"
                outputFile.renameTo(File(outputFile.parentFile, newName))
            } else if (outputFile.name.endsWith(releaseSuffix)) {
                val newName = if (isReleaseVersion) "${archivesName}-$releaseSuffix" else "${archivesName}-release-SNAPSHOT.aar"
                outputFile.renameTo(File(outputFile.parentFile, newName))
            }
        }
    }
}

fun updatePomWithDependencies(pom: MavenPom) {
    pom.withXml {
        val dependencies = asNode().appendNode("dependencies")
        configurations.getByName("implementation").allDependencies.forEach {
            val dependency = dependencies.appendNode("dependency")
            dependency.appendNode("groupId", it.group)
            dependency.appendNode("artifactId", it.name)
            dependency.appendNode("version", it.version)
        }
    }
}

fun updatePomWithPlugins(pom: MavenPom) {
    pom.withXml {
        val build = asNode().appendNode("build")
        val plugins = build.appendNode("plugins")

        // maven-gpg-plugin
        val plugin = plugins.appendNode("plugin")
        plugin.appendNode("groupId", "org.apache.maven.plugins")
        plugin.appendNode("artifactId", "maven-gpg-plugin")
        plugin.appendNode("version", "1.5")
        val executions = plugin.appendNode("executions")
        val execution = executions.appendNode("execution")
        execution.appendNode("id", "sign-artifacts")
        execution.appendNode("phase", "verify")
        val goals = execution.appendNode("goals")
        goals.appendNode("goal", "sign")

        // nexus-staging-maven-plugin
        val plugin2 = plugins.appendNode("plugin")
        plugin2.appendNode("groupId", "org.sonatype.plugins")
        plugin2.appendNode("artifactId", "nexus-staging-maven-plugin")
        plugin2.appendNode("version", "1.6.7")
        plugin2.appendNode("extensions", "true")
        val configuration = plugin2.appendNode("configuration")
        configuration.appendNode("serverId", "ossrh")
        configuration.appendNode("nexusUrl", "https://oss.sonatype.org/")
        configuration.appendNode("autoReleaseAfterClose", "false")
    }
}

afterEvaluate {
    tasks.named("assemble").configure {
        dependsOn(tasks.named("javadocJar"))
        dependsOn(tasks.named("sourcesJar"))
    }
    tasks.named("renameArtifacts").configure {
        dependsOn(tasks.named("assemble"))
    }

    publishing {
        publications {
            if (isReleaseVersion) {
                create<MavenPublication>("release") {
                    artifactId = archivesName
                    artifact(tasks.named("sourcesJar")) {
                        classifier = "sources"
                    }
                    artifact(tasks.named("javadocJar")) {
                        classifier = "javadoc"
                    }
                    artifact(releaseAARFilePath)

                    pom {
                        name.set("Shuttle")
                        packaging = "aar"
                        description.set("Shuttle provides a modern, guarded way to pass large Serializable objects with Intents or saving them in Bundle objects to avoid app crashes from TransactionTooLargeExceptions.")
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
                    updatePomWithDependencies(pom)
                    updatePomWithPlugins(pom)
                }
            } else {
                create<MavenPublication>("debug") {
                    artifactId = "${archivesName}-debug"
                    artifact(tasks.named("sourcesJar")) {
                        classifier = "sources"
                    }
                    artifact(tasks.named("javadocJar")) {
                        classifier = "javadoc"
                    }
                    artifact(debugAARFilePath) {
                        classifier = "debug"
                    }

                    pom {
                        name.set("Shuttle")
                        packaging = "aar"
                        description.set("Shuttle provides a modern, guarded way to pass large Serializable objects with Intents or saving them in Bundle objects to avoid app crashes from TransactionTooLargeExceptions.")
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
                    updatePomWithDependencies(pom)
                    updatePomWithPlugins(pom)
                }
            }
        }
        repositories {
            maven {
                name = "mavencentral"

                url = if (testPublish) {
                    val releasesRepoUrl = "${layout.buildDirectory.get()}/repos/releases"
                    val snapshotsRepoUrl = "${layout.buildDirectory.get()}/repos/snapshots"
                    uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)
                } else {
                    val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                    uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)
                }

                if (!testPublish) {
                    credentials {
                        username = System.getenv("ossrhUsername")
                        password = System.getenv("ossrhPassword")
                    }
                }
            }
        }
    }

    signing {
        setRequired(provider { !testPublish && isReleaseVersion && gradle.taskGraph.hasTask("publish") })

        val signingKeyId = System.getenv("signingKeyId")
        val signingKey = System.getenv("signingSecretKeyRingFile")
        val signingPassword = System.getenv("signingPassword")

        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

        val publicationName = if (isReleaseVersion) "release" else "debug"
        sign(publishing.publications[publicationName])
    }
}
