package com.android.moviesbymoviedb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.moviesbymoviedb.models.EventRepo
import com.android.moviesbymoviedb.models.EventUI
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.repository.ApiServicesImpl
import com.android.moviesbymoviedb.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val apiServicesImpl: ApiServicesImpl,
) : ViewModel() {

    private val _sharedFlowFetchMovies =
        MutableSharedFlow<EventUI<List<MovieModel>>>()
    val sharedFlowFetchMovies = _sharedFlowFetchMovies.asSharedFlow()

    var page = 1
    fun fetchMovies(s: String) {
        val params = hashMapOf<String, Any>()
        params["api_key"] = Constants.API_KEY
        params["language"] = "en-US"
        params["page"] = page
        params["query"] = s
        params["include_adult"] = false
        viewModelScope.launch {

            _sharedFlowFetchMovies.emit(EventUI.OnLoading(true))
            apiServicesImpl.fetchMovies(params).onEach { result ->
                _sharedFlowFetchMovies.emit(EventUI.OnLoading(false))

                when (result) {
                    is EventRepo.Success -> {
                        if (page > 1 && result.data?.isEmpty() == true) {
                            page--
                        }
                        _sharedFlowFetchMovies.emit(EventUI.OnSuccess(result.data))
                    }
                    is EventRepo.Error -> {
                        _sharedFlowFetchMovies.emit(EventUI.OnError(result.apiError.message))
                    }
                }

            }.launchIn(this)
        }
    }

    fun updateMovie(movie: MovieModel) {
        viewModelScope.launch {
            apiServicesImpl.updateMovie(movie)
        }
    }

    fun insertMoviesToLocalDatabase(listOfMovies: MutableSet<MovieModel>) {
        viewModelScope.launch {
            apiServicesImpl.insertMoviesToLocalDatabase(listOfMovies.toList())
        }
    }


    fun fetchMoviesFromLocalDatabase() {
        viewModelScope.launch {
            _sharedFlowFetchMovies.emit(EventUI.OnLoading(true))
            apiServicesImpl.fetchMoviesFromLocalDatabase().onEach { result ->
                _sharedFlowFetchMovies.emit(EventUI.OnLoading(false))
                _sharedFlowFetchMovies.emit(EventUI.OnSuccess((result as EventRepo.Success).data))
            }.launchIn(this)
        }
    }

}

