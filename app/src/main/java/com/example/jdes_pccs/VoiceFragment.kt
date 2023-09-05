package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Base64
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

    @OptIn(ExperimentalUnsignedTypes::class)
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

        //音量up
        for ((index, buttonId) in upButtonIds.withIndex()) {
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
                        val data = voiceUpDown(index.toString(), "up")
                        val cmd = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x01u, 0x00u, 0x03u, data.count().toUByte(), 0xFDu) + data
                        SocketManager.sendCommand(cmd)

                        //json 處理
                        saveJson(index.toString(), "voice", data)
                    }
                }
                true
            }
        }

        //音量down
        for ((index, buttonId) in downButtonIds.withIndex()) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnTouchListener{ downView, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downView.scaleX = 1.1f
                        downView.scaleY = 1.1f
                    }
                    MotionEvent.ACTION_UP->{
                        downView.scaleX = 1f
                        downView.scaleY = 1f

                        //data組合
                        val data = voiceUpDown(index.toString(), "down")
                        val cmd = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x01u, 0x00u, 0x03u, data.count().toUByte(), 0xFDu) + data
                        SocketManager.sendCommand(cmd)

                        //json 處理
                        saveJson(index.toString(), "voice", data)
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
                val byte3 = (0x03 + index).toUByte()
                val byte9 = (0x00 + index).toUByte()
                val byte11 = if(vm.MuteBool[index]) 0x01.toUByte() else 0x00.toUByte()
                val data = ubyteArrayOf(0xB3u, 0x21u, byte3, 0x00u, 0x2Bu, 0x01u, 0x02u, 0x00u, byte9, 0x00u, byte11, 0x00u)
                val cmd = ubyteArrayOf(0xFAu, 0x00u, 0x00u, 0x01u, 0x00u, 0x03u, data.count().toUByte(), 0xFDu) + data
                SocketManager.sendCommand(cmd)

                //json 處理
                saveJson(index.toString(), "mute", vm.MuteBool[index])
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun voiceUpDown(index : String, mode : String) : UByteArray {

        val data: UByteArray = if (saveObject.has(index)) {
            val nestedObject = saveObject.getJSONObject(index) // 取得json[index]
            if (nestedObject.has("voice")) {
                val voiceDataString = nestedObject.getString("voice")
                base64ToByteArray(voiceDataString)
            }else{
                ubyteArrayOf(0xB3u, 0x21u, 0x01u, 0x00u, 0x2Bu, 0x01u, 0x01u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u)
            }
        } else {
            ubyteArrayOf(0xB3u, 0x21u, 0x01u, 0x00u, 0x2Bu, 0x01u, 0x01u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u)
        }

        data[8] = index.toUByte()

        when(mode){
            "up" -> {
                if (data[11].toInt() != 4 || (data[11].toInt() == 4 && data[10].toInt() != 176)) {
                    if(data[10].toInt() + 10 > 255){
                        if(data[11].toInt() + 1 > 255){
                            data[11] = 0x00u
                        }else{
                            data[11] = (data[11].toInt() + 1).toUByte()
                        }
                    }
                    data[10] = (data[10] + 10u).toUByte()
                }
            }
            "down" -> {
                if (data[11].toInt() != 227 || (data[11].toInt() == 227 && data[10].toInt() != 224)) {
                    if(data[10].toInt() - 10 < 0){
                        if(data[11].toInt() - 1 < 0){
                            data[11] = 0xFFu
                        }else{
                            data[11] = (data[11].toInt() - 1).toUByte()
                        }
                    }
                    data[10] = (data[10] - 10u).toUByte()
                }
            }
        }

        return data
    }

    //讀取json
    private fun openJsonFile() : JSONObject {
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

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun saveJson(index : String, key : String, data : Any){
        //json 處理
        val existingNestedObject = saveObject.optJSONObject(index)

        if (existingNestedObject != null) {
            if (data is Boolean) {
                existingNestedObject.put(key, data)
            } else if (data is UByteArray) {
                existingNestedObject.put(key, byteArrayToBase64(data))
            }
        } else {
            val newNestedObject = JSONObject()
            if (data is Boolean) {
                newNestedObject.put(key, data)
            } else if (data is UByteArray) {
                newNestedObject.put(key, byteArrayToBase64(data))
            }
            saveObject.put(index, newNestedObject)
        }
        saveJsonToFile(requireContext().applicationContext, saveObject.toString())
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

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun byteArrayToBase64(byteArray: UByteArray): String {
        val byteArrayAsByteArray = byteArray.asByteArray() // 将 UByteArray 转换为 ByteArray
        return Base64.encodeToString(byteArrayAsByteArray, Base64.DEFAULT)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun base64ToByteArray(base64String: String): UByteArray {
        val byteArrayAsByteArray = Base64.decode(base64String, Base64.DEFAULT)
        return byteArrayAsByteArray.toUByteArray() // 将 ByteArray 转换为 UByteArray
    }
}