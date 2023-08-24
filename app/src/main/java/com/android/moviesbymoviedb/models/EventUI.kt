package com.android.moviesbymoviedb.models

sealed class EventUI<T>(){
    class OnLoading<T>(val isShowing:Boolean): EventUI<T>()
    class OnSuccess<T>(val data: T?) : EventUI<T>()
    class OnError<T>(val message:String,val field:String? = null): EventUI<T>()

}