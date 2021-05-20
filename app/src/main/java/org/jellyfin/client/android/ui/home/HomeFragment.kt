package org.jellyfin.client.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.databinding.FragmentHomeBinding
import org.jellyfin.client.android.domain.constants.Constants.ASPECT_RATIO_16_9
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_MEDIA_UUID
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomeCardAction
import org.jellyfin.client.android.ui.home.adapter.HomeRecentItemsFragmentAdapter
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import org.jellyfin.client.android.ui.player.PlayerActivity
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


class HomeFragment : DaggerFragment() {

    companion object {
        private const val RECENT_ITEM_AUTO_ROTATE_TIME_IN_SECOND = 10
    }

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    private val recentItemViewModel: RecentItemViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RecentItemViewModel::class.java)
    }

    private var autoRotateTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecentItems()
        setupSections()
    }

    private fun setupSections() {
        binding.pullToRefresh.setOnRefreshListener {
            showLoading()
            homeViewModel.refresh()
        }

        val adapter = HomeRowRecyclerViewAdapter(requireActivity())
        binding.sectionAdapter = adapter
        binding.sectionsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

        adapter.onCardClick = {
            val card = it
            if (card.homeCardAction == HomeCardAction.DETAILS) {
                val action = HomeFragmentDirections.actionMovieDetails(card.uuid.toString())
                findNavController().navigate(action)
            } else if (card.homeCardAction == HomeCardAction.PLAY) {
                val intent = Intent(requireActivity(), PlayerActivity::class.java)
                intent.putExtra(BUNDLE_TAG_MEDIA_UUID, card.uuid.toString())
                startActivity(intent)
            }
        }

        homeViewModel.getRows().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.pullToRefresh.isRefreshing = false
                    binding.contentView.visibility = View.VISIBLE
                    binding.statusView.visibility = View.GONE
                    resource.data?.let { rows ->
                        if (adapter.currentList == rows) {
                            // A duplicate list has been submitted so the adapter won't check if the list has been changed so force a redraw
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.submitList(rows)
                        }
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), resource.messages?.first()?.message, Toast.LENGTH_SHORT).show()
                    showError()
                }
                Status.LOADING -> {
                    showLoading()
                }
            }
        })
    }

    private fun setupRecentItems() {
        val displayMetrics = requireActivity().resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels
        val dpHeight = (dpWidth / ASPECT_RATIO_16_9).toInt()
        // Load the next and previous 2 items in memory so the recent items backdrop isn't loading when user swipes to it
        binding.recentItemsViewPager.offscreenPageLimit = 2
        binding.recentItemsViewPager.layoutParams =
            LinearLayout.LayoutParams(displayMetrics.widthPixels, dpHeight)

        binding.recentItemsAdapter = HomeRecentItemsFragmentAdapter(this, 0)
        binding.executePendingBindings()

        TabLayoutMediator(binding.tabLayout, binding.recentItemsViewPager) { tab, position ->
        }.attach()

        recentItemViewModel.getRecentItems().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {recentItems ->
                        if (recentItems.isNotEmpty()) {
                            setupTimer()
                        }
                        (binding.recentItemsViewPager.adapter as HomeRecentItemsFragmentAdapter).totalItems = recentItems.size
                        (binding.recentItemsViewPager.adapter as HomeRecentItemsFragmentAdapter).notifyDataSetChanged()
                        binding.recentItemsViewPager.visibility = View.VISIBLE
                        binding.tabLayout.visibility = View.VISIBLE
                    }
                }
                Status.ERROR -> {
                    showError()
                }
                Status.LOADING -> {
                    showLoading()
                }
            }
        })
    }

    private fun showLoading() {
        binding.contentView.visibility = View.GONE
        binding.statusView.visibility = View.VISIBLE
        binding.statusView.setStatusText("")
        binding.pullToRefresh.isRefreshing = true
    }

    private fun showError() {
        binding.contentView.visibility = View.GONE
        binding.statusView.visibility = View.VISIBLE
        binding.statusView.setStatusText("Error")
        binding.statusView.isRefreshing = false
    }

    private fun setupTimer() {
        autoRotateTimer?.cancel()
        autoRotateTimer = null
        autoRotateTimer = fixedRateTimer(
            "Rotate recent items",
            true,
            0L,
            RECENT_ITEM_AUTO_ROTATE_TIME_IN_SECOND * 1000L
        ) {
            requireActivity().runOnUiThread {
                val itemCount = binding.recentItemsViewPager.adapter?.itemCount ?: 0
                if (itemCount != 0) {
                    var scrollTo = binding.recentItemsViewPager.currentItem++
                    if (scrollTo == itemCount - 1) {
                        scrollTo = 0
                        binding.recentItemsViewPager.currentItem = 0
                    }
                    binding.recentItemsViewPager.viewTreeObserver.addOnGlobalLayoutListener {
                        ViewTreeObserver.OnGlobalLayoutListener {
                            binding.recentItemsViewPager.setCurrentItem(scrollTo, true)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        autoRotateTimer?.cancel()
    }
}