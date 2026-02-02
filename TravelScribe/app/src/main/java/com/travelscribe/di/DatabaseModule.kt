package com.travelscribe.di

import android.content.Context
import androidx.room.Room
import com.travelscribe.core.common.Constants
import com.travelscribe.data.local.database.TravelScribeDatabase
import com.travelscribe.data.local.database.dao.TravelDayDao
import com.travelscribe.data.local.database.dao.TravelLogDao
import com.travelscribe.data.local.database.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TravelScribeDatabase {
        return Room.databaseBuilder(
            context,
            TravelScribeDatabase::class.java,
            Constants.Database.NAME
        )
            .fallbackToDestructiveMigration()
            // Add migrations here as needed:
            // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    /**
     * Provides the TripDao.
     */
    @Provides
    @Singleton
    fun provideTripDao(database: TravelScribeDatabase): TripDao {
        return database.tripDao()
    }

    /**
     * Provides the TravelDayDao.
     */
    @Provides
    @Singleton
    fun provideTravelDayDao(database: TravelScribeDatabase): TravelDayDao {
        return database.travelDayDao()
    }

    /**
     * Provides the TravelLogDao.
     */
    @Provides
    @Singleton
    fun provideTravelLogDao(database: TravelScribeDatabase): TravelLogDao {
        return database.travelLogDao()
    }
}
