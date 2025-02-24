package com.mobiletreeplantingapp.di

import android.app.NotificationManager
import android.content.Context
import com.mobiletreeplantingapp.services.NotificationService
import com.mobiletreeplantingapp.ui.util.NotificationPermissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationPermissionHandler(
        @ApplicationContext context: Context
    ): NotificationPermissionHandler {
        return NotificationPermissionHandler(context)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideNotificationService(
        @ApplicationContext context: Context,
        permissionHandler: NotificationPermissionHandler
    ): NotificationService {
        return NotificationService(context, permissionHandler)
    }
} 