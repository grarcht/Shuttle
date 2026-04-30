plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

apply(from = "${project.rootDir}/detekt/detekt.gradle")

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
    sourceSets {
        main {
            kotlin.srcDirs("src/main/java")
        }
    }
}

dependencies {
    compileOnly(libs.compilerDeps.kotlinCompilerEmbeddable)
}
