package dev.barabu.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.os.RemoteCallbackList
import android.util.Log
import dev.barabu.server.data.IOnPersonListener
import dev.barabu.server.data.Person
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

// И Proxy и Stub имплементят IMyAidlInterface, но делают это по разному. Proxy конвертирует
// вызовы методов в транзакции, которые потом уходят через ядро в Stub. А Stub конвертирует
// транзакции в вызов методов IMyAidlInterface, которые выполняют реальную работу на стороне
// сервера.
//
// Биндер - это любой объект, который унаследовал от классса Binder (например IMyAidlInterface.Stub)
// Binder Token - это уникальный 32-х битный ID биндера в системе. Обычный Binder не публикуется
// в системе через Service Manager. Его Token известен только подключившемуся клиенту.

//
//                         --------------------
//                         .                  .                    -----------
//                         . IMyAidlInterface .                    .         .
//              .--------> .                  . <---.        .---> .  Binder . --> IBinder
//              |          --------------------     |        |     .         .
//              |                                   |        |     -----------
//    .---------'--------------                     |--------|
//    .  Proxy                .                     .        .
//    .  -----                .                     .  Stub  .
//    .  val mRemote: Binder  .                     .        .
//    -------------------------                     ----------
//
//   В Proxy методы IMyAidlInterface делают:
//
//   mRemote.transact(.....)                 -->    onTransact (op_code, in_buffer, out_buffer, ..) {
//                      |                    |        switch(op_code) {
//                      |                    |          вызовы функций из IMyAidlInterface
//                      '------> KERNEL -----'        }
//                                                 }

/**
 * Запуск сервиса в отдельном процессе:
 * https://developer.android.com/guide/topics/manifest/service-element#proc
 * https://medium.com/@rotxed/going-multiprocess-on-android-52975ed8863c
 * https://kelvinma.medium.com/exploring-android-processes-bf74ba63552c
 *
 * Важная инфа про Binder Tokens:
 * https://www.androiddesignpatterns.com/2013/07/binders-window-tokens.html
 * https://stackoverflow.com/a/33292184
 *
 * Вот тут пример того как реализовать два биндера в однм сервисе
 * https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/RemoteService.java
 */
class BotService : Service() {

    /**
     * A thread-safe variant of java.util.ArrayLis
     */
    /*private val personListeners = CopyOnWriteArrayList<IOnPersonListener>()*/

    /**
     * Лучше пользоваться этим классом, потому что он автоматически поддерживает связь с
     * биндерами клиентов (биндеры колбеков) и отслеживает link to death.
     */
    private val personListeners = RemoteCallbackList<IOnPersonListener>()

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

            val listenersQty = personListeners.beginBroadcast()

            with(fetchPerson()) {
                for (i in 0 until listenersQty) {
                    personListeners.getBroadcastItem(i).onPersonLoaded(this)
                }
            }
        }

        override fun registerListener(listener: IOnPersonListener?) {
            listener?.let(personListeners::register)
        }

        override fun unregisterListener(listener: IOnPersonListener?) {
            listener?.let(personListeners::unregister)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("LOG_TAG", "onBind. My PID is ${Process.myPid()}. My UID is ${Process.myUid()}")
        return binder
    }
}