package org.jellyfin.client.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.load
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.databinding.FragmentRecentItemsBinding
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_POSITION
import org.jellyfin.client.android.domain.models.Status
import javax.inject.Inject

class RecentItemFragment : DaggerFragment() {

    companion object {
        fun newInstance(position: Int): RecentItemFragment {
            val bundle = bundleOf(BUNDLE_TAG_POSITION to position)
            val fragment = RecentItemFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val recentItemViewModel: RecentItemViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RecentItemViewModel::class.java)
    }

    private lateinit var binding: FragmentRecentItemsBinding

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = requireArguments().getInt(BUNDLE_TAG_POSITION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentItemViewModel.getRecentItems().observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val item = resource.data?.get(position)
                    item?.let {
                        binding.background.load(it.imageUrl)
                        binding.executePendingBindings()
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