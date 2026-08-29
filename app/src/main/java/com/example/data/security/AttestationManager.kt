package com.example.data.security

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AttestationManager(private val context: Context) {

    private val logFile: File by lazy {
        File(context.filesDir, ATTESTATION_FILE_NAME)
    }

    private val hmacKeySpec = SecretKeySpec(HMAC_SECRET_KEY, "HmacSHA256")

    var isSafeMode: Boolean
        get() {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SAFE_MODE, true)
        }
        private set(value) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SAFE_MODE, value).apply()
        }

    val isExtendedMode: Boolean
        get() = !isSafeMode

    fun attestExtendedMode(deviceFingerprint: String): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            val event = "OPT_IN_EXTENDED_MODE"

            val payload = JSONObject().apply {
                put("timestamp", timestamp)
                put("event", event)
                put("fingerprint", deviceFingerprint)
                put("mode", "EXTENDED")
            }.toString()

            val signature = computeHmac(payload)

            val entry = JSONObject().apply {
                put("payload", payload)
                put("signature", signature)
            }.toString()

            logFile.appendText("$entry\n")
            isSafeMode = false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun revertToSafeMode(): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            val payload = JSONObject().apply {
                put("timestamp", timestamp)
                put("event", "REVERT_SAFE_MODE")
                put("mode", "SAFE")
            }.toString()

            val signature = computeHmac(payload)

            val entry = JSONObject().apply {
                put("payload", payload)
                put("signature", signature)
            }.toString()

            logFile.appendText("$entry\n")
            isSafeMode = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun verifyLogIntegrity(): Boolean {
        if (!logFile.exists()) return true
        return try {
            logFile.readLines().forEach { line ->
                if (line.isBlank()) return@forEach
                val json = JSONObject(line)
                val payload = json.getString("payload")
                val signature = json.getString("signature")
                val expectedSignature = computeHmac(payload)
                if (signature != expectedSignature) return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun computeHmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(hmacKeySpec)
        val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    companion object {
        private const val PREFS_NAME = "aegis_attestation_prefs"
        private const val KEY_SAFE_MODE = "key_safe_mode"
        private const val ATTESTATION_FILE_NAME = "attestation_log.jsonl"

        private val HMAC_SECRET_KEY = byteArrayOf(
            0x41, 0x65, 0x67, 0x69, 0x73, 0x41, 0x74, 0x74,
            0x65, 0x73, 0x74, 0x61, 0x74, 0x69, 0x6F, 0x6E,
            0x4B, 0x65, 0x79, 0x32, 0x30, 0x32, 0x36, 0x21,
            0x53, 0x65, 0x63, 0x75, 0x72, 0x65, 0x48, 0x53
        )
    }
}
