package com.example.data

import com.example.R
import com.example.data.local.DownloadEntity
import com.example.data.local.MovieDao
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.model.Episode
import com.example.model.Movie
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val movieDao: MovieDao) {

    val watchlist: Flow<List<WatchlistEntity>> = movieDao.getWatchlist()
    val watchHistory: Flow<List<WatchHistoryEntity>> = movieDao.getWatchHistory()
    val downloads: Flow<List<DownloadEntity>> = movieDao.getDownloads()

    fun isInWatchlist(movieId: String): Flow<Boolean> = movieDao.isInWatchlist(movieId)
    fun isDownloaded(movieId: String): Flow<Boolean> = movieDao.isDownloaded(movieId)

    suspend fun toggleWatchlist(movie: Movie, inWatchlist: Boolean) {
        if (inWatchlist) {
            movieDao.removeFromWatchlist(movie.id)
        } else {
            movieDao.addToWatchlist(
                WatchlistEntity(
                    movieId = movie.id,
                    title = movie.title,
                    posterUrl = movie.posterUrl,
                    isSeries = movie.isSeries,
                    rating = movie.rating
                )
            )
        }
    }

    suspend fun saveProgress(movieId: String, title: String, backdropUrl: String, positionMs: Long, durationMs: Long, episodeId: String? = null, episodeTitle: String? = null) {
        movieDao.savePlaybackProgress(
            WatchHistoryEntity(
                movieId = movieId,
                title = title,
                backdropUrl = backdropUrl,
                currentPositionMs = positionMs,
                totalDurationMs = durationMs,
                episodeId = episodeId,
                episodeTitle = episodeTitle
            )
        )
    }

    suspend fun removeHistory(movieId: String) {
        movieDao.deleteHistory(movieId)
    }

    suspend fun addDownload(movie: Movie, sizeMb: Int = 450, quality: String = "1080p") {
        movieDao.addDownload(
            DownloadEntity(
                movieId = movie.id,
                title = movie.title,
                posterUrl = movie.posterUrl,
                sizeMb = sizeMb,
                quality = quality,
                durationMinutes = movie.durationMinutes
            )
        )
    }

    suspend fun removeDownload(movieId: String) {
        movieDao.removeDownload(movieId)
    }

    // High quality verified streaming URLs
    private val STREAM_COSMIC = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4"
    private val STREAM_NEON = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4"
    private val STREAM_SHADOWS = "https://test-videos.co.uk/vids/sintel/mp4/h264/720/Sintel_720_10s_1MB.mp4"
    private val STREAM_VELOCITY = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4"
    private val STREAM_JELLYFISH = "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_1MB.mp4"

    // Curated catalog of movies & series with verified playable video streams
    val allMovies: List<Movie> = listOf(
        Movie(
            id = "mov-1",
            title = "Cosmic Odyssey",
            tagline = "Beyond the edge of known spacetime, hope still shines.",
            synopsis = "A team of deep-space pioneers journeys through an uncharted bioluminescent nebula to investigate a mysterious harmonic signal emanating from a dying ringed planet.",
            backdropRes = R.drawable.img_hero_billboard,
            backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&q=80",
            videoUrl = STREAM_COSMIC,
            trailerUrl = STREAM_COSMIC,
            rating = 9.2f,
            matchScore = 99,
            year = 2026,
            ageRating = "PG-13",
            durationMinutes = 148,
            quality = "4K Ultra HD",
            audio = "Spatial Audio 5.1",
            genres = listOf("Sci-Fi", "Adventure", "Drama"),
            isOriginal = true,
            cast = listOf("Elena Rostova", "Marcus Vance", "Kaelen Chen", "Sarah O'Connor"),
            director = "Denis Villeneuve"
        ),
        Movie(
            id = "mov-2",
            title = "Neon Horizon: 2099",
            tagline = "Memories are the new contraband.",
            synopsis = "In a rain-drenched megacity controlled by synthetic cartels, a rogue cyber-detective uncovers a digitized conspiracy that threatens the fabric of human consciousness.",
            backdropRes = R.drawable.img_cyber_noir,
            backdropUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80",
            videoUrl = STREAM_NEON,
            trailerUrl = STREAM_NEON,
            rating = 8.8f,
            matchScore = 96,
            year = 2025,
            ageRating = "TV-MA",
            durationMinutes = 132,
            quality = "4K Ultra HD",
            audio = "Dolby Atmos",
            genres = listOf("Cyberpunk", "Action", "Thriller"),
            isOriginal = true,
            cast = listOf("Daito Takahashi", "Aria Jensen", "Viktor Novak"),
            director = "Ridley Scott"
        ),
        Movie(
            id = "mov-3",
            title = "Shadows of the Crown",
            tagline = "Power is inherited. Loyalty is taken.",
            synopsis = "An intricate political saga tracking the secret backroom betrayals, royal assassinations, and dynastic wars of 16th-century European high courts.",
            backdropUrl = "https://images.unsplash.com/photo-1533158307587-828f0a76ef46?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&q=80",
            videoUrl = STREAM_SHADOWS,
            trailerUrl = STREAM_SHADOWS,
            rating = 9.1f,
            matchScore = 97,
            year = 2024,
            ageRating = "TV-MA",
            durationMinutes = 58,
            quality = "4K Ultra HD",
            audio = "Spatial Audio 5.1",
            genres = listOf("Drama", "History", "Political"),
            isOriginal = true,
            cast = listOf("Claire Sterling", "Arthur Pendelton", "Guillaume de Valois"),
            director = "Tom Hooper",
            isSeries = true,
            seasonsCount = 2,
            episodes = listOf(
                Episode("ep-3-1", 1, 1, "The Succession Vow", 56, "Following the King's sudden collapse, three rival dukes plot for the regency.", STREAM_SHADOWS, "https://images.unsplash.com/photo-1533158307587-828f0a76ef46?w=600&q=80"),
                Episode("ep-3-2", 2, 1, "Whispers in the Crypt", 52, "A clandestine meeting in the abbey catacombs reveals a hidden heir.", STREAM_COSMIC, "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&q=80"),
                Episode("ep-3-3", 3, 1, "Blood on Velvet", 61, "The coronation banquet takes a deadly turn when poison touches the royal chalice.", STREAM_NEON, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80"),
                Episode("ep-3-4", 4, 1, "The Siege of Ravencrest", 58, "Border armies mobilize as winter approaches and alliances fracture.", STREAM_VELOCITY, "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600&q=80")
            )
        ),
        Movie(
            id = "mov-4",
            title = "Velocity: Tokyo Driftline",
            tagline = "Speed has no conscience.",
            synopsis = "Underground street racing legends clash with high-tech automated law enforcement drones in neon-lit highway battles across Tokyo.",
            backdropUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=600&q=80",
            videoUrl = STREAM_VELOCITY,
            trailerUrl = STREAM_VELOCITY,
            rating = 8.4f,
            matchScore = 93,
            year = 2025,
            ageRating = "PG-13",
            durationMinutes = 115,
            quality = "1080p HD",
            audio = "Dolby Atmos",
            genres = listOf("Action", "Speed", "Crime"),
            isOriginal = false,
            cast = listOf("Ren Sato", "Maya Sterling", "Kenjiro Tanaka"),
            director = "Justin Lin"
        ),
        Movie(
            id = "mov-5",
            title = "The Quantum Heist",
            tagline = "To steal the future, you have to break time.",
            synopsis = "A crew of quantum physicists turned international thieves attempt the impossible: extracting 10 terabytes of encryption keys from a vault that exists in 4 dimensions.",
            backdropUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=600&q=80",
            videoUrl = STREAM_COSMIC,
            trailerUrl = STREAM_COSMIC,
            rating = 8.9f,
            matchScore = 95,
            year = 2024,
            ageRating = "PG-13",
            durationMinutes = 140,
            quality = "4K Ultra HD",
            audio = "Spatial Audio 5.1",
            genres = listOf("Sci-Fi", "Heist", "Action"),
            isOriginal = true,
            cast = listOf("Julian Drake", "Zoe Saldana", "Hugh Laurie"),
            director = "Christopher Nolan"
        ),
        Movie(
            id = "mov-6",
            title = "Aether: Spirit of the Forest",
            tagline = "Every breath of nature holds ancient magic.",
            synopsis = "A young herbalist discovers a hidden sanctuary of gentle celestial forest spirits, and must defend their sanctuary from industrial extraction machines.",
            backdropUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=600&q=80",
            videoUrl = STREAM_NEON,
            trailerUrl = STREAM_NEON,
            rating = 9.4f,
            matchScore = 98,
            year = 2025,
            ageRating = "PG",
            durationMinutes = 104,
            quality = "4K Ultra HD",
            audio = "Dolby Atmos",
            genres = listOf("Animation", "Family", "Fantasy"),
            isOriginal = true,
            cast = listOf("Mio Aoi", "Kenzo Watanabe", "Emma Watson"),
            director = "Hayao Miyazaki"
        ),
        Movie(
            id = "mov-7",
            title = "Mindhunter: Deep Circuit",
            tagline = "The serial killer was trained by machine learning.",
            synopsis = "FBI behavioral analysts partner with an eccentric AI ethics investigator to decode the cryptic algorithmic patterns of an elusive cybernetic stalker.",
            backdropUrl = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&q=80",
            videoUrl = STREAM_SHADOWS,
            trailerUrl = STREAM_SHADOWS,
            rating = 9.0f,
            matchScore = 94,
            year = 2026,
            ageRating = "TV-MA",
            durationMinutes = 55,
            quality = "4K Ultra HD",
            audio = "Spatial Audio 5.1",
            genres = listOf("Crime", "Mystery", "Psychological"),
            isOriginal = true,
            cast = listOf("Jonathan Groff", "Holt McCallany", "Anna Torv"),
            director = "David Fincher",
            isSeries = true,
            seasonsCount = 1,
            episodes = listOf(
                Episode("ep-7-1", 1, 1, "The Turing Cipher", 54, "A sequence of cryptic binary coordinates left at crime scenes prompts a federal task force.", STREAM_SHADOWS, "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=600&q=80"),
                Episode("ep-7-2", 2, 1, "Feedback Loop", 57, "Interviews with an incarcerated tech mogul reveal alarming predictive behavioral models.", STREAM_COSMIC, "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&q=80")
            )
        ),
        Movie(
            id = "mov-8",
            title = "Sub-Zero: Mariana Trench",
            tagline = "At 36,000 feet deep, fear has no temperature.",
            synopsis = "A research crew trapped in an experimental abyssal submarine discovers a subterranean geothermal ecosystem inhabited by massive prehistoric predators.",
            backdropUrl = "https://images.unsplash.com/photo-1682687220063-4742bd7fd538?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600&q=80",
            videoUrl = STREAM_JELLYFISH,
            trailerUrl = STREAM_JELLYFISH,
            rating = 8.6f,
            matchScore = 91,
            year = 2024,
            ageRating = "PG-13",
            durationMinutes = 119,
            quality = "1080p HD",
            audio = "Spatial Audio 5.1",
            genres = listOf("Thriller", "Sci-Fi", "Horror"),
            isOriginal = false,
            cast = listOf("Jason Statham", "Li Bingbing", "Rainn Wilson"),
            director = "Jon Turteltaub"
        ),
        Movie(
            id = "mov-9",
            title = "Wild Cosmos: Extreme Planets",
            tagline = "Narrated by Sir David Attenborough.",
            synopsis = "Breathtaking visual documentary exploring hypothetical extraterrestrial ecosystems, methane lakes, glass rain storms, and exotic alien biology across the Milky Way.",
            backdropUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&q=80",
            videoUrl = STREAM_NEON,
            trailerUrl = STREAM_NEON,
            rating = 9.6f,
            matchScore = 99,
            year = 2025,
            ageRating = "G",
            durationMinutes = 90,
            quality = "4K Ultra HD",
            audio = "Dolby Atmos",
            genres = listOf("Documentary", "Nature", "Science"),
            isOriginal = true,
            cast = listOf("Sir David Attenborough"),
            director = "Alastair Fothergill"
        ),
        Movie(
            id = "mov-10",
            title = "Arcane Chronicles",
            tagline = "When steel meets sorcery, empires crumble.",
            synopsis = "Two estranged sisters find practical power on opposing sides of a brutal revolution between a steampunk upper city and its oppressed underground underbelly.",
            backdropUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&q=80",
            posterUrl = "https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?w=600&q=80",
            videoUrl = STREAM_COSMIC,
            trailerUrl = STREAM_COSMIC,
            rating = 9.7f,
            matchScore = 99,
            year = 2024,
            ageRating = "TV-14",
            durationMinutes = 48,
            quality = "4K Ultra HD",
            audio = "Dolby Atmos",
            genres = listOf("Animation", "Action", "Fantasy"),
            isOriginal = true,
            cast = listOf("Hailee Steinfeld", "Ella Purnell", "Kevin Alejandro"),
            director = "Christian Linke",
            isSeries = true,
            seasonsCount = 2,
            episodes = listOf(
                Episode("ep-10-1", 1, 1, "Welcome to the Playground", 44, "Orphaned sisters Vi and Powder cause a blast in a scientist's workshop.", STREAM_COSMIC, "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&q=80"),
                Episode("ep-10-2", 2, 1, "Some Mysteries Are Better Left Unsolved", 41, "Idealistic inventor Jayce faces academy expulsion for illegal experiments.", STREAM_NEON, "https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?w=600&q=80")
            )
        )
    )

    fun getMovieById(id: String): Movie? = allMovies.find { it.id == id }

    fun getFeaturedMovie(): Movie = allMovies.first()

    fun getTrendingTop10(): List<Movie> = allMovies.take(10)

    fun getOriginals(): List<Movie> = allMovies.filter { it.isOriginal }

    fun getMoviesByGenre(genre: String): List<Movie> =
        allMovies.filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }

    fun searchMovies(query: String): List<Movie> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return allMovies.filter { movie ->
            movie.title.lowercase().contains(q) ||
                movie.genres.any { it.lowercase().contains(q) } ||
                movie.cast.any { it.lowercase().contains(q) } ||
                movie.director.lowercase().contains(q) ||
                movie.synopsis.lowercase().contains(q)
        }
    }
}
