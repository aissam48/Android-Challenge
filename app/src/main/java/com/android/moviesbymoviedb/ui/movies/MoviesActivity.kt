package com.android.moviesbymoviedb.ui.movies

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.moviesbymoviedb.R
import com.android.moviesbymoviedb.databinding.ActivityMoviesBinding
import com.android.moviesbymoviedb.models.EventUI
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.ui.favorites_movies.FavoritesActivity
import com.android.moviesbymoviedb.ui.favorites_movies.FavoritesAdapter
import com.android.moviesbymoviedb.ui.movie_details.MovieDetailsActivity
import com.android.moviesbymoviedb.viewmodel.MoviesViewModel
import com.android.moviesbymoviedb.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoviesActivity : AppCompatActivity() {

    lateinit var binding: ActivityMoviesBinding
    private val viewModel by viewModels<MoviesViewModel>()
    lateinit var moviesAdapter: MoviesAdapter
    private val listOfMovies = mutableSetOf<MovieModel>()

    private var sortedBy = ""

    private val TAG = "MoviesActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAdapter()
        collectData()
        setUpClick()
        setUpPagination()
        setUpSearch()

        if (isInternetConnected()) {
            viewModel.fetchMovies("all")
        } else {
            viewModel.fetchMoviesFromLocalDatabase()
        }

    }

    private fun setUpSearch() {
        binding.etSearch.doOnTextChanged { text, start, before, count ->
            if (isInternetConnected()) {
                listOfMovies.clear()
                viewModel.page = 1
                if (text.toString().isEmpty()) {
                    viewModel.fetchMovies("all")
                } else {
                    viewModel.fetchMovies(text.toString())
                }
            } else {
                moviesAdapter.submitList(listOfMovies.filter {
                    it.name.lowercase().contains(text.toString().lowercase())
                })
            }
        }
    }

    private fun setUpPagination() {
        binding.rvMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(0) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isInternetConnected()) {
                        viewModel.page = viewModel.page + 1
                        val searchInput = binding.etSearch.text.toString()
                        if (searchInput.isEmpty()) {
                            viewModel.fetchMovies("all")
                        } else {
                            viewModel.fetchMovies(searchInput)
                        }
                    }
                }
            }
        })
    }

    private fun setUpClick() {

        binding.buttonFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
            goForwardAnimation()
        }

        binding.buttonSort.setOnClickListener {
            if (binding.llSearch.isVisible) {
                binding.llSearch.gone()
                binding.llSort.visible()
                binding.buttonSort.setImageResource(R.drawable.ic_search_gray)
            } else {
                binding.llSearch.visible()
                binding.llSort.gone()
                binding.buttonSort.setImageResource(R.drawable.ic_sort)
            }
        }

        binding.buttonSortByAlpha.setOnClickListener {
            sortedBy = getString(R.string.button_alphabetical)
            binding.buttonSortByAlpha.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button_selected)
            binding.buttonSortByDate.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button)
            binding.buttonSortByDate.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.buttonSortByAlpha.setTextColor(ContextCompat.getColor(this, R.color.white))

            moviesAdapter.submitList(listOfMovies.sortedBy { it.name })
        }

        binding.buttonSortByDate.setOnClickListener {
            sortedBy = getString(R.string.button_date)
            binding.buttonSortByDate.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button_selected)
            binding.buttonSortByAlpha.background =
                ContextCompat.getDrawable(this, R.drawable.bg_button)
            binding.buttonSortByAlpha.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.buttonSortByDate.setTextColor(ContextCompat.getColor(this, R.color.white))

            moviesAdapter.submitList(listOfMovies.sortedBy { it.firstAirDate })
        }
    }

    private fun setUpAdapter() {
        moviesAdapter = MoviesAdapter {
            val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movie", Gson().toJson(it))
            startActivityForResult(intent, 12)
            goForwardAnimation()
        }
        binding.rvMovies.adapter = moviesAdapter
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
        if (data == null) {
            return
        }
        try {
            listOfMovies.addAll(data)
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }

        moviesAdapter.submitList(listOfMovies.toList())

        viewModel.insertMoviesToLocalDatabase(listOfMovies)

    }

    private fun updateLoading(showing: Boolean) {
        if (showing) {
            binding.prLoading.visible()
        } else {
            binding.prLoading.gone()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            val model = data.getStringExtra("movie")
            val updatedMovie =
                Gson().fromJson<MovieModel>(model, object : TypeToken<MovieModel>() {}.type)

            val targetItem = listOfMovies.find { it.id == updatedMovie.id } ?: return
            val targetIndex = listOfMovies.indexOf(targetItem)
            if (targetIndex != -1) {
                val newList = listOfMovies.toMutableList()
                newList[targetIndex] = updatedMovie
                listOfMovies.clear()
                listOfMovies.addAll(newList)
                when (sortedBy) {
                    getString(R.string.button_alphabetical) -> {
                        moviesAdapter.submitList(newList.sortedBy { it.name })
                    }
                    getString(R.string.button_date) -> {
                        moviesAdapter.submitList(newList.sortedBy { it.firstAirDate })
                    }
                    else -> {
                        moviesAdapter.submitList(newList)
                    }
                }

            }

        }
    }

}