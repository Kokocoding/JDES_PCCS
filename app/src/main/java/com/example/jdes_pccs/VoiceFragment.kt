package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

class VoiceFragment : Fragment() {

    private lateinit var buttonIds: Array<Int>
    private lateinit var upButtonIds: Array<Int>
    private lateinit var downButtonIds: Array<Int>

    private var saveObject = JSONObject()

    private lateinit var vm: ValViewModel

    private val jsonName = "json_file.json"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_voice, container, false)
    }

    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonIds = Array(5) { index -> resources.getIdentifier("buttonMute${index + 1}", "id", requireContext().packageName) }
        upButtonIds = Array(5) { index -> resources.getIdentifier("buttonUp${index + 1}", "id", requireContext().packageName) }
        downButtonIds = Array(5) { index -> resources.getIdentifier("buttonDown${index + 1}", "id", requireContext().packageName) }
        vm = ViewModelProvider(requireActivity())[ValViewModel::class.java]
        saveObject = openJsonFile()

        for (entryKey in saveObject.keys()) {
            val nestedObject = saveObject.getJSONObject(entryKey)

            //mute
            if (nestedObject.has("mute")) {
                val muteValue = nestedObject.getBoolean("mute")
                if (muteValue) {
                    // "mute" == true
                    vm.MuteBool[entryKey.toInt()] = true
                    val button = view.findViewById<Button>(buttonIds[entryKey.toInt()])
                    button.setBackgroundResource(R.drawable.button_context_select_style)
                    button.text = getString(R.string.MuteOff)
                }
            }
        }

        for ((index, buttonId) in upButtonIds.withIndex()) {
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
                        //val data = byteArrayOf(0xB3.toByte(), 0x21, 0x0D, 0x00, )
                        val byte3 = (0x03+index).toByte()
                        val byte9 = (0x00+index).toByte()
                        val data = byteArrayOf(0xB3.toByte(), 0x21, byte3, 0x00, 0x2B, 0x01, 0x02, 0x00, byte9, 0x00, 0x01, 0x00)

                        val cmd = byteArrayOf(0xFA.toByte(), 0x00, 0x00, 0x01, 0x00, 0x03,data.count().toByte(), 0xFD.toByte()) + data
                        SocketManager.sendCommand(cmd)

                        //json 處理
                        val existingNestedObject = saveObject.optJSONObject(index.toString())
                        if (existingNestedObject != null && existingNestedObject.has("voice")) {
                            existingNestedObject.put("voice", data)
                        } else {
                            val newNestedObject = JSONObject()
                            newNestedObject.put("voice", data)
                            saveObject.put(index.toString(), newNestedObject)
                        }
                        saveJsonToFile(requireContext().applicationContext, saveObject.toString())
                    }
                }
                true
            }
        }

        //靜音
        for ((index, buttonId) in buttonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)

            button.setOnClickListener{
                vm.MuteBool[index] = !vm.MuteBool[index]

                button.setBackgroundResource(if(vm.MuteBool[index]) R.drawable.button_context_select_style else R.drawable.button_context_style)
                button.text = if(vm.MuteBool[index]) getString(R.string.MuteOff) else getString(R.string.MuteOn)

                //data組合
                val byte3 = (0x03+index).toByte()
                val byte9 = (0x00+index).toByte()
                val byte11 = if(vm.MuteBool[index]) 0x01.toByte() else 0x00.toByte()
                val data = byteArrayOf(0xB3.toByte(), 0x21, byte3, 0x00, 0x2B, 0x01, 0x02, 0x00, byte9, 0x00, byte11, 0x00)
                val cmd = byteArrayOf(0xFA.toByte(), 0x00, 0x00, 0x01, 0x00, 0x03,data.count().toByte(), 0xFD.toByte()) + data
                SocketManager.sendCommand(cmd)

                //json 處理
                val existingNestedObject = saveObject.optJSONObject(index.toString())
                if (existingNestedObject != null && existingNestedObject.has("mute")) {
                    existingNestedObject.put("mute", vm.MuteBool[index])
                } else {
                    val newNestedObject = JSONObject()
                    newNestedObject.put("mute", vm.MuteBool[index])
                    saveObject.put(index.toString(), newNestedObject)
                }
                saveJsonToFile(requireContext().applicationContext, saveObject.toString())
            }
        }
    }

    //讀取json
    private fun openJsonFile(): JSONObject {

        var jsonObject = JSONObject()

        try {
            val fileInputStream = requireContext().applicationContext.openFileInput(jsonName)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            bufferedReader.close()

            val jsonString = stringBuilder.toString()

            // 使用 JSON 解析庫解析 JSON 字串為 JSON 物件
            jsonObject = JSONObject(jsonString)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return jsonObject
    }

    // 儲存 JSON 到內部儲存空間
    private fun saveJsonToFile(context: Context, jsonString: String) {
        try {
            val fileOutputStream: FileOutputStream = context.openFileOutput(jsonName, Context.MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}