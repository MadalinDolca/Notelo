package com.madalin.notelo.components

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.madalin.notelo.ApplicationClass
import com.madalin.notelo.R
import com.madalin.notelo.databinding.LayoutPopupbannerBinding

class PopupBanner {
    private var context: Context? = null // the context from which the method is called
    private var rootView: View? = null // the root view of the calling activity
    private var binding: LayoutPopupbannerBinding? = null // binder for the popup's layout
    private var popupWindow: PopupWindow? = null // popup to display
    private var isShown: Boolean = false // if the popup is shown or not
    private var gravity: Int? = null
    private var animation: Int? = null

    companion object {
        var instance: PopupBanner? = null

        // popup's position
        const val POSITION_TOP = 1000 //Gravity.TOP
        const val POSITION_BOTTOM = 1001 //Gravity.BOTTOM

        // popup's type
        const val TYPE_SUCCESS = 2000
        const val TYPE_FAILURE = 2001
        const val TYPE_INFO = 2003

        /**
         * Returns an instance of the current [PopupBanner] if it exists, otherwise creates a new one.
         *
         * @return [instance] of the current [PopupBanner]
         */
        /*fun getInstance(): PopupBanner? {
            if (instance == null) {
                instance = PopupBanner()
            }
            return instance
        }*/

        /**
         * Creates the [PopupBanner] and its styles.
         * @param context context from which the method is called
         * @param type [TYPE_SUCCESS], [TYPE_FAILURE]
         * @param message text content to display
         * @return [PopupBanner] itself
         */
        fun make(context: Context?, type: Int, message: String): PopupBanner {
            if (instance == null) {
                instance = PopupBanner() // creates a new instance if null
            } else {
                if (instance?.isShown == true) { // if the PopupBanner is already shown
                    instance?.dismissPopupBanner() // it gets dismissed
                }
            }

            instance?.context = context
            instance?.rootView = (context as Activity).window.decorView.rootView // gets the context's root view
            instance?.setLayout(type)
            instance?.setText(message)
            instance?.setGravity() // default Gravity is TOP
            instance?.setAnimation()

            return instance as PopupBanner
        }
    }

    /**
     * Creates the [PopupWindow] for the banner, applies its styles and displays the [PopupBanner].
     */
    fun show() {
        if (context != null) {
            isShown = true // marks popup as shown

            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT

            popupWindow = PopupWindow(binding?.root, width, height)
            popupWindow?.animationStyle = animation!!
            rootView?.post { popupWindow?.showAtLocation(rootView, gravity!!, 0, 0) }

            autoDismissPopupBanner()
        }
    }

    /**
     * Dismisses the current [PopupBanner].
     */
    fun dismissPopupBanner() {
        popupWindow?.dismiss()
        isShown = false
    }

    /**
     * Automatically dismisses the [PopupBanner] after a certain amount of time.
     * Default is 3000 milliseconds.
     * @param duration delay in milliseconds
     */
    fun autoDismissPopupBanner(duration: Int = 3000) {
        if (duration > 0) {
            Handler().postDelayed({ dismissPopupBanner() }, duration.toLong())
        }
    }

    /**
     * Sets the [PopupBanner]'s layout and styles it according to it's type.
     */
    fun setLayout(type: Int) {
        if (context != null) {
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.layout_popupbanner, null)
            binding = LayoutPopupbannerBinding.inflate(inflater)

            // sets the popup layout type
            when (type) {
                TYPE_SUCCESS -> binding?.imageView?.setImageResource(R.drawable.ic_check_circle)
                TYPE_FAILURE -> binding?.imageView?.setImageResource(R.drawable.ic_cancel_circle)
                TYPE_INFO -> binding?.imageView?.setImageResource(R.drawable.ic_info_circle)
            }
        }
    }

    /**
     * Sets the [PopupBanner]'s text message.
     */
    fun setText(message: String) {
        if (context != null) {
            binding?.textView?.text = message
        }
    }

    // CHAIN METHODS //
    /**
     * Sets the [PopupBanner]'s [gravity]. Default is [POSITION_TOP].
     * @param gravity [PopupBanner]'s position.
     * @return the current instance of [PopupBanner]
     */
    fun setGravity(_gravity: Int = POSITION_TOP): PopupBanner {
        when (_gravity) {
            POSITION_TOP -> gravity = Gravity.TOP
            POSITION_BOTTOM -> gravity = Gravity.BOTTOM
        }

        setAnimation() // updates the animation if this method is called again

        return instance as PopupBanner
    }

    /**
     * Sets the [PopupBanner]'s [animation] according to its [gravity].
     * @return the current instance of [PopupBanner]
     */
    fun setAnimation(): PopupBanner {
        when (gravity) {
            Gravity.TOP -> animation = R.style.PopupBannerTopAnimation
            Gravity.BOTTOM -> animation = R.style.PopupBannerBottomAnimation
        }

        return instance as PopupBanner
    }

    /**
     * Closes the [PopupBanner] and executes the given [function].
     * @return the current instance of [PopupBanner]
     */
    fun setOnClick(function: () -> Unit): PopupBanner {
        binding?.root?.setOnClickListener {
            binding?.root?.visibility = View.INVISIBLE
            popupWindow?.dismiss()

            function() // executes the given function
        }

        return instance as PopupBanner
    }
}