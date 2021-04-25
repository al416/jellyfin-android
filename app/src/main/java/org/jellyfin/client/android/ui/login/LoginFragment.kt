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
import com.google.android.material.textfield.TextInputEditText
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.home.HomeActivity
import javax.inject.Inject

class LoginFragment : DaggerFragment(), View.OnClickListener {

    private lateinit var textBaseUrl: TextInputEditText
    private lateinit var textUsername: TextInputEditText
    private lateinit var textPassword: TextInputEditText
    private lateinit var buttonLogin: Button

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginFragmentViewModel: LoginFragmentViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(LoginFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textBaseUrl = ViewCompat.requireViewById(view, R.id.textBaseUrl)
        textUsername = ViewCompat.requireViewById(view, R.id.textUsername)
        textPassword = ViewCompat.requireViewById(view, R.id.textPassword)

        buttonLogin = ViewCompat.requireViewById(view, R.id.buttonLogin)
        buttonLogin.setOnClickListener(this)

        loginFragmentViewModel.getLoginState().observe(viewLifecycleOwner, Observer {
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
                        buttonLogin.isEnabled = true
                        displayError(it.messages)
                    }
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonLogin -> {
                view.isEnabled = false
                loginFragmentViewModel.doUserLogin(baseUrl = textBaseUrl.text.toString(),
                    username = textUsername.text.toString(),
                    password = textPassword.text.toString())
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