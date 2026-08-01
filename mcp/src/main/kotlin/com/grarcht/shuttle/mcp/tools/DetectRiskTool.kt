package com.grarcht.shuttle.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private const val CODE_DESCRIPTION = "The Kotlin or Java source code to analyze."
private const val CODE_KEY = "code"
private const val HOW_TO_FIX_LABEL = "How to fix with Shuttle:"
private const val NO_RISK_MESSAGE =
    "No TransactionTooLargeException risk patterns detected in the provided snippet. " +
        "Verify that any objects passed via Intent or Bundle extras are IDs or other " +
        "primitives, not full model objects."
private const val PATTERN_LABEL = "Pattern: "
private const val RISK_COUNT_PREFIX = "Found "
private const val RISK_COUNT_SUFFIX = " risk pattern(s):\n"
private const val RISK_LABEL_PREFIX = "--- Risk "
private const val RISK_LABEL_SUFFIX = " ---"
private const val WHY_RISKY_LABEL = "Why this is risky:"

/**
 * MCP tool that analyzes Kotlin or Java code for [android.os.TransactionTooLargeException] risk
 * patterns and explains how each risk can be eliminated with Shuttle.
 */
internal object DetectRiskTool {

    const val NAME = "detect_risk"
    const val DESCRIPTION =
        "Analyzes a Kotlin or Java code snippet for TransactionTooLargeException risk. " +
            "Returns a plain-language analysis of each risky pattern found and how to fix it with Shuttle."

    val inputSchema = ToolSchema(
        properties = buildJsonObject {
            putJsonObject(CODE_KEY) {
                put(SCHEMA_KEY_TYPE, SCHEMA_TYPE_STRING)
                put(SCHEMA_KEY_DESCRIPTION, CODE_DESCRIPTION)
            }
        },
        required = listOf(CODE_KEY)
    )

    /**
     * Encapsulates a single detectable risk pattern, its explanation, and the Shuttle-based fix.
     * @param pattern regex that matches the risky code construct
     * @param description plain-language explanation of why this pattern is risky
     * @param fix Shuttle code that eliminates the risk
     */
    private data class RiskPattern(
        val pattern: Regex,
        val description: String,
        val fix: String
    )

    private val riskPatterns = listOf(
        RiskPattern(
            pattern = Regex("""intent\.putExtra\s*\(\s*\S+\s*,\s*\S+"""),
            description = "intent.putExtra() with a non-primitive value passes the object through the " +
                "binder layer. If the serialized size exceeds the binder buffer limit (roughly 1 MB), " +
                "the app crashes with TransactionTooLargeException.",
            fix = """Replace with Shuttle intent transport:

val cargoId = DataMessageType.YourType.value

shuttle.intentCargoWith(context, DestinationActivity::class.java)
    .transport(cargoId, cargoData)
    .cleanShuttleOnReturnTo(SourceActivity::class.java, DestinationActivity::class.java, cargoId)
    .deliver(context)

At the destination, pick up with shuttle.pickupCargo<YourModel>(cargoId)."""
        ),
        RiskPattern(
            pattern = Regex("""bundle\.putSerializable\s*\("""),
            description = "bundle.putSerializable() puts the full serialized object into a Bundle that " +
                "crosses the binder layer. This is a direct cause of TransactionTooLargeException " +
                "when the object is large.",
            fix = """Replace with Shuttle bundle transport:

val args = shuttle.bundleCargoWith()
    .transport(cargoId, cargoData)
    .create()
MyFragment.newInstance(args)

At the destination, pick up with shuttle.pickupCargo<YourModel>(cargoId)."""
        ),
        RiskPattern(
            pattern = Regex("""bundle\.putParcelable\s*\("""),
            description = "bundle.putParcelable() puts the full object into a Bundle. Even if the object " +
                "is small today, models grow over time. Adopting Shuttle now prevents a future production crash.",
            fix = """Replace with Shuttle bundle transport:

val args = shuttle.bundleCargoWith()
    .transport(cargoId, cargoData)
    .create()
MyFragment.newInstance(args)

At the destination, pick up with shuttle.pickupCargo<YourModel>(cargoId)."""
        ),
        RiskPattern(
            pattern = Regex("""setResult\s*\(\s*\S+\s*,\s*\S+"""),
            description = "setResult() with an Intent that carries large extras sends data through the " +
                "binder on activity finish. This can trigger TransactionTooLargeException when the " +
                "calling activity processes the result.",
            fix = """Store the result payload with Shuttle before calling setResult(), then pass only the cargo ID as a plain extra:

shuttle.intentCargoWith(context, CallerActivity::class.java)
    .transport(cargoId, resultData)
    .deliver(context)
setResult(RESULT_OK, Intent().putExtra("cargo_id", cargoId))"""
        ),
        RiskPattern(
            pattern = Regex("""implements\s+Parcelable|:\s*Parcelable"""),
            description = "A Parcelable class is detected. Parcelable objects passed via Bundle or Intent " +
                "extras still cross the binder layer. If the object grows, this will eventually trigger " +
                "TransactionTooLargeException in production.",
            fix = """Annotate the class with @ShuttleCargo and transport it through Shuttle instead of via Bundle or Intent extras directly.

@ShuttleCargo
data class YourModel(val field: String) // no explicit Serializable needed"""
        )
    )

    /**
     * Handles an MCP tool call by scanning the provided code for risk patterns and returning a
     * plain-language analysis with Shuttle-based fixes.
     * @param arguments the raw JSON arguments from the MCP tool call
     * @return [CallToolResult] containing the risk analysis
     */
    suspend fun handle(arguments: JsonObject?): CallToolResult {
        val code = arguments?.get(CODE_KEY)?.jsonPrimitive?.content ?: ""
        val findings = riskPatterns.filter { it.pattern.containsMatchIn(code) }

        val text = buildString {
            if (findings.isEmpty()) {
                appendLine(NO_RISK_MESSAGE)
            } else {
                appendLine("$RISK_COUNT_PREFIX${findings.size}$RISK_COUNT_SUFFIX")
                findings.forEachIndexed { index, finding ->
                    appendLine("$RISK_LABEL_PREFIX${index + 1}$RISK_LABEL_SUFFIX")
                    appendLine("$PATTERN_LABEL${finding.pattern.pattern}")
                    appendLine()
                    appendLine(WHY_RISKY_LABEL)
                    appendLine(finding.description)
                    appendLine()
                    appendLine(HOW_TO_FIX_LABEL)
                    appendLine(finding.fix)
                    appendLine()
                }
            }
            appendLine()
            appendLine(DOCS_FOOTER)
        }

        return CallToolResult(content = listOf(TextContent(text = text)))
    }
}
