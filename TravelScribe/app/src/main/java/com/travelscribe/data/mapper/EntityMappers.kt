package com.travelscribe.data.mapper

import com.travelscribe.data.local.database.entity.TravelDayEntity
import com.travelscribe.data.local.database.entity.TravelLogEntity
import com.travelscribe.data.local.database.entity.TripEntity
import com.travelscribe.data.remote.dto.ExpenseDto
import com.travelscribe.domain.model.Expense
import com.travelscribe.domain.model.ExpenseCategory
import com.travelscribe.domain.model.TravelDay
import com.travelscribe.domain.model.TravelLog
import com.travelscribe.domain.model.Trip
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant
import java.time.LocalDate

/**
 * Mapper functions for converting between domain models and data layer entities.
 */
object EntityMappers {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val expenseListAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, Expense::class.java)
        moshi.adapter<List<Expense>>(type)
    }

    private val stringListAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        moshi.adapter<List<String>>(type)
    }

    // ==================== Trip Mappers ====================

    fun TripEntity.toDomain(): Trip {
        return Trip(
            id = id,
            title = title,
            startDate = LocalDate.ofEpochDay(startDate),
            endDate = endDate?.let { LocalDate.ofEpochDay(it) },
            description = description,
            coverImagePath = coverImagePath,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }

    fun Trip.toEntity(): TripEntity {
        return TripEntity(
            id = id,
            title = title,
            startDate = startDate.toEpochDay(),
            endDate = endDate?.toEpochDay(),
            description = description,
            coverImagePath = coverImagePath,
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli()
        )
    }

    // ==================== TravelDay Mappers ====================

    fun TravelDayEntity.toDomain(): TravelDay {
        return TravelDay(
            id = id,
            tripId = tripId,
            date = LocalDate.ofEpochDay(date),
            dayNumber = dayNumber,
            notes = notes,
            weatherInfo = weatherInfo,
            location = location,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }

    fun TravelDay.toEntity(): TravelDayEntity {
        return TravelDayEntity(
            id = id,
            tripId = tripId,
            date = date.toEpochDay(),
            dayNumber = dayNumber,
            notes = notes,
            weatherInfo = weatherInfo,
            location = location,
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli()
        )
    }

    // ==================== TravelLog Mappers ====================

    fun TravelLogEntity.toDomain(): TravelLog {
        return TravelLog(
            id = id,
            dayId = dayId,
            rawAudioPath = rawAudioPath,
            audioDurationMs = audioDurationMs,
            transcribedText = transcribedText,
            originalLanguages = parseStringList(originalLanguages),
            expenses = parseExpenseList(expenses),
            location = location,
            isEdited = isEdited,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }

    fun TravelLog.toEntity(): TravelLogEntity {
        return TravelLogEntity(
            id = id,
            dayId = dayId,
            rawAudioPath = rawAudioPath,
            audioDurationMs = audioDurationMs,
            transcribedText = transcribedText,
            originalLanguages = stringListAdapter.toJson(originalLanguages),
            expenses = expenseListAdapter.toJson(expenses),
            location = location,
            isEdited = isEdited,
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli()
        )
    }

    // ==================== Expense Mappers ====================

    fun ExpenseDto.toDomain(): Expense {
        return Expense(
            item = item,
            amount = amount,
            currency = currency,
            category = category?.let { ExpenseCategory.fromString(it) } 
                ?: ExpenseCategory.inferFromItem(item),
            notes = notes,
            isEstimate = isEstimate ?: false
        )
    }

    fun Expense.toDto(): ExpenseDto {
        return ExpenseDto(
            item = item,
            amount = amount,
            currency = currency,
            category = category.name,
            notes = notes,
            isEstimate = isEstimate
        )
    }

    // ==================== Helper Functions ====================

    private fun parseExpenseList(json: String): List<Expense> {
        return try {
            expenseListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseStringList(json: String): List<String> {
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun List<Expense>.toJson(): String {
        return expenseListAdapter.toJson(this)
    }

    fun List<String>.toLanguagesJson(): String {
        return stringListAdapter.toJson(this)
    }
}

// Extension functions for convenience
fun TripEntity.toDomain() = EntityMappers.run { this@toDomain.toDomain() }
fun Trip.toEntity() = EntityMappers.run { this@toEntity.toEntity() }
fun TravelDayEntity.toDomain() = EntityMappers.run { this@toDomain.toDomain() }
fun TravelDay.toEntity() = EntityMappers.run { this@toEntity.toEntity() }
fun TravelLogEntity.toDomain() = EntityMappers.run { this@toDomain.toDomain() }
fun TravelLog.toEntity() = EntityMappers.run { this@toEntity.toEntity() }
fun ExpenseDto.toDomain() = EntityMappers.run { this@toDomain.toDomain() }
fun Expense.toDto() = EntityMappers.run { this@toDto.toDto() }
