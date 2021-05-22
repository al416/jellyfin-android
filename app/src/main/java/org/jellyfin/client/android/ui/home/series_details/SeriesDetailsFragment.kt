package org.jellyfin.client.android.ui.home.series_details


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentSeriesDetailsBinding
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.player.PlayerActivity
import java.util.*
import javax.inject.Inject

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

        seriesDetailsViewModel.getSeriesDetails().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {seriesDetails ->
                        binding.contents.series = seriesDetails
                        binding.contents.backdrop.load(seriesDetails.backdropUrl)
                        binding.contents.poster.load(seriesDetails.posterUrl)
                        binding.contents.overview.setText(getString(R.string.movie_details_item_overview), seriesDetails.overview)
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