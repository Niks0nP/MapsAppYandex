package com.example.mapsappyandex.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapsappyandex.data.model.entity.PointEntity
import com.example.mapsappyandex.data.repository.PointsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointViewModel @Inject constructor(private val repository: PointsRepository) : ViewModel() {

    val readAllPoints: StateFlow<List<PointEntity>> = repository.readAllPoints
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun insertNewPoint(pointEntity: PointEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNewPoint(pointEntity)
        }
    }

    fun deletePoint(pointId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePoint(pointId)
        }
    }
}