package com.fxz.client.data.remote

import com.fxz.client.data.model.SampServerInfo
import com.fxz.client.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * SA:MP UDP Query Protocol Implementation
 * Opcode 'i' = Server Info | 'r' = Rules | 'c' = Client List | 'p' = Ping
 */
object SampQueryProtocol {

    suspend fun queryServer(ip: String, port: Int): SampServerInfo? =
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(Constants.SAMP_TIMEOUT_MS.toLong()) {
                try {
                    val address = InetAddress.getByName(ip)
                    val socket = DatagramSocket().apply {
                        soTimeout = Constants.SAMP_TIMEOUT_MS
                    }

                    val pingStart = System.currentTimeMillis()
                    val packet = buildQueryPacket(ip, port, Constants.SAMP_QUERY_INFO)
                    socket.send(DatagramPacket(packet, packet.size, address, port))

                    val buf = ByteArray(2048)
                    val response = DatagramPacket(buf, buf.size)
                    socket.receive(response)
                    val ping = (System.currentTimeMillis() - pingStart).toInt()

                    socket.close()
                    parseInfoResponse(response.data, response.length, ip, port, ping)
                } catch (e: Exception) { null }
            }
        }

    suspend fun pingServer(ip: String, port: Int): Int =
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(Constants.SAMP_TIMEOUT_MS.toLong()) {
                try {
                    val address = InetAddress.getByName(ip)
                    val socket = DatagramSocket().apply { soTimeout = Constants.SAMP_TIMEOUT_MS }
                    val packet = buildQueryPacket(ip, port, Constants.SAMP_QUERY_PING)
                    val start = System.currentTimeMillis()
                    socket.send(DatagramPacket(packet, packet.size, address, port))
                    val buf = ByteArray(64)
                    socket.receive(DatagramPacket(buf, buf.size))
                    socket.close()
                    (System.currentTimeMillis() - start).toInt()
                } catch (e: Exception) { -1 }
            } ?: -1
        }

    private fun buildQueryPacket(ip: String, port: Int, opcode: Char): ByteArray {
        val buf = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
        // Magic "SAMP"
        buf.put('S'.code.toByte())
        buf.put('A'.code.toByte())
        buf.put('M'.code.toByte())
        buf.put('P'.code.toByte())
        // IP octets
        ip.split(".").forEach { buf.put(it.toInt().toByte()) }
        // Port
        buf.putShort(port.toShort())
        // Opcode
        buf.put(opcode.code.toByte())
        return buf.array()
    }

    private fun parseInfoResponse(
        data: ByteArray, len: Int,
        ip: String, port: Int, ping: Int
    ): SampServerInfo? {
        if (len < 11) return null
        return try {
            val buf = ByteBuffer.wrap(data, 0, len).order(ByteOrder.LITTLE_ENDIAN)
            buf.position(11) // skip header

            val hasPassword = buf.get().toInt() == 1
            val players    = buf.short.toInt()
            val maxPlayers = buf.short.toInt()

            fun readString(): String {
                val strLen = buf.int
                if (strLen <= 0 || strLen > 512) return ""
                val bytes = ByteArray(strLen)
                buf.get(bytes)
                return String(bytes, Charsets.UTF_8)
            }

            val serverName = readString()
            val gameMode   = readString()
            val mapName    = readString()
            val language   = readString()

            SampServerInfo(
                ip = ip, port = port, name = serverName,
                mode = gameMode, map = mapName, language = language,
                players = players, maxPlayers = maxPlayers,
                hasPassword = hasPassword, ping = ping
            )
        } catch (e: Exception) { null }
    }
}
