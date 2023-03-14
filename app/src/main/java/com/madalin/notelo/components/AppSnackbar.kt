package com.madalin.notelo.components//package com.madalin.notelo.ui
//
//import android.graphics.Color
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.FrameLayout
//import com.google.android.material.snackbar.Snackbar
//import com.google.android.material.snackbar.Snackbar.SnackbarLayout
//import com.madalin.notelo.R
//import com.madalin.notelo.databinding.LayoutSnackbarBinding
//
//object AppSnackbar {
//    enum class Icon {
//        SUCCESS, FAILURE
//    }
//
//    enum class Position {
//        TOP, BOTTOM
//    }
//
//    /**
//     * Shows a custom [Snackbar] with the given icon and message.
//     * @param view used to find a suitable ancestor ViewGroup to display the snackbar in
//     * @param icon [Snackbar]'s icon
//     * @param message [Snackbar]'s message
//     */
//    fun showSnackbar(view: View, icon: Icon, message: String, position: Position = Position.TOP) {
//        val inflater = LayoutInflater.from(view.context)
//        inflater.inflate(R.layout.layout_popupbanner, null) // inflates the view layout
//
//        val binding = LayoutSnackbarBinding.inflate(inflater) // bind the views by inflating
//        binding.textView.text = message // sets the message of the view
//
//        val params = view.layoutParams as FrameLayout.LayoutParams
//        params.gravity = Gravity.TOP
//
//        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
//        (snackbar.view as SnackbarLayout).addView(binding.root, 0) // snackbar layout
//        snackbar.view.layoutParams = params
//        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
//        snackbar.view.setPadding(0, 0, 0, 0) // if the view is not covering the whole snackbar layout
//
//        when (icon) { // sets the icon
//            Icon.SUCCESS -> binding.imageView.setImageResource(R.drawable.ic_check_circle)
//            Icon.FAILURE -> binding.imageView.setImageResource(R.drawable.ic_cancel_circle)
//        }
//
//        snackbar.show()
//    }
//}