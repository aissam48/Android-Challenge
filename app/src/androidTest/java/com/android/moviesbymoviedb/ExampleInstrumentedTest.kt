package com.android.moviesbymoviedb

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.repository.room_database.MovieDao
import com.android.moviesbymoviedb.repository.room_database.MovieDatabase
import com.android.moviesbymoviedb.ui.movies.MoviesActivity
import kotlinx.coroutines.runBlocking
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var database: MovieDatabase
    private lateinit var movieDao: MovieDao

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.android.moviesbymoviedb", appContext.packageName)
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MoviesActivity::class.java)

    @Test
    fun testSearchSuccess() {
        val searchQuery = "action"
        Espresso.onView(ViewMatchers.withId(R.id.etSearch))
            .perform(ViewActions.typeText(searchQuery))

        Espresso.onView(ViewMatchers.withId(R.id.rvMovies))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))

    }

    @Test
    fun testSearchEmpty() {
        val searchEmpty = ""
        Espresso.onView(ViewMatchers.withId(R.id.etSearch))
            .perform(ViewActions.typeText(searchEmpty))

        Espresso.onView(ViewMatchers.withId(R.id.rvMovies))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))

    }


    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = MovieDatabase.getInstance(context)
        movieDao = database.movieDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndRetrieveMovies() = runBlocking {
        val movie = MovieModel(name = "Sample Movie", voteAverage = 5.0)
        movieDao.insertMovies(listOf(movie))

        val retrievedMovies = movieDao.getAllMovies()
        assertEquals(retrievedMovies[0].name, movie.name)
        assertEquals(retrievedMovies[0].voteAverage.toString(), movie.voteAverage.toString())

    }
}