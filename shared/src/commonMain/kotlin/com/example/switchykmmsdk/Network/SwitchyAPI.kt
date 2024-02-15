package com.example.switchykmmsdk.Network

import com.example.switchykmmsdk.Entity.EnergyData
import com.example.switchykmmsdk.Entity.PowerUsage
import com.example.switchykmmsdk.Entity.RocketLaunch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

typealias Params = Map<String, Any?>
typealias Headers = Map<String, Any?>

//JSON
internal val remoteRepoJson
    get() = Json { useArrayPolymorphism = true; ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

inline val JsonElement.booleanValue: Boolean
    get() = this.jsonPrimitive.boolean

inline val JsonElement.safeBoolean: Boolean
    get() = this.jsonPrimitive.content.lowercase() != "false" && this.jsonPrimitive.content != "0"

inline val JsonElement.longValue: Long
    get() = if (this.contentValue == "") -1 else this.jsonPrimitive.content.toLong()

inline val JsonElement.contentValue: String
    get() = this.jsonPrimitive.content

val String.safeLong: Long
    get() = if (this == "") -1 else this.toLong()

private fun JsonElement.safeLong(defaultValue: Long = -1): Long {
    return if (this.jsonPrimitive.content == "") defaultValue else this.contentValue.toLong()
}

fun HttpRequestBuilder.addHeaders(headers: Headers?) {
    headers?.forEach {
        header(it.key, it.value)
    }
}

fun HttpRequestBuilder.addParams(params: Params?) {
    params?.forEach {
        parameter(it.key, it.value)
    }
}

interface APIAuthorizationDelegate {
    suspend fun getAccessToken(): String?
}

internal class SwitchyAPI(private val authDelegate: APIAuthorizationDelegate) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getAllLaunches(): List<RocketLaunch> {
        return httpClient.get("https://api.spacexdata.com/v5/launches").body()
    }

    suspend fun getPowerUsage(deviceId: String, from: Long, to: Long): Result<List<PowerUsage>> {
        val params: Params = mapOf(
            "deviceid" to deviceId,
            "type" to 2.toString(),
            "from" to from.toString(),
            "to" to to.toString()
        )

        return try {
            val items = httpClient.get("https://api2.switchy.in/energydata/") {
                addParams(params)
                val token = authDelegate.getAccessToken()
                if (token != null) addHeaders(mapOf("Authorization" to token))
            }.body<List<PowerUsage>>()
            Result.success(items)
        } catch (err: Exception) {
            Result.failure(err)
        }
    }

    suspend fun getEnergyData(deviceId: String, from: Long, to: Long): Result<List<EnergyData>> {
        val params: Params = mapOf(
            "deviceid" to deviceId,
            "type" to 3.toString(),
            "from" to from.toString(),
            "to" to to.toString()
        )

        return try {
            val items: List<EnergyData> = httpClient.get("https://api2.switchy.in/energydata/") {
                addParams(params)
                val token = authDelegate.getAccessToken()
                if (token != null) addHeaders(mapOf("Authorization" to token))
            }.body()
            Result.success(items)
        } catch (err: Exception) {
            Result.failure(err)
        }
    }

    suspend fun getTimeBounds(deviceId: String): Result<Pair<Long, Long>> {
        val params: Params = mapOf(
            "deviceid" to deviceId,
            "type" to 4.toString(),
        )
        @Serializable data class Payload(val start: Long, val end: Long)
        return try {
            val payload = httpClient.get("https://api2.switchy.in/energydata/") {
                addParams(params)
                val token = authDelegate.getAccessToken()
                if (token != null) addHeaders(mapOf("Authorization" to token))
            }.body<Payload>()
            Result.success(Pair(payload.start, payload.end))
        } catch (err: Exception) {
            Result.failure(err)
        }
    }
}