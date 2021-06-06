package org.jellyfin.client.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentHomeBinding
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.RECENT_ITEM_AUTO_ROTATE_TIME_IN_SECONDS
import org.jellyfin.client.android.domain.constants.Constants.ASPECT_RATIO_16_9
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_MEDIA_UUID
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomeCardAction
import org.jellyfin.client.android.ui.home.adapter.HomeRecentItemsFragmentAdapter
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import org.jellyfin.client.android.ui.player.PlayerActivity
import org.jellyfin.client.android.ui.player.VlcPlayerActivity
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


class HomeFragment : DaggerFragment() {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    private var autoRotateTimer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecentItems()
        setupSections()
        loadData()
    }

    private fun setupSections() {
        binding.pullToRefresh.setOnRefreshListener {
            showLoading()
            homeViewModel.refresh(true)
        }

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val adapter = HomeRowRecyclerViewAdapter(requireActivity(), displayMetrics.widthPixels)
        binding.sectionAdapter = adapter
        binding.sectionsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.executePendingBindings()

        adapter.onCardClick = {
            val card = it
            if (card.homeCardAction == HomeCardAction.DETAILS) {
                if (card.itemType == ItemType.MOVIE) {
                    val action = HomeFragmentDirections.actionMovieDetails(card.title ?: "", card.uuid.toString())
                    findNavController().navigate(action)
                } else if (card.itemType == ItemType.SERIES) {
                    val action = HomeFragmentDirections.actionSeriesDetails(card.title ?: "", card.uuid.toString())
                    findNavController().navigate(action)
                }
            } else if (card.homeCardAction == HomeCardAction.PLAY) {
                val intent = Intent(requireActivity(), VlcPlayerActivity::class.java)
                intent.putExtra(BUNDLE_TAG_MEDIA_UUID, card.uuid.toString())
                startActivity(intent)
            }
        }
    }

    private fun loadData() {
        homeViewModel.getHomePage().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.rows.let { rows ->
                        val adapter = binding.sectionsRecyclerView.adapter as HomeRowRecyclerViewAdapter
                        if (adapter.currentList == rows) {
                            // A duplicate list has been submitted so the adapter won't check if the list has been changed so force a redraw
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.submitList(rows)
                        }
                    }
                    resource.data?.recentItems?.let { recentItems ->
                        if (recentItems.isNotEmpty()) {
                            setupTimer()
                        }
                        (binding.recentItemsViewPager.adapter as HomeRecentItemsFragmentAdapter).totalItems = recentItems.size
                        (binding.recentItemsViewPager.adapter as HomeRecentItemsFragmentAdapter).notifyDataSetChanged()
                        binding.recentItemsViewPager.visibility = View.VISIBLE
                        binding.tabLayout.visibility = View.VISIBLE
                    }
                    if (resource.messages?.isNotEmpty() == true) {
                        showErrorDialog()
                    }
                    showContent()
                }
                Status.LOADING -> {
                    showLoading()
                }
                Status.ERROR -> {
                    showContent()
                    showErrorDialog()
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
    }

    private fun showLoading() {
        binding.pullToRefresh.isRefreshing = false
        binding.pullToRefresh.isEnabled = false
        binding.loadingScreen.visibility = View.VISIBLE
    }

    private fun showContent() {
        binding.pullToRefresh.isEnabled = true
        binding.loadingScreen.visibility = View.GONE
    }

    private fun showErrorDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.home_fragment_load_failed_title))
        builder.setMessage(getString(R.string.home_fragment_load_failed_message))
        builder.setPositiveButton(R.string.home_fragment_load_failed_positive_label) { dialog, _ ->
            homeViewModel.clearErrors()
            homeViewModel.refresh(false)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.home_fragment_load_failed_negative_label) { dialog, _ ->
            homeViewModel.clearErrors()
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun setupTimer() {
        autoRotateTimer?.cancel()
        autoRotateTimer = null
        autoRotateTimer = fixedRateTimer(
            "Rotate recent items",
            true,
            RECENT_ITEM_AUTO_ROTATE_TIME_IN_SECONDS * 1000L,
            RECENT_ITEM_AUTO_ROTATE_TIME_IN_SECONDS * 1000L
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
