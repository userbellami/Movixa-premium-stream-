package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MovixaBottomNav
import com.example.ui.components.MovixaTopBar
import com.example.ui.player.MovixaVideoPlayer
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailSheet
import com.example.ui.screens.NewPopularScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MovixaRed
import com.example.ui.theme.MovixaTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MovixaViewModel
import com.example.ui.viewmodel.MovixaViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MovixaViewModel by viewModels {
        MovixaViewModelFactory((application as MovixaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovixaTheme {
                MovixaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MovixaApp(viewModel: MovixaViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle ViewModel snackbars
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Secondary back button handling when not in full-screen player or detail sheet
    if (uiState.playerState == null && uiState.selectedMovieForDetail == null && uiState.currentTab != AppNavTab.HOME) {
        BackHandler {
            viewModel.setNavTab(AppNavTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    actionColor = MovixaRed,
                    snackbarData = data
                )
            }
        },
        bottomBar = {
            // Only show bottom nav if video player is not active
            if (uiState.playerState == null) {
                MovixaBottomNav(
                    currentTab = uiState.currentTab,
                    downloadsCount = downloads.size,
                    onTabSelected = { viewModel.setNavTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (uiState.playerState == null) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp(0f))
        ) {
            // Main Tab Content
            when (uiState.currentTab) {
                AppNavTab.HOME -> {
                    HomeScreen(
                        featuredMovie = viewModel.featuredMovie,
                        allMovies = viewModel.allMovies,
                        watchlist = watchlist,
                        watchHistory = watchHistory,
                        activeProfile = uiState.activeProfile,
                        categoryFilter = uiState.homeCategoryFilter,
                        onMovieClick = { viewModel.openMovieDetail(it) },
                        onPlayMovie = { viewModel.startPlaying(it) },
                        onToggleWatchlist = { movie, inList -> viewModel.toggleWatchlist(movie, inList) },
                        onRemoveHistory = { viewModel.removeHistory(it) }
                    )

                    // Sticky/Overlay Top Bar for Home
                    MovixaTopBar(
                        selectedCategory = uiState.homeCategoryFilter,
                        onSelectCategory = { viewModel.setHomeCategoryFilter(it) },
                        activeProfile = uiState.activeProfile,
                        onSearchClick = { viewModel.setNavTab(AppNavTab.SEARCH) },
                        onProfileClick = { viewModel.setNavTab(AppNavTab.PROFILE) },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                AppNavTab.SEARCH -> {
                    SearchScreen(
                        searchQuery = uiState.searchQuery,
                        searchResults = uiState.searchResults,
                        selectedGenre = uiState.selectedSearchGenre,
                        allMovies = viewModel.allMovies,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onGenreSelected = { viewModel.selectSearchGenre(it) },
                        onMovieClick = { viewModel.openMovieDetail(it) },
                        onPlayMovie = { viewModel.startPlaying(it) }
                    )
                }

                AppNavTab.NEW_POPULAR -> {
                    NewPopularScreen(
                        movies = viewModel.allMovies,
                        onMovieClick = { viewModel.openMovieDetail(it) },
                        onPlayMovie = { viewModel.startPlaying(it) },
                        onShowSnackbar = { viewModel.showSnackbar(it) }
                    )
                }

                AppNavTab.DOWNLOADS -> {
                    DownloadsScreen(
                        downloads = downloads,
                        downloadingMovieIds = uiState.downloadingMovieIds,
                        smartDownloadsEnabled = uiState.smartDownloadsEnabled,
                        wifiOnlyDownloads = uiState.wifiOnlyDownloads,
                        allMovies = viewModel.allMovies,
                        onToggleSmartDownloads = { viewModel.toggleSmartDownloads() },
                        onToggleWifiOnly = { viewModel.toggleWifiOnlyDownloads() },
                        onPlayMovie = { viewModel.startPlaying(it) },
                        onRemoveDownload = { id, title -> viewModel.removeDownload(id, title) },
                        onExploreClick = { viewModel.setNavTab(AppNavTab.HOME) }
                    )
                }

                AppNavTab.PROFILE -> {
                    ProfileScreen(
                        activeProfile = uiState.activeProfile,
                        watchlist = watchlist,
                        allMovies = viewModel.allMovies,
                        streamingQuality = uiState.streamingQualitySetting,
                        wifiOnly = uiState.wifiOnlyDownloads,
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onMovieClick = { viewModel.openMovieDetail(it) },
                        onSetStreamingQuality = { viewModel.setStreamingQuality(it) },
                        onToggleWifiOnly = { viewModel.toggleWifiOnlyDownloads() },
                        onShowSnackbar = { viewModel.showSnackbar(it) }
                    )
                }
            }

            // Movie Detail Sheet
            AnimatedVisibility(
                visible = uiState.selectedMovieForDetail != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                uiState.selectedMovieForDetail?.let { movie ->
                    BackHandler {
                        viewModel.closeMovieDetail()
                    }
                    val inWatchlist = watchlist.any { it.movieId == movie.id }
                    val isDownloaded = downloads.any { it.movieId == movie.id }
                    val progress = uiState.downloadingMovieIds[movie.id]

                    MovieDetailSheet(
                        movie = movie,
                        isInWatchlist = inWatchlist,
                        isDownloaded = isDownloaded,
                        downloadProgress = progress,
                        allMovies = viewModel.allMovies,
                        onClose = { viewModel.closeMovieDetail() },
                        onPlay = { selectedMovie, episode ->
                            viewModel.startPlaying(selectedMovie, episode)
                        },
                        onToggleWatchlist = {
                            viewModel.toggleWatchlist(movie, inWatchlist)
                        },
                        onDownload = {
                            viewModel.startDownload(movie)
                        },
                        onMovieClick = {
                            viewModel.openMovieDetail(it)
                        },
                        onShowSnackbar = { viewModel.showSnackbar(it) }
                    )
                }
            }

            // Fullscreen Cinematic Video Player
            AnimatedVisibility(
                visible = uiState.playerState != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.playerState?.let { player ->
                    MovixaVideoPlayer(
                        playerState = player,
                        onClose = { viewModel.closePlayer() },
                        onPositionUpdate = { current, total ->
                            viewModel.updatePlaybackPosition(current, total)
                        },
                        onPlayPauseToggle = { viewModel.togglePlayerPlayPause() },
                        onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                        onLockToggle = { viewModel.togglePlayerLock() },
                        onAudioSubtitleChange = { audio, sub ->
                            viewModel.setAudioAndSubtitle(audio, sub)
                        },
                        onPlayNextEpisode = { viewModel.playNextEpisode() }
                    )
                }
            }
        }
    }
}
