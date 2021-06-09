package org.jellyfin.client.android.ui.home.movie_details

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
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
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_WIDTH
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_POSTER_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_POSTER_WIDTH
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.extensions.getHoursFromTicks
import org.jellyfin.client.android.domain.extensions.getMinutesFromTicks
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.player.VlcPlayerActivity
import org.jellyfin.client.android.ui.shared.BlurHashDecoder
import org.jellyfin.client.android.ui.shared.RowWithChevronView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pullToRefresh.setOnRefreshListener {
            showLoading()
            movieDetailsViewModel.refresh()
        }

        binding.contents.btnPlay.setOnClickListener {
            val intent = Intent(requireActivity(), VlcPlayerActivity::class.java)
            intent.putExtra(Tags.BUNDLE_TAG_MEDIA_UUID, args.uuid)
            startActivity(intent)
        }

        movieDetailsViewModel.getMovieDetails().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { movieDetails ->
                        binding.contents.movie = movieDetails
                        val backdropBitmap = BlurHashDecoder.decode(movieDetails.backdropBlurHash, BLUR_HASH_BACKDROP_WIDTH, BLUR_HASH_BACKDROP_HEIGHT)
                        val backdropDrawable = BitmapDrawable(requireContext().resources, backdropBitmap)
                        binding.contents.backdrop.load(movieDetails.backdropUrl) {
                            placeholder(backdropDrawable)
                            error(backdropDrawable)
                        }
                        val posterBitmap = BlurHashDecoder.decode(movieDetails.posterBlurHash, BLUR_HASH_POSTER_WIDTH, BLUR_HASH_POSTER_HEIGHT)
                        val posterDrawable = BitmapDrawable(requireContext().resources, posterBitmap)
                        binding.contents.poster.load(movieDetails.posterUrl) {
                            placeholder(posterDrawable)
                            error(posterDrawable)
                        }
                        movieDetails.runTimeTicks?.let {
                            val hours = it.getHoursFromTicks()
                            val minutes = it.getMinutesFromTicks()
                            binding.contents.tvRuntime.text = getString(R.string.series_duration_with_units, hours, minutes)
                        }
                        binding.contents.overview.setTextAndVisibility(getString(R.string.movie_details_item_overview), movieDetails.overview)
                        movieDetails.directors?.let { directors ->
                            if (directors.isNotEmpty()) {
                                binding.contents.directors.visibility = View.VISIBLE
                                binding.contents.directorsContainer.visibility = View.VISIBLE
                                binding.contents.directorsContainer.removeAllViews()
                            }
                            directors.forEach {director ->
                                val row = RowWithChevronView(requireContext())
                                row.setText(director.name)
                                binding.contents.directorsContainer.addView(row)
                            }
                        }
                        movieDetails.genres?.let { genres ->
                            if (genres.isNotEmpty()) {
                                binding.contents.genres.visibility = View.VISIBLE
                                binding.contents.genresContainer.visibility = View.VISIBLE
                                binding.contents.genresContainer.removeAllViews()
                            }
                            genres.forEach {genre ->
                                val row = RowWithChevronView(requireContext())
                                row.setText(genre.name)
                                binding.contents.genresContainer.addView(row)
                            }
                        }
                        showContent()
                    }
                }
                Status.LOADING -> {
                    showLoading()
                }
                Status.ERROR -> {
                    showError()
                }
            }
        })
    }

    private fun showLoading() {
        binding.pullToRefresh.isRefreshing = false
        binding.pullToRefresh.isEnabled = false
        binding.contents.root.visibility = View.GONE
        binding.loadingScreen.visibility = View.VISIBLE
        binding.errorScreen.visibility = View.GONE
    }

    private fun showContent() {
        binding.pullToRefresh.isEnabled = true
        binding.contents.root.visibility = View.VISIBLE
        binding.loadingScreen.visibility = View.GONE
        binding.errorScreen.visibility = View.GONE
    }

    private fun showError() {
        binding.pullToRefresh.isEnabled = true
        binding.contents.root.visibility = View.GONE
        binding.loadingScreen.visibility = View.GONE
        binding.errorScreen.visibility = View.VISIBLE
    }
}
