package com.example.jdes_pccs

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.DataOutputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private val buttonIds = arrayOf(R.id.btnVoice, R.id.btnMatrix, R.id.btnMedia, R.id.btnRelay)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //連線
        SocketManager.connect("192.168.1.200", 6001)

        //title 按鈕事件
        for (buttonId in buttonIds) {
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener {

                for (id in buttonIds) {
                    val button = findViewById<Button>(id)
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
}