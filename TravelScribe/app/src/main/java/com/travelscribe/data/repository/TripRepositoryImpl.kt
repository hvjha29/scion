package com.travelscribe.data.repository

import com.travelscribe.core.common.ErrorCode
import com.travelscribe.core.common.Resource
import com.travelscribe.data.local.database.dao.TripDao
import com.travelscribe.data.mapper.toDomain
import com.travelscribe.data.mapper.toEntity
import com.travelscribe.domain.model.Trip
import com.travelscribe.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of [TripRepository] using Room database.
 */
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override fun observeAllTrips(): Flow<List<Trip>> {
        return tripDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTrip(tripId: Long): Flow<Trip?> {
        return tripDao.observeById(tripId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getTripById(tripId: Long): Resource<Trip> {
        return try {
            val entity = tripDao.getById(tripId)
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                Resource.Error(
                    message = "Trip not found",
                    code = ErrorCode.NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to fetch trip",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun createTrip(trip: Trip): Resource<Trip> {
        return try {
            val now = Instant.now()
            val tripToCreate = trip.copy(
                createdAt = now,
                updatedAt = now
            )
            val id = tripDao.insert(tripToCreate.toEntity())
            Resource.Success(tripToCreate.copy(id = id))
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to create trip",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun updateTrip(trip: Trip): Resource<Unit> {
        return try {
            val updatedTrip = trip.copy(updatedAt = Instant.now())
            tripDao.update(updatedTrip.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to update trip",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun deleteTrip(tripId: Long): Resource<Unit> {
        return try {
            tripDao.deleteById(tripId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to delete trip",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun searchTrips(query: String): Resource<List<Trip>> {
        return try {
            val entities = tripDao.searchByTitle(query)
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to search trips",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun getTripsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Resource<List<Trip>> {
        return try {
            val entities = tripDao.getByDateRange(
                startEpochDay = startDate.toEpochDay(),
                endEpochDay = endDate.toEpochDay()
            )
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to fetch trips by date range",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override fun observeTripCount(): Flow<Int> {
        return tripDao.observeCount()
    }
}
