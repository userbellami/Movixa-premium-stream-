package com.example

import android.app.Application
import com.example.data.MovieRepository
import com.example.data.local.MovixaDatabase

class MovixaApplication : Application() {

    val database: MovixaDatabase by lazy {
        MovixaDatabase.getDatabase(this)
    }

    val repository: MovieRepository by lazy {
        MovieRepository(database.movieDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MovixaApplication
            private set
    }
}
