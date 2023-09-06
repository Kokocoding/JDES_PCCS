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

    @OptIn(ExperimentalUnsignedTypes::class)
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
                        val byte2 = (index + 1).toUByte()
                        val byte3 = (0x09u + byte2).toUByte()
                        val byte4 = (0x09u + (byte2 * 2u)).toUByte()
                        val data = ubyteArrayOf(0x7Au, byte2, byte3, byte4, 0xFFu)
                        val cmd = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x03u, 0x00u, 0x03u, data.count().toUByte(), 0xFDu) + data
                        SocketManager.sendCommand(cmd)
                    }
                }
                true
            }
        }
    }
}