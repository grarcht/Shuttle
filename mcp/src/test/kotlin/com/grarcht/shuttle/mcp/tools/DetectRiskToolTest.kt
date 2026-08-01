package com.grarcht.shuttle.mcp.tools

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private const val BUNDLE_PUT_PARCELABLE_CODE = "bundle.putParcelable(\"item\", myParcelable)"
private const val BUNDLE_PUT_SERIALIZABLE_CODE = "bundle.putSerializable(\"data\", myModel)"
private const val CLEAN_CODE = "val id = intent.getStringExtra(\"user_id\")"
private const val CODE_KEY = "code"
private const val FIX_MARKER = "How to fix with Shuttle"
private const val INTENT_PUT_EXTRA_CODE = "intent.putExtra(\"payload\", userProfile)"
private val MULTI_RISK_CODE = "$INTENT_PUT_EXTRA_CODE\n$BUNDLE_PUT_SERIALIZABLE_CODE"
private const val NO_RISK_MARKER = "No TransactionTooLargeException risk patterns detected"
private const val PARCELABLE_IMPL_CODE = "data class UserProfile(val name: String) : Parcelable"
private const val RISK_COUNT_MARKER = "Found"
private const val SET_RESULT_CODE = "setResult(RESULT_OK, resultIntent)"
private const val TWO_RISK_FINDINGS_MARKER = "Found 2 risk pattern(s)"

/**
 * Verifies every branch in [DetectRiskTool]. Each regex risk pattern must match when the
 * corresponding code construct is present, and the clean-code path must return the expected
 * no-risk message. Incorrect detection would either miss real risks or produce false positives
 * that confuse developers.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DetectRiskToolTest {

    @Test
    fun verifyHandleWithNullArgumentsReturnsNoRiskMessage() = runBlocking {
        val result = DetectRiskTool.handle(null)

        assertAll(
            { assertNotNull(result) },
            { assertTrue(result.content.first().toString().contains(NO_RISK_MARKER)) }
        )
    }

    @Test
    fun verifyHandleWithCleanCodeReturnsNoRiskMessage() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, CLEAN_CODE) }

        val result = DetectRiskTool.handle(args)

        assertTrue(result.content.first().toString().contains(NO_RISK_MARKER))
    }

    @Test
    fun verifyHandleWithIntentPutExtraDetectsRisk() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, INTENT_PUT_EXTRA_CODE) }

        val result = DetectRiskTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(RISK_COUNT_MARKER), "Should report risk count") },
            { assertTrue(text.contains(FIX_MARKER), "Should include Shuttle fix") }
        )
    }

    @Test
    fun verifyHandleWithBundlePutSerializableDetectsRisk() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, BUNDLE_PUT_SERIALIZABLE_CODE) }

        val result = DetectRiskTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(RISK_COUNT_MARKER), "Should report risk count") },
            { assertTrue(text.contains(FIX_MARKER), "Should include Shuttle fix") }
        )
    }

    @Test
    fun verifyHandleWithBundlePutParcelableDetectsRisk() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, BUNDLE_PUT_PARCELABLE_CODE) }

        val result = DetectRiskTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(RISK_COUNT_MARKER), "Should report risk count") },
            { assertTrue(text.contains(FIX_MARKER), "Should include Shuttle fix") }
        )
    }

    @Test
    fun verifyHandleWithSetResultDetectsRisk() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, SET_RESULT_CODE) }

        val result = DetectRiskTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(RISK_COUNT_MARKER), "Should report risk count") },
            { assertTrue(text.contains(FIX_MARKER), "Should include Shuttle fix") }
        )
    }

    @Test
    fun verifyHandleWithParcelableImplementationDetectsRisk() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, PARCELABLE_IMPL_CODE) }

        val result = DetectRiskTool.handle(args)
        val text = result.content.first().toString()

        assertAll(
            { assertTrue(text.contains(RISK_COUNT_MARKER), "Should report risk count") },
            { assertTrue(text.contains(FIX_MARKER), "Should include Shuttle fix") }
        )
    }

    @Test
    fun verifyHandleWithMultipleRiskPatternsReportsAllFindings() = runBlocking {
        val args = buildJsonObject { put(CODE_KEY, MULTI_RISK_CODE) }

        val result = DetectRiskTool.handle(args)
        val text = result.content.first().toString()

        assertTrue(text.contains(TWO_RISK_FINDINGS_MARKER), "Should report 2 risks")
    }
}
