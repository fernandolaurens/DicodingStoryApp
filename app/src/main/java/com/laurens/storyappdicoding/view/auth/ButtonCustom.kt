package com.laurens.storyappdicoding.view.auth

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.laurens.storyappdicoding.R

class ButtonCustom : AppCompatButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var txtColor: Int = 0
    private var enabledBackground: Drawable
    private var disabledBackground: Drawable
    private var enabledTextColor: Int
    private var disabledTextColor: Int

    init {
        txtColor = ContextCompat.getColor(context, android.R.color.background_light)
        enabledBackground = ContextCompat.getDrawable(context, R.drawable.bg_button) as Drawable
        disabledBackground = ContextCompat.getDrawable(context, R.drawable.bg_button_disable) as Drawable

        enabledTextColor = ContextCompat.getColor(context, R.color.white)
        disabledTextColor = ContextCompat.getColor(context, R.color.grey)

        // Set default text color and size
        setTextColor(enabledTextColor)
        textSize = resources.getDimension(R.dimen.button_text_size)

        // Set default gravity to center
        gravity = Gravity.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = if(isEnabled) enabledBackground else disabledBackground
        setTextColor(txtColor)
        textSize = 14f
        gravity = Gravity.CENTER
    }
}