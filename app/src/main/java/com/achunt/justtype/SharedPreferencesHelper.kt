package com.achunt.justtype

import android.content.Context

object SharedPreferencesHelper {
    private const val PREFERENCES_NAME = "app_preferences"

    // Function to save a string value to SharedPreferences
    fun saveString(context: Context, key: String, value: String?) {
        val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    // Function to get a string value from SharedPreferences
    fun getString(context: Context, key: String, defaultValue: String? = null): String? {
        val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(key, defaultValue)
    }

    // Function to save an integer value to SharedPreferences
    fun saveInt(context: Context, key: String, value: Int) {
        val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    // Function to get an integer value from SharedPreferences
    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPref.getInt(key, defaultValue)
    }

    // Function to save a boolean value to SharedPreferences
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    // Function to get a boolean value from SharedPreferences
    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(key, defaultValue)
    }
}
