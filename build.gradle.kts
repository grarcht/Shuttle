import org.owasp.dependencycheck.gradle.tasks.Aggregate

buildscript {
    apply(from = "gradle/ext.gradle")

    repositories {
        mavenCentral()

        // Instead of using the google() repo function, specify the maven url instead.  There is
        // an issue with the hilt dependencies where there are extra dependencies that are pulled
        // in via the gradle module file.  Using the POM file as the source of truth fixes that
        // issue.  This is now being universally applied for all Google artifacts.
        maven {
            url = uri("https://maven.google.com/")
            metadataSources {
                //noinspection ForeignDelegate
                mavenPom()
                //noinspection ForeignDelegate
                artifact()
                //noinspection ForeignDelegate
                ignoreGradleMetadataRedirection()
            }
        }
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.classpathDeps.gradleBuildTools)
        classpath(libs.classpathDeps.kotlinGradlePlugin)
        //https://github.com/mannodermaus/android-junit5
        classpath(libs.classpathDeps.junit5Plugin)
        classpath(libs.dependencyInjectionDeps.hilt)
        classpath(libs.staticAnalysisDeps.detektPlugin)
        classpath(libs.dependencyInjectionDeps.hiltPlugin)
    }
}

plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.dagger.hilt) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.owasp.dependency.check)
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

repositories {
    mavenCentral()

    // Instead of using the google() repo function, specify the maven url instead.  There is
    // an issue with the hilt dependencies where there are extra dependencies that are pulled
    // in via the gradle module file.  Using the POM file as the source of truth fixes that
    // issue.  This is now being universally applied for all Google artifacts.
    maven {
        url = uri("https://maven.google.com/")
        metadataSources {
            //noinspection ForeignDelegate
            mavenPom()
            //noinspection ForeignDelegate
            artifact()
            //noinspection ForeignDelegate
            ignoreGradleMetadataRedirection()
        }
    }
    gradlePluginPortal()
}


val rootDir = projectDir.absolutePath

allprojects {
    apply(from = "$rootDir/gradle/ext.gradle")
    apply(from = "$rootDir/detekt/detekt.gradle")

    repositories {
        mavenLocal()

        // The Shuttle framework artifacts are hosted in Sonatype Nexus Maven Central.
        maven {
            url = uri("https://repo1.maven.org/maven2/com/grarcht/shuttle/")
            metadataSources {
                //noinspection ForeignDelegate
                mavenPom()
                //noinspection ForeignDelegate
                artifact()
                //noinspection ForeignDelegate
                ignoreGradleMetadataRedirection()
            }
        }

        google()

        mavenCentral()

        // Instead of using the google() repo function, specify the maven url instead.  There is
        // an issue with the hilt dependencies where there are extra dependencies that are pulled
        // in via the gradle module file.  Using the POM file as the source of truth fixes that
        // issue.  This is now being universally applied for all Google artifacts.
        maven {
            url = uri("https://maven.google.com/")
            metadataSources {
                //noinspection ForeignDelegate
                mavenPom()
                //noinspection ForeignDelegate
                artifact()
                //noinspection ForeignDelegate
                ignoreGradleMetadataRedirection()
            }
        }

        gradlePluginPortal()
    }
}

tasks.named<Delete>("clean") {
    delete(project.layout.buildDirectory)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("documentation/kotlin"))
    }
}

dependencies {
    // Aggregate coverage from the framework library modules only.
    kover(project(":framework"))
    kover(project(":framework-integrations-persistence"))
    kover(project(":framework-integrations-extensions-room"))
    kover(project(":framework-addons-navigation-component"))

    // Aggregate Dokka docs from all published modules.
    dokka(project(":framework"))
    dokka(project(":framework-integrations-persistence"))
    dokka(project(":framework-integrations-extensions-room"))
    dokka(project(":framework-addons-navigation-component"))
    dokka(project(":framework-annotations"))
    dokka(project(":framework-annotations-processor"))
    dokka(project(":framework-annotations-compiler-plugin"))
    dokka(project(":framework-annotations-gradle-plugin"))

    // Aggregate Dokka docs from all demo modules.
    dokka(project(":demos-core-foundation"))
    dokka(project(":demos-core-compose"))
    dokka(project(":demos-core-di"))
    dokka(project(":demo-mvc"))
    dokka(project(":demo-mvvm"))
    dokka(project(":demo-mvi-with-compose"))
    dokka(project(":demo-mvvm-with-a-service"))
    dokka(project(":demo-mvvm-with-compose"))
    dokka(project(":demo-mvvm-with-compose-and-navigation"))
    dokka(project(":demo-mvvm-with-process-death"))
}

tasks.withType<Aggregate>().configureEach {
    // The OWASP plugin accesses Task.project at execution time, which is
    // incompatible with Gradle configuration cache.
    notCompatibleWithConfigurationCache("OWASP Dependency Check plugin is not configuration-cache compatible")

    // Surface OWASP's internal SLF4J output at Gradle LIFECYCLE level so NVD
    // download progress is visible without requiring --info on every run.
    logging.captureStandardOutput(LogLevel.LIFECYCLE)
    logging.captureStandardError(LogLevel.WARN)

    doLast {
        val report = file("${layout.buildDirectory.get()}/reports/dependency-check-report.json")
        if (!report.exists()) return@doLast
        val json = groovy.json.JsonSlurper().parse(report) as Map<*, *>
        @Suppress("UNCHECKED_CAST")
        val deps = json["dependencies"] as? List<Map<*, *>> ?: return@doLast
        val vulns = deps.flatMap { dep ->
            @Suppress("UNCHECKED_CAST")
            val vs = dep["vulnerabilities"] as? List<Map<*, *>> ?: emptyList()
            vs.map { dep["fileName"] to it }
        }
        println("\nOWASP Dependency Check — vulnerabilities found: ${vulns.size}")
        if (vulns.isNotEmpty()) {
            vulns.forEach { (fileName, vuln) ->
                val cvss = (vuln["cvssv3"] as? Map<*, *>)?.get("baseScore")
                    ?: (vuln["cvssv2"] as? Map<*, *>)?.get("score")
                    ?: "N/A"
                val desc = (vuln["description"] as? String)?.let {
                    if (it.length > 120) it.take(120) + "…" else it
                } ?: ""
                println("  $fileName")
                println("    CVE : ${vuln["name"]}")
                println("    CVSS: $cvss")
                println("    $desc")
            }
        }
    }
}

val nvdApiKey = System.getenv("NVD_API_KEY")
    ?: (project.findProperty("NVD_API_KEY") as? String)
    ?: ""

dependencyCheck {
    // Zero trust: fail on any vulnerability not explicitly suppressed.
    failBuildOnCVSS = 0.0f
    suppressionFile = "$rootDir/config/owasp/dependency-check-suppression.xml"
    formats = listOf("HTML", "SARIF", "JSON")
    nvd {
        // Free API key: https://nvd.nist.gov/developers/request-an-api-key
        // Set NVD_API_KEY in the environment or as a GitHub Actions secret.
        // Without a key: NVD rate-limits to 5 req/30 s  → ~15 min first-run download.
        // With a key:    NVD rate-limits to 50 req/30 s → ~90 s  first-run download.
        apiKey = nvdApiKey
        delay = if (nvdApiKey.isNotEmpty()) 1000 else 6000
        // Only re-download NVD data if the local cache is older than 24 hours.
        // Default is 4 hours, which causes redundant downloads in CI.
        validForHours = 24
    }
    analyzers {
        // OSS Index rate-limits unauthenticated requests so aggressively that every
        // jar fails with an HTTP error, which corrupts the H2 regions and causes
        // "Analysis failed". NVD is the authoritative CVE source — OSS Index is
        // redundant here.
        ossIndex {
            enabled = false
        }
        // No Node.js in this project.
        nodeAuditEnabled = false
        nodeEnabled = false
    }
}

kover {
    reports {
        filters {
            excludes {
                // Hilt-annotated DI classes
                annotatedBy("dagger.Module", "dagger.hilt.InstallIn")
                // Hilt-generated class name patterns
                classes(
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*_MembersInjector",
                    "*_Factory",
                    "*_Impl",
                    "*_Impl\$*"
                )
                // DI package
                packages("*.dependencyinjection")
            }
        }
        total {
            html { onCheck = false }
            xml { onCheck = false }
        }
    }
}