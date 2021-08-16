package dev.ahmedmourad.tvmanager.movies.find

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.ahmedmourad.tvmanager.R
import dev.ahmedmourad.tvmanager.core.users.model.RetrievedMovie
import dev.ahmedmourad.tvmanager.databinding.ItemMovieBinding
import dev.ahmedmourad.tvmanager.movies.toMillis
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val decimalFormat = DecimalFormat("###.#")

private val MOVIES_COMPARATOR = object : DiffUtil.ItemCallback<RetrievedMovie>() {
    override fun areItemsTheSame(old: RetrievedMovie, new: RetrievedMovie): Boolean {
        return old.id == new.id
    }
    override fun areContentsTheSame(old: RetrievedMovie, new: RetrievedMovie): Boolean {
        return old == new
    }
}

typealias OnMovieSelectedListener = (movie: RetrievedMovie) -> Unit

class MoviesRecyclerAdapter(
    private val context: Context,
    private val onMovieSelectedListener: OnMovieSelectedListener = { }
) : PagingDataAdapter<RetrievedMovie, MoviesRecyclerAdapter.ViewHolder>(MOVIES_COMPARATOR) {

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(container.context)
            .inflate(R.layout.item_movie, container, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        if (post != null) {
            holder.bind(post)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemMovieBinding = ItemMovieBinding.bind(view)

        fun bind(item: RetrievedMovie) {

            binding.title.text = item.title.trim()

            binding.releaseDate.text = if (item.releaseDate != null) {
                context.getString(
                    R.string.release_date_formatted,
                    item.releaseDate!!.format(dateFormat)
                )
            } else {
                context.getString(R.string.release_date_empty_formatted)
            }

            binding.seasonsCount.text = if (item.seasonsCount != null) {
                context.getString(
                    R.string.seasons_count_formatted,
                    decimalFormat.format(item.seasonsCount)
                )
            } else {
                context.getString(R.string.seasons_count_empty_formatted)
            }

            binding.createdAt.text = DateUtils.getRelativeDateTimeString(
                context,
                item.createdAt.toMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0
            ).toString()

            itemView.setOnClickListener {
                onMovieSelectedListener(item)
            }
        }
    }
}
