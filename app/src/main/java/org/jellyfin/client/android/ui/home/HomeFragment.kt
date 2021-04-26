package org.jellyfin.client.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter
import javax.inject.Inject


class HomeFragment : DaggerFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var homeRowAdapter: RecyclerView.Adapter<HomeRowRecyclerViewAdapter.RowViewHolder>

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeFragmentViewModel.getHomeContents().observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {
                        homeRowAdapter = HomeRowRecyclerViewAdapter(it, requireActivity())

                        recyclerView = ViewCompat.requireViewById(view, R.id.recyclerView)
                        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
                        recyclerView.adapter = homeRowAdapter
                        homeRowAdapter.notifyDataSetChanged()
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