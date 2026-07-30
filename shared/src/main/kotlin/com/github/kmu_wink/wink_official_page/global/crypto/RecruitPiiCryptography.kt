package com.github.kmu_wink.wink_official_page.global.crypto

import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.text.Normalizer
import java.util.Base64
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Application-level protection for recruit application data.
 *
 * A single deployment secret is expanded into independent encryption, blind-index, and edit-token
 * keys. Ciphertext is self-identifying so legacy plaintext remains readable during migration while
 * malformed or unsupported protected values fail closed.
 */
class RecruitPiiCryptography private constructor(
    masterKey: ByteArray,
) {
    private val encryptionKey = SecretKeySpec(deriveKey(masterKey, ENCRYPTION_KEY_CONTEXT), "AES")
    private val blindIndexKey = deriveKey(masterKey, BLIND_INDEX_KEY_CONTEXT)
    private val editTokenKey = deriveKey(masterKey, EDIT_TOKEN_KEY_CONTEXT)
    private val secureRandom = SecureRandom()

    fun encrypt(propertyName: String, plaintext: String): String {
        require(propertyName.isNotBlank()) { "propertyName must not be blank" }
        if (plaintext.isEmpty()) {
            return plaintext
        }
        if (isEncrypted(plaintext)) {
            decrypt(propertyName, plaintext)
            return plaintext
        }
        if (plaintext.startsWith(PROTECTED_VALUE_NAMESPACE)) {
            throw IllegalArgumentException("Unsupported recruit PII ciphertext version")
        }

        val nonce = ByteArray(NONCE_BYTES).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(propertyName.toByteArray(StandardCharsets.UTF_8))
        val ciphertextWithTag = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val payload = ByteArray(nonce.size + ciphertextWithTag.size)
        nonce.copyInto(payload)
        ciphertextWithTag.copyInto(payload, nonce.size)

        return CURRENT_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(payload)
    }

    fun decrypt(propertyName: String, storedValue: String): String {
        require(propertyName.isNotBlank()) { "propertyName must not be blank" }
        if (!storedValue.startsWith(PROTECTED_VALUE_NAMESPACE)) {
            return storedValue
        }
        if (!isEncrypted(storedValue)) {
            throw IllegalArgumentException("Unsupported recruit PII ciphertext version")
        }

        try {
            val payload = Base64.getUrlDecoder().decode(storedValue.removePrefix(CURRENT_PREFIX))
            require(payload.size > NONCE_BYTES + TAG_BYTES) { "Invalid recruit PII ciphertext" }
            val nonce = payload.copyOfRange(0, NONCE_BYTES)
            val ciphertextWithTag = payload.copyOfRange(NONCE_BYTES, payload.size)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, GCMParameterSpec(TAG_BITS, nonce))
            cipher.updateAAD(propertyName.toByteArray(StandardCharsets.UTF_8))

            return String(cipher.doFinal(ciphertextWithTag), StandardCharsets.UTF_8)
        } catch (exception: GeneralSecurityException) {
            throw IllegalStateException("Recruit PII ciphertext authentication failed", exception)
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException("Recruit PII ciphertext is malformed", exception)
        }
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(CURRENT_PREFIX)

    fun normalizeStudentId(value: String): String = normalizeText(value)

    fun normalizeEmail(value: String): String = normalizeText(value).lowercase(Locale.ROOT)

    fun normalizePhoneNumber(value: String): String {
        val digits = normalizeText(value).filter(Char::isDigit)
        return when (digits.length) {
            10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
            11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
            else -> digits
        }
    }

    fun studentIdBlindIndex(value: String): String =
        blindIndex("student-id", normalizeStudentId(value))

    fun emailBlindIndex(value: String): String =
        blindIndex("email", normalizeEmail(value))

    fun phoneNumberBlindIndex(value: String): String =
        blindIndex("phone-number", normalizePhoneNumber(value))

    fun editTokenDigest(rawToken: String): String {
        require(rawToken.isNotBlank()) { "Edit token must not be blank" }
        return hmac(editTokenKey, "edit-token\u0000$rawToken")
    }

    private fun blindIndex(field: String, normalizedValue: String): String {
        require(normalizedValue.isNotBlank()) { "$field must not be blank" }
        return hmac(blindIndexKey, "$field\u0000$normalizedValue")
    }

    private fun hmac(key: ByteArray, value: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(value.toByteArray(StandardCharsets.UTF_8)))
    }

    private fun normalizeText(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFKC)

    companion object {
        const val CURRENT_PREFIX = "wink-pii:v1:"
        private const val PROTECTED_VALUE_NAMESPACE = "wink-pii:"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val NONCE_BYTES = 12
        private const val TAG_BITS = 128
        private const val TAG_BYTES = TAG_BITS / 8
        private const val MASTER_KEY_BYTES = 32
        private const val ENCRYPTION_KEY_CONTEXT = "wink/recruit-pii/aes-gcm/v1"
        private const val BLIND_INDEX_KEY_CONTEXT = "wink/recruit-pii/blind-index/v1"
        private const val EDIT_TOKEN_KEY_CONTEXT = "wink/recruit-pii/edit-token/v1"

        fun fromBase64(base64MasterKey: String): RecruitPiiCryptography {
            require(base64MasterKey.isNotBlank()) {
                "RECRUIT_PII_ENCRYPTION_KEY must be configured"
            }
            val decoded = try {
                Base64.getDecoder().decode(base64MasterKey)
            } catch (exception: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "RECRUIT_PII_ENCRYPTION_KEY must be valid Base64",
                    exception,
                )
            }
            require(decoded.size == MASTER_KEY_BYTES) {
                "RECRUIT_PII_ENCRYPTION_KEY must decode to exactly $MASTER_KEY_BYTES bytes"
            }

            return try {
                RecruitPiiCryptography(decoded)
            } finally {
                decoded.fill(0)
            }
        }

        private fun deriveKey(masterKey: ByteArray, context: String): ByteArray {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(SecretKeySpec(masterKey, HMAC_ALGORITHM))
            return mac.doFinal(context.toByteArray(StandardCharsets.UTF_8))
        }
    }
}
