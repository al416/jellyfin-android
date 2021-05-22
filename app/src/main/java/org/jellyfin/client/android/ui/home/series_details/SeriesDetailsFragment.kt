package org.jellyfin.client.android.ui.home.series_details


import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentSeriesDetailsBinding
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import org.jellyfin.client.android.ui.player.PlayerActivity
import org.jellyfin.client.android.ui.shared.RowWithChevronView
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SeriesDetailsFragment : DaggerFragment() {

    private lateinit var binding: FragmentSeriesDetailsBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val seriesDetailsViewModel: SeriesDetailsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SeriesDetailsViewModel::class.java).apply {
            initialize(UUID.fromString(args.uuid))
        }
    }

    private val args: SeriesDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeriesDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contents.btnPlay.setOnClickListener {
            val intent = Intent(requireActivity(), PlayerActivity::class.java)
            intent.putExtra(Tags.BUNDLE_TAG_MEDIA_UUID, args.uuid)
            startActivity(intent)
        }

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val adapter = HomeRowRecyclerViewAdapter(requireActivity(), displayMetrics.widthPixels)
        binding.contents.seasonAdapter = adapter
        binding.contents.seasonsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

        seriesDetailsViewModel.getSeriesDetails().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {seriesDetails ->
                        binding.contents.series = seriesDetails
                        binding.contents.backdrop.load(seriesDetails.backdropUrl)
                        binding.contents.poster.load(seriesDetails.posterUrl)
                        binding.contents.overview.setText(getString(R.string.movie_details_item_overview), seriesDetails.overview)
                        seriesDetails.directors?.let { directors ->
                            if (directors.isNotEmpty()) {
                                binding.contents.directors.visibility = View.VISIBLE
                                binding.contents.directorsContainer.visibility = View.VISIBLE
                            }
                            directors.forEach {director ->
                                val row = RowWithChevronView(requireContext())
                                row.setText(director.name)
                                binding.contents.directorsContainer.addView(row)
                            }
                        }
                        seriesDetails.genres?.let { genres ->
                            if (genres.isNotEmpty()) {
                                binding.contents.genres.visibility = View.VISIBLE
                                binding.contents.genresContainer.visibility = View.VISIBLE
                            }
                            genres.forEach {genre ->
                                val row = RowWithChevronView(requireContext())
                                row.setText(genre.name)
                                binding.contents.genresContainer.addView(row)
                            }
                        }
                        seriesDetails.seasons?.let {row ->
                            if (adapter.currentList == listOf(row)) {
                                adapter.notifyDataSetChanged()
                            } else {
                                adapter.submitList(listOf(row))
                            }
                        }
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