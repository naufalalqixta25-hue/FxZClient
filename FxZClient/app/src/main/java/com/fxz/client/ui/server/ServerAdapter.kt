package com.fxz.client.ui.server

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fxz.client.R
import com.fxz.client.data.model.Server
import com.fxz.client.databinding.ItemServerBinding
import com.fxz.client.utils.Extensions.toPingColor

class ServerAdapter(
    private val onConnect: (Server) -> Unit,
    private val onFavorite: (Server) -> Unit,
    private val onLongClick: ((Server) -> Unit)? = null
) : ListAdapter<Server, ServerAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemServerBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(server: Server) {
            b.tvServerName.text = server.displayName
            b.tvMode.text       = server.mode.ifBlank { "Unknown" }
            b.tvPlayers.text    = "${server.players}/${server.maxPlayers}"
            b.tvAddress.text    = server.address

            // Ping color + text
            if (server.ping < 0) {
                b.tvPing.text      = "OFFLINE"
                b.tvPing.setTextColor(0xFFFF3333.toInt())
            } else {
                b.tvPing.text = "${server.ping}ms"
                b.tvPing.setTextColor(server.ping.toPingColor())
            }

            // Password lock icon
            b.ivLock.visibility = if (server.hasPassword) android.view.View.VISIBLE else android.view.View.GONE

            // Favorite toggle
            val favIcon = if (server.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            b.btnFavorite.setImageResource(favIcon)

            // Player bar fill
            b.progressPlayers.max      = server.maxPlayers.coerceAtLeast(1)
            b.progressPlayers.progress = server.players
            val barColor = when {
                server.playerRatio > 0.9f -> ContextCompat.getColor(b.root.context, R.color.neon_red)
                server.playerRatio > 0.6f -> ContextCompat.getColor(b.root.context, R.color.neon_orange)
                else                      -> ContextCompat.getColor(b.root.context, R.color.neon_blue)
            }
            b.progressPlayers.progressTintList = android.content.res.ColorStateList.valueOf(barColor)

            // Clicks
            b.root.setOnClickListener       { onConnect(server) }
            b.btnFavorite.setOnClickListener { onFavorite(server) }
            b.root.setOnLongClickListener   { onLongClick?.invoke(server); true }

            // Enter animation
            b.root.alpha = 0f
            b.root.animate().alpha(1f).setDuration(200).setStartDelay((bindingAdapterPosition * 30L).coerceAtMost(300)).start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemServerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Server>() {
            override fun areItemsTheSame(a: Server, b: Server) = a.id == b.id
            override fun areContentsTheSame(a: Server, b: Server) = a == b
        }
    }
}
