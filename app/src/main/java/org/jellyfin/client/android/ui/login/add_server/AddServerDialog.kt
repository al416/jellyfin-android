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
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_SERVER_ID
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_SERVER_NAME
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_SERVER_URL
import javax.inject.Inject


class AddServerDialog : DaggerDialogFragment() {

    companion object {
        fun newInstance(serverId: Int, serverName: String, serverUrl: String): AddServerDialog {
            val args = Bundle()
            args.putInt(BUNDLE_SERVER_ID, serverId)
            args.putString(BUNDLE_SERVER_NAME, serverName)
            args.putString(BUNDLE_SERVER_URL, serverUrl)
            val fragment = AddServerDialog()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val addServerViewModel: AddServerViewModel by lazy {
        ViewModelProvider(requireParentFragment(), viewModelFactory).get(AddServerViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val serverId = requireArguments().getInt(BUNDLE_SERVER_ID)
        val serverName = requireArguments().getString(BUNDLE_SERVER_NAME)
        val serverUrl = requireArguments().getString(BUNDLE_SERVER_URL)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        val title = if (serverId == 0) R.string.add_server_dialog_title_add else R.string.add_server_dialog_title_edit
        alertDialogBuilder.setTitle(title)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_server, null)
        alertDialogBuilder.setView(view)

        val tvServerName = view.findViewById<TextInputEditText>(R.id.txtServerName)
        val tvServerUrl = view.findViewById<TextInputEditText>(R.id.txtServerUrl)

        tvServerName.setText(serverName)
        tvServerUrl.setText(serverUrl)

        val btnOkay = view.findViewById<MaterialButton>(R.id.btnOkay)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        btnOkay.setOnClickListener {
            addServerViewModel.addServer(
                serverId = serverId,
                serverUrl = tvServerUrl.text.toString(),
                serverName = tvServerName.text.toString()
            )
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return alertDialogBuilder.create()
    }

}