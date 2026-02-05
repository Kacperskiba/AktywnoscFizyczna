package com.example.projekt

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Activity : Route

    @Serializable
    data object History : Route

    @Serializable
    data class ActivityDetail(val activityId: Long) : Route

    @Serializable
    data object PhotoGallery : Route

    @Serializable
    data object Settings : Route
}
