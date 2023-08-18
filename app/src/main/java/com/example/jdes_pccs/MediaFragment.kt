package com.example.jdes_pccs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import java.io.DataOutputStream
import java.net.Socket

class MediaFragment: Fragment(R.layout.fragment_media) {

    private val buttonIds = arrayOf(R.id.button, R.id.button2, R.id.button3, R.id.button4)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener {
                //data組合
                val byte2 = index+1 // 将标签转换为您需要的数据类型
                val byte3 = byte2.toByte() + 0x09.toByte()
                val byte4 = 0x09.toByte() + (byte2*2).toByte()
                val data = byteArrayOf(0x7A, byte2.toByte(), byte3.toByte(), byte4.toByte(), 0xFF.toByte())
                val cmd = byteArrayOf(0xFA.toByte(),0x00,0x00,0x01,0x00,0x03,data.count().toByte(),0xFD.toByte()) + data
                SocketManager.sendCommand(cmd)
            }
        }
    }
}