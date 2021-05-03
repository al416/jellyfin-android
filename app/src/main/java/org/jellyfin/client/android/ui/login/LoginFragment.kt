package org.jellyfin.client.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.FragmentLoginBinding
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.ui.home.HomeActivity
import org.jellyfin.client.android.ui.login.adapter.ServerSpinnerAdapter
import javax.inject.Inject

class LoginFragment : DaggerFragment(), View.OnClickListener {

    private lateinit var binding: FragmentLoginBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener(this)
        binding.txtAddServer.setOnClickListener(this)

        loginViewModel.getLoginState().observe(viewLifecycleOwner, Observer {
            it?.let {resource ->
                when (resource.status) {
                    // Login successful so move to Home screen
                    Status.SUCCESS -> {
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        startActivity(intent)
                    }
                    // User is logging in so display loading indicator
                    Status.LOADING -> {
                        displayLoading()
                    }
                    // Login unsuccessful. Display error message
                    Status.ERROR -> {
                        binding.buttonLogin.isEnabled = true
                        displayError(it.messages)
                    }
                }
            }
        })

        loginViewModel.getServers().observe(viewLifecycleOwner, Observer {resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {
                        binding.serverAdapter = ServerSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
                    }
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonLogin -> {
                view.isEnabled = false
                val server = binding.spinnerServer.selectedItem as Server
                loginViewModel.doUserLogin(baseUrl = server.url,
                    username = binding.textUsername.text.toString(),
                    password = binding.textPassword.text.toString())
            }
            R.id.txtAddServer -> {
                val action = LoginFragmentDirections.actionAddServer()
                findNavController().navigate(action)
            }
        }
    }

    private fun displayError(errors: List<Error>?) {
        // TODO: Display a better error message or dialog. TBD.
        val error = errors?.firstOrNull()
        error?.let {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayLoading() {
        // TODO: A loading indicator will be displayed

    }

}