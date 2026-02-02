package com.travelscribe.data.repository

import com.travelscribe.core.common.ErrorCode
import com.travelscribe.core.common.Resource
import com.travelscribe.data.local.database.dao.TravelDayDao
import com.travelscribe.data.mapper.toDomain
import com.travelscribe.data.mapper.toEntity
import com.travelscribe.domain.model.TravelDay
import com.travelscribe.domain.repository.TravelDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of [TravelDayRepository] using Room database.
 */
class TravelDayRepositoryImpl @Inject constructor(
    private val travelDayDao: TravelDayDao
) : TravelDayRepository {

    override fun observeTravelDaysForTrip(tripId: Long): Flow<List<TravelDay>> {
        return travelDayDao.observeByTripId(tripId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTravelDay(dayId: Long): Flow<TravelDay?> {
        return travelDayDao.observeById(dayId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getTravelDayById(dayId: Long): Resource<TravelDay> {
        return try {
            val entity = travelDayDao.getById(dayId)
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                Resource.Error(
                    message = "Travel day not found",
                    code = ErrorCode.NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to fetch travel day",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun getTravelDayByDate(tripId: Long, date: LocalDate): Resource<TravelDay?> {
        return try {
            val entity = travelDayDao.getByTripAndDate(tripId, date.toEpochDay())
            Resource.Success(entity?.toDomain())
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to fetch travel day by date",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun createTravelDay(travelDay: TravelDay): Resource<TravelDay> {
        return try {
            val now = Instant.now()
            val dayToCreate = travelDay.copy(
                createdAt = now,
                updatedAt = now
            )
            val id = travelDayDao.insert(dayToCreate.toEntity())
            Resource.Success(dayToCreate.copy(id = id))
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to create travel day",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun getOrCreateTravelDay(tripId: Long, date: LocalDate): Resource<TravelDay> {
        return try {
            // Check if day already exists
            val existing = travelDayDao.getByTripAndDate(tripId, date.toEpochDay())
            if (existing != null) {
                return Resource.Success(existing.toDomain())
            }

            // Create new day
            val now = Instant.now()
            val existingDays = travelDayDao.getByTripId(tripId)
            val dayNumber = existingDays.size + 1

            val newDay = TravelDay(
                tripId = tripId,
                date = date,
                dayNumber = dayNumber,
                createdAt = now,
                updatedAt = now
            )

            val id = travelDayDao.insert(newDay.toEntity())
            Resource.Success(newDay.copy(id = id))
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to get or create travel day",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun updateTravelDay(travelDay: TravelDay): Resource<Unit> {
        return try {
            val updatedDay = travelDay.copy(updatedAt = Instant.now())
            travelDayDao.update(updatedDay.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to update travel day",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun deleteTravelDay(dayId: Long): Resource<Unit> {
        return try {
            travelDayDao.deleteById(dayId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to delete travel day",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override fun observeTravelDayCount(tripId: Long): Flow<Int> {
        return travelDayDao.observeCountByTrip(tripId)
    }

    override suspend fun recalculateDayNumbers(tripId: Long): Resource<Unit> {
        return try {
            val days = travelDayDao.getByTripId(tripId)
            val sortedDays = days.sortedBy { it.date }
            val updatedDays = sortedDays.mapIndexed { index, entity ->
                entity.copy(dayNumber = index + 1)
            }
            travelDayDao.updateAll(updatedDays)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to recalculate day numbers",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }
}
