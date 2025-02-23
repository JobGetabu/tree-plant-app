package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.data.model.GuideStep
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    suspend fun saveArea(area: SavedArea): Result<Unit>
    fun getUserAreas(): Flow<Result<List<SavedArea>>>
    fun getAreaById(areaId: String): Flow<Result<SavedArea>>
    
    // New methods for tree progress
    fun getTreeProgress(treeId: String): Flow<Result<TreeProgress>>
    suspend fun updateTreeProgress(progress: TreeProgress): Result<Unit>
    fun getGuideSteps(species: String): List<GuideStep>
}

