package xyz.doocode.superbus.core.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.Instant

object DateDeserializer : JsonDeserializer<Long> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Long {
        return try {
            when {
                json?.isJsonPrimitive == true -> {
                    val value = json.asString
                    value.toLongOrNull() ?: Instant.parse(value).toEpochMilli()
                }

                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}
