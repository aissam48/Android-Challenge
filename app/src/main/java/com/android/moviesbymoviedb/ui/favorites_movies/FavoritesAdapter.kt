package com.android.moviesbymoviedb.ui.favorites_movies

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.android.moviesbymoviedb.databinding.ItemMovieBinding
import com.android.moviesbymoviedb.models.MovieModel
import com.android.moviesbymoviedb.utils.loadImageWithGlide


class FavoritesAdapter(
    private val itemPressed: (item: MovieModel) -> Unit
) :
    ListAdapter<MovieModel, FavoritesAdapter.ViewHolder>(DiffUtilCallBacks()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem, position)
        }
    }


    inner class ViewHolder(
        private val context: Context,
        private val binding: ItemMovieBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(myItem: MovieModel, position: Int) {


            if (myItem.posterPath != null){
                binding.movieImage.loadImageWithGlide(myItem.posterPath)
            }

            binding.movieName.text = myItem.name
            binding.ratingValue.text = myItem.voteAverage.toString()
            binding.votesValue.text = myItem.voteCount.toString()

            binding.root.setOnClickListener {
                itemPressed(myItem)
            }

        }
    }

    class DiffUtilCallBacks : DiffUtil.ItemCallback<MovieModel>() {
        override fun areItemsTheSame(oldItem: MovieModel, newItem: MovieModel) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: MovieModel, newItem: MovieModel) =
            oldItem == newItem
    }
}