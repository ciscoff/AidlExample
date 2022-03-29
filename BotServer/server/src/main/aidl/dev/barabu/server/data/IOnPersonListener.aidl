// IOnPersonListener.aidl
package dev.barabu.server.data;

import dev.barabu.server.data.Person;

/**
 * Это callback для возврата инстанса Person клиенту.
 */
interface IOnPersonListener {
    void onPersonLoaded(in Person person);
}