package dev.barabu.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import dev.barabu.server.data.IOnPersonListener
import dev.barabu.server.data.Person
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

/**
 * Запуск сервиса в отдельном процессе:
 * https://developer.android.com/guide/topics/manifest/service-element#proc
 * https://medium.com/@rotxed/going-multiprocess-on-android-52975ed8863c
 * https://kelvinma.medium.com/exploring-android-processes-bf74ba63552c
 */
class BotService : Service() {

    /**
     * A thread-safe variant of java.util.ArrayLis
     */
    private val personListeners = CopyOnWriteArrayList<IOnPersonListener>()

    private val personRepo = listOf(
        Person("Mike", "Green", 25),
        Person("Anna", "Ya", 35),
        Person("Kate", "Moss", 21),
        Person("John", "Smith", 42),
        Person("Bruce", "Lee", 57),
    )

    private val binder = object : IBotInterface.Stub() {
        override fun getCallerPid(): Int {
            return getCallingPid()
        }

        override fun getCallerUid(): Int {
            return getCallingUid()
        }

        override fun getCalleePid(): Int {
            return Process.myPid()
        }

        override fun getCalleeUid(): Int {
            return Process.myUid()
        }

        override fun add(a: Float, b: Float): Float {
            return a + b
        }

        override fun greeting(firstName: String, lastName: String): String {
            return "Hello, ${firstName.uppercase()} ${lastName.uppercase()}"
        }

        override fun fetchPerson(): Person {
            return personRepo[Random.nextInt(0, personRepo.lastIndex)]
        }

        /**
         * Это "асинхронный" метод. Он сразу возвращает управление, а потом выполняет работу
         * и отправляет результат через callback.
         *
         * NOTE: "Асинхронный" метод помечается в AIDL модификатором 'oneway'. Методы без
         * модификатора - синхронные (caller ждет)
         */
        override fun loadPerson() {
            Log.d(LOG_TAG, "loadPerson thread ${Thread.currentThread().name}")
            Thread.sleep(Random.nextLong(2, 5) * 1000)
            with(fetchPerson()) {
                personListeners.forEach { it.onPersonLoaded(this) }
            }
        }

        override fun registerListener(listener: IOnPersonListener) {
            if (!personListeners.contains(listener)) {
                personListeners.add(listener)
            }
        }

        override fun unregisterListener(listener: IOnPersonListener) {
            personListeners.remove(listener)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("LOG_TAG", "onBind. My PID is ${Process.myPid()}. My UID is ${Process.myUid()}")
        return binder
    }
}