package com.fxz.client.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fxz.client.R
import com.fxz.client.data.model.Server
import com.fxz.client.databinding.FragmentHomeBinding
import com.fxz.client.service.InGameOverlayService
import com.fxz.client.ui.server.ServerAdapter
import com.fxz.client.utils.Constants
import com.fxz.client.utils.Extensions.fadeIn
import com.fxz.client.utils.Extensions.gone
import com.fxz.client.utils.Extensions.toast
import com.fxz.client.utils.Extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var recentAdapter: ServerAdapter
    private lateinit var favAdapter: ServerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupAdapters()
        observeViewModel()
        startEnterAnimation()
    }

    private fun setupUI() {
        binding.btnPlay.setOnClickListener {
            if (viewModel.isGameReady.value) {
                navigateToLastServer()
            } else {
                findNavController().navigate(R.id.downloadFragment)
            }
        }

        binding.btnServerBrowser.setOnClickListener {
            findNavController().navigate(R.id.serverBrowserFragment)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        binding.cardLastServer.setOnClickListener {
            viewModel.lastServer.value?.let { s ->
                navigateToConnect(s)
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshDeviceStats()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupAdapters() {
        recentAdapter = ServerAdapter(
            onConnect = { server -> navigateToConnect(server) },
            onFavorite = { /* handled in browser */ }
        )
        favAdapter = ServerAdapter(
            onConnect = { server -> navigateToConnect(server) },
            onFavorite = { /* handled in browser */ }
        )

        binding.rvRecent.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
            itemAnimator = null
        }

        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = favAdapter
            itemAnimator = null
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.activeProfile.collectLatest { profile ->
                    if (profile != null) {
                        binding.tvPlayerName.text = profile.name
                        binding.tvPlayTime.text = "Play time: ${formatPlayTime(profile.totalPlayTime)}"
                    } else {
                        binding.tvPlayerName.text = "Guest"
                        binding.tvPlayTime.text = "Setup profile to play"
                    }
                }
            }
            launch {
                viewModel.recentServers.collectLatest { servers ->
                    recentAdapter.submitList(servers)
                    binding.groupRecent.visibility = if (servers.isEmpty()) View.GONE else View.VISIBLE
                }
            }
            launch {
                viewModel.favoriteServers.collectLatest { servers ->
                    favAdapter.submitList(servers)
                    binding.groupFavorites.visibility = if (servers.isEmpty()) View.GONE else View.VISIBLE
                }
            }
            launch {
                viewModel.isGameReady.collectLatest { ready ->
                    binding.btnPlay.text = if (ready) "▶  PLAY" else "⬇  DOWNLOAD GAME"
                    binding.tvGameStatus.text = if (ready) "Game ready" else "Download required"
                    val color = if (ready)
                        requireContext().getColor(R.color.neon_blue)
                    else
                        requireContext().getColor(R.color.neon_orange)
                    binding.tvGameStatus.setTextColor(color)
                }
            }
            launch {
                viewModel.deviceStats.collectLatest { stats ->
                    stats ?: return@collectLatest
                    binding.tvRamInfo.text = "RAM: ${stats.ramAvailable / 1_048_576}MB free"
                    binding.tvNetworkInfo.text = "Network: ${stats.networkType}"
                    binding.tvBattery.text = "Battery: ${stats.batteryLevel}%"
                    binding.progressRam.progress = ((1.0 - stats.ramAvailable.toDouble() / stats.ramTotal) * 100).toInt()
                }
            }
            launch {
                viewModel.uiMessage.collect { msg -> toast(msg) }
            }
        }
    }

    private fun startEnterAnimation() {
        binding.cardPlayer.fadeIn(300)
        binding.cardGame.apply { alpha = 0f; postDelayed({ fadeIn(300) }, 100) }
        binding.cardStats.apply { alpha = 0f; postDelayed({ fadeIn(300) }, 200) }
    }

    private fun navigateToLastServer() {
        val server = viewModel.lastServer.value
            ?: viewModel.recentServers.value.firstOrNull()
        if (server != null) navigateToConnect(server)
        else findNavController().navigate(R.id.serverBrowserFragment)
    }

    private fun navigateToConnect(server: Server) {
        val bundle = android.os.Bundle().apply {
            putParcelable("server", server)
        }
        findNavController().navigate(R.id.serverBrowserFragment, bundle)
    }

    private fun formatPlayTime(ms: Long): String {
        val h = ms / 3_600_000; val m = (ms % 3_600_000) / 60_000
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
