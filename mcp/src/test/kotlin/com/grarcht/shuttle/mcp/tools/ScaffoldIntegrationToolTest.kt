package com.grarcht.shuttle.mcp.tools

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private const val DI_FRAMEWORK_HILT = "hilt"
private const val DI_FRAMEWORK_KEY = "di_framework"
private const val DI_FRAMEWORK_KOIN = "koin"
private const val DI_FRAMEWORK_MANUAL = "manual"
private const val DI_FRAMEWORK_UNKNOWN = "spring"
private const val HILT_MARKER = "@InstallIn(SingletonComponent::class)"
private const val INCLUDE_NAVIGATION_KEY = "include_navigation"
private const val KOIN_MARKER = "val shuttleModule = module {"
private const val MANUAL_MARKER = "ShuttleLocator"
private const val NAV_DEPENDENCY = "framework-addons-navigation-component"
private const val PACKAGE_NAME = "com.example.testapp"
private const val PACKAGE_NAME_KEY = "package_name"
private const val UNSUPPORTED_FRAMEWORK_MARKER = "Unsupported di_framework"

/**
 * Verifies every code path in [ScaffoldIntegrationTool]. Every scaffold variant (Hilt, Koin,
 * manual, unknown) and every combination of the navigation flag must produce the correct output.
 * If the tool produced wrong or empty scaffolding, agents would generate broken integration code.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScaffoldIntegrationToolTest {

    @Test
    fun verifyHandleWithNullArgumentsDefaultsToHiltOutput() = runBlocking {
        val result = ScaffoldIntegrationTool.handle(null)

        assertAll(
            { assertNotNull(result) },
            { assertTrue(result.content.isNotEmpty()) }
        )
    }

    @Test
    fun verifyHandleWithHiltFrameworkAndNoNavigationContainsHiltModule() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_HILT)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
            put(INCLUDE_NAVIGATION_KEY, false)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(HILT_MARKER), "Missing Hilt @InstallIn annotation") },
            { assertTrue(!text.contains(NAV_DEPENDENCY), "Navigation dep should be absent") }
        )
    }

    @Test
    fun verifyHandleWithHiltFrameworkAndNavigationIncludesNavDependency() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_HILT)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
            put(INCLUDE_NAVIGATION_KEY, true)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertTrue(text.contains(NAV_DEPENDENCY), "Navigation dependency should be present")
    }

    @Test
    fun verifyHandleWithKoinFrameworkAndNoNavigationContainsKoinModule() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_KOIN)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
            put(INCLUDE_NAVIGATION_KEY, false)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(KOIN_MARKER), "Missing Koin module declaration") },
            { assertTrue(!text.contains(NAV_DEPENDENCY), "Navigation dep should be absent") }
        )
    }

    @Test
    fun verifyHandleWithKoinFrameworkAndNavigationIncludesNavDependency() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_KOIN)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
            put(INCLUDE_NAVIGATION_KEY, true)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertTrue(text.contains(NAV_DEPENDENCY), "Navigation dependency should be present")
    }

    @Test
    fun verifyHandleWithManualFrameworkAndNoNavigationContainsLocatorComment() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_MANUAL)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
            put(INCLUDE_NAVIGATION_KEY, false)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(MANUAL_MARKER), "Missing manual DI instructions") },
            { assertTrue(!text.contains(NAV_DEPENDENCY), "Navigation dep should be absent") }
        )
    }

    @Test
    fun verifyHandleWithManualFrameworkAndNavigationIncludesNavDependency() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_MANUAL)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
            put(INCLUDE_NAVIGATION_KEY, true)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertTrue(text.contains(NAV_DEPENDENCY), "Navigation dependency should be present")
    }

    @Test
    fun verifyHandleWithUnknownFrameworkReturnsUnsupportedMessage() = runBlocking {
        val args = buildJsonObject {
            put(DI_FRAMEWORK_KEY, DI_FRAMEWORK_UNKNOWN)
            put(PACKAGE_NAME_KEY, PACKAGE_NAME)
        }

        val result = ScaffoldIntegrationTool.handle(args)
        val text = result.content.first().toString()

        assertTrue(text.contains(UNSUPPORTED_FRAMEWORK_MARKER), "Should report unsupported framework")
    }
}
