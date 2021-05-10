package org.jellyfin.client.android.ui.login.add_server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentAddServerBinding
import org.jellyfin.client.android.domain.constants.Tags.DIALOG_ADD_SERVER
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.login.adapter.ServerRecyclerViewAdapter
import javax.inject.Inject

class AddServerFragment : DaggerFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddServerBinding

    private val args: AddServerFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val addServerViewModel: AddServerViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AddServerViewModel::class.java).apply {
            initialize(args.servers.servers)
        }
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

        binding.toolbar.setTitle(R.string.add_server_page_title)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.inflateMenu(R.menu.add_server_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> {
                    val servers = binding.adapter?.currentList ?: emptyList()
                    addServerViewModel.updateServers(servers)
                    true
                }
                else -> false
            }
        }

        binding.btnAddServer.setOnClickListener(this)
        val adapter = ServerRecyclerViewAdapter()
        binding.adapter = adapter

        adapter.onListChanged = {
            addServerViewModel.onListChanged(it)
        }

        val callback: ItemTouchHelper.Callback = ServerItemTouchHelper(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.serverRecyclerView)

        binding.serverRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.executePendingBindings()

        addServerViewModel.getServers().observe(viewLifecycleOwner, { servers ->
            displayServerList(servers.isNotEmpty())
            if (adapter.currentList == servers) {
                // A duplicate list has been submitted so the adapter won't check if the list has been changed so force a redraw
                adapter.notifyDataSetChanged()
            } else {
                adapter.submitList(servers)
            }
        })

        addServerViewModel.getAddServerStatus().observe(viewLifecycleOwner, { resource ->
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

        addServerViewModel.getUpdateServersStatus().observe(viewLifecycleOwner, { resource ->
            if (resource != null) {
                when (resource.status) {
                    Status.SUCCESS -> {
                        findNavController().popBackStack()
                    }
                    else -> {

                    }
                }
            }
        })

        addServerViewModel.getSaveState().observe(viewLifecycleOwner, {
            val saveItem = binding.toolbar.menu.findItem(R.id.save)
            saveItem.isVisible = it
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