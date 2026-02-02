package com.travelscribe.di

import android.content.Context
import com.travelscribe.core.audio.VoiceRecorderManager
import com.travelscribe.data.audio.VoiceRecorderManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Main application-level Hilt module.
 * Provides application-scoped dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the application context.
     */
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context

    /**
     * Provides the VoiceRecorderManager implementation.
     */
    @Provides
    @Singleton
    fun provideVoiceRecorderManager(
        @ApplicationContext context: Context
    ): VoiceRecorderManager = VoiceRecorderManagerImpl(context)
}
