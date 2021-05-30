package org.jellyfin.client.android.ui.home.season_details

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
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.EpisodeRowRecyclerViewAdapter
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SeasonDetailsFragment : DaggerFragment() {

    private lateinit var binding: FragmentSeasonDetailsBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val seasonDetailsViewModel: SeasonDetailsViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SeasonDetailsViewModel::class.java).apply {
            initialize(seriesId = UUID.fromString(args.seriesId), seasonId = UUID.fromString(args.seasonId))
        }
    }

    private val args: SeasonDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeasonDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EpisodeRowRecyclerViewAdapter(requireContext())
        binding.adapter = adapter
        binding.itemsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

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