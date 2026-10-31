package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val movieId: String,
    val title: String,
    val posterUrl: String,
    val isSeries: Boolean,
    val rating: Float,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val movieId: String,
    val title: String,
    val backdropUrl: String,
    val currentPositionMs: Long,
    val totalDurationMs: Long,
    val episodeId: String? = null,
    val episodeTitle: String? = null,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val movieId: String,
    val title: String,
    val posterUrl: String,
    val sizeMb: Int,
    val quality: String,
    val durationMinutes: Int,
    val downloadedAt: Long = System.currentTimeMillis()
)
