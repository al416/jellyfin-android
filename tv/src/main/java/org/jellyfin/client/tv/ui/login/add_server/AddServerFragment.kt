package org.jellyfin.client.tv.ui.login.add_server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.tv.R
import org.jellyfin.client.tv.databinding.FragmentAddServerBinding
import org.jellyfin.client.tv.ui.login.adapter.TvServerRecyclerViewAdapter

class AddServerFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddServerBinding

    private val args: AddServerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddServer.setOnClickListener(this)
        val adapter = TvServerRecyclerViewAdapter()
        binding.adapter = adapter

        adapter.onItemClicked = {
            displayDialog(it.id, it.name, it.url)
        }

        binding.serverRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)

        val servers = listOf(
            Server(1, "Test1", "", 1),
            Server(2, "Test2", "", 2),
            Server(3, "Test3", "", 3),
            Server(4, "Test4", "", 4),
            Server(5, "Test5", "", 5)
        )
        adapter.submitList(servers)
        adapter.notifyDataSetChanged()

        binding.executePendingBindings()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddServer -> {
                displayDialog(0, "", "")
            }
        }
    }

    private fun displayDialog(serverId: Int, serverName: String, serverUrl: String) {
        val dialog = childFragmentManager.findFragmentByTag(Tags.DIALOG_ADD_SERVER)
        // If the dialog is visible on the screen then do not display it again
        if (dialog is DialogFragment) {
            return
        }
        AddServerDialog.newInstance(serverId, serverName, serverUrl).show(childFragmentManager,
            Tags.DIALOG_ADD_SERVER
        )
    }
}