package com.example.jdes_pccs

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private val buttonIds = arrayOf(R.id.btnVoice, R.id.btnMachine, R.id.btnMedia, R.id.btnRelay, R.id.btnPower)
    private val AllCall = arrayOf(R.id.allCallOpen, R.id.allCallClose)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //連線
        SocketManager.connect("192.168.1.200", 6001)

        var builder = AlertDialog.Builder(this)

        //title 按鈕事件
        for (buttonId in buttonIds) {
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener {

                for (id in buttonIds) {
                    val buttonO = findViewById<Button>(id)
                    buttonO.scaleX = 1f
                    buttonO.scaleY = 1f
                    buttonO.setBackgroundResource(R.drawable.button_title_style)
                }

                button.scaleX = 1.1f
                button.scaleY = 1.1f

                button.setBackgroundResource(R.drawable.button_title_click_style)
                when (buttonId) {
                    R.id.btnVoice -> replaceFragment(VoiceFragment())
                    R.id.btnMachine -> replaceFragment(MachineFragment())
                    R.id.btnRelay -> replaceFragment(RelayFragment())
                    R.id.btnMedia -> replaceFragment(MediaFragment())
                    R.id.btnPower -> replaceFragment(PowerFragment())
                }
            }
        }

        //allCall按鈕區
        for(buttonId in AllCall){
            val button = findViewById<Button>(buttonId)

            button.setOnTouchListener{ AllCallView, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        AllCallView.scaleX = 1.1f
                        AllCallView.scaleY = 1.1f
                    }
                    MotionEvent.ACTION_UP->{
                        AllCallView.scaleX = 1f
                        AllCallView.scaleY = 1f

                        if(buttonId == R.id.allCallOpen) {
                            startLoading("open", builder)
                        }

                        if(buttonId == R.id.allCallClose){
                            startLoading("close", builder)
                        }
                    }
                }
                true
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
        fragmentTransaction.commit()
    }

    private val progressBarMax = 100
    private val progressIncrement = 2
    private val handler = Handler(Looper.getMainLooper())
    private var currentProgress = 0

    private fun startLoading(mode: String, builder: AlertDialog.Builder) {
        // 使用 Handler 每秒更新一次进度
        handler.postDelayed(object : Runnable {
            override fun run() {
                val progressBarLiner = findViewById<LinearLayout>(R.id.progressLiner)
                val fragmentLiner = findViewById<LinearLayout>(R.id.fragmentLiner)
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)

                if (currentProgress < progressBarMax) {
                    fragmentLiner.visibility = View.GONE
                    progressBarLiner.visibility = View.VISIBLE

                    currentProgress += progressIncrement
                    progressBar.progress = currentProgress
                    handler.postDelayed(this, 200) // 延遲0.2秒
                } else {
                    // 载入完成后
                    fragmentLiner.visibility = View.VISIBLE
                    progressBarLiner.visibility = View.GONE

                    handler.removeCallbacksAndMessages(null)
                    currentProgress = 0

                    // 創建dialog
                    if(mode == "open"){
                        builder.setTitle("開") // 標題
                        builder.setMessage("已全開完成") // 内容
                    }
                    if(mode == "close"){
                        builder.setTitle("關") // 標題
                        builder.setMessage("已全關完成") // 内容
                    }

                    // dialog 按鈕
                    builder.setPositiveButton("確定") { dialog, _ ->
                        dialog.cancel()
                    }

                    // 顯示dialog
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }, 200) // 延遲0.2秒
    }
}