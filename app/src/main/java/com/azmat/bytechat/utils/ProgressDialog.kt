@file:Suppress("DEPRECATION")

package com.azmat.bytechat.utils

import android.app.ProgressDialog
import android.content.Context

fun buildDialog(context: Context, message: String): ProgressDialog{
    val dialog = ProgressDialog(context)
    dialog.setMessage(message)
    dialog.setCancelable(false)
    return dialog
}