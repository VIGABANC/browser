package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AegisAiMessage
import com.example.data.model.AiTaskType
import com.example.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "AegisGeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private fun getModelForTask(taskType: AiTaskType): String {
        return when (taskType) {
            AiTaskType.PAGE_SUMMARY -> "gemini-3.5-flash"
            else -> "gemini-3.1-pro-preview"
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun executeDeepReasoning(
        prompt: String,
        taskType: AiTaskType,
        pageUrl: String? = null,
        pageTitle: String? = null,
        pageContentSnippet: String? = null,
        detectedMediaContext: String? = null
    ): AegisAiMessage = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val modelName = getModelForTask(taskType)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide an informative, high-value local fallback analysis when API key is unconfigured
            return@withContext generateLocalDeepAnalysis(prompt, taskType, pageUrl, pageTitle, pageContentSnippet, detectedMediaContext)
        }

        try {
            val systemInstruction = buildSystemPrompt(taskType)
            val fullUserPrompt = buildUserContextPrompt(prompt, taskType, pageUrl, pageTitle, pageContentSnippet, detectedMediaContext)

            val requestJson = JSONObject().apply {
                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", fullUserPrompt)
                            })
                        })
                    })
                })

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })

                // Generation Config
                val config = JSONObject().apply {
                    put("temperature", if (taskType == AiTaskType.PAGE_SUMMARY) 0.2 else 0.4)
                }
                if (modelName == "gemini-3.1-pro-preview") {
                    config.put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "high")
                    })
                }
                put("generationConfig", config)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL$modelName:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error: ${response.code} $responseBodyString")
                return@withContext generateLocalDeepAnalysis(
                    prompt = prompt,
                    taskType = taskType,
                    pageUrl = pageUrl,
                    pageTitle = pageTitle,
                    pageContentSnippet = pageContentSnippet,
                    detectedMediaContext = detectedMediaContext,
                    apiErrorMessage = "API Status ${response.code}: $responseBodyString"
                )
            }

            parseGeminiResponse(responseBodyString, taskType)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini thinking request", e)
            generateLocalDeepAnalysis(
                prompt = prompt,
                taskType = taskType,
                pageUrl = pageUrl,
                pageTitle = pageTitle,
                pageContentSnippet = pageContentSnippet,
                detectedMediaContext = detectedMediaContext,
                apiErrorMessage = e.localizedMessage
            )
        }
    }

    private fun parseGeminiResponse(jsonStr: String, taskType: AiTaskType): AegisAiMessage {
        val root = JSONObject(jsonStr)
        val candidates = root.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            return AegisAiMessage(
                role = MessageRole.ASSISTANT,
                content = "No candidate response returned by Gemini model.",
                taskType = taskType
            )
        }

        val firstCandidate = candidates.getJSONObject(0)
        val contentObj = firstCandidate.optJSONObject("content")
        val parts = contentObj?.optJSONArray("parts") ?: JSONArray()

        var thinkingAccumulator = StringBuilder()
        var answerAccumulator = StringBuilder()

        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val isThought = part.optBoolean("thought", false)
            val text = part.optString("text", "")

            if (isThought) {
                thinkingAccumulator.append(text).append("\n")
            } else {
                answerAccumulator.append(text)
            }
        }

        val finalThinking = if (thinkingAccumulator.isNotEmpty()) {
            thinkingAccumulator.toString().trim()
        } else null

        val finalAnswer = if (answerAccumulator.isNotEmpty()) {
            answerAccumulator.toString().trim()
        } else {
            "Analysis complete."
        }

        return AegisAiMessage(
            role = MessageRole.ASSISTANT,
            content = finalAnswer,
            thinkingTrace = finalThinking,
            isThinkingExpanded = true,
            taskType = taskType
        )
    }

    private fun buildSystemPrompt(taskType: AiTaskType): String {
        return when (taskType) {
            AiTaskType.DEEP_REASONING -> """
                You are Aegis Deep-Reasoning AI, a high-intelligence browser assistant.
                You utilize systematic, rigorous chain-of-thought to break down complex queries, verify logical premises, provide comprehensive structured answers, and evaluate edge cases.
            """.trimIndent()
            AiTaskType.PAGE_SUMMARY -> """
                You are Aegis Web Content Synthesizer. You read web page context, article text, or documentation and extract:
                1. Executive Summary
                2. Key Arguments & Data Points
                3. Technical / Critical Takeaways
                4. Actionable Next Steps or Conclusions.
            """.trimIndent()
            AiTaskType.MEDIA_ANALYSIS -> """
                You are Aegis Media & Video Architecture Inspector. You analyze detected web video/audio streams, determine content themes, explain technical container/codec/bitrate trade-offs (e.g. 1080p H.264 vs Opus/MP3 audio extraction), and extract video learning points.
            """.trimIndent()
            AiTaskType.COPYRIGHT_AUDIT -> """
                You are Aegis Legal & Copyright Compliance Auditor. You evaluate web resources, Creative Commons licensing (CC-BY, CC0, Public Domain), fair-use considerations for personal archiving, Terms of Service implications, and give clear compliance guidance.
            """.trimIndent()
            AiTaskType.PRIVACY_SCAN -> """
                You are Aegis Security & Privacy Auditor. You analyze the current website domain, cookie policies, third-party trackers, fingerprinting scripts, SSL posture, and suggest optimal Shield configurations.
            """.trimIndent()
        }
    }

    private fun sanitizeSnippet(raw: String): String {
        return raw
            .replace(Regex("""(?i)(password|secret|apikey|token|auth)\s*[:=]\s*['"][^'"]+['"]"""), "$1: [REDACTED]")
            .replace(Regex("""\b\d{4}[ -]?\d{4}[ -]?\d{4}[ -]?\d{4}\b"""), "[CARD REDACTED]")
            .take(4000)
    }

    private fun buildUserContextPrompt(
        prompt: String,
        taskType: AiTaskType,
        pageUrl: String?,
        pageTitle: String?,
        pageContentSnippet: String?,
        detectedMediaContext: String?
    ): String {
        val sb = StringBuilder()
        sb.append("User Query: ").append(prompt).append("\n\n")

        if (!pageTitle.isNullOrBlank()) {
            sb.append("Current Page Title: ").append(pageTitle).append("\n")
        }
        if (!pageUrl.isNullOrBlank()) {
            sb.append("Current Page URL: ").append(pageUrl).append("\n")
        }
        if (!detectedMediaContext.isNullOrBlank()) {
            sb.append("Detected Media Stream Details:\n").append(detectedMediaContext).append("\n")
        }
        if (!pageContentSnippet.isNullOrBlank()) {
            sb.append("\nPage Context Extract:\n").append(sanitizeSnippet(pageContentSnippet)).append("\n")
        }

        return sb.toString()
    }

    private fun generateLocalDeepAnalysis(
        prompt: String,
        taskType: AiTaskType,
        pageUrl: String?,
        pageTitle: String?,
        pageContentSnippet: String?,
        detectedMediaContext: String?,
        apiErrorMessage: String? = null
    ): AegisAiMessage {
        val domain = pageUrl?.substringAfter("://")?.substringBefore("/") ?: "web domain"
        val title = pageTitle ?: "Web Page"

        val notice = if (!apiErrorMessage.isNullOrBlank()) {
            "Note: Gemini API returned an error ($apiErrorMessage). Showing local overview."
        } else {
            "Note: Set your Gemini API key in Secrets to enable live Gemini cloud synthesis."
        }

        val thinking = """
            [Local Analysis Engine]
            Evaluating available page metadata for target '${title}' ($domain).
            Query: "$prompt"
            Task: $taskType
        """.trimIndent()

        val answer = when (taskType) {
            AiTaskType.PAGE_SUMMARY -> """
                ### 📑 Page Overview: ${title}
                
                **Page Information:**
                • **Title:** ${title}
                • **Domain:** ${domain}
                • **Snippet Preview:** ${pageContentSnippet?.take(200)?.trim()?.ifBlank { "No text extracted." } ?: "No text extracted."}
                
                > *$notice*
            """.trimIndent()

            AiTaskType.MEDIA_ANALYSIS -> """
                ### 🎬 Media Stream Overview
                
                **Detected Media Information:**
                • **Domain:** $domain
                • **Stream Details:** ${detectedMediaContext ?: "No direct stream detected on page."}
                
                > *$notice*
            """.trimIndent()

            AiTaskType.COPYRIGHT_AUDIT -> """
                ### ⚖️ Content & Domain Information: $domain
                
                • **Source URL:** ${pageUrl ?: domain}
                • **Policy:** Please verify content licensing and permissions directly with the host domain before archiving or downloading media.
                
                > *$notice*
            """.trimIndent()

            AiTaskType.PRIVACY_SCAN -> """
                ### 🛡️ Privacy & Security Overview: $domain
                
                • **Protocol:** ${if (pageUrl?.startsWith("https") == true) "HTTPS Secure Connection" else "HTTP Unencrypted Connection"}
                • **Aegis Shields:** Active local blocking of known advertising & tracker hostlists.
                
                > *$notice*
            """.trimIndent()

            AiTaskType.DEEP_REASONING -> """
                ### 🧠 Query Analysis: ${prompt.ifBlank { title }}
                
                • **Target:** ${title} ($domain)
                • **Prompt:** $prompt
                
                > *$notice*
            """.trimIndent()
        }

        return AegisAiMessage(
            role = MessageRole.ASSISTANT,
            content = answer,
            thinkingTrace = thinking,
            isThinkingExpanded = false,
            taskType = taskType
        )
    }
}
