package com.fxz.client.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun View.visible() { visibility = View.VISIBLE }
fun View.gone()    { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.fadeIn(duration: Long = 250) {
    alpha = 0f; visible()
    animate().alpha(1f).setDuration(duration).start()
}

fun View.fadeOut(duration: Long = 250, onEnd: (() -> Unit)? = null) {
    animate().alpha(0f).setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                gone(); onEnd?.invoke()
            }
        }).start()
}

fun View.slideUp(duration: Long = 300) {
    translationY = height.toFloat(); visible()
    animate().translationY(0f).setDuration(duration).start()
}

fun View.slideDown(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    animate().translationY(height.toFloat()).setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                gone(); onEnd?.invoke()
            }
        }).start()
}

fun View.pulse(scale: Float = 1.05f, duration: Long = 150) {
    animate().scaleX(scale).scaleY(scale).setDuration(duration)
        .withEndAction { animate().scaleX(1f).scaleY(1f).setDuration(duration).start() }
        .start()
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.toast(msg: String) = requireContext().toast(msg)

fun Long.toTimeAgo(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000L          -> "Just now"
        diff < 3_600_000L       -> "${diff / 60_000}m ago"
        diff < 86_400_000L      -> "${diff / 3_600_000}h ago"
        diff < 2_592_000_000L   -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
    }
}

fun Long.toPlayTimeString(): String {
    val hours   = this / 3_600_000
    val minutes = (this % 3_600_000) / 60_000
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else      -> "${minutes}m"
    }
}

fun String.isValidIpOrHost(): Boolean {
    if (isBlank()) return false
    // IPv4 pattern
    val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
    if (matches(ipRegex)) {
        return split(".").all { it.toIntOrNull() in 0..255 }
    }
    // Hostname pattern
    return matches(Regex("""^[a-zA-Z0-9][a-zA-Z0-9\-\.]{1,253}[a-zA-Z0-9]$"""))
}

fun Int.toPingColor(): Int = when {
    this < 0   -> 0xFF888888.toInt()
    this < 80  -> 0xFF00FF88.toInt()
    this < 150 -> 0xFFFFCC00.toInt()
    this < 300 -> 0xFFFF8800.toInt()
    else       -> 0xFFFF3333.toInt()
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return Math.round(this * multiplier) / multiplier
}
