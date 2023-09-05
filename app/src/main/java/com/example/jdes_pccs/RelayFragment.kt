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

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //ViewModel 記憶
        vm = ViewModelProvider(requireActivity())[ValViewModel::class.java]
        buttonIds = Array(17) { index -> resources.getIdentifier("button${index + 1}", "id", requireContext().packageName) }

        for ((index, buttonId) in buttonIds.withIndex()) {
            if(index == 16) continue
            val textView = view.findViewById<Button>(buttonId)
            val formattedText = getString(R.string.PowerSup, index + 1)
            textView.text = formattedText
        }

        //記憶那些電源是開的
        for ((index, btmB) in vm.RelaybtnBool.withIndex()) {
            if (btmB){
                val button = view.findViewById<Button>(buttonIds[index])
                button.setBackgroundResource(R.drawable.button_context_select_style)
            }
        }

        //button Listener
        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener {
                vm.RelaybtnBool[index] = !vm.RelaybtnBool[index]

                //按鈕UI變更 index=16 全開
                if(index == 16){
                    for((i, b) in buttonIds.withIndex()){
                        if(i != 16) vm.RelaybtnBool[i] = vm.RelaybtnBool[index]
                        view.findViewById<Button>(b).setBackgroundResource(if(vm.RelaybtnBool[index]) R.drawable.button_context_select_style else R.drawable.button_context_style)
                    }
                }else{
                    //16區都按了=>全開按鈕亮起
                    val allButtonsTrue = vm.RelaybtnBool.copyOfRange(0, 16).all { it }
                    if(allButtonsTrue){
                        vm.RelaybtnBool[16] = true
                        view.findViewById<Button>(buttonIds[16]).setBackgroundResource(R.drawable.button_context_select_style)
                    }
                    //有一顆關閉而且全開打開中=>全開按鈕關閉
                    if(!vm.RelaybtnBool[index] && vm.RelaybtnBool[16]){
                        vm.RelaybtnBool[16] = false
                        view.findViewById<Button>(buttonIds[16]).setBackgroundResource(R.drawable.button_context_style)
                    }

                    button.setBackgroundResource(if(vm.RelaybtnBool[index]) R.drawable.button_context_select_style else R.drawable.button_context_style)
                }

                view.findViewById<Button>(buttonIds[16]).text = if(vm.RelaybtnBool[16]) getString(R.string.AllCallOff) else getString(R.string.AllCall)

                //data組合
                val byte3 = if(index == 16) 0x20.toUByte() else index.toUByte()
                val byte4 = if(vm.RelaybtnBool[index]) 0x01.toUByte() else 0x02.toUByte()
                val data = ubyteArrayOf(0xAAu, 0x00u, byte3, byte4, 0x55u)
                val cmd = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x04u, 0x00u, 0x03u, data.count().toUByte(), 0xFDu) + data
                SocketManager.sendCommand(cmd)
            }
        }
    }
}