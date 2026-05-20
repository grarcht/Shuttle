plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.signing)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.kover)
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("dagger.Module", "dagger.hilt.InstallIn", "androidx.annotation.RequiresApi", "com.grarcht.shuttle.framework.ExcludeFromCoverage")
                classes(
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*_MembersInjector",
                    "*_Factory",
                    "*_Impl",
                    "*_Impl\$*"
                )
                packages("*.dependencyinjection")
            }
        }
        total {
            html { onCheck = false }
            xml { onCheck = false }
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("documentation/kotlin"))
    }
    dokkaSourceSets.configureEach {
        suppress.set(name != "modulesRelease")
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
        create("distribution")
    }

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())
    testOptions.unitTests.isReturnDefaultValues = true

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    packaging {
        resources {
            excludes.add("META-INF/LICENSE*")
        }
    }

    namespace = "com.grarcht.shuttle.framework.integrations.extensions.room"
}

dependencies {
    implementation(project(":framework-annotations"))
    implementation(libs.jetbrainsKotlinDeps.stdlib)
    implementation(libs.android.coreKtx)
    implementation(libs.android.appCompat)
    implementation(libs.android.annotationJvm)
    api(libs.roomDeps.runtime)
    ksp(libs.roomDeps.compiler)
    api(libs.roomDeps.ktx)
    api(project(":framework-integrations-persistence"))

    androidTestImplementation(libs.testingDeps.junit.androidTestRunner)
    androidTestImplementation(libs.testingDeps.junit.jupiterApi)
    androidTestImplementation(libs.testingDeps.junit.jupiterEngine)
    androidTestImplementation(libs.testingDeps.junit.jupiterParams)
    androidTestImplementation(libs.testingDeps.junit.junit5AndroidTestCore)
    androidTestRuntimeOnly(libs.testingDeps.junit.junit5AndroidTestRunner)

    testImplementation(libs.roomDeps.testHelpers)
    testImplementation(libs.testingDeps.kotlin.coroutines)
    testImplementation(libs.testingDeps.mockito.core)
    testImplementation(libs.testingDeps.mockito.kotlin)
    testImplementation(libs.testingDeps.junit.jupiterApi)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    testImplementation(libs.testingDeps.junit.androidCore)
    testRuntimeOnly(libs.testingDeps.junit.platformCommons) // enables a package of tests to be run
}

group = "com.grarcht.shuttle"
val archivesName = "framework-integrations-extensions-room"
extensions.getByType<BasePluginExtension>().archivesName.set(archivesName)
version = libs.versions.shuttle.get()
val publishToBuildLocal = true
val distributionAARFilePath = "${projectDir}/build/outputs/aar/${archivesName}.aar"
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

abstract class RenameArtifactsTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val aarsDir: DirectoryProperty

    @get:Input
    abstract val artifactName: Property<String>

    @TaskAction
    fun rename() {
        val dir = aarsDir.get().asFile
        dir.listFiles()?.forEach { outputFile ->
            if (outputFile.name.endsWith("distribution.aar")) {
                outputFile.renameTo(File(outputFile.parentFile, "${artifactName.get()}.aar"))
            }
        }
    }
}

tasks.register<RenameArtifactsTask>("renameArtifacts") {
    aarsDir.set(layout.buildDirectory.dir("outputs/aar/"))
    artifactName.set(archivesName)
}

fun updatePomWithDependencies(pom: MavenPom) {
    val projectGroup = project.group.toString()
    val projectVersion = project.version.toString()
    val depData = configurations.getByName("implementation").allDependencies.map { dep ->
        Triple(
            if (dep is ProjectDependency) projectGroup else dep.group,
            dep.name,
            if (dep is ProjectDependency) projectVersion else dep.version
        )
    }
    pom.withXml {
        val dependenciesNode = asNode().appendNode("dependencies")
        depData.forEach { (group, name, version) ->
            val dependency = dependenciesNode.appendNode("dependency")
            dependency.appendNode("groupId", group)
            dependency.appendNode("artifactId", name)
            dependency.appendNode("version", version)
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

    tasks.withType<AbstractPublishToMaven>().configureEach {
        if (name.contains("distribution", ignoreCase = true)) {
            dependsOn("renameArtifacts")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("distribution") {
                artifactId = archivesName
                artifact(tasks.named("sourcesJar")) {
                    classifier = "sources"
                }
                artifact(tasks.named("javadocJar")) {
                    classifier = "javadoc"
                }
                artifact(distributionAARFilePath)

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
}

