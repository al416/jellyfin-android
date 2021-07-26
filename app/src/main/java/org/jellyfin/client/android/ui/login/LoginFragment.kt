package org.jellyfin.client.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.progress_bar.view.*
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentLoginBinding
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.models.display_model.ServerList
import org.jellyfin.client.android.ui.home.HomeActivity
import org.jellyfin.client.android.ui.login.adapter.ServerSpinnerAdapter
import javax.inject.Inject

class LoginFragment : DaggerFragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener(this)
        binding.btnAddAServer.setOnClickListener(this)

        loginViewModel.getLoginState().observe(viewLifecycleOwner, {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                    }
                    // User is logging in so display loading indicator
                    Status.LOADING -> {
                        displayLoading()
                    }
                    // Login unsuccessful. Display error message
                    Status.ERROR -> {
                        displayError(it.messages)
                        loginViewModel.resetLoginState()
                    }
                }
            }
        })

        loginViewModel.getServers().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {
                        binding.serverAdapter = ServerSpinnerAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            it
                        )
                        displayLoginPage(it.isNotEmpty())
                        val text = if (it.isEmpty()) R.string.login_fragment_add_a_server_text else R.string.login_fragment_manage_servers_text
                        binding.btnAddAServer.text = getString(text)
                        binding.btnAddAServer.visibility = View.VISIBLE
                    }
                }
                else -> {
                    // Do nothing
                }
            }
        })

        loginViewModel.getCurrentSession().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                else -> {
                    // Do nothing
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogin -> {
                view.isEnabled = false
                val server = binding.spinnerServer.selectedItem as Server
                loginViewModel.doUserLogin(
                    server = server,
                    username = binding.textUsername.text.toString(),
                    password = binding.textPassword.text.toString()
                )
            }
            R.id.btnAddAServer -> {
                val servers = loginViewModel.getServers().value?.data ?: emptyList()
                val action = LoginFragmentDirections.actionAddServer(ServerList(servers))
                findNavController().navigate(action)
            }
        }
    }

    private fun displayError(errors: List<Error>?) {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.spinnerServer.isEnabled = true
        binding.btnAddAServer.isEnabled = true

        val error = errors?.firstOrNull()
        if (error != null) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.login_fragment_error_dialog_title))
            builder.setMessage(error.message)
            builder.setPositiveButton(R.string.login_fragment_error_dialog_ok) { dialog, _ ->
                binding.textUsername.isEnabled = true
                binding.textPassword.isEnabled = true
                dialog.dismiss()
            }
            builder.setCancelable(false)
            builder.show()
        } else {
            binding.textUsername.isEnabled = true
            binding.textPassword.isEnabled = true
        }
    }

    private fun displayLoading() {
        binding.progressBar.tvLabel.text = getString(R.string.login_fragment_progress_text)
        binding.progressBar.visibility = View.VISIBLE
        binding.textUsername.isEnabled = false
        binding.textPassword.isEnabled = false
        binding.btnLogin.isEnabled = false
        binding.spinnerServer.isEnabled = false
        binding.btnAddAServer.isEnabled = false
    }

    private fun displayLoginPage(shouldDisplay: Boolean) {
        val visibility = if (shouldDisplay) View.VISIBLE else View.GONE
        binding.textInputLayoutUsername.visibility = visibility
        binding.textInputLayoutPassword.visibility = visibility
        binding.btnLogin.visibility = visibility
        binding.txtServer.visibility = visibility
        binding.spinnerServer.visibility = visibility
    }
}
