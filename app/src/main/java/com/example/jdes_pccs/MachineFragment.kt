package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider

class MachineFragment() : Fragment() {

    private lateinit var buttonIds: Array<Int>
    private lateinit var vm: ValViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_machine, container, false)
    }

    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(requireActivity())[ValViewModel::class.java]

        buttonIds = Array(1) { index -> resources.getIdentifier("button${index + 1}", "id", requireContext().packageName) }

        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener{

                vm.MachineBtnBool[index] = !vm.MachineBtnBool[index]

                button.setBackgroundResource(if(vm.MachineBtnBool[index]) R.drawable.button_context_select_style else R.drawable.button_context_style)

            }
        }

    }

}