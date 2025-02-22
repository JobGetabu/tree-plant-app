package com.mobiletreeplantingapp.data.repository

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class CoroutineDispatchers(
    val io: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    val main: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Main,
    val default: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default
) 