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
    private val appDatabase = AppDatabase(driverFactory.createDriver())
    private val dbQuery = appDatabase.appDatabaseQueries

    init {
        dbQuery.createHousePowerUsageTableIfNeeded()
        dbQuery.createHouseEnergyUsageTableIfNeeded()
        dbQuery.createSyncStatusTableIfNeeded()
    }

    fun insertHousePowerUsage(powerUsages: List<PowerUsage>) {
        val dbPowerUsages = powerUsages.map { it.toDbObject() }
        dbQuery.transaction {
            dbPowerUsages.forEach {
                dbQuery.insertHousePowerUsage(it)
            }
        }
    }

    fun getAllHousePowerUsages(): List<PowerUsage> {
        return dbQuery.getAllHousePowerUsage().executeAsList().map { it.toDomainObject() }
    }

    fun removeHousePowerUsages() {
        dbQuery.transaction {
            dbQuery.removeAllHousePowerUsage()
        }
    }

    fun removeHousePowerUsages(fromEpochMills: Long, toEpochMillis: Long) {
        dbQuery.transaction {
            dbQuery.removeHousePowerUsage(fromEpochMills, toEpochMillis)
        }
    }


    fun insertHouseEnergyUsage(energyData: List<EnergyData>) {
        val dbEnergyData = energyData.map { it.toDbObject() }
        dbQuery.transaction {
            dbEnergyData.forEach {
                dbQuery.insertHouseEnergyUsage(it)
            }
        }
    }

    fun getAllHouseEnergyUsages(): List<EnergyData> {
        return dbQuery.getAllHouseEnergyUsage().executeAsList().map { it.toDomainObject() }
    }

    fun removeHouseEnergyUsages() {
        dbQuery.transaction {
            dbQuery.removeAllHouseEnergyUsage()
        }
    }

    fun removeHouseEnergyUsages(fromEpochMills: Long, toEpochMillis: Long) {
        dbQuery.transaction {
            dbQuery.removeHouseEnergyUsage(fromEpochMills, toEpochMillis)
        }
    }

    fun getSyncStatus(tableName: String): SyncStatus? {
        return dbQuery.getSyncStatus(tableName)
            .executeAsList()
            .firstOrNull()
            ?.toDomainObject()
    }
    fun getAllSyncStatus(): List<SyncStatus> {
        return dbQuery.getSyncStatus(null)
            .executeAsList()
            .map { it.toDomainObject() }
    }
    fun insertOrUpdateSyncStatus(syncStatus: SyncStatus) {
        dbQuery.insertSyncStatus(syncStatus.toDbObject())
    }

    fun removeSyncStatus(tableName: String?) {
        dbQuery.transaction {
            dbQuery.removeSyncStatus(tableName)
        }
    }
}

fun PowerUsage.toDbObject(): DBHousePowerUsage = DBHousePowerUsage(
    this.epochMilliSeconds,
    this.power
)

fun DBHousePowerUsage.toDomainObject() = PowerUsage(
    this.epochMilliSeconds,
    this.power
)

fun EnergyData.toDbObject(): DBHouseEnergyUsage = DBHouseEnergyUsage(
    this.epochMilliSeconds,
    this.energy
)

fun DBHouseEnergyUsage.toDomainObject() = EnergyData (
    this.epochMilliSeconds,
    this.energy
)

fun SyncStatus.toDbObject(): DBSyncStatus = DBSyncStatus(
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