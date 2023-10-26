package dev.barabu.server;

import dev.barabu.server.data.Person;
import dev.barabu.server.data.IOnPersonListener;

interface IBotInterface {

    /**
     * Get caller process id
     */
    int getCallerPid();

    /**
     * Get caller uid
     */
    int getCallerUid();

    /**
     * Get callee process id
     */
    int getCalleePid();

    /**
     * Get callee uid
     */
    int getCalleeUid();

    /**
     * Add two numbers
     */
    float add(float a, float b);

    /**
     * Get any person synchronously
     */
    Person fetchPerson();

    /**
     * Get any person asynchronously
     * TODO: Важно !!!
     * TODO: Используем модификатор 'oneway', чтобы метод работал "асинхронно" - то есть
     * TODO: сразу возвращает управление. А вычисленный результат позже через callback.
     *
     * NOTE: Кстати, можно передавать "одноразовые" листенеры прямо аргументом в метод, например:
     *
     *   void loadPerson(IOnPersonListener listener);
     *
     * Тогда метод сразу вернет управление, но в конце работы должен передать результат через
     * listener. Однако есть опасность, что листенер уже умер (например его активити) и тогда
     * его вызов не будет успешным. Поэтому перед вызовом можно делать проверку:
     *
     *   listener.asBinder().isBinderAlive
     *
     * NOTE: Помни, что все листенеры - это Binder'ы на стороне клиента !!!
     */
    oneway void loadPerson();

    /**
     * Create greeting for name
     */
    String greeting(String firstName, String lastName);

    /**
     * Register IOnPersonListener
     */
    void registerListener(IOnPersonListener listener);

    /**
     * Unregister IOnPersonListener
     */
    void unregisterListener(IOnPersonListener listener);

    /**
     * NOTE: Важно чтобы в PACKAGE_NAME было прописано applicationId приложения, а не имя пакета
     * сервиса внутри этого приложения. То есть нам нужно указывать 'dev.barabu.botserver',
     * а не 'dev.barabu.server'. А вот имя класса должно быть фактическим.
     *
     * NOTE: Это понадобится на стороне клиента для создания интента привязки к сервису.
     */
    const String PACKAGE_NAME = "dev.barabu.botserver";
    const String SERVICE_CLASS_NAME = "dev.barabu.server.BotService";
}