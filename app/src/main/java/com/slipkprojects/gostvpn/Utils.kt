package com.slipkprojects.gostvpn

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

object Utils {
     fun copyToClipboard(context: Context, text: String) {
         (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.apply {
             setPrimaryClip(ClipData.newPlainText("Message", text))
             Toast.makeText(context, "Copiado!", Toast.LENGTH_SHORT)
                 .show()
         }
     }

    fun getLastFromClipboard(context: Context): CharSequence? {
        return (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let { clipboardManager ->
            clipboardManager.primaryClip?.let {
                it.getItemAt(it.itemCount - 1).text
            }
        }
    }

     fun hideKeyboard(activity: Activity) {
         (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
             if (activity.window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                 hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
             }
         }
     }

    fun fromHtmlCompat(text: String): Spanned {
        val textFinal = text.replace("\n", "<br/>")

        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Html.fromHtml(textFinal)
        } else {
            Html.fromHtml(textFinal, Html.FROM_HTML_MODE_LEGACY)
        }
    }
}