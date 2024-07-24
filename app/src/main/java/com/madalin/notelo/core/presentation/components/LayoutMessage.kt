package com.madalin.notelo.core.presentation.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.madalin.notelo.databinding.LayoutMessageBinding

class LayoutMessage {
    lateinit var binding: LayoutMessageBinding
    lateinit var parent: ViewGroup
    lateinit var layoutParams: ConstraintLayout.LayoutParams

    /**
     * Used to create a layout that contains certain data to display inside other layouts.
     */
    companion object {
        var instance: LayoutMessage? = null

        /**
         * Creates the [LayoutMessage] by instantiating its properties.
         * @param context from which the method is called
         * @param parentView the view group
         * @param topView the view that should be at the top
         * @return this [LayoutMessage]
         */
        fun make(context: Context?, parentView: ViewGroup, topView: View): LayoutMessage {
            if (instance == null) {
                instance = LayoutMessage()
            }

            // gets the layout and binds to it
            val inflater = LayoutInflater.from(context)
            instance!!.binding = LayoutMessageBinding.inflate(inflater, parentView, false)
            //inflater.inflate(R.layout.layout_message, null)

            // instantiates the variables
            instance!!.binding.buttonAction.visibility = View.INVISIBLE // hides the button for now
            instance!!.parent = parentView

            instance!!.layoutParams = ConstraintLayout.LayoutParams( // sets the layout parameters
                ConstraintLayout.LayoutParams.MATCH_PARENT, // width
                ConstraintLayout.LayoutParams.MATCH_PARENT // height
            ).apply {
                topToBottom = topView.id
                startToStart = parentView.id
                endToEnd = parentView.id
            }

            return instance!!
        }
    }

    /**
     * Sets the layout's content.
     * @param lottie animation to display
     * @param message message to display
     * @return this [LayoutMessage]
     */
    fun setContent(lottie: Int, message: String): LayoutMessage {
        with(instance!!) {
            binding.lottie.setAnimation(lottie)
            binding.lottie.playAnimation()
            binding.textViewMessage.text = message
        }

        return instance!!
    }

    /**
     * Shows a button with the given [buttonText] and sets its onClick [action].
     * @param buttonText to show inside the button
     * @param action to execute on click
     * @return this [LayoutMessage]
     */
    fun setButtonAction(buttonText: String, action: () -> Unit): LayoutMessage {
        with(instance!!.binding.buttonAction) {
            visibility = View.VISIBLE
            text = buttonText
            setOnClickListener { action() }
        }

        return instance!!
    }

    /**
     * Adds the created layout as a child view to the given [parent] with the specified layout parameters.
     */
    fun show() {
        val rootView = binding.root

        // checks if the view is already the child of this parent
        if (rootView.parent == null) {
            instance!!.parent.addView(rootView, layoutParams)
        }
    }

    /**
     * Removes the view containing the message from its parent.
     */
    fun hide() {
        if (instance != null && instance!!.binding.root.parent != null) {
            instance!!.parent.removeView(instance!!.binding.root)
        }
    }
}