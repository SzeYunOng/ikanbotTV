package com.ikanbot.tv

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class TVAccessibilityService : AccessibilityService() {

    companion object {

        private var instance: TVAccessibilityService? = null

        fun pressBack(): Boolean {

            val service = instance

            if (service == null) {
                Log.d(
                    "TV_ACCESSIBILITY",
                    "Service unavailable"
                )

                return false
            }

            Log.d(
                "TV_ACCESSIBILITY",
                "GLOBAL BACK"
            )

            return service.performGlobalAction(
                GLOBAL_ACTION_BACK
            )
        }

        fun showLoading(text: String) {
            instance?.showLoadingInternal(text)
        }

        fun hideLoading() {
            instance?.hideLoadingInternal()
        }
    }


    private var loadingOverlay: FrameLayout? = null

    private var loadingTitle: TextView? = null

    private var loadingAnimationHandler: android.os.Handler? = null


    override fun onServiceConnected() {
        super.onServiceConnected()

        instance = this

        Log.d(
            "TV_ACCESSIBILITY",
            "Service connected"
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {

        if (instance === this) {
            instance = null
        }
        hideLoadingInternal()
        super.onDestroy()
    }

    private fun startLoadingAnimation() {

        loadingAnimationHandler?.removeCallbacksAndMessages(null)

        val handler =
            android.os.Handler(
                android.os.Looper.getMainLooper()
            )

        loadingAnimationHandler = handler

        var dotCount = 0

        val runnable =
            object : Runnable {

                override fun run() {

                    val dots =
                        ".".repeat(dotCount)

                    loadingTitle?.text =
                        getString(
                            R.string.searching_with_dots,
                            dots
                        )

                    dotCount =
                        (dotCount + 1) % 4

                    handler.postDelayed(
                        this,
                        400
                    )
                }
            }

        handler.post(runnable)
    }

    private fun showLoadingInternal(text: String) {

        hideLoadingInternal()

        val windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        val overlay =
            FrameLayout(this).apply {

                setBackgroundColor(
                    Color.argb(
                        230,
                        0,
                        0,
                        0
                    )
                )
            }

        val content =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER
            }

        val title =
            TextView(this).apply {

                this.text = "正在搜索"

                textSize = 28f

                setTextColor(
                    Color.WHITE
                )

                gravity =
                    Gravity.CENTER
            }

        loadingTitle = title

        val query =
            TextView(this).apply {

                this.text = text

                textSize = 18f

                alpha = 0.75f

                setTextColor(
                    Color.WHITE
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    12,
                    0,
                    0
                )
            }

        content.addView(title)
        content.addView(query)

        overlay.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )

        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

        try {

            windowManager.addView(
                overlay,
                params
            )

            loadingOverlay = overlay

            startLoadingAnimation()

            Log.d(
                "TV_ACCESSIBILITY",
                "Loading overlay shown: $text"
            )

        } catch (e: WindowManager.BadTokenException) {

            Log.e(
                "TV_ACCESSIBILITY",
                "Loading overlay failed: invalid accessibility window token",
                e
            )

            loadingOverlay = null
            loadingTitle = null
        }
    }

    private fun hideLoadingInternal() {
        loadingAnimationHandler
            ?.removeCallbacksAndMessages(null)

        loadingAnimationHandler = null
        loadingTitle = null

        val overlay =
            loadingOverlay
                ?: return

        val windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        try {

            windowManager.removeView(
                overlay
            )

        } catch (_: Exception) {
        }

        loadingOverlay = null

        Log.d(
            "TV_ACCESSIBILITY",
            "Loading overlay hidden"
        )
    }




}