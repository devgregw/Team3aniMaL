package dev.gregwhatley.cse3310.team3animal

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class ProgressDialogHelper {
    companion object {
        fun create(context: Context, title: String): AlertDialog {
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val padding = 30
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(padding, padding, padding, padding)
                gravity = Gravity.CENTER
                this.layoutParams = layoutParams
            }

            val progressBar = ProgressBar(context).apply {
                isIndeterminate = true
                setPadding(0, 0, padding, 0)
                this.layoutParams = layoutParams
            }

            val textView = TextView(context).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 20f
                this.layoutParams = layoutParams
            }

            layout.addView(progressBar)
            layout.addView(textView)

            val dialog = AlertDialog.Builder(context)
                .setCancelable(true)
                .setView(
                    layout)
                .create()
            dialog.show()
            val window: Window? = dialog.window
            if (window != null) {
                val windowLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
                    copyFrom(dialog.window!!.attributes)
                    width = LinearLayout.LayoutParams.WRAP_CONTENT
                    LinearLayout.LayoutParams.WRAP_CONTENT
                }
                dialog.window!!.attributes = windowLayoutParams
            }
            return dialog
        }
    }
}