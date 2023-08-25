package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider

class RelayFragment : Fragment() {

    private lateinit var buttonIds: Array<Int>
    private lateinit var vm: ValViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_relay, container, false)
    }

    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ViewModel 記憶
        vm = ViewModelProvider(requireActivity())[ValViewModel::class.java]
        buttonIds = Array(17) { index -> resources.getIdentifier("button${index + 1}", "id", requireContext().packageName) }

        //記憶那些電源是開的
        for ((index, btmB) in vm.btnBool.withIndex()) {
            if (btmB){
                val button = view.findViewById<Button>(buttonIds[index])
                button.setBackgroundResource(R.drawable.button_context_select_style)
            }
        }

        //button Listener
        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener {
                vm.btnBool[index] = !vm.btnBool[index]

                //按鈕UI變更 index=16 全開
                if(index == 16){
                    for((i, b) in buttonIds.withIndex()){
                        if(i != 16) vm.btnBool[i] = vm.btnBool[index]
                        view.findViewById<Button>(b).setBackgroundResource(if(vm.btnBool[index]) R.drawable.button_context_select_style else R.drawable.button_context_style)
                    }
                }else{
                    //16區都按了=>全開按鈕亮起
                    val allButtonsTrue = vm.btnBool.copyOfRange(0, 16).all { it }
                    if(allButtonsTrue){
                        vm.btnBool[16] = true
                        view.findViewById<Button>(buttonIds[16]).setBackgroundResource(R.drawable.button_context_select_style)
                    }
                    //有一顆關閉而且全開打開中=>全開按鈕關閉
                    if(!vm.btnBool[index] && vm.btnBool[16]){
                        vm.btnBool[16] = false
                        view.findViewById<Button>(buttonIds[16]).setBackgroundResource(R.drawable.button_context_style)
                    }

                    button.setBackgroundResource(if(vm.btnBool[index]) R.drawable.button_context_select_style else R.drawable.button_context_style)
                }

                //data組合
                val byte3 = if(index == 16) 0x20.toByte() else index.toByte()
                val byte4 = if(vm.btnBool[index]) 0x01.toByte() else 0x02.toByte()
                val data = byteArrayOf(0xAA.toByte(), 0x00, byte3, byte4, 0x55)
                val cmd = byteArrayOf(0xFA.toByte(), 0x00, 0x00, 0x01, 0x00, 0x03,data.count().toByte(), 0xFD.toByte()) + data
                SocketManager.sendCommand(cmd)
            }
        }
    }
}