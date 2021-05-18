package org.jellyfin.client.android.ui.home.movie_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentMovieDetailsBinding
import org.jellyfin.client.android.domain.models.Status
import java.util.*
import javax.inject.Inject

class MovieDetailsFragment : DaggerFragment() {

    private lateinit var binding: FragmentMovieDetailsBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val movieDetailsViewModel: MovieDetailsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MovieDetailsViewModel::class.java).apply {
            initialize(UUID.fromString(args.uuid))
        }
    }

    private val args: MovieDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieDetailsViewModel.getMovieDetails().observe(viewLifecycleOwner, {resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {movieDetails ->
                        binding.contents.movie = movieDetails
                        binding.contents.backdrop.load(movieDetails.backdropUrl)
                        binding.contents.poster.load(movieDetails.posterUrl)
                        binding.contents.overview.setText(getString(R.string.movie_details_item_overview), movieDetails.overview)
                        binding.contents.genres.setText(getString(R.string.movie_details_item_genre), "")
                        binding.contents.director.setText(getString(R.string.movie_details_item_director), "")
                    }
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {

                }
            }
        })
    }
}