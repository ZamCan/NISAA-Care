package com.zamcan.nisaacare

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.zamcan.nisaacare.core.ads.AdsConsent
import com.zamcan.nisaacare.core.ads.AdsConsentStore
import com.zamcan.nisaacare.core.ads.AdsConfiguration
import com.zamcan.nisaacare.ui.AdsConsentSheet
import com.zamcan.nisaacare.ui.Glass
import com.zamcan.nisaacare.ui.NisaaDesign

/**
 * Presents the advertising consent sheet.
 *
 * Kept separate from the sheet's own construction so the flow can be reused:
 * the settings screen offers "change choice", which resets the stored decision
 * and shows the same sheet again.
 */
object AdsConsentPresenter {

    /**
     * Shows the consent sheet if it is appropriate.
     *
     * Returns false when no sheet was shown, so the caller can decide whether
     * to continue with its own flow.
     */
    fun showIfNeeded(context: Context, store: AdsConsentStore): Boolean {
        if (!AdsConsentSheet.shouldShow(context, store)) return false
        show(context, store)
        return true
    }

    /**
     * Shows the consent sheet unconditionally.
     *
     * Used by the settings "change choice" action, which resets the stored
     * decision first so the sheet always opens.
     */
    fun show(context: Context, store: AdsConsentStore) {
        val dialog = Dialog(context, android.R.style.Theme_DeviceDefault_NoActionBar)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val content = AdsConsentSheet.build(context) { decision ->
            when (decision) {
                AdsConsent.GRANTED -> store.grant()
                AdsConsent.DENIED -> store.deny()
                AdsConsent.UNDECIDED -> store.reset()
            }
            dialog.dismiss()
        }

        dialog.setContentView(content)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.window?.let { window ->
            // A dim scrim behind the sheet keeps the aurora visible but pushes
            // it back, so the consent text is the focus.
            window.setBackgroundDrawable(ColorDrawable(Color.argb(90, 0, 0, 0)))
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.setGravity(Gravity.BOTTOM)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
