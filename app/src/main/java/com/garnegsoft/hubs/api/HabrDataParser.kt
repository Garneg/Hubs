package com.garnegsoft.hubs.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class HabrDataParser<T> {
    companion object{
        inline fun <reified T> parseJson(json: String): T {
            val customJson = Json { isLenient = true
                ignoreUnknownKeys = true }
            return parseJson(customJson.parseToJsonElement(json))
        }

        inline fun <reified T> parseJson(json: JsonElement): T {
            val customJson = Json { isLenient = true
                ignoreUnknownKeys = true }
            return parseJson(json, customJson)
        }

        inline fun <reified T> parseJson(jsonElement: JsonElement, json: Json): T {
            return json.decodeFromJsonElement(jsonElement)
        }
    }
}
