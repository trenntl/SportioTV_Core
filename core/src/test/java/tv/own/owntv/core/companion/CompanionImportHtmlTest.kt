package tv.own.owntv.core.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.own.owntv.core.model.SourceAnalysisAiState
import tv.own.owntv.core.model.SourceMessageConfidence
import tv.own.owntv.core.model.SourceType

/**
 * The two pages that read a pasted provider message: the paste panel and the confirmation.
 *
 * They are rendered without an Android context (the labels come in resolved), so what they put on
 * the screen is checkable here: the consent, the values, the type and the choice of address.
 */
class CompanionImportHtmlTest {

    private fun labels() = CompanionHtml.ImportLabels(
        heading = "Paste the provider's message",
        description = "Paste the message with your access data.",
        messageLabel = "Message",
        consent = "I agree that this message is sent to DeepSeek.",
        analyze = "Analyze",
        analyzing = "Reading the message",
        confirmHeading = "Check the data before it goes to the TV",
        confirmDescription = "These values were read from your message.",
        typeLabel = "Type",
        xtream = "Xtream",
        m3u = "M3U",
        stalker = "Stalker",
        name = "Name (optional)",
        namePlaceholder = "My IPTV",
        serverUrl = "Server URL",
        portalUrl = "Portal URL",
        username = "Username",
        password = "Password",
        mac = "MAC address",
        sendToTv = "Send to TV",
        chooseUrl = "Which address?",
        showUrl = "Show this address",
        readByAi = "The values were read by the AI.",
        readByRules = "The values were read by the TV's own rules.",
        aiNoKey = "No AI key is set.",
        aiInvalidKey = "The AI provider rejected the key.",
        aiNoCredit = "There is no credit left.",
        aiFailed = "The AI could not be reached.",
        aiDisabled = "The AI functions are switched off on the TV.",
        guessed = "Some values were only guessed.",
        nothingFound = "Nothing readable was found in the message.",
        missingFields = "Fill in everything the source needs.",
    )

    private fun analysis(
        type: SourceType = SourceType.XTREAM,
        server: String = "http://h:8080",
        username: String = "u",
        password: String = "p",
        mac: String = "",
        urls: List<String> = listOf("http://h:8080"),
        url: String? = "http://h:8080",
        confidence: SourceMessageConfidence = SourceMessageConfidence.HIGH,
        fromAi: Boolean = true,
        aiState: SourceAnalysisAiState = SourceAnalysisAiState.USED,
    ) = CompanionSourceAnalysis(
        type = type,
        server = server,
        username = username,
        password = password,
        mac = mac,
        urls = urls,
        url = url,
        confidence = confidence,
        fromAi = fromAi,
        aiState = aiState,
    )

    // ---- the paste panel ----

    @Test
    fun `the paste panel carries the message and asks for the consent`() {
        val html = CompanionHtml.messagePanelHtml(labels(), pin = "123456", message = "hallo", error = null, active = false)

        assertTrue(html.contains("action=\"/source/analyze?pin=123456\""))
        assertTrue(html.contains("<textarea id=\"srcMessage\" name=\"message\""))
        assertTrue(html.contains(">hallo</textarea>"))
        assertTrue(html.contains("name=\"consent\""))
        assertTrue(html.contains("I agree that this message is sent to DeepSeek."))
        assertFalse(html.contains("class=\"err\""))
    }

    @Test
    fun `the paste panel shows the error and keeps the message`() {
        val html = CompanionHtml.messagePanelHtml(labels(), pin = "123456", message = "hallo", error = "Please confirm.", active = true)

        assertTrue(html.contains("class=\"err\""))
        assertTrue(html.contains("Please confirm."))
        assertTrue(html.contains(">hallo</textarea>"))
        assertTrue(html.contains("panel active"))
    }

    @Test
    fun `a message with markup in it cannot break the page`() {
        val html = CompanionHtml.messagePanelHtml(labels(), pin = "123456", message = "<script>x</script>", error = null, active = false)

        assertTrue(html.contains("&lt;script&gt;"))
        assertFalse(html.contains("<script>x</script>"))
    }

    // ---- the confirmation ----

    @Test
    fun `the confirmation posts to the endpoint of the type`() {
        val xtream = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(), "hallo")
        assertTrue(xtream.contains("action=\"/xtream?pin=123456\""))

        val m3u = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(type = SourceType.M3U), "hallo")
        assertTrue(m3u.contains("action=\"/m3u?pin=123456\""))

        val stalker = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(type = SourceType.STALKER), "hallo")
        assertTrue(stalker.contains("action=\"/stalker?pin=123456\""))
    }

    @Test
    fun `the confirmation marks the type the analysis found`() {
        val html = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(type = SourceType.M3U), "hallo")

        assertTrue(html.contains("<option value=\"m3u\" selected>"))
        assertTrue(html.contains("<option value=\"xtream\">"))
    }

    @Test
    fun `the confirmation carries the values that were read`() {
        val html = CompanionHtml.sourceConfirmBody(
            labels(),
            "123456",
            analysis(server = "http://h:8080", username = "user1", password = "pass1", mac = "00:1A:79:AA:BB:CC"),
            "hallo",
        )

        assertTrue(html.contains("value=\"http://h:8080\""))
        assertTrue(html.contains("value=\"user1\""))
        assertTrue(html.contains("value=\"pass1\""))
        assertTrue(html.contains("value=\"00:1A:79:AA:BB:CC\""))
    }

    @Test
    fun `the confirmation says who read the values`() {
        val ai = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(fromAi = true), "hallo")
        assertTrue(ai.contains("The values were read by the AI."))
        assertFalse(ai.contains("The values were read by the TV's own rules."))

        val rules = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(fromAi = false), "hallo")
        assertTrue(rules.contains("The values were read by the TV's own rules."))

        val guessed = CompanionHtml.sourceConfirmBody(
            labels(),
            "123456",
            analysis(confidence = SourceMessageConfidence.GUESS),
            "hallo",
        )
        assertTrue(guessed.contains("Some values were only guessed."))
    }

    @Test
    fun `a message nothing could be read from says so`() {
        val html = CompanionHtml.sourceConfirmBody(
            labels(),
            "123456",
            analysis(server = "", username = "", password = ""),
            "hallo",
        )

        assertTrue(html.contains("Nothing readable was found in the message."))
    }

    @Test
    fun `a missing key is named and the way to it is given`() {
        val html = CompanionHtml.sourceConfirmBody(
            labels(),
            "123456",
            analysis(fromAi = false, aiState = SourceAnalysisAiState.NO_KEY),
            "hallo",
        )

        assertTrue(html.contains("No AI key is set."))
        assertFalse(html.contains("The values were read by the AI."))
    }

    @Test
    fun `every reason the ai did not read is told apart`() {
        fun hint(state: SourceAnalysisAiState) = CompanionHtml.sourceConfirmBody(
            labels(),
            "123456",
            analysis(fromAi = false, aiState = state),
            "hallo",
        )

        assertTrue(hint(SourceAnalysisAiState.INVALID_KEY).contains("The AI provider rejected the key."))
        assertTrue(hint(SourceAnalysisAiState.NO_CREDIT).contains("There is no credit left."))
        assertTrue(hint(SourceAnalysisAiState.FAILED).contains("The AI could not be reached."))
        assertTrue(hint(SourceAnalysisAiState.NOT_ASKED).contains("The values were read by the TV's own rules."))
        // Switched off on the TV: the page says so and names where the switch is, so it is not
        // mistaken for a message the AI simply did not get to.
        assertTrue(hint(SourceAnalysisAiState.DISABLED).contains("The AI functions are switched off on the TV."))
    }

    // ---- the choice of address ----

    @Test
    fun `one address needs no choice`() {
        val html = CompanionHtml.sourceConfirmBody(labels(), "123456", analysis(), "hallo")

        assertFalse(html.contains("name=\"url\""))
    }

    @Test
    fun `several addresses are offered with the chosen one ticked`() {
        val html = CompanionHtml.sourceConfirmBody(
            labels(),
            "123456",
            analysis(
                urls = listOf("http://a:8080", "http://b:8080"),
                url = "http://b:8080",
            ),
            "hallo",
        )

        assertTrue(html.contains("Which address?"))
        assertTrue(html.contains("value=\"http://a:8080\""))
        assertTrue(html.contains("value=\"http://b:8080\" checked"))
        // The message travels in the body, never in the URL.
        assertTrue(html.contains("name=\"message\" value=\"hallo\""))
    }
}
