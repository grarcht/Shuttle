package com.grarcht.shuttle.framework.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

private const val PLUGIN_GROUP_ID = "com.grarcht.shuttle"
private const val PLUGIN_ARTIFACT_ID = "framework-annotations-compiler-plugin"
private const val COMPILER_PLUGIN_ID = "com.grarcht.shuttle.framework.plugin"

/**
 * A Gradle plugin that automatically wires the Shuttle cargo compiler plugin into every Kotlin
 * compilation in the consumer project. Applying this plugin is the only step required for
 * [com.grarcht.shuttle.framework.ShuttleCargo] to work without any additional build script
 * configuration.
 */
class ShuttleCargoGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = PLUGIN_GROUP_ID,
        artifactId = PLUGIN_ARTIFACT_ID,
        version = PLUGIN_VERSION
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }
}
