package org.jellyfin.client.android.ui.home.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import org.jellyfin.client.android.databinding.FragmentLibraryBinding
import org.jellyfin.client.android.ui.home.adapter.PagedCardAdapter
import javax.inject.Inject

class LibraryFragment : DaggerFragment() {

    private lateinit var binding: FragmentLibraryBinding

    private val args: LibraryFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val libraryViewModel: LibraryViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LibraryViewModel::class.java).apply {
            initialize(args.library)
        }
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