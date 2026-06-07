package com.fxz.client.ui.server

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fxz.client.R
import com.fxz.client.data.model.Server
import com.fxz.client.databinding.FragmentServerBrowserBinding
import com.fxz.client.service.InGameOverlayService
import com.fxz.client.ui.game.GameLaunchActivity
import com.fxz.client.utils.Constants
import com.fxz.client.utils.Extensions.gone
import com.fxz.client.utils.Extensions.toast
import com.fxz.client.utils.Extensions.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ServerBrowserFragment : Fragment() {

    private var _b: FragmentServerBrowserBinding? = null
    private val b get() = _b!!
    private val vm: ServerBrowserViewModel by viewModels()
    @Inject lateinit var prefs: SharedPreferences

    private lateinit var serverAdapter: ServerAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentServerBrowserBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupSearch()
        setupFab()
        setupChips()
        observeVm()

        // Auto-connect if arriving from home with a server arg
        arguments?.getParcelable<Server>("server")?.let { launchGame(it) }
    }

    private fun setupRecycler() {
        serverAdapter = ServerAdapter(
            onConnect   = { launchGame(it) },
            onFavorite  = { vm.toggleFavorite(it) },
            onLongClick = { showServerOptions(it) }
        )
        b.rvServers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serverAdapter
            setHasFixedSize(true)
        }
        b.swipeRefresh.setOnRefreshListener { vm.refreshAll() }
    }

    private fun setupSearch() {
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { vm.setSearch(s?.toString() ?: "") }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b2: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b2: Int, c: Int) {}
        })
    }

    private fun setupFab() {
        b.fabAddServer.setOnClickListener { showAddServerDialog() }
    }

    private fun setupChips() {
        b.chipPlayers.setOnClickListener { vm.setSortMode(ServerBrowserViewModel.SortMode.PLAYERS) }
        b.chipPing.setOnClickListener    { vm.setSortMode(ServerBrowserViewModel.SortMode.PING) }
        b.chipName.setOnClickListener    { vm.setSortMode(ServerBrowserViewModel.SortMode.NAME) }
        b.chipFav.setOnClickListener     { vm.setSortMode(ServerBrowserViewModel.SortMode.FAVORITE) }
    }

    private fun observeVm() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                vm.filteredServers.collectLatest { servers ->
                    serverAdapter.submitList(servers)
                    b.tvEmpty.visibility = if (servers.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            launch {
                vm.isRefreshing.collectLatest { b.swipeRefresh.isRefreshing = it }
            }
            launch {
                vm.uiMessage.collect { toast(it) }
            }
        }
    }

    private fun showAddServerDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.FxZBottomSheet)
        val dView = layoutInflater.inflate(R.layout.bottom_sheet_add_server, null)
        dialog.setContentView(dView)

        val etIp   = dView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etServerIp)
        val etPort = dView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etServerPort)
        val btnAdd = dView.findViewById<android.widget.Button>(R.id.btnAddServer)

        btnAdd.setOnClickListener {
            val ip   = etIp.text?.toString()?.trim() ?: ""
            val port = etPort.text?.toString()?.toIntOrNull() ?: 7777
            if (ip.isBlank()) { toast("Enter server IP"); return@setOnClickListener }
            vm.addServer(ip, port)
            toast("Adding $ip:$port…")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showServerOptions(server: Server) {
        val options = arrayOf(
            "Connect", "Ping", if (server.isFavorite) "Remove Favorite" else "Add Favorite",
            "Copy Address", "Delete"
        )
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.FxZAlertDialog)
            .setTitle(server.displayName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchGame(server)
                    1 -> vm.pingServer(server)
                    2 -> vm.toggleFavorite(server)
                    3 -> {
                        val cm = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("server", server.address))
                        toast("Copied: ${server.address}")
                    }
                    4 -> vm.deleteServer(server)
                }
            }.show()
    }

    private fun launchGame(server: Server) {
        val playerName = prefs.getString(Constants.PREF_PLAYER_NAME, "")?.trim() ?: ""
        if (playerName.isEmpty()) {
            toast("Set your player name in Profile first")
            findNavController().navigate(R.id.profileFragment)
            return
        }
        if (server.hasPassword) {
            showPasswordDialog(server, playerName)
        } else {
            startGame(server, playerName, "")
        }
    }

    private fun showPasswordDialog(server: Server, playerName: String) {
        val input = com.google.android.material.textfield.TextInputEditText(requireContext())
        input.hint = "Server Password"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.FxZAlertDialog)
            .setTitle("Password Required")
            .setView(input)
            .setPositiveButton("Connect") { _, _ ->
                startGame(server, playerName, input.text?.toString() ?: "")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startGame(server: Server, playerName: String, password: String) {
        val intent = Intent(requireContext(), GameLaunchActivity::class.java).apply {
            putExtra(Constants.EXTRA_SERVER_IP,   server.ip)
            putExtra(Constants.EXTRA_SERVER_PORT, server.port)
            putExtra(Constants.EXTRA_SERVER_NAME, server.displayName)
            putExtra(Constants.EXTRA_PASSWORD,    password)
            putExtra("player_name",               playerName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
