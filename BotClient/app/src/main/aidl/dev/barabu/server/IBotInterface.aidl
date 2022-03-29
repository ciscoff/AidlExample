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

    const String PACKAGE_NAME = "dev.barabu.botserver";
    const String SERVICE_CLASS_NAME = "dev.barabu.server.BotService";
}