package org.jellyfin.client.android.ui.login.add_server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentAddServerBinding
import org.jellyfin.client.android.domain.constants.Tags.DIALOG_ADD_SERVER
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.login.LoginViewModel
import org.jellyfin.client.android.ui.login.adapter.ServerRecyclerViewAdapter
import javax.inject.Inject

class AddServerFragment : DaggerFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddServerBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(LoginViewModel::class.java)
    }

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddServer.setOnClickListener(this)
        val adapter = ServerRecyclerViewAdapter()
        binding.adapter = adapter

        val callback: ItemTouchHelper.Callback = ServerItemTouchHelper(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.serverRecyclerView)

        binding.serverRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.executePendingBindings()

        loginViewModel.getServers().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {servers ->
                        displayServerList(servers.isNotEmpty())
                        if (adapter.currentList == servers) {
                            // A duplicate list has been submitted so the adapter won't check if the list has been changed so force a redraw
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.submitList(servers)
                        }
                    }
                }
            }
        })

        loginViewModel.getAddServerStatus().observe(viewLifecycleOwner, { resource ->
            if (resource != null) {
                when (resource.status) {
                    Status.SUCCESS -> {
                        val dialog = childFragmentManager.findFragmentByTag(DIALOG_ADD_SERVER)
                        if (dialog is DialogFragment) {
                            dialog.dismiss()
                        }
                    }
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), resource.messages?.first()?.message, Toast.LENGTH_LONG).show()
                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddServer -> {
                AddServerDialog.newInstance("", "").show(childFragmentManager, DIALOG_ADD_SERVER)
            }
        }
    }

    private fun displayServerList(shouldDisplay: Boolean) {
        val visibility = if (shouldDisplay) View.VISIBLE else View.GONE
        binding.txtAvailableServers.visibility = visibility
        binding.serverRecyclerView.visibility = visibility
    }
}