package com.grarcht.shuttle.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private const val BOM_COORDINATE = "com.grarcht.shuttle:framework-bom:4.0.0"
private const val CARGO_PLUGIN_ID = "com.grarcht.shuttle.cargo"
private const val DEFAULT_PACKAGE_NAME = "com.example.app"
private const val DEPENDENCY_ANNOTATIONS = "com.grarcht.shuttle:framework-annotations"
private const val DEPENDENCY_ANNOTATIONS_PROCESSOR = "com.grarcht.shuttle:framework-annotations-processor"
private const val DEPENDENCY_EXTENSIONS_ROOM = "com.grarcht.shuttle:framework-integrations-extensions-room"
private const val DEPENDENCY_FRAMEWORK = "com.grarcht.shuttle:framework"
private const val DEPENDENCY_PERSISTENCE = "com.grarcht.shuttle:framework-integrations-persistence"
private const val DI_FRAMEWORK_DESCRIPTION = "The dependency injection framework in use. One of: hilt, koin, manual."
private const val DI_FRAMEWORK_HILT = "hilt"
private const val DI_FRAMEWORK_KEY = "di_framework"
private const val DI_FRAMEWORK_KOIN = "koin"
private const val DI_FRAMEWORK_MANUAL = "manual"
private const val INCLUDE_NAVIGATION_DESCRIPTION = "Set to true to include the Navigation Component add-on dependency and example."
private const val INCLUDE_NAVIGATION_KEY = "include_navigation"
private const val NAV_DEPENDENCY_ARTIFACT = "com.grarcht.shuttle:framework-addons-navigation-component"
private const val NAV_DEP_LINE = "\n    implementation(\"" + NAV_DEPENDENCY_ARTIFACT + "\")"
private const val PACKAGE_NAME_DESCRIPTION = "The root package name for the generated code, e.g. com.example.myapp."
private const val PACKAGE_NAME_KEY = "package_name"
private const val UNSUPPORTED_FRAMEWORK_HINT = ". Use "
private const val UNSUPPORTED_FRAMEWORK_PREFIX = "Unsupported "

/**
 * MCP tool that generates ready-to-paste Shuttle integration boilerplate for a given DI
 * framework. Supports Hilt, Koin, and manual wiring.
 */
internal object ScaffoldIntegrationTool {

    const val NAME = "scaffold_integration"
    const val DESCRIPTION =
        "Generates Shuttle integration boilerplate for a given DI framework. " +
            "Returns ready-to-paste Kotlin code for wiring Shuttle into a project."

    val inputSchema = ToolSchema(
        properties = buildJsonObject {
            putJsonObject(DI_FRAMEWORK_KEY) {
                put(SCHEMA_KEY_TYPE, SCHEMA_TYPE_STRING)
                put(SCHEMA_KEY_DESCRIPTION, DI_FRAMEWORK_DESCRIPTION)
                put(
                    SCHEMA_KEY_ENUM,
                    buildJsonArray {
                        add(DI_FRAMEWORK_HILT)
                        add(DI_FRAMEWORK_KOIN)
                        add(DI_FRAMEWORK_MANUAL)
                    }
                )
            }
            putJsonObject(PACKAGE_NAME_KEY) {
                put(SCHEMA_KEY_TYPE, SCHEMA_TYPE_STRING)
                put(SCHEMA_KEY_DESCRIPTION, PACKAGE_NAME_DESCRIPTION)
            }
            putJsonObject(INCLUDE_NAVIGATION_KEY) {
                put(SCHEMA_KEY_TYPE, SCHEMA_TYPE_BOOLEAN)
                put(SCHEMA_KEY_DESCRIPTION, INCLUDE_NAVIGATION_DESCRIPTION)
            }
        },
        required = listOf(DI_FRAMEWORK_KEY, PACKAGE_NAME_KEY)
    )

    /**
     * Handles an MCP tool call by generating DI boilerplate for the requested framework.
     * @param arguments the raw JSON arguments from the MCP tool call
     * @return [CallToolResult] containing Kotlin code ready to paste into the project
     */
    suspend fun handle(arguments: JsonObject?): CallToolResult {
        val diFramework = arguments?.get(DI_FRAMEWORK_KEY)?.jsonPrimitive?.content ?: DI_FRAMEWORK_HILT
        val packageName = arguments?.get(PACKAGE_NAME_KEY)?.jsonPrimitive?.content ?: DEFAULT_PACKAGE_NAME
        val includeNavigation = arguments?.get(INCLUDE_NAVIGATION_KEY)?.jsonPrimitive?.content?.toBoolean() ?: false

        val code = when (diFramework.lowercase()) {
            DI_FRAMEWORK_HILT -> hiltScaffold(packageName, includeNavigation)
            DI_FRAMEWORK_KOIN -> koinScaffold(packageName, includeNavigation)
            DI_FRAMEWORK_MANUAL -> manualScaffold(packageName, includeNavigation)
            else -> "$UNSUPPORTED_FRAMEWORK_PREFIX$DI_FRAMEWORK_KEY: $diFramework$UNSUPPORTED_FRAMEWORK_HINT$DI_FRAMEWORK_HILT, $DI_FRAMEWORK_KOIN, or $DI_FRAMEWORK_MANUAL."
        }

        return CallToolResult(content = listOf(TextContent(text = code)))
    }

    private fun buildGradleSection(navDep: String): String {
        return """
// build.gradle.kts
plugins {
    id("$CARGO_PLUGIN_ID")
}

dependencies {
    implementation(platform("$BOM_COORDINATE"))
    implementation("$DEPENDENCY_FRAMEWORK")
    implementation("$DEPENDENCY_PERSISTENCE")
    implementation("$DEPENDENCY_EXTENSIONS_ROOM")
    implementation("$DEPENDENCY_ANNOTATIONS")
    ksp("$DEPENDENCY_ANNOTATIONS_PROCESSOR")$navDep
}
        """.trimIndent()
    }

    private fun hiltScaffold(packageName: String, includeNavigation: Boolean): String {
        val navDep = if (includeNavigation) NAV_DEP_LINE else ""
        return buildGradleSection(navDep) + "\n\n" + """
// $packageName.di.ShuttleModule.kt
package $packageName.di

import android.app.Application
import android.content.Context
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataModelFactory
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import com.grarcht.shuttle.framework.integrations.persistence.room.ShuttleRepository
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.screen.ShuttleCargoFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShuttleModule {

    @Provides
    @Singleton
    fun provideShuttle(facade: ShuttleFacade, warehouse: ShuttleWarehouse): Shuttle =
        CargoShuttle(facade, warehouse)

    @Provides
    @Singleton
    fun provideShuttleFacade(
        @ApplicationContext context: Context,
        warehouse: ShuttleWarehouse
    ): ShuttleFacade = ShuttleCargoFacade(context as Application, warehouse)

    @Provides
    @Singleton
    fun provideShuttleWarehouse(
        dao: ShuttleDataAccessObject,
        factory: ShuttleDataModelFactory,
        @ApplicationContext context: Context,
        gateway: ShuttleFileSystemGateway
    ): ShuttleWarehouse = ShuttleRepository(dao, factory, context.filesDir.absolutePath, gateway)
}
        """.trimIndent()
    }

    private fun koinScaffold(packageName: String, includeNavigation: Boolean): String {
        val navDep = if (includeNavigation) NAV_DEP_LINE else ""
        return buildGradleSection(navDep) + "\n\n" + """
// $packageName.di.ShuttleModule.kt
package $packageName.di

import android.app.Application
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import com.grarcht.shuttle.framework.integrations.persistence.room.ShuttleRepository
import com.grarcht.shuttle.framework.screen.ShuttleCargoFacade
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val shuttleModule = module {
    single<ShuttleWarehouse> {
        ShuttleRepository(
            dao = get(),
            factory = get(),
            filesDir = androidContext().filesDir.absolutePath,
            gateway = get()
        )
    }
    single {
        ShuttleCargoFacade(androidContext() as Application, get())
    }
    single<Shuttle> {
        CargoShuttle(facade = get(), warehouse = get())
    }
}
        """.trimIndent()
    }

    private fun manualScaffold(packageName: String, includeNavigation: Boolean): String {
        val navDep = if (includeNavigation) NAV_DEP_LINE else ""
        return buildGradleSection(navDep) + "\n\n" + """
// $packageName.ShuttleLocator.kt
// Wire these components in your Application.onCreate() before any Activity starts.
//
// Required dependencies to construct manually:
//   ShuttleDataAccessObject    — provided by Room (ShuttleDatabase.shuttleDataAccessObject())
//   ShuttleDataModelFactory    — provided by the extensions-room module
//   ShuttleFileSystemGateway   — provided by the extensions-room module
//
// Example (pseudo-code):
//
//   val db = Room.databaseBuilder(this, ShuttleDatabase::class.java, "shuttle.db").build()
//   val warehouse = ShuttleRepository(db.shuttleDataAccessObject(), dataModelFactory, filesDir.absolutePath, gateway)
//   val facade = ShuttleCargoFacade(this, warehouse)
//   val shuttle: Shuttle = CargoShuttle(facade, warehouse)
//
// Store the Shuttle instance in a singleton or service locator and inject it wherever needed.
// Shuttle must be a singleton — never construct CargoShuttle inline at a call site.
        """.trimIndent()
    }
}
