package com.android.moviesbymoviedb.ui.movie_details

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.moviesbymoviedb.R
import com.android.moviesbymoviedb.databinding.ActivityMovieDetailsBinding
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.utils.goBackAnimation
import com.android.moviesbymoviedb.utils.goForwardAnimation
import com.android.moviesbymoviedb.utils.loadImageWithGlide
import com.android.moviesbymoviedb.viewmodel.MoviesViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityMovieDetailsBinding
    lateinit var movie: MovieModel

    private val viewModel by viewModels<MoviesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpIntent()
        setOnClick()
    }

    private fun setOnClick() {
        binding.ivBack.setOnClickListener {
            val intent = Intent()
            intent.putExtra("movie", Gson().toJson(movie))
            setResult(RESULT_OK, intent)
            finish()
            goBackAnimation()
        }

        binding.buttonFavorite.setOnClickListener {
            movie.isFavorite = !movie.isFavorite
            viewModel.updateMovie(movie)
            setUpUI(movie)
        }
    }

    private fun setUpIntent() {
        if (intent.hasExtra("movie")) {
            movie = Gson().fromJson(
                intent.getStringExtra("movie"),
                object : TypeToken<MovieModel>() {}.type
            )

            setUpUI(movie)
        }
    }

    private fun setUpUI(movie: MovieModel?) {
        if (movie == null) {
            return
        }
        binding.movieImage.loadImageWithGlide(movie.posterPath ?: "")
        binding.tvMovieName.text = movie.name
        binding.tvOverview.text = movie.overview

        binding.ratingValue.text = movie.voteAverage.toString()
        binding.votesValue.text = movie.voteCount.toString()

        if (movie.isFavorite) {
            binding.buttonFavorite.setImageResource(R.drawable.ic_favorite)
        } else {
            binding.buttonFavorite.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("movie", Gson().toJson(movie))
        setResult(RESULT_OK, intent)
        super.onBackPressed()
        goBackAnimation()
    }
}