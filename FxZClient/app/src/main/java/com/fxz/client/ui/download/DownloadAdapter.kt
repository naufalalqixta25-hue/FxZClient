package com.fxz.client.ui.download

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fxz.client.data.model.DownloadStatus
import com.fxz.client.data.model.DownloadTask
import com.fxz.client.databinding.ItemDownloadBinding
import com.fxz.client.utils.StorageUtils

class DownloadAdapter(
    private val storageUtils: StorageUtils,
    private val onPause: (String) -> Unit,
    private val onResume: (String) -> Unit,
    private val onCancel: (String) -> Unit
) : ListAdapter<DownloadTask, DownloadAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemDownloadBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: DownloadTask) {
            b.tvName.text     = t.name
            b.tvProgress.text = "${t.progress}%"
            b.tvSize.text     = "${storageUtils.formatBytes(t.downloadedBytes)} / ${storageUtils.formatBytes(t.totalBytes)}"
            b.tvSpeed.text    = if (t.isRunning) storageUtils.formatSpeed(t.speed) else ""
            b.progressBar.progress = t.progress

            val statusColor = when (t.status) {
                DownloadStatus.COMPLETED  -> 0xFF00FF88.toInt()
                DownloadStatus.FAILED     -> 0xFFFF3355.toInt()
                DownloadStatus.PAUSED     -> 0xFFFFCC00.toInt()
                DownloadStatus.DOWNLOADING -> 0xFF00B4FF.toInt()
                else -> 0xFF888888.toInt()
            }
            b.tvStatus.text = t.status.name
            b.tvStatus.setTextColor(statusColor)

            b.btnPauseResume.visibility = if (t.isRunning || t.status == DownloadStatus.PAUSED)
                android.view.View.VISIBLE else android.view.View.GONE
            b.btnCancel.visibility = if (!t.isComplete) android.view.View.VISIBLE else android.view.View.GONE

            b.btnPauseResume.text = if (t.isRunning) "⏸" else "▶"
            b.btnPauseResume.setOnClickListener {
                if (t.isRunning) onPause(t.id) else onResume(t.id)
            }
            b.btnCancel.setOnClickListener { onCancel(t.id) }

            if (t.error != null) {
                b.tvError.text = "Error: ${t.error}"
                b.tvError.visibility = android.view.View.VISIBLE
            } else b.tvError.visibility = android.view.View.GONE
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemDownloadBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DownloadTask>() {
            override fun areItemsTheSame(a: DownloadTask, b: DownloadTask) = a.id == b.id
            override fun areContentsTheSame(a: DownloadTask, b: DownloadTask) = a == b
        }
    }
}
