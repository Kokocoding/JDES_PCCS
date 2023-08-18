package com.example.jdes_pccs

import java.io.DataOutputStream
import java.net.Socket

object SocketManager {
    private lateinit var socket: Socket

    fun connect(address: String, port: Int) {
        Thread {
            try {
                socket = Socket(address, port)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun sendCommand(data: ByteArray) {
        if (::socket.isInitialized && socket.isConnected) {
            Thread {
                try {
                    val outputStream = socket.getOutputStream()
                    val dataOutputStream = DataOutputStream(outputStream)
                    dataOutputStream.write(data)
                    dataOutputStream.flush()
                    outputStream.flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        } else {
            println("Socket not initialized or not connected")
        }
    }
}