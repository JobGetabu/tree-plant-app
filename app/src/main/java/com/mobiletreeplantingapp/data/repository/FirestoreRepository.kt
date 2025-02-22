package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.SavedArea
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    suspend fun saveArea(area: SavedArea): Result<Unit>
    fun getUserAreas(): Flow<Result<List<SavedArea>>>
    fun getAreaById(areaId: String): Flow<Result<SavedArea>>
}

