plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.google.ksp)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

tasks.named("dokkaHtml") {
    (this as org.jetbrains.dokka.gradle.DokkaTask).outputDirectory.set(file("documentation/kotlin"))
}

android {
    namespace = "com.grarcht.shuttle.demo.mvc"

    defaultConfig {
        applicationId = "com.grarcht.shuttle.demo.mvc"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlin {
        jvmToolchain(libs.versions.jvmTarget.get().toInt())
    }

    val dependenciesType = "dependenciesType"
    flavorDimensions.add(dependenciesType)

    productFlavors {
        create("artifacts") { dimension = dependenciesType }
        create("modules") { dimension = dependenciesType }
    }
}

dependencies {
    implementation(libs.jetbrainsKotlinDeps.stdlib)
    implementation(libs.jetbrainsKotlinDeps.coroutines)
    implementation(libs.android.annotationJvm)
    implementation(libs.android.appCompat)
    implementation(libs.android.constraintLayout)
    implementation(libs.android.material)
    implementation(libs.android.lifecycle.extensions)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.android.coreKtx)

    api(libs.dependencyInjectionDeps.hilt)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)

    implementation(project(":demos-core-lib"))

    // Lighter weight, independent dependencies
    implementation(project(":framework"))
    implementation(project(":framework-integrations-persistence"))
    implementation(project(":framework-integrations-extensions-room"))

    // To use maven dependencies, use the following:
    // implementation(libs.shuttle.framework)
    // implementation(libs.shuttle.integrationsPersistence)
    // implementation(libs.shuttle.integrationsExtensionsRoom)
}
