package com.madalin.notelo.utilities

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.madalin.notelo.R
import com.madalin.notelo.databinding.LayoutProgressDialogBinding

class AppProgressDialog {
    private var context: Context? = null
    private var binding: LayoutProgressDialogBinding? = null
    private var progressDialog: ProgressDialog? = null
    private var isShown: Boolean = false

    companion object {
        var instance: AppProgressDialog? = null

        // types
        const val TYPE_SUCCESS = 4000
        const val TYPE_FAILURE = 4001

        /**
         * Creates and shows the [AppProgressDialog] and its styles.
         * @param context context from which the method is called
         * @param message text to display
         */
        fun make(context: Context?, message: String)/*: AppProgressDialog*/ {
            if (instance == null) {
                instance = AppProgressDialog() // creates a new instance if null
            } else {
                if (instance?.isShown == true) { // if the AppProgressDialog is already shown
                    dismiss() // it gets dismissed
                }
            }

            instance?.context = context
            instance?.setLayout(message)
            instance?.show()

            //return instance as AppProgressDialog
        }

        /**
         * Updates the content of the current [AppProgressDialog].
         * @param type lottie animation to display [TYPE_SUCCESS], [TYPE_FAILURE]
         * @param message text to display
         */
        fun update(type: Int, message: String) {
            instance?.binding?.apply {
                progressBar.visibility = View.GONE // hides the spinner
                lottie.visibility = View.VISIBLE // shows the lottie

                when (type) { // sets the resource
                    TYPE_SUCCESS -> lottie.setAnimation(R.raw.lottie_success)
                    TYPE_FAILURE -> lottie.setAnimation(R.raw.lottie_failure)
                }

                lottie.playAnimation()
                textViewMessage.text = message // sets a text message
            }
        }

        /**
         * Dismisses the current [AppProgressDialog].
         */
        fun dismiss() {
            instance?.progressDialog?.dismiss()
            instance?.isShown = false
        }
    }

    /**
     * Creates the [ProgressDialog], displays it and applies its contentView.
     */
    fun show() {
        if (context != null) {
            instance?.progressDialog = ProgressDialog(context) // creates the dialog
            instance?.progressDialog?.apply {
                show()
                window?.setBackgroundDrawableResource(android.R.color.transparent) // transparent background
                setContentView(instance?.binding!!.root)
            }
        }
    }

    /**
     * Sets the [AppProgressDialog]'s layout and text.
     * @param message text to show
     */
    fun setLayout(message: String) {
        if (context != null) {
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.layout_progress_dialog, null)
            binding = LayoutProgressDialogBinding.inflate(inflater)
            binding?.textViewMessage?.text = message
        }
    }
}