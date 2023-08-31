package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class MediaFragment: Fragment(R.layout.fragment_media) {

    private lateinit var buttonIds: Array<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonIds = Array(4) { index -> resources.getIdentifier("button${index + 1}", "id", requireContext().packageName) }

        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)

            button.setOnTouchListener{ view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.scaleX = 1.1f
                        view.scaleY = 1.1f
                    }
                    MotionEvent.ACTION_UP->{
                        view.scaleX = 1f
                        view.scaleY = 1f

                        //data組合
                        val byte2 = (index + 1).toByte()
                        val byte3 = (0x09 + byte2).toByte()
                        val byte4 = (0x09 + (byte2*2)).toByte()
                        val data = byteArrayOf(0x7A, byte2, byte3, byte4, 0xFF.toByte())
                        val cmd = byteArrayOf(0xFA.toByte(), 0x00, 0x00, 0x02, 0x00, 0x03,data.count().toByte(), 0xFD.toByte()) + data
                        SocketManager.sendCommand(cmd)
                    }
                }
                true
            }
        }
    }
}