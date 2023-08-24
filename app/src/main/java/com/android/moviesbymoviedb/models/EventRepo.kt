package com.android.moviesbymoviedb.models


sealed class EventRepo<T>() {
    class Success<T>(val data: T?) : EventRepo<T>()
    class Error<T>(val apiError: APIError) : EventRepo<T>()
}
