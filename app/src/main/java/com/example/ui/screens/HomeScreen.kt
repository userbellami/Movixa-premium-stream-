package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.model.Movie
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.HeroBillboard
import com.example.ui.components.MoviePosterCard
import com.example.ui.components.TrendingTop10Card
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MovixaRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Profile

@Composable
fun HomeScreen(
    featuredMovie: Movie,
    allMovies: List<Movie>,
    watchlist: List<WatchlistEntity>,
    watchHistory: List<WatchHistoryEntity>,
    activeProfile: Profile,
    categoryFilter: String,
    onMovieClick: (Movie) -> Unit,
    onPlayMovie: (Movie) -> Unit,
    onToggleWatchlist: (Movie, Boolean) -> Unit,
    onRemoveHistory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val inWatchlist = watchlist.any { it.movieId == featuredMovie.id }

    // Filter movies according to categoryFilter
    val filteredMovies = when (categoryFilter) {
        "TV Series" -> allMovies.filter { it.isSeries }
        "Movies" -> allMovies.filter { !it.isSeries }
        else -> allMovies
    }

    val trendingList = filteredMovies.take(10)
    val originalsList = filteredMovies.filter { it.isOriginal }
    val sciFiActionList = filteredMovies.filter { movie ->
        movie.genres.any { it in listOf("Sci-Fi", "Action", "Cyberpunk", "Speed") }
    }
    val dramaThrillerList = filteredMovies.filter { movie ->
        movie.genres.any { it in listOf("Drama", "Thriller", "Crime", "Mystery", "History") }
    }
    val animationFamilyList = filteredMovies.filter { movie ->
        movie.genres.any { it in listOf("Animation", "Family", "Fantasy") }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Hero Billboard Header
        item {
            HeroBillboard(
                movie = featuredMovie,
                isInWatchlist = inWatchlist,
                onPlayClick = { onPlayMovie(featuredMovie) },
                onWatchlistToggle = { onToggleWatchlist(featuredMovie, inWatchlist) },
                onInfoClick = { onMovieClick(featuredMovie) }
            )
        }

        // Continue Watching Row (if user has watched titles)
        if (watchHistory.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Continue Watching for ${activeProfile.name}",
                    onSeeAll = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(watchHistory) { history ->
                        val movie = allMovies.find { it.id == history.movieId }
                        ContinueWatchingCard(
                            history = history,
                            movie = movie,
                            onPlayClick = {
                                if (movie != null) {
                                    val ep = movie.episodes.find { it.id == history.episodeId }
                                    onPlayMovie(movie)
                                }
                            },
                            onInfoClick = {
                                movie?.let(onMovieClick)
                            },
                            onRemoveClick = {
                                onRemoveHistory(history.movieId)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Top 10 in World Today
        if (trendingList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Top 10 in Movies & Series Today",
                    onSeeAll = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(trendingList) { index, movie ->
                        TrendingTop10Card(
                            rank = index + 1,
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Movixa Originals Row
        if (originalsList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Only on Movixa",
                    subtitle = "Award-Winning Originals & Exclusives",
                    onSeeAll = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(originalsList) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            width = 135,
                            height = 195
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Sci-Fi & Action Row
        if (sciFiActionList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Sci-Fi & Action Blockbusters",
                    onSeeAll = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sciFiActionList) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Drama & Thriller Row
        if (dramaThrillerList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Critically Acclaimed Dramas & Thrillers",
                    onSeeAll = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dramaThrillerList) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Animation & Fantasy Row
        if (animationFamilyList.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Animation & Fantasy Worlds",
                    onSeeAll = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(animationFamilyList) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            if (onSeeAll != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onSeeAll)
                ) {
                    Text(
                        text = "See All",
                        color = MovixaRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "See All",
                        tint = MovixaRed
                    )
                }
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
