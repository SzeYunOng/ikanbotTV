package com.ikanbot.tv.web

import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout

class IkanbotWebChromeClient(
    private val webView: WebView,
    private val rootView: FrameLayout,
    private val onCustomViewShown: (
        View,
        CustomViewCallback
    ) -> Unit,
    private val onCustomViewHidden: () -> Unit
) : WebChromeClient() {

    private var customView: View? = null

    override fun onShowCustomView(
        view: View?,
        callback: CustomViewCallback?
    ) {

        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        if (view == null || callback == null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view

        webView.visibility = View.GONE

        rootView.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        view.requestFocus()

        onCustomViewShown(
            view,
            callback
        )
    }

    override fun onHideCustomView() {
        customView = null
        onCustomViewHidden()
    }
}