package com.ikanbot.tv

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

import android.webkit.JavascriptInterface
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import com.ikanbot.tv.javascript.PageLayoutScripts
import com.ikanbot.tv.javascript.VideoPlayerScripts
import com.ikanbot.tv.javascript.styles.PlayPageStyles
import com.ikanbot.tv.web.IkanbotWebChromeClient

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import android.content.pm.PackageManager
import android.util.Log
import com.ikanbot.tv.javascript.SearchNavigationScripts

import android.Manifest
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.result.contract.ActivityResultContracts

import android.app.SearchManager

class MainActivity : ComponentActivity() {

    private lateinit var webView: TVWebView
    private lateinit var rootView: FrameLayout
    private var voiceLoadingView: View? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private var speechRecognizer: SpeechRecognizer? = null

    private lateinit var katnissVoiceMonitor: KatnissVoiceMonitor
    private var pendingVoiceQuery: String? = null
    private var voiceSearchLoading = false


    private var shouldSearchOnResume = false

    private var isActivityResumed = false
    private var ignoreBackAfterVoiceSearch = false
    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startVoiceRecognition()
            } else {
                Log.d(
                    "TV_VOICE",
                    "Microphone permission denied"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("TV_TEST", "===== MAINACTIVITY ONCREATE =====")

        Log.d(
            "TV_NATIVE_SEARCH",
            "onCreate action=${intent.action}"
        )

        if (Intent.ACTION_SEARCH == intent.action) {

            val query =
                intent.getStringExtra(
                    SearchManager.QUERY
                )

            Log.d(
                "TV_NATIVE_SEARCH",
                "SEARCH QUERY = $query"
            )
        }

        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.rootView)
        webView = findViewById(R.id.webView)

        webView.onImeGo = {

            Log.d(
                "TV_SEARCH",
                "IME GO received"
            )

            webView.post {

                webView.evaluateJavascript(
                    """
            (function() {

                var button =
                    document.querySelector('#search_btn');

                var form =
                    button
                        ? button.closest('form')
                        : null;

                var input =
                    form
                        ? form.querySelector('input[name="q"]')
                        : null;

                console.log(
                    'TV IME GO DEBUG',
                    'button=', !!button,
                    'form=', !!form,
                    'input=', !!input,
                    'value=', input ? input.value : null
                );

                if (
                    form &&
                    input &&
                    input.value.trim()
                ) {

                    console.log(
                        'TV IME GO SUBMIT',
                        input.value
                    );

                    form.submit();
                }

            })();
            """.trimIndent(),
                    null
                )
            }
        }

        // Allow Chrome on Mac to inspect the TV WebView
        WebView.setWebContentsDebuggingEnabled(true)

        setupWebView()
        katnissVoiceMonitor =
            KatnissVoiceMonitor(

                onVoiceResult = { text ->

                    runOnUiThread {

                        pendingVoiceQuery = text
                        voiceSearchLoading = true
                        TVAccessibilityService.showLoading(text)
                    }
                },

                onAssistantFinished = {

                    runOnUiThread {

                        Log.d(
                            "TV_KATNISS",
                            "Assistant finished - GLOBAL BACK"
                        )

                        val backSuccess =
                            TVAccessibilityService.pressBack()

                        if (!backSuccess) {

                            Log.d(
                                "TV_KATNISS",
                                "GLOBAL BACK FAILED - cancel voice search"
                            )

                            pendingVoiceQuery = null
                            shouldSearchOnResume = false
                            voiceSearchLoading = false
                            TVAccessibilityService.hideLoading()

                            return@runOnUiThread
                        }

                        webView.postDelayed({

                            Log.d(
                                "TV_KATNISS",
                                "RESTORE IKANBOT AFTER BACK"
                            )

                            val intent =
                                Intent(
                                    this,
                                    MainActivity::class.java
                                ).apply {

                                    addFlags(
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    )
                                }

                            shouldSearchOnResume = true

                            Log.d(
                                "TV_KATNISS",
                                "SET shouldSearchOnResume=true pendingVoiceQuery=$pendingVoiceQuery"
                            )

                            startActivity(intent)

                            // 如果 Activity 本身已经 resumed，startActivity()
                            // 不一定再次触发 onResume，所以这里提供 fallback。
                            webView.postDelayed({

                                if (isActivityResumed && shouldSearchOnResume) {

                                    Log.d(
                                        "TV_KATNISS",
                                        "Activity already resumed - perform pending search"
                                    )

                                    performPendingVoiceSearch()
                                }

                            }, 100)

                        }, 150)
                    }
                }
            )

        katnissVoiceMonitor.start()

        setupBackButton()

        if (savedInstanceState == null) {
            webView.loadUrl("https://www.ikanbot.com/")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(
            "TV_NATIVE_SEARCH",
            "onCreate action=${intent.action}"
        )

        if (Intent.ACTION_SEARCH == intent.action) {

            val query =
                intent.getStringExtra(
                    SearchManager.QUERY
                )

            Log.d(
                "TV_NATIVE_SEARCH",
                "SEARCH QUERY = $query"
            )
        }

        Log.d(
            "TV_NATIVE_SEARCH",
            "onNewIntent action=${intent.action}"
        )

        if (Intent.ACTION_SEARCH == intent.action) {

            val query =
                intent.getStringExtra(
                    SearchManager.QUERY
                )

            Log.d(
                "TV_NATIVE_SEARCH",
                "SEARCH QUERY = $query"
            )
        }
    }

    private fun performPendingVoiceSearch() {

        if (!shouldSearchOnResume) {
            return
        }

        val text =
            pendingVoiceQuery
                ?: return

        // 先清掉，避免 onResume + delayed callback 重复搜索
        shouldSearchOnResume = false
        pendingVoiceQuery = null

        Log.d(
            "TV_KATNISS",
            "PERFORM VOICE SEARCH = $text"
        )

        val searchUrl =
            android.net.Uri.Builder()
                .scheme("https")
                .authority("www.ikanbot.com")
                .appendPath("search")
                .appendQueryParameter("q", text)
                .build()
                .toString()

        Log.d(
            "TV_KATNISS",
            "LOAD SEARCH URL = $searchUrl"
        )

        ignoreBackAfterVoiceSearch = true

        webView.loadUrl(searchUrl)

        webView.postDelayed({

            ignoreBackAfterVoiceSearch = false

            Log.d(
                "TV_KATNISS",
                "Voice search BACK protection ended"
            )

        }, 800)

        voiceSearchLoading = false
        TVAccessibilityService.hideLoading()
    }

    override fun onResume() {
        super.onResume()

        isActivityResumed = true

        Log.d(
            "TV_KATNISS",
            "onResume shouldSearchOnResume=$shouldSearchOnResume pendingVoiceQuery=$pendingVoiceQuery"
        )

        performPendingVoiceSearch()
        webView.postDelayed({

            applyCurrentPageLayout()

        }, 300)
    }

    override fun onPause() {
        isActivityResumed = false
        super.onPause()
    }

    private fun applyCurrentPageLayout() {

        val url = webView.url ?: return

        if (
            !url.contains("ikanbot.com") &&
            !url.contains("www1.ikanbot.com")
        ) {
            return
        }

        val isPlayPage =
            url.contains("/play/")

        val isSearchPage =
            url.contains("/search")

        when {

            isPlayPage -> {

                webView.evaluateJavascript(
                    PageLayoutScripts.playPageSetup(),
                    null
                )

                webView.postDelayed({

                    webView.evaluateJavascript(
                        PageLayoutScripts.installPlayStyle(
                            PlayPageStyles.css()
                        ),
                        null
                    )

                    webView.setInitialScale(68)

                }, 300)
            }


            isSearchPage -> {

                // 离开 Play page 后恢复正常 scale
                webView.setInitialScale(0)

                // 先应用基础 layout
                webView.evaluateJavascript(
                    PageLayoutScripts.homePageSetup(),
                    null
                )

                // 再应用 Search Result 专属 layout
                webView.postDelayed({

                    webView.evaluateJavascript(
                        PageLayoutScripts.searchPageSetup(),
                        null
                    )

                }, 150)
            }


            else -> {

                webView.setInitialScale(0)

                webView.evaluateJavascript(
                    PageLayoutScripts.homePageSetup(),
                    null
                )
            }
        }
    }

    private fun setupWebView() {

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode =
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            loadWithOverviewMode = true
            useWideViewPort = true

            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false

            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
        }

        webView.addJavascriptInterface(
            AndroidTVBridge(),
            "AndroidTV"
        )

        // Keep normal links inside this WebView
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (view == null || url == null) return

                if (
                    url.contains("ikanbot.com") ||
                    url.contains("www1.ikanbot.com")
                ) {

                    // ==========================================
                    // SEARCH NAVIGATION
                    // ==========================================

                    view.evaluateJavascript(
                        SearchNavigationScripts.install(),
                        null
                    )

                    val isPlayPage = url.contains("/play/")

                    if (isPlayPage) {

                        // ==========================================
                        // PLAY PAGE SETUP
                        // ==========================================

                        view.evaluateJavascript(
                            PageLayoutScripts.playPageSetup(),
                            null
                        )


                        // ==========================================
                        // PLAY PAGE STYLE - DELAYED INSTALL
                        // ==========================================

                        // ==========================================
                        // PLAY PAGE STYLE - DELAYED INSTALL
                        // ==========================================

                         view.postDelayed({

                              view.evaluateJavascript(
                              PageLayoutScripts.installPlayStyle(
                                PlayPageStyles.css()
                                   ),
                                  null
                                   )

                         }, 1000)


                        // ==========================================
                        // VIDEO PLAYER TV CONTROLS
                        // ==========================================

                        view.evaluateJavascript(
                            VideoPlayerScripts.install(),
                            null
                        )


                        // ==========================================
                        // WEBVIEW SCALE
                        // ==========================================

                        view.postDelayed({

                            view.setInitialScale(68)
                            view.scrollTo(0, 0)

                        }, 300)

                    } else {

                        // ==========================================
                        // HOME / SEARCH / HISTORY / RANKING
                        // ==========================================

                        // Reset the scale left by the Play page.
                        view.setInitialScale(0)

                        view.evaluateJavascript(
                            PageLayoutScripts.homePageSetup(),
                            null
                        )

                        view.postDelayed({
                            view.scrollTo(0, 0)
                        }, 100)
                    }
                    if (
                        voiceSearchLoading &&
                        url.contains("/search")
                    ) {
                        TVAccessibilityService.hideLoading()

                        voiceSearchLoading = false
                    }

                }
            }
        }
        // Native WebView HTML5 fullscreen support
        // ==========================================
        // WEB CHROME CLIENT
        // ==========================================

        webView.webChromeClient = IkanbotWebChromeClient(
            webView = webView,
            rootView = rootView,

            onCustomViewShown = { view, callback ->

                customView = view
                customViewCallback = callback
            },

            onCustomViewHidden = {

                exitNativeFullscreen()
            }
        )

        // Android TV remote focus
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        // =====================================================
        // ANDROID TV REMOTE FOCUS
        // =====================================================
        //
        // WebView itself must be focusable so the TV remote
        // can send DPAD / ENTER key events to the webpage.
        //
        // IMPORTANT:
        // Do NOT handle DPAD LEFT / RIGHT / UP / DOWN here.
        //
        // Player navigation is handled inside JavaScript:
        //
        // document.addEventListener(
        //     "keydown",
        //     function(e) { ... },
        //     true
        // );
        //
        // Using JavaScript capture phase prevents Video.js
        // from stealing LEFT / RIGHT, especially the
        // Playback Rate (1x) control.
        // =====================================================

        webView.isFocusable = true

        webView.isFocusableInTouchMode = true

        webView.requestFocus()
    }


    private fun setupBackButton() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    // =====================================
                    // EXIT TV FULLSCREEN FIRST
                    // =====================================

                    if (isTVFullscreen) {
                        exitTVFullscreen()
                        return
                    }

                    /*
                     * CASE 1
                     *
                     * WebChromeClient fullscreen is active.
                     */
                    if (customView != null) {
                        exitNativeFullscreen()
                        return
                    }

                    /*
                     * CASE 2
                     *
                     * Check whether the webpage itself thinks
                     * it is currently fullscreen.
                     */
                    checkAndExitWebFullscreen()
                }
            }
        )
    }

    private fun showVoiceLoading(text: String) {

        hideVoiceLoading()

        val overlay =
            FrameLayout(this).apply {

                setBackgroundColor(
                    android.graphics.Color.argb(
                        210,
                        0,
                        0,
                        0
                    )
                )

                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
            }

        val content =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                gravity =
                    android.view.Gravity.CENTER

                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.CENTER
                    )
            }

        val title =
            android.widget.TextView(this).apply {

                this.text = "正在搜索"

                textSize = 28f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                gravity =
                    android.view.Gravity.CENTER
            }

        val query =
            android.widget.TextView(this).apply {

                this.text = text

                textSize = 18f

                alpha = 0.75f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                gravity =
                    android.view.Gravity.CENTER

                setPadding(
                    0,
                    12,
                    0,
                    0
                )
            }

        content.addView(title)
        content.addView(query)

        overlay.addView(content)

        rootView.addView(overlay)

        voiceLoadingView = overlay
    }

    private fun hideVoiceLoading() {

        voiceLoadingView?.let {

            rootView.removeView(it)
        }

        voiceLoadingView = null
    }

    inner class AndroidTVBridge {
        @JavascriptInterface
        fun startVoiceSearch() {

            runOnUiThread {
                startVoiceRecognition()
            }
        }

        @JavascriptInterface
        fun stopVoiceSearch() {
            runOnUiThread {
                Log.d("TV_VOICE", "Manual stopListening")
                speechRecognizer?.stopListening()
            }
        }

        @JavascriptInterface
        fun showKeyboard() {
            runOnUiThread {

                webView.postDelayed({

                    val imm =
                        getSystemService(
                            android.content.Context.INPUT_METHOD_SERVICE
                        ) as android.view.inputmethod.InputMethodManager

                    imm.showSoftInput(
                        webView,
                        android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
                    )

                }, 200)
            }
        }

        @JavascriptInterface
        fun hideKeyboard() {
            runOnUiThread {

                val imm =
                    getSystemService(
                        android.content.Context.INPUT_METHOD_SERVICE
                    ) as android.view.inputmethod.InputMethodManager

                imm.hideSoftInputFromWindow(
                    webView.windowToken,
                    0
                )

                Log.d(
                    "TV_SEARCH",
                    "Hide Gboard for physical keyboard"
                )
            }
        }

        @JavascriptInterface
        fun toggleFullscreen() {
            runOnUiThread {
                if (isTVFullscreen) {
                    exitTVFullscreen()
                } else {
                    enterTVFullscreen()
                }
            }
        }

        @JavascriptInterface
        fun enterFullscreen() {
            runOnUiThread {
                enterTVFullscreen()
            }
        }

        @JavascriptInterface
        fun exitFullscreen() {
            runOnUiThread {
                exitTVFullscreen()
            }
        }

        // ==========================================
        // PICTURE IN PICTURE
        // ==========================================

        @JavascriptInterface
        fun enterPictureInPicture() {
            runOnUiThread {
                enterTVPictureInPicture()
            }
        }

    }

    private fun startVoiceRecognition() {

        // ==========================================
        // CHECK MICROPHONE PERMISSION
        // ==========================================

        if (
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                "TV_VOICE",
                "Requesting microphone permission"
            )

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )

            return
        }


        // ==========================================
        // CHECK SPEECH RECOGNITION SUPPORT
        // ==========================================

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            Log.e(
                "TV_VOICE",
                "Speech recognition unavailable"
            )

            return
        }


        // ==========================================
        // DESTROY OLD RECOGNIZER
        // ==========================================

        speechRecognizer?.destroy()

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)


        // ==========================================
        // RECOGNITION LISTENER
        // ==========================================

        speechRecognizer?.setRecognitionListener(

            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    Log.d(
                        "TV_VOICE",
                        "Ready for speech"
                    )
                }


                override fun onBeginningOfSpeech() {
                    Log.d(
                        "TV_VOICE",
                        "Speech started"
                    )
                }


                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                    // Don't log this.
                    // It fires continuously while listening.
                }


                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }


                override fun onEndOfSpeech() {
                    Log.d(
                        "TV_VOICE",
                        "Speech ended"
                    )
                }


                override fun onError(
                    error: Int
                ) {
                    Log.e(
                        "TV_VOICE",
                        "Recognition error = $error"
                    )
                }


                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val text = matches?.firstOrNull()

                    Log.d(
                        "TV_VOICE",
                        "FINAL RESULT = $text"
                    )

                    if (!text.isNullOrBlank()) {

                        val safeText = org.json.JSONObject.quote(text)

                        webView.evaluateJavascript(
                            """
            (function() {

                var searchButton =
                    document.querySelector('#search_btn');

                var form =
                    searchButton
                        ? searchButton.closest('form')
                        : null;

                var input =
                    form
                        ? form.querySelector('input[name="q"]')
                        : null;

                if (!input || !searchButton) {
                    console.log(
                        'TV VOICE SEARCH: search elements not found'
                    );
                    return;
                }

                input.value = $safeText;

                input.dispatchEvent(
                    new Event(
                        'input',
                        { bubbles: true }
                    )
                );

                input.dispatchEvent(
                    new Event(
                        'change',
                        { bubbles: true }
                    )
                );

                console.log(
                    'TV VOICE SEARCH:',
                    input.value
                );

                searchButton.click();

            })();
            """.trimIndent(),
                            null
                        )
                    }
                }


                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                    // Intentionally ignored.
                    // We only want the FINAL result.
                }


                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )


        // ==========================================
        // RECOGNIZER INTENT
        // ==========================================

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )

                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2000L
                )

                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1500L
                )

                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    1000L
                )

            }


        Log.d(
            "TV_VOICE",
            "Starting speech recognition"
        )

        speechRecognizer?.startListening(intent)
    }

    private var isTVFullscreen = false

    private fun enterTVPictureInPicture() {

        // ==========================================
        // CHECK DEVICE PIP SUPPORT
        // ==========================================

        val hasPipFeature =
            packageManager.hasSystemFeature(
                PackageManager.FEATURE_PICTURE_IN_PICTURE
            )

        Log.d(
            "TV_PIP",
            "FEATURE_PICTURE_IN_PICTURE = $hasPipFeature"
        )

        Log.d(
            "TV_PIP",
            "SDK = ${Build.VERSION.SDK_INT}"
        )


        // ==========================================
        // CHECK ANDROID VERSION
        // ==========================================

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            Log.d(
                "TV_PIP",
                "PiP unsupported: Android version too old"
            )

            return
        }


        // ==========================================
        // EXIT OUR CUSTOM FULLSCREEN FIRST
        // ==========================================

        if (isTVFullscreen) {
            exitTVFullscreen()
        }


        // ==========================================
        // CREATE PIP PARAMS
        // ==========================================

        val params =
            PictureInPictureParams.Builder()
                .setAspectRatio(
                    Rational(16, 9)
                )
                .build()


        // ==========================================
        // ENTER PIP
        // ==========================================

        try {

            val success =
                enterPictureInPictureMode(params)

            Log.d(
                "TV_PIP",
                "enterPictureInPictureMode result = $success"
            )

        } catch (e: Exception) {

            Log.e(
                "TV_PIP",
                "PiP failed",
                e
            )
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {

        super.onPictureInPictureModeChanged(
            isInPictureInPictureMode,
            newConfig
        )

        Log.d(
            "TV_PIP",
            "PiP mode changed = $isInPictureInPictureMode"
        )
    }

    private fun enterTVFullscreen() {

        if (isTVFullscreen) return

        isTVFullscreen = true

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val controller =
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


        // Tell webpage to make player fill WebView
        webView.evaluateJavascript(
            """
        (function() {

            var player =
                document.querySelector(
                    '#ikanbot-player'
                );

            if (!player) return;
            
            // =========================================
            // FULLSCREEN STATE
            // =========================================
            
            player.classList.add(
                'tv-native-fullscreen'
            );
            
            // Save current inline styles
            if (!player.dataset.tvOldStyle) {
                player.dataset.tvOldStyle =
                    player.getAttribute('style') || '';
            }


            player.style.setProperty(
                'position',
                'fixed',
                'important'
            );

            player.style.setProperty(
                'left',
                '0',
                'important'
            );

            player.style.setProperty(
                'top',
                '0',
                'important'
            );

            player.style.setProperty(
                'width',
                '100vw',
                'important'
            );

            player.style.setProperty(
                'height',
                '100vh',
                'important'
            );

            player.style.setProperty(
                'max-width',
                'none',
                'important'
            );

            player.style.setProperty(
                'max-height',
                'none',
                'important'
            );

            player.style.setProperty(
                'margin',
                '0',
                'important'
            );

            player.style.setProperty(
                'z-index',
                '2147483647',
                'important'
            );


            document.documentElement.style
                .setProperty(
                    'overflow',
                    'hidden',
                    'important'
                );

            document.body.style
                .setProperty(
                    'overflow',
                    'hidden',
                    'important'
                );

            // =========================================
            // HIDE VIDEO.JS CONTROL BAR
            // =========================================

            var controlBar =
                player.querySelector(
                    '.vjs-control-bar'
                );

            if (controlBar) {

                controlBar.style.setProperty(
                    'visibility',
                    'hidden',
                    'important'
                );

                controlBar.style.setProperty(
                    'opacity',
                    '0',
                    'important'
                );

                controlBar.style.setProperty(
                    'pointer-events',
                    'none',
                    'important'
                );
            }

            player.classList.remove(
                'vjs-user-active'
            );

            player.classList.add(
                'vjs-user-inactive'
            );


            // =========================================
            // FOCUS VIDEO SCREEN
            // =========================================

            player.setAttribute(
                'tabindex',
                '0'
            );

            try {

                player.focus({
                    preventScroll: true
                });

            } catch (error) {

                player.focus();
            }


            console.log(
                'TV FULLSCREEN VIDEO FOCUS:',
                document.activeElement === player
            );
            
            console.log(
                'ANDROID NATIVE FULLSCREEN ENTER'
            );

        })();
        """.trimIndent(),
            null
        )
    }

    private fun exitTVFullscreen() {

        if (!isTVFullscreen) return

        isTVFullscreen = false

        webView.evaluateJavascript(
            """
        (function() {

            var player =
                document.querySelector(
                    '#ikanbot-player'
                );

            if (!player) return;
            
            // =========================================
            // EXIT FULLSCREEN STATE
            // =========================================
            
            player.classList.remove(
                'tv-native-fullscreen'
            );

            // =========================================
            // RESTORE ORIGINAL PLAYER STYLE
            // =========================================

            var oldStyle =
                player.dataset.tvOldStyle || '';

            if (oldStyle) {

                player.setAttribute(
                    'style',
                    oldStyle
                );

            } else {

                player.removeAttribute(
                    'style'
                );
            }

            delete player.dataset.tvOldStyle;


            // =========================================
            // RESTORE PAGE
            // =========================================

            document.documentElement.style
                .removeProperty('overflow');

            document.body.style
                .removeProperty('overflow');


            // =========================================
            // RESTORE VIDEO.JS CONTROL BAR
            // =========================================

            var controlBar =
                player.querySelector(
                    '.vjs-control-bar'
                );

            if (controlBar) {

                controlBar.style.removeProperty(
                    'display'
                );

                controlBar.style.removeProperty(
                    'visibility'
                );

                controlBar.style.removeProperty(
                    'opacity'
                );
            }


            // Make controls visible again
            player.classList.add(
                'vjs-user-active'
            );

            player.classList.remove(
                'vjs-user-inactive'
            );


            // =========================================
            // RESTORE FULLSCREEN BUTTON FOCUS
            // =========================================

            var fullscreenButton =
                player.querySelector(
                    '.vjs-fullscreen-control'
                );

            if (fullscreenButton) {

                fullscreenButton.setAttribute(
                    'tabindex',
                    '0'
                );

            // =========================================
            // RESTORE VIDEO CONTROL FOCUS
            // =========================================
            
            function restoreVideoFocus() {
            
                var button =
                    player.querySelector(
                        '.vjs-fullscreen-control'
                    );
            
                if (!button) {
                    return;
                }
            
                button.setAttribute(
                    'tabindex',
                    '0'
                );
            
                try {
            
                    button.focus({
                        preventScroll: true
                    });
            
                } catch (error) {
            
                    button.focus();
                }
            
                console.log(
                    'TV FOCUS RESTORED:',
                    document.activeElement,
                    'success=',
                    document.activeElement === button
                );
            }
            
            
            // First attempt
            restoreVideoFocus();
            
            
            // WebView / page may steal focus while
            // fullscreen layout is being restored.
            setTimeout(
                restoreVideoFocus,
                100
            );
            
            setTimeout(
                restoreVideoFocus,
                300
            );
            
            setTimeout(
                restoreVideoFocus,
                600
            );
            }


            console.log(
                'ANDROID NATIVE FULLSCREEN EXIT'
            );

        })();
        """.trimIndent(),
            null
        )


        // =========================================
        // RESTORE ANDROID SYSTEM BARS
        // =========================================

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )
    }

    /**
     * Exit WebChromeClient native fullscreen.
     */
    private fun exitNativeFullscreen() {

        val fullscreenView = customView ?: return

        rootView.removeView(fullscreenView)

        customView = null

        val callback = customViewCallback
        customViewCallback = null

        callback?.onCustomViewHidden()

        webView.visibility = View.VISIBLE
        webView.requestFocus()
    }

    /**
     * Ask JavaScript whether the website/video player
     * is currently using browser fullscreen.
     *
     * If yes:
     *      exit fullscreen
     *
     * If no:
     *      perform normal Android TV BACK behaviour
     */
    private fun checkAndExitWebFullscreen() {

        val javascript = """
            (function() {

                try {

                    var fullscreenElement =
                        document.fullscreenElement ||
                        document.webkitFullscreenElement ||
                        document.mozFullScreenElement ||
                        document.msFullscreenElement;

                    if (fullscreenElement) {

                        if (document.exitFullscreen) {
                            document.exitFullscreen();
                        }
                        else if (document.webkitExitFullscreen) {
                            document.webkitExitFullscreen();
                        }
                        else if (document.mozCancelFullScreen) {
                            document.mozCancelFullScreen();
                        }
                        else if (document.msExitFullscreen) {
                            document.msExitFullscreen();
                        }

                        return "fullscreen_exited";
                    }

                    /*
                     * Some video implementations expose
                     * WebKit presentation mode.
                     */
                    var videos = document.getElementsByTagName("video");

                    for (var i = 0; i < videos.length; i++) {

                        var video = videos[i];

                        if (
                            video.webkitPresentationMode &&
                            video.webkitPresentationMode === "fullscreen"
                        ) {

                            if (video.webkitSetPresentationMode) {
                                video.webkitSetPresentationMode("inline");
                                return "fullscreen_exited";
                            }
                        }
                    }

                    return "not_fullscreen";

                }
                catch (e) {
                    return "not_fullscreen";
                }

            })();
        """.trimIndent()

        webView.evaluateJavascript(javascript) { result ->

            if (result == "\"fullscreen_exited\"") {

                // Fullscreen was handled by JavaScript.
                webView.requestFocus()

            } else {

                // Website wasn't fullscreen.
                normalBackNavigation()
            }
        }
    }

    /**
     * Normal BACK behaviour when we're definitely
     * not in fullscreen.
     */
    private fun normalBackNavigation() {

        Log.d(
            "TV_KATNISS",
            "normalBackNavigation canGoBack=${webView.canGoBack()} shouldSearchOnResume=$shouldSearchOnResume pendingVoiceQuery=$pendingVoiceQuery ignoreBackAfterVoiceSearch=$ignoreBackAfterVoiceSearch"
        )

        if (ignoreBackAfterVoiceSearch) {

            Log.d(
                "TV_KATNISS",
                "Ignore delayed BACK after voice search"
            )

            return
        }

        if (pendingVoiceQuery != null) {

            Log.d(
                "TV_KATNISS",
                "Ignore BACK during voice search restore"
            )

            return
        }

        if (webView.canGoBack()) {

            webView.goBack()

            webView.postDelayed({

                applyCurrentPageLayout()

            }, 500)

        } else {

            Log.d(
                "TV_KATNISS",
                "normalBackNavigation -> finish()"
            )

            finish()
        }
    }



    override fun onSaveInstanceState(outState: Bundle) {

        webView.saveState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        Log.d(
            "TV_KATNISS",
            "MainActivity onDestroy"
        )

        if (customView != null) {
            exitNativeFullscreen()
        }

        speechRecognizer?.destroy()
        speechRecognizer = null

        webView.stopLoading()
        webView.destroy()

        if (::katnissVoiceMonitor.isInitialized) {
            katnissVoiceMonitor.stop()
        }

        super.onDestroy()
    }
}



//MainActivity
//│
//├── setup WebView
//├── setup Back
//└── connect managers