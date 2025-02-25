package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.data.model.GuideStep
import com.mobiletreeplantingapp.data.model.SavedTree
import com.mobiletreeplantingapp.data.model.UserStats
import com.mobiletreeplantingapp.data.model.TreeRecommendationData
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.ui.screen.navigation.settings.SettingsViewModel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

interface FirestoreRepository {
    suspend fun saveArea(area: SavedArea): Result<Unit>
    fun getUserAreas(): Flow<Result<List<SavedArea>>>
    fun getAreaById(areaId: String): Flow<Result<SavedArea>>

    // New methods for tree progress
    suspend fun updateTreeProgress(progress: TreeProgress): Result<Unit>
    fun getGuideSteps(species: String): List<GuideStep>

    // New methods for saved trees
    suspend fun saveTree(tree: SavedTree): Result<Unit>
    suspend fun deleteTree(treeId: String): Result<Unit>
    suspend fun updateTree(tree: SavedTree): Result<Unit>
    fun getAreaTrees(areaId: String): Flow<Result<List<SavedTree>>>
    suspend fun saveTreeProgress(progress: TreeProgress): Result<Unit>
    suspend fun getTreeProgress(treeId: String): Flow<Result<TreeProgress?>>

    suspend fun getUserStats(): Flow<Result<UserStats>>

    suspend fun getTreeRecommendations(
        soilType: String,
        elevation: Double,
        climateZone: String
    ): Flow<Result<List<TreeRecommendationData>>>

    suspend fun getTreeById(treeId: String): Result<SavedTree?>

    // New methods for user profile
    suspend fun updateUserProfile(displayName: String): Result<Unit>
    fun getUserProfile(): Flow<Result<SettingsViewModel.UserProfile>>

    suspend fun deleteArea(areaId: String): Result<Unit>
    suspend fun deleteTreesByAreaId(areaId: String): Result<Unit>
}
