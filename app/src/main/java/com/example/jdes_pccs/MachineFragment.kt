package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class MachineFragment() : Fragment() {

    private lateinit var buttonIds: Array<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_machine, container, false)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonIds = arrayOf(R.id.buttonOn, R.id.buttonOff)

        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnTouchListener{ upView, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        upView.scaleX = 1.1f
                        upView.scaleY = 1.1f
                    }
                    MotionEvent.ACTION_UP->{
                        upView.scaleX = 1f
                        upView.scaleY = 1f

                        //data組合
                        val data = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x01u, 0x00u, 0x03u, 0xFDu)
                        val cmd = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x01u, 0x00u, 0x03u, data.count().toUByte(), 0xFDu) + data
                        SocketManager.sendCommand(cmd)
                    }
                }
                true
            }
        }
    }

}