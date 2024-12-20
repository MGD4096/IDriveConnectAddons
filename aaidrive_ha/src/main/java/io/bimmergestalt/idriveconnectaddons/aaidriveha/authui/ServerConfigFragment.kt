package io.bimmergestalt.idriveconnectaddons.aaidriveha.authui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.bimmergestalt.idriveconnectaddons.aaidriveha.OauthAccess
import io.bimmergestalt.idriveconnectaddons.aaidriveha.R
import io.bimmergestalt.idriveconnectaddons.aaidriveha.authui.ServerConfigBinding
import io.bimmergestalt.idriveconnectaddons.aaidriveha.data.ServerConfigPersistence

class ServerConfigFragment: Fragment() {
    val viewModel by activityViewModels<ServerConfigViewModel>()
    val oauthAccess by lazy { OauthAccess(requireContext(), viewModel.serverConfig.authState) {
        // trigger the LiveData to update with the authState, even if it's the same
        viewModel.serverConfig.authState = it
    } }
    val serverConfigPersistence by lazy { ServerConfigPersistence(requireContext(), lifecycleScope) }
    val controller by lazy {ServerConfigController(lifecycleScope, viewModel.serverConfig, oauthAccess)}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        serverConfigPersistence.load()
        serverConfigPersistence.startSaving()
        viewModel.isAuthorized.observe(viewLifecycleOwner) {
            oauthAccess.tryRefreshToken()
        }

        val binding = ServerConfigBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.controller = controller
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        oauthAccess.handleAuthorizationResponse(requireActivity().intent)
        view.findViewById<EditText>(R.id.txt_instance_url).setOnEditorActionListener { v, actionId, event ->
            // couldn't figure out databinding syntax
            // android:onEditorAction="@{(view, actionId, event) -> controller.startLogin()}"
            controller.startLogin()
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        oauthAccess.dispose()
    }
}