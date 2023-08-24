package com.android.moviesbymoviedb.ui.favorites_movies

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.android.moviesbymoviedb.R
import com.android.moviesbymoviedb.databinding.ActivityFavoritesBinding
import com.android.moviesbymoviedb.models.EventUI
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.ui.movie_details.MovieDetailsActivity
import com.android.moviesbymoviedb.utils.collectLatestLifecycleFlow
import com.android.moviesbymoviedb.utils.goBackAnimation
import com.android.moviesbymoviedb.utils.goForwardAnimation
import com.android.moviesbymoviedb.viewmodel.MoviesViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    lateinit var binding: ActivityFavoritesBinding

    private val viewModel by viewModels<MoviesViewModel>()
    lateinit var favoritesAdapter: FavoritesAdapter
    private val favoriteMovies = mutableSetOf<MovieModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAdapter()
        collectData()
        setOnClick()

        viewModel.fetchMoviesFromLocalDatabase()
    }

    private fun setOnClick() {
        binding.ivBack.setOnClickListener {
            finish()
            goBackAnimation()
        }
    }

    private fun collectData() {
        collectLatestLifecycleFlow(viewModel.sharedFlowFetchMovies) {
            when (it) {
                is EventUI.OnLoading -> {
                    updateLoading(it.isShowing)
                }
                is EventUI.OnSuccess -> {
                    updateIU(it.data)
                }
                is EventUI.OnError -> {
                    Snackbar.make(binding.root, it.message, 1500)
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.color4))
                        .show()
                }
            }
        }
    }

    private fun updateIU(data: List<MovieModel>?) {
        if (data==null){
            return
        }
        favoriteMovies.clear()
        favoriteMovies.addAll(data.filter { it.isFavorite })
        favoritesAdapter.submitList(favoriteMovies.toList())
    }

    private fun updateLoading(showing: Boolean) {

    }

    private fun setUpAdapter() {
        favoritesAdapter = FavoritesAdapter{ val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movie", Gson().toJson(it))
            startActivityForResult(intent, 12)
            goForwardAnimation()
        }
        binding.rvMovies.adapter = favoritesAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            val model = data.getStringExtra("movie")
            val updatedMovie =
                Gson().fromJson<MovieModel>(model, object : TypeToken<MovieModel>() {}.type)

            if (updatedMovie.isFavorite){
                return
            }
            val targetItem = favoriteMovies.find { it.id == updatedMovie.id } ?: return
            val targetIndex = favoriteMovies.indexOf(targetItem)
            if (targetIndex != -1) {
                val newList = favoriteMovies.toMutableList()
                newList.removeAt(targetIndex)
                favoriteMovies.clear()
                favoriteMovies.addAll(newList)
                favoritesAdapter.submitList(newList)
            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goBackAnimation()
    }
}