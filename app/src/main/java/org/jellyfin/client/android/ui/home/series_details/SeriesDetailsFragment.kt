package org.jellyfin.client.android.ui.home.series_details

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentSeriesDetailsBinding
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_WIDTH
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_POSTER_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_POSTER_WIDTH
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import org.jellyfin.client.android.ui.player.VlcPlayerActivity
import org.jellyfin.client.android.ui.shared.BlurHashDecoder
import org.jellyfin.client.android.ui.shared.RowWithChevronView
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SeriesDetailsFragment : DaggerFragment() {

    private var _binding: FragmentSeriesDetailsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val seriesDetailsViewModel: SeriesDetailsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SeriesDetailsViewModel::class.java).apply {
            initialize(UUID.fromString(args.uuid))
        }
    }

    private val args: SeriesDetailsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeriesDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pullToRefresh.setOnRefreshListener {
            showLoading()
            seriesDetailsViewModel.refresh()
        }

        binding.contents.btnPlay.setOnClickListener {
            val intent = Intent(requireActivity(), VlcPlayerActivity::class.java)
            intent.putExtra(Tags.BUNDLE_TAG_MEDIA_UUID, args.uuid)
            startActivity(intent)
        }

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val adapter = HomeRowRecyclerViewAdapter(requireActivity(), displayMetrics.widthPixels)
        binding.contents.seasonAdapter = adapter
        binding.contents.seasonsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

        adapter.onCardClick = { card ->
            val action = if (card.itemType == ItemType.SEASON) {
                SeriesDetailsFragmentDirections.actionSeasonDetails(
                    title = card.title ?: "",
                    seriesId = args.uuid.toString(),
                    seasonId = card.uuid.toString()
                )
            } else {
                SeriesDetailsFragmentDirections.actionSeriesDetails(
                    title = card.title ?: "",
                    uuid = card.uuid.toString()
                )
            }
            findNavController().navigate(action)
        }

        seriesDetailsViewModel.getSeriesDetails().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { seriesDetails ->
                        binding.contents.series = seriesDetails
                        val backdropBitmap = BlurHashDecoder.decode(seriesDetails.backdropBlurHash, BLUR_HASH_BACKDROP_WIDTH, BLUR_HASH_BACKDROP_HEIGHT)
                        val backdropDrawable = BitmapDrawable(requireContext().resources, backdropBitmap)
                        binding.contents.backdrop.load(seriesDetails.backdropUrl) {
                            placeholder(backdropDrawable)
                            error(backdropDrawable)
                        }
                        val posterBitmap = BlurHashDecoder.decode(seriesDetails.posterBlurHash, BLUR_HASH_POSTER_WIDTH, BLUR_HASH_POSTER_HEIGHT)
                        val posterDrawable = BitmapDrawable(requireContext().resources, posterBitmap)
                        binding.contents.poster.load(seriesDetails.posterUrl) {
                            placeholder(posterDrawable)
                            error(posterDrawable)
                        }
                        binding.contents.overview.setTextAndVisibility(getString(R.string.movie_details_item_overview), seriesDetails.overview)
                        seriesDetails.directors?.let { directors ->
                            if (directors.isNotEmpty()) {
                                binding.contents.directors.visibility = View.VISIBLE
                                binding.contents.directorsContainer.visibility = View.VISIBLE
                                binding.contents.directorsContainer.removeAllViews()
                            }
                            directors.forEach { director ->
                                val row = RowWithChevronView(requireContext())
                                row.setText(director.name)
                                binding.contents.directorsContainer.addView(row)
                            }
                        }
                        seriesDetails.genres?.let { genres ->
                            if (genres.isNotEmpty()) {
                                binding.contents.genres.visibility = View.VISIBLE
                                binding.contents.genresContainer.visibility = View.VISIBLE
                                binding.contents.genresContainer.removeAllViews()
                            }
                            genres.forEach { genre ->
                                val row = RowWithChevronView(requireContext())
                                row.setOnClickListener {
                                    val action = SeriesDetailsFragmentDirections.actionBrowseGenre(genre.name ?: "", genre)
                                    findNavController().navigate(action)
                                }
                                row.setText(genre.name)
                                binding.contents.genresContainer.addView(row)
                            }
                        }
                        val rows = listOf(seriesDetails.seasons, seriesDetails.similarItems)
                        if (adapter.currentList == listOf(rows)) {
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.submitList(rows)
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
