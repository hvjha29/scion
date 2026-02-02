package com.travelscribe.di

import com.travelscribe.data.repository.TranscriptionRepositoryImpl
import com.travelscribe.data.repository.TravelDayRepositoryImpl
import com.travelscribe.data.repository.TravelLogRepositoryImpl
import com.travelscribe.data.repository.TripRepositoryImpl
import com.travelscribe.domain.repository.TranscriptionRepository
import com.travelscribe.domain.repository.TravelDayRepository
import com.travelscribe.domain.repository.TravelLogRepository
import com.travelscribe.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds TripRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository

    /**
     * Binds TravelDayRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindTravelDayRepository(
        impl: TravelDayRepositoryImpl
    ): TravelDayRepository

    /**
     * Binds TravelLogRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindTravelLogRepository(
        impl: TravelLogRepositoryImpl
    ): TravelLogRepository

    /**
     * Binds TranscriptionRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindTranscriptionRepository(
        impl: TranscriptionRepositoryImpl
    ): TranscriptionRepository
}
