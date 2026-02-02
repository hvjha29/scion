package com.travelscribe.data.local.database

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.travelscribe.domain.model.Expense
import com.travelscribe.domain.model.ExpenseCategory

/**
 * Room TypeConverters for complex data types.
 */
class DatabaseConverters {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val expenseListAdapter: JsonAdapter<List<Expense>> by lazy {
        val type = Types.newParameterizedType(List::class.java, Expense::class.java)
        moshi.adapter(type)
    }

    private val stringListAdapter: JsonAdapter<List<String>> by lazy {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        moshi.adapter(type)
    }

    // Expense List Converters
    @TypeConverter
    fun fromExpenseList(expenses: List<Expense>?): String {
        return expenses?.let { expenseListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toExpenseList(json: String?): List<Expense> {
        return json?.let { 
            try {
                expenseListAdapter.fromJson(it) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    // String List Converters (for languages)
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.let { stringListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        return json?.let {
            try {
                stringListAdapter.fromJson(it) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    // ExpenseCategory Converters
    @TypeConverter
    fun fromExpenseCategory(category: ExpenseCategory): String {
        return category.name
    }

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory {
        return try {
            ExpenseCategory.valueOf(value)
        } catch (e: Exception) {
            ExpenseCategory.OTHER
        }
    }
}
