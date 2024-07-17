package com.example.switchykmmsdk.Cache

import com.example.switchykmmsdk.DatabaseDriverFactory
import com.example.switchykmmsdk.Entity.EnergyData
import com.example.switchykmmsdk.Entity.PowerUsage
import com.example.switchykmmsdk.Entity.SyncStatus
import com.switchy.kmm.cache.AppDatabase
import comswitchykmmcache.DBHouseEnergyUsage
import comswitchykmmcache.DBHousePowerUsage
import comswitchykmmcache.DBSyncStatus

enum class DbTables {
    EnergyData,
    PowerUsage,
}
internal class Database(driverFactory: DatabaseDriverFactory) {
    private val houseId = driverFactory.houseId
    private val appDatabase = AppDatabase(driverFactory.createDriver())
    private val dbQuery = appDatabase.appDatabaseQueries

    init {
        dbQuery.createHousePowerUsageTableIfNeeded()
        dbQuery.createHouseEnergyUsageTableIfNeeded()
        dbQuery.createSyncStatusTableIfNeeded()
    }

    fun insertHousePowerUsage(powerUsages: List<PowerUsage>, deviceId: String) {
        val dbPowerUsages = powerUsages.map { it.toDbObject(houseId, deviceId) }
        dbQuery.transaction {
            dbPowerUsages.forEach {
                dbQuery.insertHousePowerUsage(it)
            }
        }
    }

    fun getPowerUsages(from: Long?, to: Long?, deviceId: String): List<PowerUsage> {
        return dbQuery.getHousePowerUsage(houseId, deviceId, from, to).executeAsList().map { it.toDomainObject() }
    }

    fun removeHousePowerUsages() {
        dbQuery.transaction {
            dbQuery.removeAllHousePowerUsage()
        }
    }

    fun removeHousePowerUsages(fromEpochMills: Long, toEpochMillis: Long, deviceId: String) {
        dbQuery.transaction {
            dbQuery.removeHousePowerUsage(houseId, deviceId, fromEpochMills, toEpochMillis)
        }
    }


    fun insertHouseEnergyUsage(energyData: List<EnergyData>, deviceId: String) {
        val dbEnergyData = energyData.map { it.toDbObject(houseId, deviceId) }
        dbQuery.transaction {
            dbEnergyData.forEach {
                dbQuery.insertHouseEnergyUsage(it)
            }
        }
    }

    fun getAllHouseEnergyUsages(from: Long?, to: Long?, deviceId: String): List<EnergyData> {
        return dbQuery.getHouseEnergyUsage(houseId, deviceId, from, to).executeAsList().map { it.toDomainObject() }
    }

    fun removeHouseEnergyUsages() {
        dbQuery.transaction {
            dbQuery.removeAllHouseEnergyUsage()
        }
    }

    fun removeHouseEnergyUsages(fromEpochMills: Long, toEpochMillis: Long, deviceId: String) {
        dbQuery.transaction {
            dbQuery.removeHouseEnergyUsage(houseId, deviceId, fromEpochMills, toEpochMillis)
        }
    }

    fun getSyncStatus(tableName: String, deviceId: String): SyncStatus? {
        return dbQuery.getSyncStatus(houseId, deviceId, tableName)
            .executeAsList()
            .firstOrNull()
            ?.toDomainObject()
    }
    fun getAllSyncStatus(deviceId: String): List<SyncStatus> {
        return dbQuery.getSyncStatus(houseId, deviceId,null)
            .executeAsList()
            .map { it.toDomainObject() }
    }
    fun insertOrUpdateSyncStatus(syncStatus: SyncStatus, deviceId: String) {
        dbQuery.insertSyncStatus(syncStatus.toDbObject(houseId, deviceId))
    }

    fun removeSyncStatus(tableName: String?, deviceId: String) {
        dbQuery.transaction {
            dbQuery.removeSyncStatus(houseId, deviceId, tableName)
        }
    }

    fun removeAllSyncStatus() {
        dbQuery.transaction {
            dbQuery.removeAllSyncStatus()
        }
    }
}

fun PowerUsage.toDbObject(houseId: String, deviceId: String): DBHousePowerUsage = DBHousePowerUsage(
    houseId,
    deviceId,
    this.epochSeconds,
    this.power
)

fun DBHousePowerUsage.toDomainObject() = PowerUsage(
    this.epochSeconds,
    this.power
)

fun EnergyData.toDbObject(houseId: String, deviceId: String): DBHouseEnergyUsage = DBHouseEnergyUsage(
    houseId,
    deviceId,
    this.epochSeconds,
    this.energy
)

fun DBHouseEnergyUsage.toDomainObject() = EnergyData (
    this.epochSeconds,
    this.energy
)

fun SyncStatus.toDbObject(houseId: String, deviceId: String): DBSyncStatus = DBSyncStatus(
    houseId,
    deviceId,
    this.tableName,
    this.mostRecentTime,
    this.leastRecentTime,
    this.startTime
)

fun DBSyncStatus.toDomainObject(): SyncStatus = SyncStatus(
    this.tableName,
    this.mostRecentTime,
    this.leastRecentTime,
    this.startTime
)