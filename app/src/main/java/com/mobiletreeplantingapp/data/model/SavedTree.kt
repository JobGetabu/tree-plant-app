package com.mobiletreeplantingapp.data.model

data class SavedTree(
    val id: String,
    val species: String,
    val notes: String,
    val areaId: String,
    val dateAdded: Long
) 