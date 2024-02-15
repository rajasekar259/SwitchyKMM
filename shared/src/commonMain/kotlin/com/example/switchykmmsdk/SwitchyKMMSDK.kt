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
import kotlin.math.abs
import kotlin.math.min

class SwitchyKMMSDK(driverFactory: DatabaseDriverFactory?, authDelegate: APIAuthorizationDelegate) {
    private val database: Database? = if (driverFactory != null) Database(driverFactory) else null
    private val api = SwitchyAPI(authDelegate)

    /* **** Power Usages **** */
    private suspend fun insertHousePowerUsage(powerUsages: List<PowerUsage>) {
        database!!.insertHousePowerUsage(powerUsages)
    }
    private suspend fun removeHousePowerUsages() {
        database!!.removeHousePowerUsages()
    }
    suspend fun getAllHousePowerUsages(): List<PowerUsage> {
        return database!!.getAllHousePowerUsages()
    }

    suspend fun getPowerUsageFromAPI(deviceId: String, from: Long, to: Long): Result<List<PowerUsage>> {
        return api.getPowerUsage(deviceId, from, to)
    }

    suspend fun fetchNewerPowerUsage(deviceId: String): Boolean {
        var syncStatus = database!!.getSyncStatus(DbTables.PowerUsage.name)
        val timeFrame = getTimeFrame(syncStatus, newData = true)

        if (abs(timeFrame.second - timeFrame.first) < 60 * 60 * 24 * 4 * 1000) return false

        val data = api.getPowerUsage(deviceId, timeFrame.first, timeFrame.second).getOrNull()

        return if (data != null) {
            database.insertHousePowerUsage(data)

            if (syncStatus == null) {
                syncStatus = SyncStatus(
                    DbTables.PowerUsage.name,
                    timeFrame.second,
                    timeFrame.first,
                    api.getTimeBounds(deviceId).getOrNull()?.first)
            }
            syncStatus.mostRecentTime = timeFrame.second
            database.insertOrUpdateSyncStatus(syncStatus)

            true
        } else false
    }

    suspend fun fetchOlderPowerUsage(deviceId: String): Boolean {
        val syncStatus = database!!.getSyncStatus(DbTables.PowerUsage.name)
        return if (syncStatus == null) {
            fetchNewerPowerUsage(deviceId)
        } else if (syncStatus.leastRecentTime == -1L) {
            false
        } else {
            if (syncStatus.startTime == null)
                syncStatus.startTime = api.getTimeBounds(deviceId).getOrNull()?.first

            val timeFrame = getTimeFrame(syncStatus, newData = false)
            val data = api.getPowerUsage(deviceId, timeFrame.first, timeFrame.second)
                .getOrNull()

            if (data != null) {
                database.insertHousePowerUsage(data)
                syncStatus.leastRecentTime = if (timeFrame.first <= (syncStatus.startTime
                        ?: 0L)
                ) timeFrame.first else -1L
                database.insertOrUpdateSyncStatus(syncStatus)
                true
            } else false
        }
    }


    /* **** Energy Data **** */

    suspend fun getAllEnergyData(): List<EnergyData> {
        return database!!.getAllHouseEnergyUsages()
    }

    suspend fun fetchOlderEnergyData(deviceId: String): Boolean {
        val syncStatus = database!!.getSyncStatus(DbTables.EnergyData.name)
        return if (syncStatus == null) {
            fetchNewerEnergyData(deviceId)
        } else if (syncStatus.leastRecentTime == -1L) {
            false
        } else {
            if (syncStatus.startTime == null)
                syncStatus.startTime = api.getTimeBounds(deviceId).getOrNull()?.first

            val timeFrame = getTimeFrame(syncStatus, newData = false)
            val data = api.getEnergyData(deviceId, timeFrame.first, timeFrame.second)
                .getOrNull()

            if (data != null) {
                database.insertHouseEnergyUsage(data)
                syncStatus.leastRecentTime = if (timeFrame.first <= (syncStatus.startTime
                        ?: 0L)
                ) timeFrame.first else -1L
                database.insertOrUpdateSyncStatus(syncStatus)
                true
            } else false
        }
    }
    suspend fun fetchEnergyDataFromAPI(deviceId: String, from: Long, to: Long) = api.getEnergyData(deviceId, from, to)
    suspend fun fetchNewerEnergyData(deviceId: String): Boolean {
        var syncStatus = database!!.getSyncStatus(DbTables.EnergyData.name)
        val timeFrame = getTimeFrame(syncStatus, newData = true)

        if (abs(timeFrame.second - timeFrame.first) < 60 * 60 * 24 * 4 * 1000) return false

        val data = api.getEnergyData(deviceId, timeFrame.first, timeFrame.second).getOrNull()

        return if (data != null) {
            database.insertHouseEnergyUsage(data)

            if (syncStatus == null) {
                syncStatus = SyncStatus(
                    DbTables.EnergyData.name,
                    timeFrame.second,
                    timeFrame.first,
                    api.getTimeBounds(deviceId).getOrNull()?.first)
            }
            syncStatus.mostRecentTime = timeFrame.second
            database.insertOrUpdateSyncStatus(syncStatus)

            true
        } else false
    }

    private fun getTimeFrame(syncStatus: SyncStatus?, offsetDays: Long = 30, newData: Boolean): Pair<Long, Long> {
        val offset: Long = 60 * 60 * 24 * offsetDays * 1000
        val newerDataOffset: Long = 60 * 60 * 24 * 3 * 1000

        val now = Clock.System.now()
        val timeFrame: Pair<Long, Long> = if (syncStatus != null) {
            if (newData) Pair(syncStatus.mostRecentTime - newerDataOffset, min(syncStatus.mostRecentTime + offset, now.toEpochMilliseconds()))
            else Pair(syncStatus.leastRecentTime - offset, syncStatus.leastRecentTime)
        } else {
            Pair(now.toEpochMilliseconds() - offset, now.toEpochMilliseconds())
        }
        return timeFrame
    }

    suspend fun getAllLaunches(): List<RocketLaunch> {
        return api.getAllLaunches()
    }
}