package dev.barabu.botserver

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.barabu.server.BotService
import dev.barabu.server.IBotInterface
import dev.barabu.server.LOG_TAG

class MainActivity : AppCompatActivity() {

    private lateinit var textInfo: TextView

    private var botService: IBotInterface? = null

    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(LOG_TAG, "onServiceConnected")
            botService = IBotInterface.Stub.asInterface(service)
            workWithService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(LOG_TAG, "onServiceDisconnected")
            botService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textInfo = findViewById(R.id.text_info)

        val intent = Intent(this, BotService::class.java)

        isBound = bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        Log.d(LOG_TAG, "isBound=$isBound")
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun workWithService() {

        botService?.let { bot ->
            textInfo.text = bot.greeting("Sergey", "Ya")

            Log.d(
                LOG_TAG,
                "My PID ${Process.myPid()}. Bot's Caller PID ${bot.callerPid}, Bot's PID ${bot.calleePid}"
            )
            Log.d(
                LOG_TAG,
                "My UID ${Process.myUid()}. Bot's Caller UID ${bot.callerUid}, Bot's UID ${bot.calleeUid}"
            )
            Log.d(LOG_TAG, "Random person ${bot.fetchPerson()}")
            Log.d(LOG_TAG, "5 + 6 = ${bot.add(5f, 6f)}")
        }
    }
}