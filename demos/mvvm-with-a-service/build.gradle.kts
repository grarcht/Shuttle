plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.dokka)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

dokka {
    dokkaSourceSets.configureEach {
        suppress.set(name != "modulesRelease")
    }
}

android {
    namespace = "com.grarcht.shuttle.demo.mvvmwithaservice"

    defaultConfig {
        applicationId = "com.grarcht.shuttle.demo.mvvm_with_a_service"
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

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())

    buildFeatures {
        viewBinding = true
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
    implementation(libs.android.appCompat)
    implementation(libs.android.constraintLayout)
    implementation(libs.android.material)
    implementation(libs.android.fragmentKtx)
    implementation(libs.android.lifecycle.runtimeKtx)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.dependencyInjectionDeps.hilt)
    implementation(libs.android.coreKtx)
    implementation(libs.android.annotationJvm)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)
    implementation(project(":demos-core-foundation"))

    // Lighter weight, independent dependencies
    // implementation(project(":framework"))
    // implementation(project(":framework-integrations-persistence"))
    // implementation(project(":framework-integrations-extensions-room"))

    // To use maven dependencies, use the following:
    implementation(platform(libs.shuttle.bom))
    implementation(libs.shuttle.framework)
    implementation(libs.shuttle.integrationsPersistence)
    implementation(libs.shuttle.integrationsExtensionsRoom)

    testImplementation(libs.testingDeps.junit.jupiterApi)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    testRuntimeOnly(libs.testingDeps.junit.platformCommons)
}
