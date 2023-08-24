package com.android.moviesbymoviedb.models


data class APIError(
    var statusCode: Int = -1,
    var message: String = "",
)

