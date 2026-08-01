plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    application
}

group = "com.grarcht.shuttle"
version = libs.versions.shuttle.get()

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
}

application {
    mainClass = "com.grarcht.shuttle.mcp.ShuttleMcpServerKt"
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.mcp.kotlin.sdk.server)

    testImplementation(libs.testingDeps.junit.jupiterApi)
    testImplementation(libs.testingDeps.kotlin.coroutines)
    testRuntimeOnly(libs.testingDeps.junit.jupiterEngine)
    testRuntimeOnly(libs.testingDeps.junit.platformCommons)
    testRuntimeOnly(libs.testingDeps.junit.platformLauncher)
}
