package com.android.moviesbymoviedb.di

import android.content.Context
import androidx.room.Room
import com.android.moviesbymoviedb.repository.ApiManager
import com.android.moviesbymoviedb.repository.room_database.MovieDatabase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(Logging)
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
    }

    @Provides
    @Singleton
    fun providesClientApiManager(client: HttpClient, @ApplicationContext appContext: Context): ApiManager {
        return ApiManager(client, appContext)
    }


    @Provides
    @Singleton
    fun providesRoomDatabase(@ApplicationContext appContext: Context):MovieDatabase{
        return Room.databaseBuilder(appContext, MovieDatabase::class.java, "movies_table")
            .build()

    }



}