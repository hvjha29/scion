package com.travelscribe.domain.model

import java.util.UUID

/**
 * Domain model representing an expense extracted from a travel log.
 * Expenses are parsed from the LLM transcription and represent
 * individual spending items during travel.
 *
 * @property id Unique identifier for this expense
 * @property item Description of what was purchased (e.g., "Train ticket", "Lunch at cafe")
 * @property amount Numeric value of the expense
 * @property currency Currency code (e.g., "INR", "USD", "EUR", "JPY")
 * @property category Categorization of the expense for analytics
 * @property notes Optional additional notes about this expense
 * @property isEstimate Whether this amount is an estimate vs exact
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val item: String,
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val notes: String? = null,
    val isEstimate: Boolean = false
) {
    /**
     * Returns a formatted amount string with currency symbol.
     */
    fun getFormattedAmount(): String {
        val symbol = getCurrencySymbol()
        return "$symbol${String.format("%.2f", amount)}"
    }

    /**
     * Returns the currency symbol for common currencies.
     */
    private fun getCurrencySymbol(): String {
        return when (currency.uppercase()) {
            "INR" -> "₹"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CNY" -> "¥"
            "THB" -> "฿"
            "AUD" -> "A$"
            "CAD" -> "C$"
            "SGD" -> "S$"
            else -> "$currency "
        }
    }

    companion object {
        /**
         * Common currencies for travel expenses.
         */
        val COMMON_CURRENCIES = listOf(
            "INR", "USD", "EUR", "GBP", "JPY", 
            "CNY", "THB", "AUD", "CAD", "SGD"
        )
    }
}

/**
 * Categories for expense classification.
 * Used for analytics and expense breakdown.
 */
enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD("Food & Drinks", "🍽️"),
    TRANSPORT("Transport", "🚗"),
    ACCOMMODATION("Accommodation", "🏨"),
    ATTRACTION("Attractions & Activities", "🎭"),
    SHOPPING("Shopping", "🛍️"),
    HEALTH("Health & Medical", "💊"),
    COMMUNICATION("Communication", "📱"),
    ENTERTAINMENT("Entertainment", "🎬"),
    TIPS("Tips & Gratuity", "💵"),
    FEES("Fees & Charges", "📋"),
    OTHER("Other", "📦");

    companion object {
        /**
         * Attempts to parse a category from a string.
         * Returns [OTHER] if no match is found.
         */
        fun fromString(value: String): ExpenseCategory {
            return entries.find { 
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: OTHER
        }

        /**
         * Attempts to infer category from expense item description.
         */
        fun inferFromItem(item: String): ExpenseCategory {
            val lowercaseItem = item.lowercase()
            return when {
                // Food patterns
                lowercaseItem.containsAny("food", "lunch", "dinner", "breakfast", 
                    "coffee", "tea", "restaurant", "cafe", "snack", "meal", 
                    "drink", "beer", "wine") -> FOOD
                
                // Transport patterns
                lowercaseItem.containsAny("taxi", "uber", "ola", "cab", "bus", 
                    "train", "metro", "flight", "ticket", "petrol", "gas", 
                    "fuel", "rickshaw", "auto") -> TRANSPORT
                
                // Accommodation patterns
                lowercaseItem.containsAny("hotel", "hostel", "airbnb", "stay", 
                    "room", "accommodation", "lodge", "resort") -> ACCOMMODATION
                
                // Attraction patterns
                lowercaseItem.containsAny("entry", "museum", "temple", "tour", 
                    "guide", "park", "zoo", "show", "ticket") -> ATTRACTION
                
                // Shopping patterns
                lowercaseItem.containsAny("shop", "souvenir", "gift", "clothes", 
                    "market", "store") -> SHOPPING
                
                // Health patterns
                lowercaseItem.containsAny("medicine", "pharmacy", "doctor", 
                    "hospital", "health") -> HEALTH
                
                // Communication patterns
                lowercaseItem.containsAny("sim", "phone", "internet", "wifi", 
                    "data", "call") -> COMMUNICATION
                
                // Tips patterns
                lowercaseItem.containsAny("tip", "gratuity") -> TIPS
                
                // Fees patterns
                lowercaseItem.containsAny("fee", "visa", "tax", "charge", 
                    "atm", "currency") -> FEES
                
                else -> OTHER
            }
        }

        private fun String.containsAny(vararg keywords: String): Boolean {
            return keywords.any { this.contains(it) }
        }
    }
}
