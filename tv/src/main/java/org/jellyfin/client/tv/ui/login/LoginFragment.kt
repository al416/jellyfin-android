package org.jellyfin.client.tv.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.models.display_model.ServerList
import org.jellyfin.client.tv.R
import org.jellyfin.client.tv.databinding.FragmentLoginBinding
import org.jellyfin.client.tv.ui.login.adapter.ServerSpinnerAdapter

class LoginFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val servers = listOf(Server(1, "Test1", "", 1),
            Server(2, "Test2", "", 2),
            Server(3, "Test3", "", 3),
            Server(4, "Test4", "", 4),
            Server(5, "Test4", "", 5))
        binding.btnAddAServer.setOnClickListener(this)
        binding.serverAdapter = ServerSpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            servers
        )
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddAServer -> {
                val servers = listOf(Server(1, "Test1", "", 1),
                    Server(2, "Test2", "", 2),
                    Server(3, "Test3", "", 3),
                    Server(4, "Test4", "", 4),
                    Server(5, "Test4", "", 5))
                val action = LoginFragmentDirections.actionAddServer(ServerList(servers))
                findNavController().navigate(action)
            }
        }
    }
}