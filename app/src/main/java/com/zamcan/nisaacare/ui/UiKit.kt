package com.zamcan.nisaacare.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import com.zamcan.nisaacare.R

object NisaaDesign {
    const val MAX_CONTENT_WIDTH_DP = 720

    fun color(context: Context, resource: Int): Int = context.getColor(resource)

    fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    fun sp(context: Context, value: Float): Float = value * context.resources.displayMetrics.scaledDensity

    fun rounded(
        context: Context,
        fill: Int,
        stroke: Int? = null,
        radiusDp: Int = 20,
        strokeWidthDp: Int = 1
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(context, radiusDp).toFloat()
        setColor(fill)
        if (stroke != null) setStroke(dp(context, strokeWidthDp), stroke)
    }

    fun text(
        context: Context,
        value: CharSequence,
        sizeSp: Float = 16f,
        colorRes: Int = R.color.nisaa_ink,
        bold: Boolean = false,
        gravity: Int = Gravity.START
    ): TextView = TextView(context).apply {
        this.text = value
        textSize = sizeSp
        setTextColor(color(context, colorRes))
        this.gravity = gravity
        typeface = Typeface.create("sans", if (bold) Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding = true
        setLineSpacing(dp(context, 3).toFloat(), 1f)
    }

    fun serif(
        context: Context,
        value: CharSequence,
        sizeSp: Float = 28f,
        colorRes: Int = R.color.nisaa_ink
    ): TextView = text(context, value, sizeSp, colorRes, true).apply {
        typeface = Typeface.create("serif", Typeface.BOLD)
    }

    fun card(context: Context, paddingDp: Int = 18): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, paddingDp), dp(context, paddingDp), dp(context, paddingDp), dp(context, paddingDp))
        background = rounded(context, color(context, R.color.nisaa_surface), color(context, R.color.nisaa_line), 20)
        elevation = dp(context, 1).toFloat()
        isFocusable = true
    }

    fun softCard(context: Context, paddingDp: Int = 18): LinearLayout = card(context, paddingDp).apply {
        background = rounded(context, color(context, R.color.nisaa_surface_alt), null, 20)
    }

    fun primaryButton(
        context: Context,
        label: CharSequence,
        iconRes: Int? = null,
        onClick: (() -> Unit)? = null
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        minimumHeight = dp(context, 50)
        setPadding(dp(context, 18), dp(context, 8), dp(context, 18), dp(context, 8))
        background = rounded(context, color(context, R.color.nisaa_rose), null, 16)
        isClickable = true
        isFocusable = true
        contentDescription = label
        if (onClick != null) setOnClickListener { onClick() }
        if (iconRes != null) {
            addView(icon(context, iconRes, R.color.nisaa_white, 20))
            addView(space(context, 8, 1, 0f))
        }
        addView(text(context, label, 15f, R.color.nisaa_white, true, Gravity.CENTER))
    }

    fun secondaryButton(
        context: Context,
        label: CharSequence,
        iconRes: Int? = null,
        onClick: (() -> Unit)? = null
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        minimumHeight = dp(context, 50)
        setPadding(dp(context, 18), dp(context, 8), dp(context, 18), dp(context, 8))
        background = rounded(context, color(context, R.color.nisaa_rose_soft), color(context, R.color.nisaa_rose), 16)
        isClickable = true
        isFocusable = true
        contentDescription = label
        if (onClick != null) setOnClickListener { onClick() }
        if (iconRes != null) {
            addView(icon(context, iconRes, R.color.nisaa_rose_dark, 20))
            addView(space(context, 8, 1, 0f))
        }
        addView(text(context, label, 15f, R.color.nisaa_rose_dark, true, Gravity.CENTER))
    }

    fun outlineButton(
        context: Context,
        label: CharSequence,
        iconRes: Int? = null,
        onClick: (() -> Unit)? = null
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        minimumHeight = dp(context, 48)
        setPadding(dp(context, 16), dp(context, 6), dp(context, 16), dp(context, 6))
        background = rounded(context, Color.TRANSPARENT, color(context, R.color.nisaa_line), 16)
        isClickable = true
        isFocusable = true
        contentDescription = label
        if (onClick != null) setOnClickListener { onClick() }
        if (iconRes != null) {
            addView(icon(context, iconRes, R.color.nisaa_ink_soft, 19))
            addView(space(context, 8, 1, 0f))
        }
        addView(text(context, label, 14f, R.color.nisaa_ink, true, Gravity.CENTER))
    }

    fun iconButton(
        context: Context,
        iconRes: Int,
        description: CharSequence,
        onClick: (() -> Unit)? = null
    ): ImageButton = ImageButton(context).apply {
        setImageResource(iconRes)
        imageTintList = ColorStateList.valueOf(color(context, R.color.nisaa_ink))
        setBackgroundColor(Color.TRANSPARENT)
        minimumWidth = dp(context, 48)
        minimumHeight = dp(context, 48)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        contentDescription = description
        if (onClick != null) setOnClickListener { onClick() }
    }

    fun icon(
        context: Context,
        iconRes: Int,
        tintRes: Int = R.color.nisaa_ink,
        sizeDp: Int = 24
    ): ImageView = ImageView(context).apply {
        setImageResource(iconRes)
        imageTintList = ColorStateList.valueOf(color(context, tintRes))
        layoutParams = LinearLayout.LayoutParams(dp(context, sizeDp), dp(context, sizeDp))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun chip(
        context: Context,
        label: CharSequence,
        selected: Boolean = false,
        onClick: (() -> Unit)? = null
    ): TextView = text(context, label, 13f, if (selected) R.color.nisaa_rose_dark else R.color.nisaa_ink_soft, selected).apply {
        gravity = Gravity.CENTER
        minHeight = dp(context, 40)
        setPadding(dp(context, 14), dp(context, 6), dp(context, 14), dp(context, 6))
        background = rounded(
            context,
            color(context, if (selected) R.color.nisaa_rose_soft else R.color.nisaa_surface),
            color(context, if (selected) R.color.nisaa_rose else R.color.nisaa_line),
            20
        )
        isClickable = onClick != null
        isFocusable = onClick != null
        if (onClick != null) setOnClickListener { onClick() }
    }

    fun field(
        context: Context,
        hint: CharSequence,
        inputType: Int = android.text.InputType.TYPE_CLASS_TEXT
    ): EditText = EditText(context).apply {
        this.hint = hint
        this.inputType = inputType
        textSize = 16f
        setTextColor(color(context, R.color.nisaa_ink))
        setHintTextColor(color(context, R.color.nisaa_ink_soft))
        setSingleLine(true)
        minHeight = dp(context, 52)
        setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8))
        background = rounded(context, color(context, R.color.nisaa_surface), color(context, R.color.nisaa_line), 14)
    }

    fun multilineField(
        context: Context,
        hint: CharSequence,
        inputType: Int = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
    ): EditText = field(context, hint, inputType).apply {
        minHeight = dp(context, 104)
        setGravity(Gravity.TOP or Gravity.START)
        setSingleLine(false)
    }

    fun sectionTitle(context: Context, value: CharSequence): TextView = serif(context, value, 23f).apply {
        setPadding(0, dp(context, 4), 0, dp(context, 10))
    }

    fun eyebrow(context: Context, value: CharSequence): TextView = text(context, value.toString().uppercase(), 11f, R.color.nisaa_rose, true).apply {
        letterSpacing = 0.14f
    }

    fun body(context: Context, value: CharSequence): TextView = text(context, value, 15f, R.color.nisaa_ink_soft).apply {
        setLineSpacing(dp(context, 4).toFloat(), 1.05f)
    }

    fun divider(context: Context): View = View(context).apply {
        setBackgroundColor(color(context, R.color.nisaa_line))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 1))
    }

    fun space(context: Context, widthDp: Int, heightDp: Int = widthDp, weight: Float = 0f): Space = Space(context).apply {
        layoutParams = if (weight > 0f) {
            LinearLayout.LayoutParams(dp(context, widthDp), dp(context, heightDp), weight)
        } else {
            LinearLayout.LayoutParams(dp(context, widthDp), dp(context, heightDp))
        }
    }

    fun scrollBody(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, 20), dp(context, 20), dp(context, 20), dp(context, 32))
        clipToPadding = false
    }

    fun screenScroll(context: Context, body: LinearLayout = scrollBody(context)): ScrollView = ScrollView(context).apply {
        isFillViewport = true
        setBackgroundColor(color(context, R.color.nisaa_cream))
        addView(body, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    fun header(
        context: Context,
        title: CharSequence,
        onBack: (() -> Unit)? = null,
        action: View? = null
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        if (onBack != null) addView(iconButton(context, R.drawable.ic_chevron_right, context.getString(R.string.onboarding_back)) {
            onBack()
        }.also { it.rotation = 180f })
        addView(space(context, 8, 1, 1f))
        addView(serif(context, title, 24f).apply { maxLines = 2 })
        if (action != null) {
            addView(space(context, 8, 1, 1f))
            addView(action)
        }
    }

    fun statusPill(
        context: Context,
        label: CharSequence,
        positive: Boolean = true
    ): TextView = text(context, label, 12f, if (positive) R.color.nisaa_sage else R.color.nisaa_rose_dark, true).apply {
        gravity = Gravity.CENTER
        setPadding(dp(context, 10), dp(context, 5), dp(context, 10), dp(context, 5))
        background = rounded(
            context,
            color(context, if (positive) R.color.nisaa_sage_soft else R.color.nisaa_rose_soft),
            null,
            20
        )
    }

    fun dividerLabel(context: Context, label: CharSequence, value: CharSequence): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(body(context, label), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(text(context, value, 15f, R.color.nisaa_ink, true, Gravity.END))
    }

    fun statePanel(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        iconRes: Int? = null,
        action: View? = null
    ): LinearLayout = softCard(context, 20).apply {
        gravity = Gravity.CENTER
        if (iconRes != null) {
            addView(icon(context, iconRes, R.color.nisaa_rose, 30))
            addView(space(context, 0, 10))
        }
        addView(text(context, title, 18f, R.color.nisaa_ink, true, Gravity.CENTER))
        addView(space(context, 0, 6))
        addView(body(context, message).apply { gravity = Gravity.CENTER })
        if (action != null) {
            addView(space(context, 0, 14))
            addView(action)
        }
    }

    fun setMargins(view: View, context: Context, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        val params = (view.layoutParams as? ViewGroup.MarginLayoutParams)
            ?: ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(dp(context, left), dp(context, top), dp(context, right), dp(context, bottom))
        view.layoutParams = params
    }

    fun ensureContrast(foreground: Int, background: Int): Int {
        fun channel(value: Int): Double {
            val srgb = value / 255.0
            return if (srgb <= 0.03928) srgb / 12.92 else Math.pow((srgb + 0.055) / 1.055, 2.4)
        }
        val luminance = 0.2126 * channel(Color.red(foreground)) +
            0.7152 * channel(Color.green(foreground)) +
            0.0722 * channel(Color.blue(foreground))
        val backgroundLuminance = 0.2126 * channel(Color.red(background)) +
            0.7152 * channel(Color.green(background)) +
            0.0722 * channel(Color.blue(background))
        val ratio = (maxOf(luminance, backgroundLuminance) + 0.05) /
            (minOf(luminance, backgroundLuminance) + 0.05)
        return if (ratio >= 4.5) foreground else Color.BLACK
    }
}
