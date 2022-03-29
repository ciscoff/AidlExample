package dev.barabu.server.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * NOTE: Этот класс является клоном аналогичного класса из проекта сервера. Клонированный
 * класс должен иметь такое же имя пакета, что и оригинал. То есть в директорию /src/main/
 * данного проекта вручную была добавлена структура подпапок /server/data/ для размещения
 * файла класса Person.
 */
@Parcelize
data class Person(val firstName: String, val lastName: String, val age: Int) : Parcelable