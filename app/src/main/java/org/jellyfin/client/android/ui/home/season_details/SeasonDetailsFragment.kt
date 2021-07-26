package org.jellyfin.client.android.ui.home.season_details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.android.databinding.FragmentSeasonDetailsBinding
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.EpisodeRowRecyclerViewAdapter
import org.jellyfin.client.android.ui.player.PlayerActivity
import org.jellyfin.client.android.ui.player.VlcPlayerActivity
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SeasonDetailsFragment : DaggerFragment() {

    private var _binding: FragmentSeasonDetailsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val seasonDetailsViewModel: SeasonDetailsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SeasonDetailsViewModel::class.java).apply {
            initialize(seriesId = UUID.fromString(args.seriesId), seasonId = UUID.fromString(args.seasonId))
        }
    }

    private val args: SeasonDetailsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeasonDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EpisodeRowRecyclerViewAdapter(requireContext())
        binding.adapter = adapter
        binding.itemsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

        adapter.onCardClick = {episode ->
            val intent = Intent(requireActivity(), VlcPlayerActivity::class.java)
            intent.putExtra(Tags.BUNDLE_TAG_MEDIA_UUID, episode.episodeId.toString())
            startActivity(intent)
        }

        seasonDetailsViewModel.getSeasonDetails().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    if (adapter.currentList == resource.data) {
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.submitList(resource.data)
                    }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })
    }
}
