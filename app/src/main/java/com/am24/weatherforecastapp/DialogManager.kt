package com.am24.weatherforecastapp

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        dialog.setTitle(R.string.enable_location)
        dialog.setMessage(context.getString(R.string.want_enable_location))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") {
            _, _, -> listener.onClick(null)
            dialog.dismiss()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel)) {
                _, _, -> dialog.dismiss()
        }

        dialog.show()
    }

    fun citySearchDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val editText = EditText(context)
        builder.setView(editText)
        val dialog = builder.create()

        dialog.setTitle(context.getString(R.string.city_name))
        dialog.setMessage(context.getString(R.string.enter_city))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") {
                _, _, -> listener.onClick(editText.text.toString())
            dialog.dismiss()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel)) {
                _, _, -> dialog.dismiss()
        }

        dialog.show()
    }

    interface Listener {
        fun onClick(name: String?)
    }
}