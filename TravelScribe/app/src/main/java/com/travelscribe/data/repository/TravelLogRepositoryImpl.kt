package com.travelscribe.data.repository

import com.travelscribe.core.common.ErrorCode
import com.travelscribe.core.common.Resource
import com.travelscribe.data.local.database.dao.TravelLogDao
import com.travelscribe.data.mapper.EntityMappers.toJson
import com.travelscribe.data.mapper.toDomain
import com.travelscribe.data.mapper.toEntity
import com.travelscribe.domain.model.Expense
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.repository.TravelLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of [TravelLogRepository] using Room database.
 */
class TravelLogRepositoryImpl @Inject constructor(
    private val travelLogDao: TravelLogDao
) : TravelLogRepository {

    override fun observeTravelLogsForDay(dayId: Long): Flow<List<TravelLog>> {
        return travelLogDao.observeByDayId(dayId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTravelLog(logId: Long): Flow<TravelLog?> {
        return travelLogDao.observeById(logId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getTravelLogById(logId: Long): Resource<TravelLog> {
        return try {
            val entity = travelLogDao.getById(logId)
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                Resource.Error(
                    message = "Travel log not found",
                    code = ErrorCode.NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to fetch travel log",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun createTravelLog(travelLog: TravelLog): Resource<TravelLog> {
        return try {
            val now = Instant.now()
            val logToCreate = travelLog.copy(
                createdAt = now,
                updatedAt = now
            )
            val id = travelLogDao.insert(logToCreate.toEntity())
            Resource.Success(logToCreate.copy(id = id))
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to create travel log",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun updateTravelLog(travelLog: TravelLog): Resource<Unit> {
        return try {
            val updatedLog = travelLog.copy(updatedAt = Instant.now())
            travelLogDao.update(updatedLog.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to update travel log",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun updateTranscribedText(logId: Long, text: String): Resource<Unit> {
        return try {
            travelLogDao.updateTranscribedText(
                logId = logId,
                text = text,
                updatedAt = Instant.now().toEpochMilli()
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to update transcribed text",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun updateExpenses(logId: Long, expenses: List<Expense>): Resource<Unit> {
        return try {
            travelLogDao.updateExpenses(
                logId = logId,
                expensesJson = expenses.toJson(),
                updatedAt = Instant.now().toEpochMilli()
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to update expenses",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun addExpense(logId: Long, expense: Expense): Resource<Unit> {
        return try {
            val log = travelLogDao.getById(logId)?.toDomain()
                ?: return Resource.Error("Travel log not found", ErrorCode.NOT_FOUND)

            val updatedExpenses = log.expenses + expense
            updateExpenses(logId, updatedExpenses)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to add expense",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun removeExpense(logId: Long, expenseId: String): Resource<Unit> {
        return try {
            val log = travelLogDao.getById(logId)?.toDomain()
                ?: return Resource.Error("Travel log not found", ErrorCode.NOT_FOUND)

            val updatedExpenses = log.expenses.filter { it.id != expenseId }
            updateExpenses(logId, updatedExpenses)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to remove expense",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun deleteTravelLog(logId: Long, deleteAudioFile: Boolean): Resource<Unit> {
        return try {
            if (deleteAudioFile) {
                val audioPath = travelLogDao.getAudioPath(logId)
                audioPath?.let { path ->
                    withContext(Dispatchers.IO) {
                        File(path).delete()
                    }
                }
            }
            travelLogDao.deleteById(logId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to delete travel log",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override suspend fun getTravelLogsForTrip(tripId: Long): Resource<List<TravelLog>> {
        return try {
            val entities = travelLogDao.getByTripId(tripId)
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to fetch travel logs for trip",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    override fun observeTravelLogCount(dayId: Long): Flow<Int> {
        return travelLogDao.observeCountByDay(dayId)
    }

    override suspend fun searchTravelLogs(query: String, tripId: Long?): Resource<List<TravelLog>> {
        return try {
            val entities = if (tripId != null) {
                travelLogDao.searchByTextInTrip(query, tripId)
            } else {
                travelLogDao.searchByText(query)
            }
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "Failed to search travel logs",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }
}
