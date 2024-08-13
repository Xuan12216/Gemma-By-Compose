package com.xuan.gemma.util.converters

import android.net.Uri
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

class UriTypeAdapter : JsonSerializer<Uri>, JsonDeserializer<Uri> {
    override fun serialize(src: Uri?, typeOfSrc: java.lang.reflect.Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: java.lang.reflect.Type?, context: JsonDeserializationContext?): Uri {
        return when {
            json == null -> throw JsonParseException("JsonElement is null")
            json.isJsonPrimitive -> Uri.parse(json.asString)
            json.isJsonObject -> {
                val jsonObject = json.asJsonObject
                println("TestXuan: $jsonObject")
                if (jsonObject.has("uri")) {
                    Uri.parse(jsonObject.get("uri").asString)
                }
                else {
                    throw JsonParseException("JsonObject does not contain 'uri' field")
                }
            }
            else -> throw JsonParseException("Expected JsonPrimitive or JsonObject but found ${json.javaClass}")
        }
    }
}