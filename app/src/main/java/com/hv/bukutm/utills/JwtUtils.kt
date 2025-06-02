package com.hv.bukutm.utils

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object JwtUtils {

    fun getRoleFromToken(token: String?): String? {
        if (token.isNullOrBlank()) return null

        try {
            // JWT token has 3 parts: Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val jsonObject = JSONObject(payload)

            // Extract the "role" claim
            return jsonObject.optString("role", null)
        } catch (e: Exception) {
            return null
        }
    }

    fun getNameFromToken(token: String?): String? {
        if (token.isNullOrBlank()) return null

        try {
            // JWT token has 3 parts: Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val jsonObject = JSONObject(payload)

            // Extract the "unique_name" claim
            return jsonObject.optString("unique_name", null)
        } catch (e: Exception) {
            return null
        }
    }

    fun getEmailFromToken(token: String?): String? {
        if (token.isNullOrBlank()) return null

        try {
            // JWT token has 3 parts: Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val jsonObject = JSONObject(payload)

            // Extract the "email" claim
            return jsonObject.optString("email", null)
        } catch (e: Exception) {
            return null
        }
    }

    fun getUserIdFromToken(token: String?): String? {
        if (token.isNullOrBlank()) return null

        try {
            // JWT token has 3 parts: Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8)
            val jsonObject = JSONObject(payload)

            // Extract the "nameid" claim
            return jsonObject.optString("nameid", null)
        } catch (e: Exception) {
            return null
        }
    }
}