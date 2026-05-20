plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.dokka)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

dokka {
    dokkaSourceSets.configureEach {
        suppress.set(name != "modulesRelease")
    }
}

android {
    namespace = "com.grarcht.shuttle.demo.core"

    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())
}

dependencies {
    implementation(libs.jetbrainsKotlinDeps.stdlib)
    implementation(libs.android.annotationJvm)
    implementation(libs.dependencyInjectionDeps.hilt)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)
    implementation(libs.android.activityKtx)
    implementation(libs.android.coreKtx)
    implementation(libs.android.lifecycle.runtimeKtx)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.android.material)

    implementation(project(":framework"))
    implementation(project(":framework-integrations-persistence"))
    implementation(project(":framework-integrations-extensions-room"))
    implementation(project(":framework-annotations"))
    ksp(project(":framework-annotations-processor"))

    // Wire the Shuttle compiler plugin from source. External consumers apply the published
    // Gradle plugin instead: id("com.grarcht.shuttle.cargo") from :framework-annotations-gradle-plugin.
    // Within this multi-project build the plugin is wired manually because the gradle-plugin
    // module cannot simultaneously be a regular subproject and a pluginManagement includeBuild.
    val shuttleCompilerPlugin by configurations.creating
    shuttleCompilerPlugin(project(":framework-annotations-compiler-plugin"))

    testImplementation(libs.testingDeps.junit.jupiterApi)
    testImplementation(libs.testingDeps.kotlin.coroutines)
    testImplementation(libs.testingDeps.mockito.core)
    testImplementation(libs.testingDeps.mockito.kotlin)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    testRuntimeOnly(libs.testingDeps.junit.platformCommons)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    val pluginConfig = configurations["shuttleCompilerPlugin"]
    inputs.files(pluginConfig)
    compilerOptions {
        freeCompilerArgs.addAll(
            provider { pluginConfig.files.map { "-Xplugin=${it.absolutePath}" } }
        )
    }
}
