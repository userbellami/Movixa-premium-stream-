package com.example.model

data class Episode(
    val id: String,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val title: String,
    val durationMinutes: Int,
    val synopsis: String,
    val videoUrl: String,
    val thumbnailUrl: String
)

data class Movie(
    val id: String,
    val title: String,
    val tagline: String,
    val synopsis: String,
    val backdropRes: Int? = null,
    val backdropUrl: String,
    val posterUrl: String,
    val videoUrl: String,
    val trailerUrl: String,
    val rating: Float,
    val matchScore: Int,
    val year: Int,
    val ageRating: String,
    val durationMinutes: Int,
    val quality: String = "4K Ultra HD",
    val audio: String = "Spatial Audio 5.1",
    val genres: List<String>,
    val isOriginal: Boolean = false,
    val cast: List<String>,
    val director: String,
    val isSeries: Boolean = false,
    val seasonsCount: Int = 1,
    val episodes: List<Episode> = emptyList()
)
