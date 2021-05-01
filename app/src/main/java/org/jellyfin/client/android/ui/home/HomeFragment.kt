package org.jellyfin.client.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.databinding.FragmentHomeBinding
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_MEDIA_UUID
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.HomeRecentItemsFragmentAdapter
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import org.jellyfin.client.android.ui.player.PlayerActivity
import javax.inject.Inject


class HomeFragment : DaggerFragment() {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    private val recentItemViewModel: RecentItemViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RecentItemViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecentItems()
        setupSections()
    }

    private fun setupSections() {
        val adapter = HomeRowRecyclerViewAdapter(requireActivity())
        binding.sectionAdapter = adapter
        binding.sectionsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

        adapter.onCardClick = {
            val card = it
            val intent = Intent(requireActivity(), PlayerActivity::class.java)
            intent.putExtra(BUNDLE_TAG_MEDIA_UUID, card.uuid.toString())
            startActivity(intent)
        }

        homeViewModel.getRows().observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {rows ->
                        if (adapter.currentList == rows) {
                            // A duplicate list has been submitted so the adapter won't check if the list has been changed so force a redraw
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.submitList(rows)
                        }
                    }
                }
                // TODO: Display error message
                Status.ERROR -> {

                }
                // TODO: Display loading indicator
                Status.LOADING -> {

                }
            }
        })
    }

    private fun setupRecentItems() {
        binding.recentItemsAdapter = HomeRecentItemsFragmentAdapter(this, 0)
        binding.executePendingBindings()

        TabLayoutMediator(binding.tabLayout, binding.recentItemsViewPager) { tab, position ->
        }.attach()

        recentItemViewModel.getRecentItems().observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {recentItems ->
                        (binding.recentItemsViewPager.adapter as HomeRecentItemsFragmentAdapter).totalItems = recentItems.size
                        (binding.recentItemsViewPager.adapter as HomeRecentItemsFragmentAdapter).notifyDataSetChanged()
                    }
                }
                // TODO: Display error message
                Status.ERROR -> {

                }
                // TODO: Display loading indicator
                Status.LOADING -> {

                }
            }
        })
    }
}