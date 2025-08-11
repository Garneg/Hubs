package com.garnegsoft.hubs.api

import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.Firebase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class HabrDataParser<T> {
    companion object{
        val customJson = Json { ignoreUnknownKeys = true }
        inline fun <reified T> parseJson(json: String): T {
            return parseJson(customJson.parseToJsonElement(json))
        }

        inline fun <reified T> parseJson(json: JsonElement): T {
            return parseJson(json, customJson)
        }

        inline fun <reified T> parseJson(jsonElement: JsonElement, json: Json): T {
            return json.decodeFromJsonElement(jsonElement)

        }

        inline fun <reified T> parseJsonOrNull(json: String): T? {
            return try {
                parseJson(json)
            } catch (ex: Exception) {
                null
            }
        }

        inline fun <reified T> parseJsonResult(json: String): Result<T> {
            return try {
                val result = parseJson<T>(json)
                Result.success(result)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }
}
