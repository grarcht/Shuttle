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
    alias(libs.plugins.detect)
    alias(libs.plugins.jetbrains.dokka) apply false
    alias(libs.plugins.kover)
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

// Aggregate coverage from the framework library modules only.
dependencies {
    kover(project(":framework"))
    kover(project(":framework-integrations-persistence"))
    kover(project(":framework-integrations-extensions-room"))
    kover(project(":framework-addons-navigation-component"))
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