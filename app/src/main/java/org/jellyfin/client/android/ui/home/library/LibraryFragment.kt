package org.jellyfin.client.android.ui.home.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.jellyfin.client.android.databinding.FragmentLibraryBinding
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_GENRE
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_LIBRARY
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.ui.home.adapter.PagedCardAdapter
import org.jellyfin.client.android.ui.home.library_home.LibraryHomeFragmentDirections
import javax.inject.Inject

@ExperimentalCoroutinesApi
class LibraryFragment : DaggerFragment() {

    companion object {
        fun newInstance(library: Library?, genre: Genre): LibraryFragment {
            val bundle = bundleOf(BUNDLE_LIBRARY to library, BUNDLE_GENRE to genre)
            val fragment = LibraryFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: FragmentLibraryBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val args: LibraryFragmentArgs by navArgs()

    private var library: Library? = null

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
        genre = if (arguments?.containsKey(BUNDLE_GENRE) == true) {
            requireArguments().getSerializable(BUNDLE_GENRE) as Genre
        } else {
            args.genre
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PagedCardAdapter(requireContext())
        binding.adapter = adapter

        binding.itemsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.executePendingBindings()

        adapter.onCardClick = { card ->
            if (card.itemType == ItemType.MOVIE) {
                val action = LibraryHomeFragmentDirections.actionMovieDetails(card.title ?: "", card.uuid.toString())
                findNavController().navigate(action)
            } else if (card.itemType == ItemType.SERIES) {
                val action = LibraryHomeFragmentDirections.actionSeriesDetails(card.title ?: "", card.uuid.toString())
                findNavController().navigate(action)
            }
        }

        libraryViewModel.getItems().observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch {
                adapter.submitData(it)
            }
        }
    }
}
