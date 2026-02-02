package com.travelscribe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for TravelScribe.
 * Initializes Hilt dependency injection and other app-wide configurations.
 */
@HiltAndroidApp
class TravelScribeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production, you might want to plant a crash reporting tree
            // Timber.plant(CrashReportingTree())
        }

        Timber.d("TravelScribe application initialized")
    }
}
