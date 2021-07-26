package org.jellyfin.client.android.ui.home.library_home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.android.databinding.FragmentLibraryHomeBinding
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.LibraryFragmentAdapter
import javax.inject.Inject

@ExperimentalCoroutinesApi
class LibraryHomeFragment : DaggerFragment() {

    private var _binding: FragmentLibraryHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val args: LibraryHomeFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val libraryHomeViewModel: LibraryHomeViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LibraryHomeViewModel::class.java).apply {
            initialize(args.library)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the next and previous 1 item in memory so the list of items isn't loading when the user swipes to it
        binding.libraryItemsViewPager.offscreenPageLimit = 1

        binding.libraryItemsAdapter = LibraryFragmentAdapter(this, args.library, 0)
        binding.executePendingBindings()

        TabLayoutMediator(binding.tabLayout, binding.libraryItemsViewPager) { tab, position ->
            val title = libraryHomeViewModel.getGenres().value?.data?.get(position)?.name
            tab.text = title
        }.attach()

        libraryHomeViewModel.getGenres().observe(viewLifecycleOwner) {resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { genres ->
                        (binding.libraryItemsViewPager.adapter as LibraryFragmentAdapter).totalItems = genres.size
                        (binding.libraryItemsViewPager.adapter as LibraryFragmentAdapter).genres.clear()
                        (binding.libraryItemsViewPager.adapter as LibraryFragmentAdapter).genres.addAll(genres)
                        (binding.libraryItemsViewPager.adapter as LibraryFragmentAdapter).notifyDataSetChanged()
                    }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        }

    }
}
