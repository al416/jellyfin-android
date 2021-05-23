package org.jellyfin.client.android.ui.home.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.jellyfin.client.android.databinding.FragmentLibraryBinding
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_GENRE
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_LIBRARY
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.ui.home.adapter.PagedCardAdapter
import javax.inject.Inject

@ExperimentalCoroutinesApi
class LibraryFragment : DaggerFragment() {

    companion object {
        fun newInstance(library: Library, genre: Genre): LibraryFragment {
            val bundle = bundleOf(BUNDLE_LIBRARY to library, BUNDLE_GENRE to genre)
            val fragment = LibraryFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var binding: FragmentLibraryBinding

    private lateinit var library: Library

    private lateinit var genre: Genre

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val libraryViewModel: LibraryViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LibraryViewModel::class.java).apply {
            initialize(library, genre)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        library = requireArguments().getSerializable(BUNDLE_LIBRARY) as Library
        genre = requireArguments().getSerializable(BUNDLE_GENRE) as Genre
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PagedCardAdapter()
        binding.adapter = adapter

        binding.itemsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.executePendingBindings()

        libraryViewModel.getItems().observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch {
                adapter.submitData(it)
            }
        }
    }
}