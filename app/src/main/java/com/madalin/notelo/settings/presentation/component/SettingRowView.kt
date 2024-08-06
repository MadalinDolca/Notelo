package com.madalin.notelo.settings.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.getStringOrThrow
import com.madalin.notelo.R

class SettingRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_row, this, true)

        iconView = findViewById(R.id.imageViewIcon)
        textView = findViewById(R.id.textViewText)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SettingRowView, defStyleAttr, 0)

        try {
            val icon = attributes.getResourceIdOrThrow(R.styleable.SettingRowView_icon)
            val text = attributes.getStringOrThrow(R.styleable.SettingRowView_text)

            setIcon(icon)
            setText(text)
        } catch (e: Exception) {
            // attribute not provided
        } finally {
            attributes.recycle()
        }
    }

    fun setIcon(iconResId: Int) {
        iconView.setImageResource(iconResId)
        invalidate()
        requestLayout()
    }

    fun setText(text: String) {
        textView.text = text
        invalidate()
        requestLayout()
    }
}