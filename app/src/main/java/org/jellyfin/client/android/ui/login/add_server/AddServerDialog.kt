package org.jellyfin.client.android.ui.login.add_server

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.android.support.DaggerDialogFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_SERVER_NAME
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_SERVER_URL
import org.jellyfin.client.android.ui.login.LoginViewModel
import javax.inject.Inject


class AddServerDialog : DaggerDialogFragment() {

    companion object {
        fun newInstance(serverName: String, serverUrl: String): AddServerDialog {
            val args = Bundle()
            args.putString(BUNDLE_SERVER_NAME, serverName)
            args.putString(BUNDLE_SERVER_URL, serverUrl)
            val fragment = AddServerDialog()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(LoginViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setTitle(R.string.add_server_dialog_title)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_server, null)
        alertDialogBuilder.setView(view)

        val serverName = view.findViewById<TextInputEditText>(R.id.txtServerName)
        val serverUrl = view.findViewById<TextInputEditText>(R.id.txtServerUrl)

        val btnOkay = view.findViewById<MaterialButton>(R.id.btnOkay)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        btnOkay.setOnClickListener {
            loginViewModel.addServer(
                serverUrl = serverUrl.text.toString(),
                serverName = serverName.text.toString()
            )
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return alertDialogBuilder.create()
    }

}