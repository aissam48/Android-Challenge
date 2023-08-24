package com.android.moviesbymoviedb.repository

import com.android.moviesbymoviedb.models.EventRepo
import com.android.moviesbymoviedb.models.MovieModel
import kotlinx.coroutines.flow.Flow

interface ApiServices {

    suspend fun fetchMovies(params:HashMap<String, Any>):Flow<EventRepo<List<MovieModel>>>
    suspend fun insertMoviesToLocalDatabase(movies:List<MovieModel>)
    suspend fun fetchMoviesFromLocalDatabase():Flow<EventRepo<List<MovieModel>>>
    suspend fun updateMovie(movie: MovieModel)
}