package com.android.moviesbymoviedb.repository

import android.app.Application
import android.content.Context
import android.util.Log
import com.android.moviesbymoviedb.models.EventRepo
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.repository.ApiManager
import com.android.moviesbymoviedb.repository.ApiServices
import com.android.moviesbymoviedb.repository.BaseUrl
import com.android.moviesbymoviedb.repository.room_database.MovieDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import javax.inject.Inject
import kotlin.collections.HashMap

class ApiServicesImpl @Inject constructor(
    private val apiManager: ApiManager
) : ApiServices {

    @Inject
    @ApplicationContext
    lateinit var appContext: Context

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var movieDatabase: MovieDatabase

    override suspend fun fetchMovies(params: HashMap<String, Any>): Flow<EventRepo<List<MovieModel>>> =
        flow {

            val parameters = arrayListOf<Pair<String, Any>>()

            for ((key, value) in params) {
                parameters.add(Pair(key, value))
            }

            apiManager.makeRequest(
                url = BaseUrl.URL.value,
                bodyMap = null,
                reqMethod = HttpMethod.Get,
                parameterFormData = parameters,
                failureCallback = { error ->
                    emit(EventRepo.Error(error.apiError))
                },
                successCallback = {
                    var resultsArray = JSONArray()

                    if (it.has("results")) {
                        resultsArray = it.getJSONArray("results")
                    }

                    val listOfMovies = Gson().fromJson<List<MovieModel>>(
                        resultsArray.toString(),
                        object : TypeToken<List<MovieModel>>() {}.type
                    )

                    emit(EventRepo.Success(listOfMovies))
                }
            )
        }

    override suspend fun insertMoviesToLocalDatabase(movies: List<MovieModel>) {
        Log.e("insertMoviesToLocalDatabase", movies.size.toString())
        runBlocking(Dispatchers.IO) {
            movieDatabase.movieDao().insertMovies(movies)
        }

    }

    override suspend fun fetchMoviesFromLocalDatabase(): Flow<EventRepo<List<MovieModel>>> =
        flow {

            var movies = listOf<MovieModel>()
            runBlocking(Dispatchers.IO) {
                movies = movieDatabase.movieDao().getAllMovies()
            }
            Log.e("fetchMoviesFromLocalDatabase", movies.size.toString())

            emit(EventRepo.Success(movies))
        }

    override suspend fun updateMovie(movie: MovieModel) {
        Log.e("updateMovie", movie.toString())
        runBlocking(Dispatchers.IO) {
            movieDatabase.movieDao().updateMovie(movie)
        }
    }

}