package tv.own.owntv.core.companion

import tv.own.owntv.core.model.SourceAnalysisAiState
import tv.own.owntv.core.model.SourceMessageConfidence
import tv.own.owntv.core.model.SourceType

/**
 * What one analysis of a pasted provider message produced: exactly the fields the add-source form
 * needs, already validated.
 *
 * The companion page shows these as pre-filled, editable values. Nothing here is *verified* — a
 * wrong server costs minutes of sync, so the page always asks the user to check before the import
 * is started on the TV.
 */
data class CompanionSourceAnalysis(
    /** The kind of source the values belong to. */
    val type: SourceType = SourceType.XTREAM,
    /** Xtream base, playlist URL or Stalker portal URL — the value the form's URL field takes. */
    val server: String = "",
    val username: String = "",
    val password: String = "",
    /** Canonical `AA:BB:CC:DD:EE:FF` when the message carried a MAC; empty otherwise. */
    val mac: String = "",
    /** Every URL candidate the message carried, in the order of appearance. */
    val urls: List<String> = emptyList(),
    /** The candidate the fields above belong to; null when the message had no URL at all. */
    val url: String? = null,
    /** How sure the reader is — see [SourceMessageConfidence]. */
    val confidence: SourceMessageConfidence = SourceMessageConfidence.NONE,
    /**
     * True when the values were read by the AI, false when only the local rules ran (no key, no
     * network, no budget). The page uses it to say why the result looks the way it does.
     */
    val fromAi: Boolean = false,
    /**
     * What the AI did with the message — or why it did not. The page turns this into the hint
     * that tells the user where the API key belongs.
     */
    val aiState: SourceAnalysisAiState = SourceAnalysisAiState.NOT_ASKED,
) {
    /** True when the analysis carries something to pre-fill: a server or a MAC. */
    val hasContent: Boolean get() = server.isNotBlank() || mac.isNotBlank()
}

/**
 * Reads a pasted provider message. [preferredUrl] picks one of several candidate URLs (the "which
 * one?" step); null asks for the reader's own choice.
 *
 * The AI client lives in the app, so core asks for the result instead of making the call itself:
 * core must not import anything from the app. A null result means "nothing readable" — the page
 * then shows the empty form and whatever candidates the local rules found.
 */
typealias CompanionSourceAnalyzer = suspend (message: String, preferredUrl: String?) -> CompanionSourceAnalysis?

/**
 * One analysis request from the companion page: the pasted [message] and the URL candidate the
 * user picked ([preferredUrl], null while nothing is chosen).
 */
data class CompanionAnalysisRequest(
    val message: String,
    val preferredUrl: String? = null,
    /** True when the user agreed that this message may be sent to the AI. */
    val consented: Boolean = false,
)

/**
 * What the "From message" panel of the add-source page shows.
 *
 * [available] is false when the host app has no reader at all (no AI client, so nobody could read
 * a message) — the tab is then not offered instead of leading to a dead end.
 */
data class CompanionMessagePanel(
    val available: Boolean = true,
    /** The pasted text, kept so a refused consent does not lose it. */
    val message: String = "",
    /** True when this panel is the one the page opens on (after a refused consent). */
    val active: Boolean = false,
    /** The error to show, e.g. the missing consent. */
    val error: String? = null,
)
