plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.android.junit5)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("documentation/kotlin"))
    }
    dokkaSourceSets.register("main") {
        sourceRoots.from(file("src/main/java"))
    }
}

android {
    namespace = "com.grarcht.shuttle.demo.mvvmwithnavigation"

    defaultConfig {
        applicationId = "com.grarcht.shuttle.demo.mvvm_with_navigation"
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
    implementation(libs.android.annotationJvm)
    implementation(libs.android.appCompat)
    implementation(libs.android.constraintLayout)
    implementation(libs.android.material)
    implementation(libs.android.coreKtx)
    implementation(libs.android.fragmentKtx)
    implementation(libs.android.lifecycle.extensions)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.android.navigationFragmentKtx)

    api(libs.dependencyInjectionDeps.hilt)
    ksp(libs.dependencyInjectionDeps.hiltCompiler)

    implementation(project(":demos-core-lib"))

    // Lighter weight, independent dependencies
    implementation(project(":framework"))
    implementation(project(":framework-integrations-persistence"))
    implementation(project(":framework-integrations-extensions-room"))
    implementation(project(":framework-addons-navigation-component"))

    // To use maven dependencies, use the following:
    // implementation(libs.shuttle.framework)
    // implementation(libs.shuttle.integrationsPersistence)
    // implementation(libs.shuttle.integrationsExtensionsRoom)
    // implementation(libs.shuttle.addOnsNavigationComponent)

    androidTestImplementation(libs.testingDeps.junit.ext)
    androidTestImplementation(libs.testingDeps.espresso.core)
    androidTestRuntimeOnly(libs.testingDeps.junit.junit5AndroidTestRunner)
    testImplementation(libs.testingDeps.junit.jupiterApi)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
}
