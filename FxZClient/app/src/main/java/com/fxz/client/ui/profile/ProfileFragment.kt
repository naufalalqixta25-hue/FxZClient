package com.fxz.client.ui.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fxz.client.R
import com.fxz.client.data.model.PlayerProfile
import com.fxz.client.databinding.FragmentProfileBinding
import com.fxz.client.utils.Extensions.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val vm: ProfileViewModel by viewModels()
    private lateinit var adapter: ProfileAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProfileBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuickLogin()
        setupRecycler()
        setupFab()
        observeVm()
    }

    private fun setupQuickLogin() {
        b.etName.setText(vm.getSavedName())
        b.etPassword.setText(vm.getSavedPassword())
        b.btnSaveQuick.setOnClickListener {
            val name = b.etName.text?.toString()?.trim() ?: ""
            val pass = b.etPassword.text?.toString() ?: ""
            if (name.isEmpty()) { toast("Enter a name"); return@setOnClickListener }
            vm.saveQuick(name, pass)
        }
    }

    private fun setupRecycler() {
        adapter = ProfileAdapter(
            onSelect = { vm.setActive(it) },
            onDelete = { showDeleteConfirm(it) }
        )
        b.rvProfiles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun setupFab() {
        b.fabCreate.setOnClickListener { showCreateDialog() }
    }

    private fun observeVm() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                vm.profiles.collectLatest { list ->
                    adapter.submitList(list)
                    b.tvNoProfiles.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            launch {
                vm.activeProfile.collectLatest { p ->
                    b.tvActiveProfile.text = if (p != null) "Active: ${p.name}" else "No active profile"
                }
            }
            launch { vm.uiMessage.collect { toast(it) } }
            launch { vm.showCreateDialog.collect { showCreateDialog() } }
        }
    }

    private fun showCreateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_profile, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etProfileName)
        val etPass = dialogView.findViewById<TextInputEditText>(R.id.etProfilePassword)
        MaterialAlertDialogBuilder(requireContext(), R.style.FxZAlertDialog)
            .setTitle("Create Profile")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = etName.text?.toString()?.trim() ?: ""
                val pass = etPass.text?.toString() ?: ""
                vm.createProfile(name, pass)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirm(profile: PlayerProfile) {
        MaterialAlertDialogBuilder(requireContext(), R.style.FxZAlertDialog)
            .setTitle("Delete Profile")
            .setMessage("Delete \"${profile.name}\"?")
            .setPositiveButton("Delete") { _, _ -> vm.deleteProfile(profile) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
