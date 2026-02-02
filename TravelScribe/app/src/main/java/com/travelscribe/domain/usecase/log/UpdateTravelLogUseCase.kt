package com.travelscribe.domain.usecase.log

import com.travelscribe.core.common.Resource
import com.travelscribe.domain.model.Expense
import com.travelscribe.domain.repository.TravelLogRepository
import javax.inject.Inject

/**
 * Use case for updating a travel log's text or expenses.
 */
class UpdateTravelLogUseCase @Inject constructor(
    private val travelLogRepository: TravelLogRepository
) {
    /**
     * Updates the transcribed text of a travel log.
     *
     * @param logId The travel log ID
     * @param text The new transcribed text
     * @return Resource indicating success or failure
     */
    suspend fun updateText(logId: Long, text: String): Resource<Unit> {
        if (text.isBlank()) {
            return Resource.Error("Text cannot be empty")
        }
        return travelLogRepository.updateTranscribedText(logId, text.trim())
    }

    /**
     * Updates the expenses of a travel log.
     *
     * @param logId The travel log ID
     * @param expenses The new list of expenses
     * @return Resource indicating success or failure
     */
    suspend fun updateExpenses(logId: Long, expenses: List<Expense>): Resource<Unit> {
        // Validate expenses
        for (expense in expenses) {
            if (expense.item.isBlank()) {
                return Resource.Error("Expense item cannot be empty")
            }
            if (expense.amount < 0) {
                return Resource.Error("Expense amount cannot be negative")
            }
            if (expense.currency.isBlank()) {
                return Resource.Error("Expense currency cannot be empty")
            }
        }
        return travelLogRepository.updateExpenses(logId, expenses)
    }

    /**
     * Adds a single expense to a travel log.
     *
     * @param logId The travel log ID
     * @param expense The expense to add
     * @return Resource indicating success or failure
     */
    suspend fun addExpense(logId: Long, expense: Expense): Resource<Unit> {
        if (expense.item.isBlank()) {
            return Resource.Error("Expense item cannot be empty")
        }
        if (expense.amount < 0) {
            return Resource.Error("Expense amount cannot be negative")
        }
        return travelLogRepository.addExpense(logId, expense)
    }

    /**
     * Removes an expense from a travel log.
     *
     * @param logId The travel log ID
     * @param expenseId The expense ID to remove
     * @return Resource indicating success or failure
     */
    suspend fun removeExpense(logId: Long, expenseId: String): Resource<Unit> {
        return travelLogRepository.removeExpense(logId, expenseId)
    }
}
