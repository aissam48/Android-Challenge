package com.android.moviesbymoviedb

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoviesByMovieDbApp:Application() {

    override fun onCreate() {
        super.onCreate()

    }
}