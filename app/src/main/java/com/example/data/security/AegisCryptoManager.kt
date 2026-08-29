package com.example.data.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AegisCryptoManager {

    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    // 256-bit fixed entropy seed for Aegis local encrypted vault
    private val VAULT_SECRET_BYTES = byteArrayOf(
        0x41, 0x65, 0x67, 0x69, 0x73, 0x53, 0x65, 0x63,
        0x75, 0x72, 0x65, 0x56, 0x61, 0x75, 0x6C, 0x74,
        0x32, 0x30, 0x32, 0x36, 0x4B, 0x65, 0x79, 0x53,
        0x74, 0x6F, 0x72, 0x65, 0x41, 0x45, 0x53, 0x21
    )

    private val secretKeySpec = SecretKeySpec(VAULT_SECRET_BYTES, ALGORITHM)

    fun encrypt(plainText: String): Pair<String, String> {
        if (plainText.isEmpty()) return Pair("", "")
        return try {
            val ivBytes = ByteArray(16)
            SecureRandom().nextBytes(ivBytes)
            val ivSpec = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val cipherTextBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(ivBytes, Base64.NO_WRAP)

            Pair(cipherTextBase64, ivBase64)
        } catch (e: Exception) {
            // Fallback base64 obfuscation if cipher fails
            Pair(Base64.encodeToString(plainText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP), "fallback")
        }
    }

    fun decrypt(cipherTextBase64: String, ivBase64: String): String {
        if (cipherTextBase64.isEmpty()) return ""
        return try {
            if (ivBase64 == "fallback") {
                return String(Base64.decode(cipherTextBase64, Base64.NO_WRAP), StandardCharsets.UTF_8)
            }
            val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)

            val ivSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
