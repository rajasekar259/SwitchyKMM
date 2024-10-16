package com.example.switchykmmsdk

import com.example.switchykmmsdk.Cache.Database
import com.example.switchykmmsdk.Cache.DbTables
import com.example.switchykmmsdk.Entity.EnergyData
import com.example.switchykmmsdk.Entity.PowerUsage
import com.example.switchykmmsdk.Entity.RocketLaunch
import com.example.switchykmmsdk.Entity.SyncStatus
import com.example.switchykmmsdk.Network.APIAuthorizationDelegate
import com.example.switchykmmsdk.Network.SwitchyAPI
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.format.*
import kotlinx.datetime.toInstant
import kotlin.math.abs
import kotlin.math.min

class SwitchyKMMSDK(driverFactory: DatabaseDriverFactory?, authDelegate: APIAuthorizationDelegate) {
    private val database: Database? = if (driverFactory != null) Database(driverFactory) else null
    private val api = SwitchyAPI(authDelegate)

    /* **** Power Usages **** */
    private suspend fun insertHousePowerUsage(powerUsages: List<PowerUsage>, deviceId: String) {
        database!!.insertHousePowerUsage(powerUsages, deviceId)
    }
    private suspend fun removeHousePowerUsages() {
        database!!.removeHousePowerUsages()
    }
    suspend fun getAllPowerUsages(deviceId: String): List<PowerUsage> {
        return database!!.getPowerUsages(null, null, deviceId)
    }

    suspend fun getPowerUsages(deviceId: String, from: Long, to: Long): List<PowerUsage> {
        val syncStatus = database!!.getSyncStatus(DbTables.PowerUsage.name, deviceId)

        if (!(syncStatus != null && syncStatus.mostRecentTime >= to && syncStatus.leastRecentTime <= from)) {
            while ((database.getSyncStatus(DbTables.PowerUsage.name, deviceId)?.mostRecentTime ?: 0) < to) {
                if (!fetchNewerPowerUsage(deviceId)) break
            }
            while ((database.getSyncStatus(DbTables.PowerUsage.name, deviceId)?.leastRecentTime ?: Long.MAX_VALUE) > from) {
                if (!fetchOlderPowerUsage(deviceId)) break
            }
        }
        return database.getPowerUsages(from, to, deviceId)
    }

    suspend fun getPowerUsageFromAPI(deviceId: String, from: Long, to: Long): Result<List<PowerUsage>> {
        return api.getPowerUsage(deviceId, from, to)
    }

    suspend fun fetchNewerPowerUsage(deviceId: String): Boolean {
        var syncStatus = database!!.getSyncStatus(DbTables.PowerUsage.name, deviceId)
        if (syncStatus != null &&
            Clock.System.now().epochSeconds - syncStatus.mostRecentTime <= 60 * 60 * 2) return false

        val timeFrame = getTimeFrame(syncStatus, newData = true)
        val data = api.getPowerUsage(deviceId, timeFrame.first, timeFrame.second).getOrNull()

        return if (data != null) {
            database.insertHousePowerUsage(data, deviceId)

            if (syncStatus == null) {
                syncStatus = SyncStatus(
                    DbTables.PowerUsage.name,
                    timeFrame.second,
                    timeFrame.first,
                    getStartTime(deviceId))
            }
            syncStatus.mostRecentTime = timeFrame.second
            database.insertOrUpdateSyncStatus(syncStatus, deviceId)

            true
        } else false
    }

    suspend fun fetchOlderPowerUsage(deviceId: String): Boolean {
        val syncStatus = database!!.getSyncStatus(DbTables.PowerUsage.name, deviceId)
        return if (syncStatus == null) {
            fetchNewerPowerUsage(deviceId)
        } else if (syncStatus.leastRecentTime == -1L) {
            false
        } else {
            if (syncStatus.startTime == null)
                syncStatus.startTime = getStartTime(deviceId)

            val timeFrame = getTimeFrame(syncStatus, newData = false)
            val data = api.getPowerUsage(deviceId, timeFrame.first, timeFrame.second)
                .getOrNull()

            if (data != null) {
                database.insertHousePowerUsage(data, deviceId)
                syncStatus.leastRecentTime = if (timeFrame.first <= (syncStatus.startTime
                        ?: 0L)
                ) timeFrame.first else -1L
                database.insertOrUpdateSyncStatus(syncStatus, deviceId)
                true
            } else false
        }
    }


    /* **** Energy Data **** */

    suspend fun getAllEnergyData(deviceId: String): List<EnergyData> {
        return database!!.getAllHouseEnergyUsages(null, null, deviceId)
    }
    suspend fun getEnergyData(deviceId: String, from: Long, to: Long): List<EnergyData> {
        val syncStatus = database!!.getSyncStatus(DbTables.EnergyData.name, deviceId)

        if (!(syncStatus != null && syncStatus.mostRecentTime >= to && syncStatus.leastRecentTime <= from)) {
            while ((database.getSyncStatus(DbTables.EnergyData.name, deviceId)?.mostRecentTime ?: 0) < to) {
                if (!fetchNewerEnergyData(deviceId)) break
            }
            while ((database.getSyncStatus(DbTables.EnergyData.name, deviceId)?.leastRecentTime ?: Long.MAX_VALUE) > from) {
                if (!fetchOlderEnergyData(deviceId)) break
            }
        }
        return database.getAllHouseEnergyUsages(from, to, deviceId)
    }

    suspend fun fetchOlderEnergyData(deviceId: String): Boolean {
        val syncStatus = database!!.getSyncStatus(DbTables.EnergyData.name, deviceId)
        return if (syncStatus == null) {
            fetchNewerEnergyData(deviceId)
        } else if (syncStatus.leastRecentTime == -1L) {
            false
        } else {
            if (syncStatus.startTime == null)
                syncStatus.startTime = getStartTime(deviceId)

            val timeFrame = getTimeFrame(syncStatus, newData = false)
            val data = api.getEnergyData(deviceId, timeFrame.first, timeFrame.second)
                .getOrNull()

            if (data != null) {
                database.insertHouseEnergyUsage(data, deviceId)
                syncStatus.leastRecentTime = if (timeFrame.first <= (syncStatus.startTime
                        ?: 0L)
                ) timeFrame.first else -1L
                database.insertOrUpdateSyncStatus(syncStatus, deviceId)
                true
            } else false
        }
    }
    suspend fun fetchEnergyDataFromAPI(deviceId: String, from: Long, to: Long) = api.getEnergyData(deviceId, from, to)
    
suspend fun fetchNewerEnergyData(deviceId: String): Boolean {
    var syncStatus = database!!.getSyncStatus(DbTables.EnergyData.name, deviceId)
    val timeFrame = getTimeFrame(syncStatus, newData = true)

    if (syncStatus != null) {
        val lastFetchLocalDateTime = Instant.fromEpochSeconds(syncStatus.mostRecentTime)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val currentLocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        if (lastFetchLocalDateTime.dayOfMonth == currentLocalDateTime.dayOfMonth) return false
    }

    val data = api.getEnergyData(deviceId, timeFrame.first, timeFrame.second).getOrNull()

    return if (data != null) {
        database.insertHouseEnergyUsage(data, deviceId)

        if (syncStatus == null) {
            syncStatus = SyncStatus(
                DbTables.EnergyData.name,
                timeFrame.second,
                timeFrame.first,
                getStartTime(deviceId))
        }
        syncStatus.mostRecentTime = timeFrame.second
        database.insertOrUpdateSyncStatus(syncStatus, deviceId)
        true
    } else false
}

    private fun getTimeFrame(syncStatus: SyncStatus?, offsetDays: Long = 30, newData: Boolean): Pair<Long, Long> {
        val offset: Long = 60 * 60 * 24 * offsetDays
        val newerDataOffset: Long = 60 * 60 * 24 * 3

        val now = Clock.System.now()
        val timeFrame: Pair<Long, Long> = if (syncStatus != null) {
            if (newData) Pair(syncStatus.mostRecentTime - newerDataOffset, min(syncStatus.mostRecentTime + offset, now.epochSeconds))
            else Pair(syncStatus.leastRecentTime - offset, syncStatus.leastRecentTime)
        } else {
            Pair(now.epochSeconds - offset, now.epochSeconds)
        }
        return Pair(timeFrame.first, timeFrame.second)
    }

    suspend fun putCurrentEnergyUsageInLocalCache(energy: Double, deviceId: String) {
        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year.toString()
        val input = "${year}-${month}-${day}T00:00:00"
        val startDate = LocalDateTime.parse(input)
        val startTime = startDate.toInstant(TimeZone.currentSystemDefault()).epochSeconds

        database!!.insertHouseEnergyUsage(listOf(
            EnergyData(startTime, energy)
        ), deviceId)
    }

    suspend fun getStartTime(deviceId: String): Long? {
        return database!!.getSyncStatus(DbTables.EnergyData.name, deviceId)?.startTime ?:
        database.getSyncStatus(DbTables.PowerUsage.name, deviceId)?.startTime ?:
        api.getTimeBounds(deviceId).getOrNull()?.first
    }

    suspend fun getAllLaunches(): List<RocketLaunch> {
        return api.getAllLaunches()
    }

    suspend fun resetAllTables() {
        database!!.removeAllSyncStatus()
        database.removeHouseEnergyUsages()
        database.removeHousePowerUsages()
    }

}
