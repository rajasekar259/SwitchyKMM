package com.example.switchykmmsdk.Entity

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnergyData(
    @SerialName("time") val epochSeconds: Long,
    val energy: Double
)

@Serializable
data class PowerUsage(
    @SerialName("time") val epochSeconds: Long,
    val power: Double
)

data class SyncStatus(
    val tableName: String,
    var mostRecentTime: Long,
    var leastRecentTime: Long,
    var startTime: Long?
)

@Serializable
data class RocketLaunch(
    @SerialName("flight_number")
    val flightNumber: Int,
    @SerialName("name")
    val missionName: String,
    @SerialName("date_utc")
    val launchDateUTC: String,
    @SerialName("details")
    val details: String?,
    @SerialName("success")
    val launchSuccess: Boolean?,
    @SerialName("links")
    val links: Links
) {
    var launchYear = launchDateUTC.toInstant().toLocalDateTime(TimeZone.UTC).year
}

@Serializable
data class Links(
    @SerialName("patch")
    val patch: Patch?,
    @SerialName("article")
    val article: String?
)

@Serializable
data class Patch(
    @SerialName("small")
    val small: String?,
    @SerialName("large")
    val large: String?
)