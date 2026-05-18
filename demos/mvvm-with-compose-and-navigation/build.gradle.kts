plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.ksp)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

android {
    namespace = "com.grarcht.shuttle.demo.mvvmcomposewithnavigation"

    defaultConfig {
        applicationId = "com.grarcht.shuttle.mvvm_compose_with_navigation"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    buildFeatures {
        compose = true
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
    implementation(libs.android.compose.activity)
    implementation(libs.android.compose.foundation)
    implementation(libs.android.compose.foundationLayout)
    implementation(libs.android.compose.material)
    implementation(libs.android.compose.runtime)
    implementation(libs.android.compose.ui)
    implementation(libs.android.compose.uiTooling)
    implementation(libs.android.coreKtx)
    implementation(libs.android.fragmentKtx)
    implementation(libs.android.lifecycle.extensions)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.android.material)
    implementation(libs.android.navigationFragmentKtx)

    api(libs.dependencyInjectionDeps.hilt)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)

    implementation(project(":demos-core-foundation"))
    implementation(project(":demos-core-di"))
    implementation(project(":demos-core-compose"))

    // Lighter weight, independent dependencies
    // implementation(project(":framework"))
    // implementation(project(":framework-integrations-persistence"))
    // implementation(project(":framework-integrations-extensions-room"))
    // implementation(project(":framework-addons-navigation-component"))

    // To use maven dependencies, use the following:
    implementation(platform(libs.shuttle.bom))
    implementation(libs.shuttle.framework)
    implementation(libs.shuttle.integrationsPersistence)
    implementation(libs.shuttle.integrationsExtensionsRoom)
    implementation(libs.shuttle.addOnsNavigationComponent)

    androidTestImplementation(libs.testingDeps.junit.ext)
    androidTestImplementation(libs.testingDeps.espresso.core)
    androidTestRuntimeOnly(libs.testingDeps.junit.junit5AndroidTestRunner)
    testImplementation(libs.testingDeps.junit.jupiterApi)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    testRuntimeOnly(libs.testingDeps.junit.platformCommons)
}
