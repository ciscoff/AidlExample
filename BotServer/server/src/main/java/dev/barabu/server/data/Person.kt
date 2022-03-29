package dev.barabu.server.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * NOTE: При создании клиента нашего сервера придется переносить в проект клиента этот
 * файл и оба .aidl файла НЕ МЕНЯЯ имя пакета (https://russianblogs.com/article/70891198408/)
 */

@Parcelize
data class Person(val firstName: String, val lastName: String, val age: Int) : Parcelable