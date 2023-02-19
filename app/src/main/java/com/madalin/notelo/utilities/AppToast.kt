package com.madalin.notelo.utilities//package com.madalin.notelo.ui
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.widget.Toast
//import com.madalin.notelo.R
//import com.madalin.notelo.databinding.LayoutSnackbarBinding
//
//object AppToast {
//    private var currentToast: Toast? = null
//
//    enum class Icon {
//        SUCCESS, FAILURE
//    }
//
//    /**
//     * Shows a custom [Toast] with the given icon and message. If a toast already exists it gets replaced.
//     * @param context [Context] to use in order to display the [Toast]
//     * @param icon [Toast]'s icon
//     * @param message [Toast]'s message
//     */
//    fun showToast(context: Context, icon: Icon, message: String) {
//        currentToast?.cancel()
//
//        val inflater = LayoutInflater.from(context)
//        inflater.inflate(R.layout.layout_popupbanner, null) // inflates the view layout
//        val binding = LayoutSnackbarBinding.inflate(inflater) // bind the views by inflating
//
//        binding.textView.text = message
//
//        when (icon) {
//            Icon.SUCCESS -> binding.imageView.setImageResource(R.drawable.ic_check_circle)
//            Icon.FAILURE -> binding.imageView.setImageResource(R.drawable.ic_cancel_circle)
//        }
//
//        currentToast = Toast(context)
//
//        currentToast?.apply {
//            duration = Toast.LENGTH_LONG
//            view = binding.root
//            show()
//        }
//    }
//}