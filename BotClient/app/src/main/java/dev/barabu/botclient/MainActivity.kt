package dev.barabu.botclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.barabu.server.IBotInterface
import dev.barabu.server.data.IOnPersonListener
import dev.barabu.server.data.Person

class MainActivity : AppCompatActivity() {

    private lateinit var textInfo: TextView

    private var botService: IBotInterface? = null

    private val onPersonListener = object : IOnPersonListener.Stub() {
        override fun onPersonLoaded(person: Person?) {
            person?.let { p ->
                Log.d(LOG_TAG, "Loaded person $p")
            }
        }
    }

    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(LOG_TAG, "onServiceConnected in thread ${Thread.currentThread().name}")
            botService = IBotInterface.Stub.asInterface(service).apply {

                try {
                    registerListener(onPersonListener)
                } catch (e: RemoteException) {
                    Log.d(LOG_TAG, "Service crashed unexpectedly")
                }
            }

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

        /**
         * Все три метода рабочие. Однако чтобы они работали нужно в файле манифеста добавить
         * <query> (см. манифест). Без <query> вызов bindService всегда возвращает false.
         */
        bindBotServerRecipe01()
//        bindBotServerRecipe02()
//        bindBotServerRecipe03()
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            unbindService(serviceConnection)
            botService?.unregisterListener(onPersonListener)
            isBound = false
        }
    }

    /**
     * Рабочий вариант. Создаем пустой интент и явно прописываем в него packageName и полное
     * имя класса сервиса. Фактически это аналогично методу bindBotServerRecipe02.
     *
     * NOTE: Важно чтобы в packageName было прописано applicationId приложения, в котором
     * живет сервис, а не имя пакета сервиса внутри этого приложения. То есть нам нужно
     * указывать 'dev.barabu.botserver', а не 'dev.barabu.server'
     */
    private fun bindBotServerRecipe01() {
        val intent = Intent().apply {
            setClassName(IBotInterface.PACKAGE_NAME, IBotInterface.SERVICE_CLASS_NAME)
        }

        isBound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d(LOG_TAG, "bindBotServerRecipe01 isBound=$isBound")
    }

    private fun bindBotServerRecipe02() {
        val intent = Intent(IBotInterface.SERVICE_CLASS_NAME).apply {
            component = ComponentName(IBotInterface.PACKAGE_NAME, IBotInterface.SERVICE_CLASS_NAME)
        }

        isBound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d(LOG_TAG, "bindBotServerRecipe02 isBound=$isBound")
    }

    /**
     * Здесь используем action из intent-filter сервиса и его packageName (он же applicationId)
     */
    private fun bindBotServerRecipe03() {
        val intent = Intent(IBotInterface.SERVICE_CLASS_NAME).apply {
            `package` = IBotInterface.PACKAGE_NAME
        }

        isBound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d(LOG_TAG, "bindBotServerRecipe03 isBound=$isBound")
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

            bot.loadPerson()
        }
    }
}