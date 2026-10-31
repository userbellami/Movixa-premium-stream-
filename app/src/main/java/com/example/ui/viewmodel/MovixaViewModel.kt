package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MovieRepository
import com.example.data.local.DownloadEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.model.Episode
import com.example.model.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    HOME("Home"),
    SEARCH("Search"),
    NEW_POPULAR("New & Hot"),
    DOWNLOADS("Downloads"),
    PROFILE("My Movixa")
}

data class PlayerState(
    val movie: Movie? = null,
    val episode: Episode? = null,
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isLocked: Boolean = false,
    val audioTrack: String = "English [Original]",
    val subtitleTrack: String = "Off",
    val quality: String = "Auto (1080p)"
)

data class Profile(
    val id: String,
    val name: String,
    val avatarBgColorHex: Long,
    val isKids: Boolean = false
)

data class MovixaUiState(
    val currentTab: AppNavTab = AppNavTab.HOME,
    val homeCategoryFilter: String = "All", // "All", "Movies", "TV Series", "Categories"
    val selectedMovieForDetail: Movie? = null,
    val playerState: PlayerState? = null,
    val searchQuery: String = "",
    val searchResults: List<Movie> = emptyList(),
    val selectedSearchGenre: String? = null,
    val activeProfile: Profile = Profile("prof-1", "Spiky", 0xFFE50914),
    val downloadingMovieIds: Map<String, Float> = emptyMap(), // movieId -> progress 0..1
    val snackbarMessage: String? = null,
    val smartDownloadsEnabled: Boolean = true,
    val wifiOnlyDownloads: Boolean = true,
    val streamingQualitySetting: String = "High (4K Ultra HD)"
)

class MovixaViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MovixaUiState())
    val uiState: StateFlow<MovixaUiState> = _uiState.asStateFlow()

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovies = repository.allMovies
    val featuredMovie = repository.getFeaturedMovie()

    private val downloadJobs = mutableMapOf<String, Job>()

    fun setNavTab(tab: AppNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setHomeCategoryFilter(filter: String) {
        _uiState.update { it.copy(homeCategoryFilter = filter) }
    }

    fun openMovieDetail(movie: Movie) {
        _uiState.update { it.copy(selectedMovieForDetail = movie) }
    }

    fun closeMovieDetail() {
        _uiState.update { it.copy(selectedMovieForDetail = null) }
    }

    fun startPlaying(movie: Movie, episode: Episode? = null, resumePositionMs: Long = 0L) {
        _uiState.update {
            it.copy(
                playerState = PlayerState(
                    movie = movie,
                    episode = episode,
                    isPlaying = true,
                    currentPositionMs = resumePositionMs,
                    totalDurationMs = if (episode != null) episode.durationMinutes * 60 * 1000L else movie.durationMinutes * 60 * 1000L
                )
            )
        }
    }

    fun closePlayer() {
        // Save history on close if movie was active
        val current = _uiState.value.playerState
        if (current?.movie != null) {
            viewModelScope.launch {
                repository.saveProgress(
                    movieId = current.movie.id,
                    title = current.movie.title,
                    backdropUrl = current.movie.backdropUrl,
                    positionMs = current.currentPositionMs,
                    durationMs = if (current.totalDurationMs > 0) current.totalDurationMs else (current.movie.durationMinutes * 60 * 1000L),
                    episodeId = current.episode?.id,
                    episodeTitle = current.episode?.title
                )
            }
        }
        _uiState.update { it.copy(playerState = null) }
    }

    fun updatePlaybackPosition(currentPos: Long, totalDuration: Long) {
        _uiState.update { state ->
            state.playerState?.let { player ->
                state.copy(
                    playerState = player.copy(
                        currentPositionMs = currentPos,
                        totalDurationMs = if (totalDuration > 0) totalDuration else player.totalDurationMs
                    )
                )
            } ?: state
        }
    }

    fun togglePlayerPlayPause() {
        _uiState.update { state ->
            state.playerState?.let { player ->
                state.copy(playerState = player.copy(isPlaying = !player.isPlaying))
            } ?: state
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { state ->
            state.playerState?.let { player ->
                state.copy(playerState = player.copy(playbackSpeed = speed))
            } ?: state
        }
        showSnackbar("Playback speed set to ${speed}x")
    }

    fun togglePlayerLock() {
        _uiState.update { state ->
            state.playerState?.let { player ->
                val nextLock = !player.isLocked
                state.copy(playerState = player.copy(isLocked = nextLock))
            } ?: state
        }
    }

    fun setAudioAndSubtitle(audio: String, subtitle: String) {
        _uiState.update { state ->
            state.playerState?.let { player ->
                state.copy(playerState = player.copy(audioTrack = audio, subtitleTrack = subtitle))
            } ?: state
        }
        showSnackbar("Audio: $audio, Subtitles: $subtitle")
    }

    fun playNextEpisode() {
        val current = _uiState.value.playerState ?: return
        val movie = current.movie ?: return
        val currentEp = current.episode
        if (movie.isSeries && movie.episodes.isNotEmpty()) {
            val currentIndex = movie.episodes.indexOfFirst { it.id == currentEp?.id }
            if (currentIndex != -1 && currentIndex < movie.episodes.size - 1) {
                val nextEp = movie.episodes[currentIndex + 1]
                startPlaying(movie, nextEp, 0L)
                showSnackbar("Now playing: ${nextEp.title}")
            } else {
                showSnackbar("No more episodes in this season")
            }
        }
    }

    fun toggleWatchlist(movie: Movie, currentlyInWatchlist: Boolean) {
        viewModelScope.launch {
            repository.toggleWatchlist(movie, currentlyInWatchlist)
            val msg = if (currentlyInWatchlist) "Removed from My List" else "Added to My List"
            showSnackbar(msg)
        }
    }

    fun startDownload(movie: Movie) {
        val movieId = movie.id
        if (downloadJobs.containsKey(movieId) || _uiState.value.downloadingMovieIds.containsKey(movieId)) {
            showSnackbar("Download already in progress")
            return
        }

        showSnackbar("Downloading '${movie.title}'...")
        val job = viewModelScope.launch {
            // realistic progress step simulation
            for (progress in 1..10) {
                delay(400)
                _uiState.update { state ->
                    val map = state.downloadingMovieIds.toMutableMap()
                    map[movieId] = progress / 10f
                    state.copy(downloadingMovieIds = map)
                }
            }
            // Complete download into Room
            repository.addDownload(movie)
            _uiState.update { state ->
                val map = state.downloadingMovieIds.toMutableMap()
                map.remove(movieId)
                state.copy(downloadingMovieIds = map)
            }
            downloadJobs.remove(movieId)
            showSnackbar("Downloaded '${movie.title}' for offline viewing")
        }
        downloadJobs[movieId] = job
    }

    fun removeDownload(movieId: String, title: String) {
        viewModelScope.launch {
            repository.removeDownload(movieId)
            showSnackbar("Removed '$title' from Downloads")
        }
    }

    fun removeHistory(movieId: String) {
        viewModelScope.launch {
            repository.removeHistory(movieId)
            showSnackbar("Removed from Continue Watching")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val results = if (query.isBlank()) {
                if (state.selectedSearchGenre != null) {
                    repository.getMoviesByGenre(state.selectedSearchGenre)
                } else emptyList()
            } else {
                repository.searchMovies(query)
            }
            state.copy(searchQuery = query, searchResults = results)
        }
    }

    fun selectSearchGenre(genre: String?) {
        _uiState.update { state ->
            val newGenre = if (state.selectedSearchGenre == genre) null else genre
            val results = if (newGenre != null) {
                repository.getMoviesByGenre(newGenre)
            } else if (state.searchQuery.isNotBlank()) {
                repository.searchMovies(state.searchQuery)
            } else emptyList()

            state.copy(selectedSearchGenre = newGenre, searchResults = results)
        }
    }

    fun switchProfile(profile: Profile) {
        _uiState.update { it.copy(activeProfile = profile) }
        showSnackbar("Switched profile to ${profile.name}")
    }

    fun toggleSmartDownloads() {
        _uiState.update { it.copy(smartDownloadsEnabled = !it.smartDownloadsEnabled) }
    }

    fun toggleWifiOnlyDownloads() {
        _uiState.update { it.copy(wifiOnlyDownloads = !it.wifiOnlyDownloads) }
    }

    fun setStreamingQuality(quality: String) {
        _uiState.update { it.copy(streamingQualitySetting = quality) }
        showSnackbar("Streaming quality set to $quality")
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

class MovixaViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovixaViewModel::class.java)) {
            return MovixaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
