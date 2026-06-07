package com.fxz.client.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fxz.client.data.model.PlayerProfile
import com.fxz.client.databinding.ItemProfileBinding
import com.fxz.client.utils.Extensions.toPlayTimeString

class ProfileAdapter(
    private val onSelect: (PlayerProfile) -> Unit,
    private val onDelete: (PlayerProfile) -> Unit
) : ListAdapter<PlayerProfile, ProfileAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemProfileBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: PlayerProfile) {
            b.tvName.text      = p.name
            b.tvPlayTime.text  = "Play time: ${p.totalPlayTime.toPlayTimeString()}"
            b.tvSessions.text  = "Sessions: ${p.serversJoined}"
            val activeColor = if (p.isActive) 0xFF00B4FF.toInt() else 0xFF555555.toInt()
            b.root.strokeColor = activeColor
            b.tvActive.visibility = if (p.isActive) android.view.View.VISIBLE else android.view.View.GONE
            b.btnSelect.setOnClickListener { onSelect(p) }
            b.btnDelete.setOnClickListener { onDelete(p) }
            b.root.setOnClickListener { onSelect(p) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemProfileBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PlayerProfile>() {
            override fun areItemsTheSame(a: PlayerProfile, b: PlayerProfile) = a.id == b.id
            override fun areContentsTheSame(a: PlayerProfile, b: PlayerProfile) = a == b
        }
    }
}
