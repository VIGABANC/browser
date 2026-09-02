package com.example

import com.example.data.security.AegisCryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AegisCryptoManagerTest {

    @Test
    fun `encrypt and decrypt round trip preserves plaintext`() {
        val originalSecret = "SuperSecretP@ssw0rd!2026"
        val encrypted = AegisCryptoManager.encrypt(originalSecret)

        assertTrue(encrypted.isNotBlank())
        assertTrue(encrypted.startsWith("v2:"))

        val decrypted = AegisCryptoManager.decrypt(encrypted)
        assertEquals(originalSecret, decrypted)
    }

    @Test
    fun `identical plaintexts produce distinct nonces and ciphertexts`() {
        val secret = "ConsistentSecretToken123"
        val enc1 = AegisCryptoManager.encrypt(secret)
        val enc2 = AegisCryptoManager.encrypt(secret)

        assertNotEquals(enc1, enc2)
        assertEquals(secret, AegisCryptoManager.decrypt(enc1))
        assertEquals(secret, AegisCryptoManager.decrypt(enc2))
    }

    @Test
    fun `tampered ciphertext or tag fails decryption and does not return plaintext`() {
        val secret = "VaultFinancialPIN#9876"
        val encrypted = AegisCryptoManager.encrypt(secret)

        val parts = encrypted.split(":")
        assertEquals(3, parts.size)
        // Corrupt the ciphertext payload
        val corruptedPayload = parts[0] + ":" + parts[1] + ":" + parts[2].reversed()

        val decrypted = AegisCryptoManager.decrypt(corruptedPayload)
        // Must fail closed (empty or blank), never return plaintext
        assertTrue(decrypted.isEmpty())
    }

    @Test
    fun `legacy CBC record format migrates cleanly`() {
        // Test legacy encryption string
        val legacySecret = "LegacyPasswordToMigrate"
        val legacyPair = AegisCryptoManager.encryptLegacyForMigrationTesting(legacySecret)
        
        val decryptedLegacy = AegisCryptoManager.decryptLegacy(legacyPair.first, legacyPair.second)
        assertEquals(legacySecret, decryptedLegacy)
    }
}
