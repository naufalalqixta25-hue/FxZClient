package com.fxz.client.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fxz.client.databinding.FragmentSettingsBinding
import com.fxz.client.utils.Extensions.toast
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private val vm: SettingsViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSettingsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFpsChips()
        setupThemeChips()
        setupSwitches()
        setupSliders()
        setupButtons()
        observeVm()
        populateDeviceInfo()
    }

    private fun setupFpsChips() {
        vm.fpsOptions.forEach { fps ->
            val chip = Chip(requireContext()).apply {
                text = "$fps"
                isCheckable = true
                setOnClickListener { vm.updateFpsLock(fps) }
            }
            b.chipGroupFps.addView(chip)
        }
    }

    private fun setupThemeChips() {
        vm.themes.forEach { theme ->
            val chip = Chip(requireContext()).apply {
                text = theme.name
                isCheckable = true
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(theme.color)
                setOnClickListener { vm.setTheme(theme.id) }
            }
            b.chipGroupTheme.addView(chip)
        }
    }

    private fun setupSwitches() {
        b.switchAntiLag.setOnCheckedChangeListener     { _, v -> vm.updateAntiLag(v) }
        b.switchGpuOpt.setOnCheckedChangeListener      { _, v -> vm.updateGpuOpt(v) }
        b.switchPerfMode.setOnCheckedChangeListener    { _, v -> vm.updatePerformanceMode(v) }
        b.switchBatterySaver.setOnCheckedChangeListener{ _, v -> vm.updateBatterySaver(v) }
        b.switchOverlay.setOnCheckedChangeListener     { _, v -> vm.updateOverlay(v) }
        b.switchVoiceChat.setOnCheckedChangeListener   { _, v -> vm.updateVoiceChat(v) }
        b.switchAudioEnhancer.setOnCheckedChangeListener{ _, v -> vm.updateAudioEnhancer(v) }
    }

    private fun setupSliders() {
        b.seekSensitivity.max = 100
        b.seekSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) {
                    val v = p / 50f
                    vm.updateSensitivity(v)
                    b.tvSensitivityVal.text = "%.1f".format(v)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun setupButtons() {
        b.btnBackupConfig.setOnClickListener  { vm.backupConfig() }
        b.btnRestoreConfig.setOnClickListener { vm.restoreConfig() }
        b.btnCleanCache.setOnClickListener    { vm.cleanCache() }
    }

    private fun observeVm() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                vm.config.collectLatest { cfg ->
                    b.switchAntiLag.isChecked      = cfg.antiLag
                    b.switchGpuOpt.isChecked       = cfg.gpuOptimization
                    b.switchPerfMode.isChecked     = cfg.performanceMode
                    b.switchBatterySaver.isChecked = cfg.batterySaverMode
                    b.switchOverlay.isChecked      = cfg.showFpsOverlay
                    b.switchVoiceChat.isChecked    = cfg.voiceChat
                    b.switchAudioEnhancer.isChecked= cfg.audioEnhancer
                    b.seekSensitivity.progress     = (cfg.sensitivity * 50).toInt()
                    b.tvSensitivityVal.text        = "%.1f".format(cfg.sensitivity)
                    // Check the correct FPS chip
                    for (i in 0 until b.chipGroupFps.childCount) {
                        val chip = b.chipGroupFps.getChildAt(i) as? Chip
                        chip?.isChecked = chip?.text?.toString()?.toIntOrNull() == cfg.fpsLock
                    }
                }
            }
            launch { vm.uiMessage.collect { toast(it) } }
        }
    }

    private fun populateDeviceInfo() {
        b.tvDeviceModel.text  = vm.deviceModel
        b.tvAndroidVer.text   = vm.androidVersion
        b.tvCpuAbi.text       = "ABI: ${vm.cpuAbi}"
        b.tvDeviceScore.text  = "Device Score: ${vm.getDeviceScore()}/100"
        b.tvRecommendedFps.text = "Recommended FPS: ${vm.getRecommendedFps()}"
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
