package org.jellyfin.client.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.databinding.FragmentHomeBinding
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import javax.inject.Inject


class HomeFragment : DaggerFragment() {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val homeFragmentViewModel: HomeFragmentViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeFragmentViewModel::class.java)
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

        val adapter = HomeRowRecyclerViewAdapter(requireActivity())
        binding.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        homeFragmentViewModel.getRows().observe(viewLifecycleOwner, Observer { resource ->
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
}