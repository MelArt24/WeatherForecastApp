package com.am24.weatherforecastapp

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

/**
 * Менеджер діалогових вікон.
 * Використовує патерн Singleton (через ключове слово object),
 * щоб зручно викликати діалоги з будь-якої частини програми.
 */
object DialogManager {

    /**
     * Діалог, який пропонує користувачеві увімкнути GPS.
     */
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        dialog.setTitle(R.string.enable_location)
        dialog.setMessage(context.getString(R.string.want_enable_location))

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            listener.onClick(null) // Передаємо null, бо ми просто підтвердили дію
            dialog.dismiss() // Закриваємо вікно
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel)) { _, _ -> dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Діалог для введення назви міста вручну.
     */
    fun citySearchDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val editText = EditText(context)
        builder.setView(editText)
        val dialog = builder.create()

        dialog.setTitle(context.getString(R.string.city_name))
        dialog.setMessage(context.getString(R.string.enter_city))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ -> listener.onClick(editText.text.toString())
            dialog.dismiss()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel)) { _, _ -> dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Інтерфейс-слухач. Він потрібен, щоб той, хто викликав діалог (наприклад, MainFragment),
     * дізнався, що користувач натиснув кнопку "OK" і яке місто він ввів.
     */
    interface Listener {
        fun onClick(name: String?)
    }
}