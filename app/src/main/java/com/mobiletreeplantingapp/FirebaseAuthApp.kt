package com.mobiletreeplantingapp

import android.app.Application
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FirebaseAuthApp: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

         // Optional: Enable debug logging
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(30000)
        if (BuildConfig.DEBUG) {
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }
    }
}