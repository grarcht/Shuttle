plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.ksp)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

tasks.named("dokkaHtml") {
    (this as org.jetbrains.dokka.gradle.DokkaTask).outputDirectory.set(file("documentation/kotlin"))
}

android {
    namespace = "com.grarcht.shuttle.demo.mvvmwithcompose"

    defaultConfig {
        applicationId = "com.grarcht.shuttle.mvvm_with_compose"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            val name = requested.name
            if (name == "androidx.collection:collection:1.1.0" ||
                    name == "androidx.collection:collection-jvm:1.4.0") {
                useVersion("1.1.0")
            }
        }
    }

    kotlin.jvmToolchain(libs.versions.jvmTarget.get().toInt())

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinComposeCompilerExt.get()
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

    implementation(libs.android.activityKtx)
    implementation(libs.android.annotationJvm)
    implementation(libs.android.appCompat)
    implementation(libs.android.compose.activity)
    implementation(libs.android.compose.foundation)
    implementation(libs.android.compose.foundationLayout)
    implementation(libs.android.compose.material)
    implementation(libs.android.compose.materialIcons)
    implementation(libs.android.compose.runtime)
    implementation(libs.android.compose.ui)
    implementation(libs.android.compose.uiTooling)
    implementation(libs.android.constraintLayout)
    implementation(libs.android.coreKtx)
    implementation(libs.android.lifecycle.extensions)
    implementation(libs.android.lifecycle.viewModel)
    implementation(libs.android.material)

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

    androidTestImplementation(libs.testingDeps.junit.ext)
    androidTestImplementation(libs.testingDeps.espresso.core)
    androidTestRuntimeOnly(libs.testingDeps.junit.junit5AndroidTestRunner)
    testImplementation(libs.testingDeps.junit.jupiterApi)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
}
