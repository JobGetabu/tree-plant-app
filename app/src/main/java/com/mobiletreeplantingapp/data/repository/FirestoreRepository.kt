package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.data.model.GuideStep
import com.mobiletreeplantingapp.data.model.SavedTree
import kotlinx.coroutines.flow.Flow

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
}

