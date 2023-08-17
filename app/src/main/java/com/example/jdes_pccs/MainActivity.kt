package com.example.jdes_pccs

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import java.io.DataOutputStream
import java.net.Socket
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    lateinit var sck: Socket

    private val buttonIds = arrayOf(R.id.btnVoice, R.id.btnMatrix, R.id.btnMedia, R.id.btnRelay)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            try {
                sck = Socket("192.168.1.200", 6001)
            } catch (e: java.lang.Exception) {
                println(e)
            }
        }.start()

        for (buttonId in buttonIds) {
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener {

                for (buttonId in buttonIds) {
                    val button = findViewById<Button>(buttonId)
                    button.scaleX = 1f
                    button.scaleY = 1f
                    button.setBackgroundResource(R.drawable.button_title_style)
                }

                button.scaleX = 1.1f
                button.scaleY = 1.1f

                button.setBackgroundResource(R.drawable.button_title_click_style)
                when (buttonId) {
                    R.id.btnVoice -> replaceFragment(VoiceFragment())
                    R.id.btnMatrix -> replaceFragment(MatrixFragment())
                    R.id.btnRelay -> replaceFragment(RelayFragment())
                    R.id.btnMedia -> replaceFragment(MediaFragment())
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
        fragmentTransaction.commit()
    }

    fun onButtonClick(view: View) {
        val tag = view.tag
        if (tag is String) {
            val buttonValue = tag.toInt() // 将标签转换为您需要的数据类型

            val cmd = byteArrayOf(0xFA.toByte(),0x00,0x00,buttonValue.toByte(),0x00,0x03,0x05,0xFD.toByte(),0x01,0x01,0x01,0x01,0x01)
            sendCmd(cmd)
        }
    }

    private fun sendCmd(data: ByteArray){
        if(::sck.isInitialized && sck.isConnected){
            Thread{
                try {
                    val outputStream = sck.getOutputStream()
                    val dataOutputStream = DataOutputStream(outputStream)
                    dataOutputStream.write(data)
                    dataOutputStream.flush()
                    outputStream.flush()
                }
                catch (e: java.lang.Exception){
                    println(e)
                }
            }.start()
        }else {
            // 在这里处理未初始化的情况
            println("sck未初始化或未连接")
        }
    }
}