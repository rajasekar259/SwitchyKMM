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

    fun insertHousePowerUsage(powerUsages: List<PowerUsage>) {
        val dbPowerUsages = powerUsages.map { it.toDbObject(houseId) }
        dbQuery.transaction {
            dbPowerUsages.forEach {
                dbQuery.insertHousePowerUsage(it)
            }
        }
    }

    fun getPowerUsages(from: Long?, to: Long?): List<PowerUsage> {
        return dbQuery.getHousePowerUsage(houseId, from, to).executeAsList().map { it.toDomainObject() }
    }

    fun removeHousePowerUsages() {
        dbQuery.transaction {
            dbQuery.removeAllHousePowerUsage()
        }
    }

    fun removeHousePowerUsages(fromEpochMills: Long, toEpochMillis: Long) {
        dbQuery.transaction {
            dbQuery.removeHousePowerUsage(houseId, fromEpochMills, toEpochMillis)
        }
    }


    fun insertHouseEnergyUsage(energyData: List<EnergyData>) {
        val dbEnergyData = energyData.map { it.toDbObject(houseId) }
        dbQuery.transaction {
            dbEnergyData.forEach {
                dbQuery.insertHouseEnergyUsage(it)
            }
        }
    }

    fun getAllHouseEnergyUsages(from: Long?, to: Long?): List<EnergyData> {
        return dbQuery.getHouseEnergyUsage(houseId, from, to).executeAsList().map { it.toDomainObject() }
    }

    fun removeHouseEnergyUsages() {
        dbQuery.transaction {
            dbQuery.removeAllHouseEnergyUsage()
        }
    }

    fun removeHouseEnergyUsages(fromEpochMills: Long, toEpochMillis: Long) {
        dbQuery.transaction {
            dbQuery.removeHouseEnergyUsage(houseId, fromEpochMills, toEpochMillis)
        }
    }

    fun getSyncStatus(tableName: String): SyncStatus? {
        return dbQuery.getSyncStatus(houseId, tableName)
            .executeAsList()
            .firstOrNull()
            ?.toDomainObject()
    }
    fun getAllSyncStatus(): List<SyncStatus> {
        return dbQuery.getSyncStatus(houseId,null)
            .executeAsList()
            .map { it.toDomainObject() }
    }
    fun insertOrUpdateSyncStatus(syncStatus: SyncStatus) {
        dbQuery.insertSyncStatus(syncStatus.toDbObject(houseId))
    }

    fun removeSyncStatus(tableName: String?) {
        dbQuery.transaction {
            dbQuery.removeSyncStatus(houseId, tableName)
        }
    }
}

fun PowerUsage.toDbObject(houseId: String): DBHousePowerUsage = DBHousePowerUsage(
    houseId,
    this.epochSeconds,
    this.power
)

fun DBHousePowerUsage.toDomainObject() = PowerUsage(
    this.epochSeconds,
    this.power
)

fun EnergyData.toDbObject(houseId: String): DBHouseEnergyUsage = DBHouseEnergyUsage(
    houseId,
    this.epochSeconds,
    this.energy
)

fun DBHouseEnergyUsage.toDomainObject() = EnergyData (
    this.epochSeconds,
    this.energy
)

fun SyncStatus.toDbObject(houseId: String): DBSyncStatus = DBSyncStatus(
    houseId,
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