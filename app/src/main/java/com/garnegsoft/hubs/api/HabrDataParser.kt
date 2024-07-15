package com.garnegsoft.hubs.api

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
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
    }
}
