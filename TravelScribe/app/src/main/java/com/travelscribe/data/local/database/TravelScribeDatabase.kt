package com.travelscribe.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.travelscribe.core.common.Constants
import com.travelscribe.data.local.database.dao.TravelDayDao
import com.travelscribe.data.local.database.dao.TravelLogDao
import com.travelscribe.data.local.database.dao.TripDao
import com.travelscribe.data.local.database.entity.TravelDayEntity
import com.travelscribe.data.local.database.entity.TravelLogEntity
import com.travelscribe.data.local.database.entity.TripEntity

/**
 * Main Room database for TravelScribe application.
 */
@Database(
    entities = [
        TripEntity::class,
        TravelDayEntity::class,
        TravelLogEntity::class
    ],
    version = Constants.Database.VERSION,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class TravelScribeDatabase : RoomDatabase() {

    /**
     * Returns the Trip DAO.
     */
    abstract fun tripDao(): TripDao

    /**
     * Returns the TravelDay DAO.
     */
    abstract fun travelDayDao(): TravelDayDao

    /**
     * Returns the TravelLog DAO.
     */
    abstract fun travelLogDao(): TravelLogDao
}
