package com.example.projekt.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekt.data.database.FitnessDatabase
import com.example.projekt.data.database.entity.PhotoEntity
import com.example.projekt.data.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class PhotoGalleryUiState(
    val photos: List<PhotoEntity> = emptyList(),
    val selectedPhoto: PhotoEntity? = null,
    val isLoading: Boolean = true
)

class PhotoGalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FitnessDatabase.getDatabase(application)
    private val activityRepository = ActivityRepository(
        database.activityDao(),
        database.routePointDao(),
        database.photoDao()
    )

    private val _uiState = MutableStateFlow(PhotoGalleryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            activityRepository.getAllPhotos().collect { photos ->
                _uiState.update {
                    it.copy(
                        photos = photos,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectPhoto(photo: PhotoEntity?) {
        _uiState.update { it.copy(selectedPhoto = photo) }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            // Delete file from storage
            try {
                val uri = Uri.parse(photo.filePath)
                if (uri.scheme == "file") {
                    File(uri.path ?: "").delete()
                }
            } catch (e: Exception) {
                // Ignore file deletion errors
            }

            // Delete from database
            activityRepository.deletePhoto(photo)

            // Clear selection if deleted photo was selected
            if (_uiState.value.selectedPhoto?.id == photo.id) {
                _uiState.update { it.copy(selectedPhoto = null) }
            }
        }
    }

    fun getPhotoUri(photo: PhotoEntity): Uri {
        return Uri.parse(photo.filePath)
    }
}
