package com.ikanbot.tv

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.webkit.WebView

class TVWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(
    context,
    attrs,
    defStyleAttr
) {

    var onImeGo: (() -> Unit)? = null

    override fun onCreateInputConnection(
        outAttrs: EditorInfo
    ): InputConnection? {

        val base =
            super.onCreateInputConnection(outAttrs)
                ?: return null

        return object :
            InputConnectionWrapper(
                base,
                false
            ) {

            override fun performEditorAction(
                editorAction: Int
            ): Boolean {

                if (
                    editorAction == EditorInfo.IME_ACTION_GO ||
                    editorAction == EditorInfo.IME_ACTION_SEARCH ||
                    editorAction == EditorInfo.IME_ACTION_DONE
                ) {

                    onImeGo?.invoke()

                    return true
                }

                return super.performEditorAction(
                    editorAction
                )
            }
        }
    }
}