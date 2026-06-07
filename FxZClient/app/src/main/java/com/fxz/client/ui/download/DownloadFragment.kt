package com.fxz.client.ui.download

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fxz.client.R
import com.fxz.client.databinding.FragmentDownloadBinding
import com.fxz.client.utils.Extensions.gone
import com.fxz.client.utils.Extensions.toast
import com.fxz.client.utils.Extensions.visible
import com.fxz.client.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadFragment : Fragment() {

    private var _b: FragmentDownloadBinding? = null
    private val b get() = _b!!
    private val vm: DownloadViewModel by viewModels()
    @Inject lateinit var permissionUtils: PermissionUtils
    @Inject lateinit var storageUtils: com.fxz.client.utils.StorageUtils

    private lateinit var downloadAdapter: DownloadAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) vm.startFullSetup()
        else toast("Storage permission required")
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDownloadBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupButtons()
        observeVm()
    }

    private fun setupRecycler() {
        downloadAdapter = DownloadAdapter(
            storageUtils = storageUtils,
            onPause  = { vm.pauseDownload(it) },
            onResume = { vm.resumeDownload(it) },
            onCancel = { vm.cancelDownload(it) }
        )
        b.rvDownloads.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadAdapter
        }
    }

    private fun setupButtons() {
        b.btnStartDownload.setOnClickListener {
            if (permissionUtils.hasStoragePermission()) {
                vm.startFullSetup()
            } else {
                permissionUtils.requestStoragePermission(requireActivity())
            }
        }
        b.btnClearCompleted.setOnClickListener { vm.clearCompleted() }
        b.btnPlay.setOnClickListener {
            findNavController().navigate(R.id.serverBrowserFragment)
        }
        b.btnValidate.setOnClickListener {
            toast("Validating files…")
            lifecycleScope.launch {
                // re-check
            }
        }
    }

    private fun observeVm() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                vm.downloads.collectLatest { tasks ->
                    downloadAdapter.submitList(tasks)
                    b.rvDownloads.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
                }
            }
            launch {
                vm.isGameReady.collectLatest { ready ->
                    b.btnPlay.visibility          = if (ready) View.VISIBLE else View.GONE
                    b.btnStartDownload.visibility  = if (!ready) View.VISIBLE else View.GONE
                    b.ivGameReady.setImageResource(
                        if (ready) R.drawable.ic_check_circle else R.drawable.ic_download_cloud
                    )
                }
            }
            launch {
                vm.statusText.collectLatest { b.tvStatus.text = it }
            }
            launch {
                vm.phase.collectLatest { phase ->
                    b.progressSetup.visibility = when (phase) {
                        DownloadViewModel.SetupPhase.DOWNLOADING,
                        DownloadViewModel.SetupPhase.EXTRACTING -> View.VISIBLE
                        else -> View.GONE
                    }
                }
            }
            launch { vm.uiMessage.collect { toast(it) } }
        }
        b.tvStorageInfo.text = vm.getStorageInfo()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
