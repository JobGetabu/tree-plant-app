package com.mobiletreeplantingapp.domain.repository

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    // User
    fun getCurrentUser(): FirebaseUser?

    // Email & password
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?>
    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?>
    suspend fun resetPassword(email: String): Result<Unit>

    // Google
    fun signInWithGoogleResult(task: Task<GoogleSignInAccount>): Result<GoogleSignInAccount>
    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<FirebaseUser>
    fun signInWithGoogle(googleSignInLauncher: ActivityResultLauncher<Intent>)

    // Sign out
    fun signOut()
}