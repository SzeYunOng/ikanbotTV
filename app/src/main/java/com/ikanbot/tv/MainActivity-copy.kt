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

class `MainActivity-copy` : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var rootView: FrameLayout

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.rootView)
        webView = findViewById(R.id.webView)

        // Allow Chrome on Mac to inspect the TV WebView
        WebView.setWebContentsDebuggingEnabled(true)

        setupWebView()
        setupBackButton()

        if (savedInstanceState == null) {
            webView.loadUrl("https://www.ikanbot.com/")
        } else {
            webView.restoreState(savedInstanceState)
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

                    val isPlayPage = url.contains("/play/")

                    if (isPlayPage) {

                        view.evaluateJavascript(
                            """
        (function() {

            // =====================================================
            // VIEWPORT
            // =====================================================

            var meta = document.querySelector(
                'meta[name="viewport"]'
            );

            if (!meta) {
                meta = document.createElement('meta');
                meta.name = 'viewport';
                document.head.appendChild(meta);
            }

            meta.setAttribute(
                'content',
                'width=1800, initial-scale=1.0'
            );

            // =====================================================
            // PLAY PAGE LAYOUT
            // =====================================================
            
            // Try to find the Android TV style first
            var style =
                document.getElementById(
                    'android-tv-style'
                );
            
            
            // If it does not exist, create it
            if (!style) {
            
                style =
                    document.createElement(
                        'style'
                    );
            
                style.id =
                    'android-tv-style';
            
                document.head.appendChild(
                    style
                );
            }
            
            
            // =====================================================
            // ALWAYS UPDATE PLAY PAGE STYLE
            // =====================================================
            //
            // Important:
            // Even if android-tv-style already exists,
            // always rewrite it when entering /play/.
            //
            // This prevents an old page style from remaining.
            // =====================================================
            
            style.innerHTML = `
            
                .container {
                    width: 75% !important;
                    max-width: none !important;
                    margin-left: 12.5% !important;
                    margin-right: 12.5% !important;
                }
            
            
                /* =========================================
                   VIDEO.JS CONTROL FOCUS
                   ========================================= */
            
                #ikanbot-player button:focus,
                #ikanbot-player [role="slider"]:focus {
            
                    outline: 4px solid white !important;
                    outline-offset: 2px !important;
                }
            
            
                /* =========================================
                   VIDEO SCREEN FOCUS - NORMAL MODE
                   ========================================= */
                
                #ikanbot-player:focus {
                    outline: none !important;
                
                    box-shadow:
                        inset 0 0 0 10px #FFBF00 !important;
                }
                
                
                /* =========================================
                   VIDEO SCREEN FOCUS - FULLSCREEN
                   Hide yellow border
                   ========================================= */
                
                #ikanbot-player.tv-native-fullscreen:focus {
                    outline: none !important;
                    box-shadow: none !important;
                }
            
            `;
            
            
            // =====================================================
            // PAGE OVERFLOW
            // =====================================================
            
            document.documentElement.style.overflowX =
                'hidden';
            
            document.body.style.overflowX =
                'hidden';
            
            // =====================================================
            // DEBUG PLAY PAGE LAYOUT
            // =====================================================

            setTimeout(function() {

                var containers =
                    document.querySelectorAll(
                        '.container'
                    );

                console.log(
                    'TV DEBUG container count =',
                    containers.length
                );

                containers.forEach(
                    function(el, index) {

                        var css =
                            window.getComputedStyle(el);

                        var rect =
                            el.getBoundingClientRect();

                        console.log(
                            'TV DEBUG CONTAINER',
                            index,
                            'class=',
                            el.className,
                            'width=',
                            css.width,
                            'marginLeft=',
                            css.marginLeft,
                            'marginRight=',
                            css.marginRight,
                            'rectWidth=',
                            rect.width
                        );
                    }
                );


                var player =
                    document.querySelector(
                        '#ikanbot-player'
                    );

                if (player) {

                    var playerRect =
                        player.getBoundingClientRect();

                    var parent =
                        player.parentElement;

                    console.log(
                        'TV DEBUG PLAYER WIDTH =',
                        playerRect.width
                    );

                    console.log(
                        'TV DEBUG PLAYER PARENT =',
                        parent
                            ? parent.tagName +
                              ' .' +
                              parent.className
                            : 'NONE'
                    );

                    console.log(
                        'TV DEBUG PLAYER PARENT WIDTH =',
                        parent
                            ? parent.getBoundingClientRect().width
                            : 'NONE'
                    );
                }

            }, 1000);
            
            // =====================================================
            // PLAYER
            // =====================================================
            
            var player =
                document.querySelector(
                    '#ikanbot-player'
                );
            
            // =====================================================
            // PLAYER
            // =====================================================

            var player = document.querySelector(
                '#ikanbot-player'
            );

            if (!player) {

                console.log(
                    'TV NAV: player not found'
                );

                return;
            }
            
            // =====================================================
            // VIDEO SCREEN / FIRST PLAY
            // =====================================================

            // Make the whole video screen focusable
            player.setAttribute(
                'tabindex',
                '0'
            );

            var video =
                player.querySelector('video');

            var bigPlayButton =
                player.querySelector(
                    '.vjs-big-play-button'
                );


            // Has this video started before?
            if (
                typeof window.tvVideoHasStarted ===
                'undefined'
            ) {

                window.tvVideoHasStarted =
                    video &&
                    (
                        video.currentTime > 0 ||
                        !video.paused
                    );
            }


            // Once playback starts, remember it.
            if (
                video &&
                !window.tvVideoPlayListenerInstalled
            ) {

                window.tvVideoPlayListenerInstalled = true;

                video.addEventListener(
                    'play',
                    function() {
                
                        // Must check this BEFORE setting it to true
                        var isFirstTVPlay =
                            !window.tvVideoHasStarted;
                
                        window.tvVideoHasStarted = true;
                
                        console.log(
                            'TV VIDEO HAS STARTED',
                            'first=',
                            isFirstTVPlay
                        );
                
                        // Only first play returns to Video Screen
                        if (isFirstTVPlay) {
                
                            setTimeout(
                                function() {
                
                                    hideControlBar();
                
                                },
                                500
                            );
                        }
                    }
                );
                }
            
            // =====================================================
            // PREPARE PICTURE IN PICTURE BUTTON
            // =====================================================

            var pipButton = player.querySelector(
                '.vjs-picture-in-picture-control'
            );

            if (pipButton) {

                pipButton.disabled = false;

                pipButton.removeAttribute(
                    'disabled'
                );

                pipButton.classList.remove(
                    'vjs-disabled'
                );

                pipButton.setAttribute(
                    'aria-disabled',
                    'false'
                );

                pipButton.setAttribute(
                    'tabindex',
                    '0'
                );

                console.log(
                    'TV PIP prepared:',
                    pipButton.className
                );
            }


            // =====================================================
            // VOLUME SLIDER
            //
            // Physical TV remote VOL+/VOL- handles volume.
            // The internal slider must NOT steal focus.
            // =====================================================

            var volumeBar = player.querySelector(
                '.vjs-volume-bar'
            );

            if (volumeBar) {

                volumeBar.setAttribute(
                    'tabindex',
                    '-1'
                );
            }
            
           // =====================================================
            // PICTURE IN PICTURE
            // Enable button for Android TV navigation
            // =====================================================
            
            var pipButton = player.querySelector(
                '.vjs-picture-in-picture-control'
            );
            
            if (pipButton) {
            
                // Remove Video.js disabled state
                pipButton.disabled = false;
            
                pipButton.removeAttribute(
                    'disabled'
                );
            
                pipButton.classList.remove(
                    'vjs-disabled'
                );
            
                pipButton.setAttribute(
                    'aria-disabled',
                    'false'
                );
            
                pipButton.setAttribute(
                    'tabindex',
                    '0'
                );
            
                console.log(
                    'TV PIP enabled:',
                    pipButton.disabled,
                    pipButton.className
                );
            }


            // =====================================================
            // PROGRESS MODE
            // =====================================================

            if (
                typeof window.tvProgressAdjustMode ===
                'undefined'
            ) {

                window.tvProgressAdjustMode = false;
            }


            // =====================================================
            // DON'T INSTALL TWICE
            // =====================================================

            if (window.tvDocumentNavigationInstalled) {

                console.log(
                    'Android TV document navigation already installed'
                );

                return;
            }

            window.tvDocumentNavigationInstalled = true;


            // =====================================================
            // GET CONTROLS
            // =====================================================

            function getTVControls() {

                var list = [

                    // 0 - Play / Pause
                    player.querySelector(
                        '.vjs-play-control'
                    ),

                    // 1 - Back 10 seconds
                    player.querySelector(
                        '.vjs-skip-backward-10'
                    ),

                    // 2 - Forward 10 seconds
                    player.querySelector(
                        '.vjs-skip-forward-10'
                    ),

                    // 3 - Mute / Volume button
                    player.querySelector(
                        '.vjs-mute-control'
                    ),

                    // 4 - Progress
                    player.querySelector(
                        '.vjs-progress-holder'
                    ),

                    // 5 - Seek to live
                    player.querySelector(
                        '.vjs-seek-to-live-control'
                    ),

                    // 6 - Playback rate / 1x
                    player.querySelector(
                        '.vjs-playback-rate'
                    ),

                    // 7 - Chapters
                    player.querySelector(
                        '.vjs-chapters-button'
                    ),

                    // 8 - Description
                    player.querySelector(
                        '.vjs-descriptions-button'
                    ),

                    // 9 - Subtitle
                    player.querySelector(
                        '.vjs-subs-caps-button'
                    ),

                    // 10 - Audio
                    player.querySelector(
                        '.vjs-audio-button'
                    ),

                    // 11 - Picture in Picture
                    player.querySelector(
                        '.vjs-picture-in-picture-control'
                    ),

                    // 12 - Fullscreen
                    player.querySelector(
                        '.vjs-fullscreen-control'
                    )

                ];


                // Remove only controls that don't exist.
                //
                // IMPORTANT:
                // Do NOT use offsetParent here.
                //
                // PiP may currently be marked disabled by Video.js,
                // but we still want it in our TV navigation order.

                return list.filter(function(el) {
                    return el !== null;
                });
            }


            // =====================================================
            // FIND WHICH TV CONTROL CURRENTLY HAS FOCUS
            // =====================================================

            function findCurrentControl(
                controls,
                current
            ) {

                // Direct match
                var index =
                    controls.indexOf(current);

                if (index !== -1) {
                    return index;
                }


                // Sometimes Video.js focuses an element
                // inside a menu button rather than the
                // outer control itself.

                for (
                    var i = 0;
                    i < controls.length;
                    i++
                ) {

                    var control = controls[i];

                    if (
                        control &&
                        control.contains &&
                        control.contains(current)
                    ) {

                        return i;
                    }
                }


                return -1;
            }

            // =====================================================
            // FOCUS CONTROL
            // =====================================================

            function focusControl(control) {

                if (!control) {
                    return false;
                }


                // =================================================
                // PiP special handling
                // =================================================

                if (
                    control.classList.contains(
                        'vjs-picture-in-picture-control'
                    )
                ) {

                    control.disabled = false;

                    control.removeAttribute(
                        'disabled'
                    );

                    control.classList.remove(
                        'vjs-disabled'
                    );

                    control.setAttribute(
                        'aria-disabled',
                        'false'
                    );

                    control.setAttribute(
                        'tabindex',
                        '0'
                    );
                }


                // =================================================
                // Make control focusable
                // =================================================

                if (
                    control.getAttribute('tabindex') === '-1'
                ) {

                    control.setAttribute(
                        'tabindex',
                        '0'
                    );
                }


                // =================================================
                // Focus
                // =================================================

                try {

                    control.focus({
                        preventScroll: true
                    });

                } catch (error) {

                    control.focus();
                }


                // =================================================
                // Check focus result
                // =================================================

                var success =
                    document.activeElement === control ||
                    control.contains(
                        document.activeElement
                    );


                console.log(
                    'TV FOCUS:',
                    control.className,
                    'success=',
                    success
                );


                return success;
            }


            // =====================================================
            // SHOW CONTROL BAR
            // =====================================================

            function showControlBar() {

                player.classList.add(
                    'vjs-user-active'
                );

                player.classList.remove(
                    'vjs-user-inactive'
                );


                var controlBar =
                    player.querySelector(
                        '.vjs-control-bar'
                    );


                if (controlBar) {

                    controlBar.style.setProperty(
                        'visibility',
                        'visible',
                        'important'
                    );

                    controlBar.style.setProperty(
                        'opacity',
                        '1',
                        'important'
                    );

                    controlBar.style.removeProperty(
                        'pointer-events'
                    );
                }


                var playButton =
                    player.querySelector(
                        '.vjs-play-control'
                    );


                if (playButton) {

                    focusControl(
                        playButton
                    );
                }


                console.log(
                    'TV CONTROL BAR: SHOW'
                );
            }


            // =====================================================
            // HIDE CONTROL BAR
            // =====================================================

            function hideControlBar() {

                player.classList.remove(
                    'vjs-user-active'
                );

                player.classList.add(
                    'vjs-user-inactive'
                );


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
                    'TV CONTROL BAR: HIDE'
                );

                console.log(
                    'TV VIDEO SCREEN FOCUS:',
                    document.activeElement,
                    'success=',
                    document.activeElement === player
                );
            }
                
            // =====================================================
            // DOCUMENT KEYDOWN - CAPTURE PHASE
            // =====================================================

            document.addEventListener(
                'keydown',
                function(e) {

                    var current =
                        document.activeElement;
                        
                    // =====================================================
                    // VIDEO SCREEN - OK
                    // =====================================================
                    
                    if (
                        current === player &&
                        (
                            e.key === 'Enter' ||
                            e.keyCode === 13
                        )
                    ) {
                    
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                    
                    
                        // =============================================
                        // FIRST LOAD
                        // First OK = click center Big Play button
                        // =============================================
                    
                        if (!window.tvVideoHasStarted) {
                    
                            var bigPlay =
                                player.querySelector(
                                    '.vjs-big-play-button'
                                );
                    
                            if (bigPlay) {
                    
                                bigPlay.click();
                    
                                console.log(
                                    'TV FIRST PLAY'
                                );
                            }
                    
                            return false;
                        }
                    
                    
                        // =============================================
                        // VIDEO ALREADY STARTED
                        // OK = enter Control Mode
                        // =============================================
                    
                        showControlBar();
                    
                        return false;
                    }
                    
                    
                   // =====================================================
                    // CONTROL BAR - DOWN
                    // Control -> Video Screen
                    // =====================================================
                    
                    if (
                        e.key === 'ArrowDown' ||
                        e.keyCode === 40
                    ) {
                    
                        var controls =
                            getTVControls();
                    
                        var controlIndex =
                            findCurrentControl(
                                controls,
                                current
                            );
                    
                    
                        // =================================================
                        // Currently inside Control Bar
                        // =================================================
                    
                        if (controlIndex !== -1) {
                    
                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();
                    
                            console.log(
                                'TV DOWN: CONTROL -> VIDEO SCREEN'
                            );
                    
                            hideControlBar();
                    
                            return false;
                        }
                    
                    
                        // =================================================
                        // Already on Video Screen
                        // Let WebView handle DOWN naturally
                        // =================================================
                    
                        if (current === player) {
                    
                            console.log(
                                'TV DOWN: VIDEO SCREEN -> PAGE'
                            );
                    
                            // IMPORTANT:
                            // Do not preventDefault here.
                            // Do not stopPropagation here.
                    
                            return;
                        }
                    }
                    
                    
                    // =====================================================
                    // PROGRESS
                    // =====================================================
                    
                    var progress =
                        player.querySelector(
                            '.vjs-progress-holder'
                        ); 

                    // =================================================
                    // PROGRESS BAR
                    // OK = enter / exit adjustment mode
                    // =================================================

                    if (
                        current === progress &&
                        (
                            e.key === 'Enter' ||
                            e.keyCode === 13
                        )
                    ) {

                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();


                        window.tvProgressAdjustMode =
                            !window.tvProgressAdjustMode;


                        console.log(
                            'Progress adjust mode:',
                            window.tvProgressAdjustMode
                        );


                        return false;
                    }


                    // =================================================
                    // PROGRESS ADJUST MODE
                    // =================================================

                    if (
                        current === progress &&
                        window.tvProgressAdjustMode === true
                    ) {

                        var video =
                            player.querySelector(
                                'video'
                            );


                        if (!video) {
                            return;
                        }


                        // ---------------------------------------------
                        // RIGHT = +10 seconds
                        // ---------------------------------------------

                        if (
                            e.key === 'ArrowRight' ||
                            e.keyCode === 39
                        ) {

                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();


                            var forwardTime =
                                video.currentTime + 10;


                            if (
                                isFinite(
                                    video.duration
                                )
                            ) {

                                forwardTime =
                                    Math.min(
                                        video.duration,
                                        forwardTime
                                    );
                            }


                            video.currentTime =
                                forwardTime;


                            console.log(
                                'TV SEEK +10:',
                                video.currentTime
                            );


                            return false;
                        }


                        // ---------------------------------------------
                        // LEFT = -10 seconds
                        // ---------------------------------------------

                        if (
                            e.key === 'ArrowLeft' ||
                            e.keyCode === 37
                        ) {

                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();


                            video.currentTime =
                                Math.max(
                                    0,
                                    video.currentTime - 10
                                );


                            console.log(
                                'TV SEEK -10:',
                                video.currentTime
                            );


                            return false;
                        }
                    }
                    
                        // =====================================================
                        // FULLSCREEN - OK / ENTER
                        // =====================================================
                        
                        var fullscreenButton =
                            player.querySelector(
                                '.vjs-fullscreen-control'
                            );
                        
                        if (
                            current === fullscreenButton &&
                            (
                                e.key === 'Enter' ||
                                e.keyCode === 13
                            )
                        ) {
                        
                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();
                        
                            console.log(
                                'TV FULLSCREEN OK'
                            );
                        
                        
                            // =================================================
                            // CALL ANDROID NATIVE FULLSCREEN
                            // =================================================
                        
                            if (
                                typeof window.AndroidTV !== 'undefined' &&
                                typeof window.AndroidTV.enterFullscreen === 'function'
                            ) {
                        
                                console.log(
                                    'TV FULLSCREEN -> ANDROID'
                                );
                        
                                window.AndroidTV.toggleFullscreen();
                        
                            } else {
                        
                                console.log(
                                    'AndroidTV bridge not found'
                                );
                            }
                        
                        
                            return false;
                        }
                        
                    // =================================================
                    // NORMAL LEFT / RIGHT
                    // =================================================

                    var isRight =
                        e.key === 'ArrowRight' ||
                        e.keyCode === 39;


                    var isLeft =
                        e.key === 'ArrowLeft' ||
                        e.keyCode === 37;


                    if (!isRight && !isLeft) {
                        return;
                    }


                    var controls =
                        getTVControls();


                    var index =
                        findCurrentControl(
                            controls,
                            current
                        );


                    console.log(
                        'TV CURRENT:',
                        current
                            ? current.className
                            : 'NONE'
                    );

                    console.log(
                        'TV INDEX:',
                        index
                    );


                    // Current focus isn't inside the
                    // Video.js controls.
                    //
                    // Don't steal LEFT/RIGHT from
                    // the rest of the website.

                    if (index === -1) {
                        return;
                    }


                    // =================================================
                    // We own this LEFT / RIGHT.
                    //
                    // This happens during CAPTURE phase,
                    // BEFORE Video.js PlaybackRate/Menu sees it.
                    // =================================================

                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();


                    // =================================================
                    // RIGHT
                    // =================================================

                    if (isRight) {

                        var nextIndex =
                            index + 1;


                        while (
                            nextIndex <
                            controls.length
                        ) {

                            var next =
                                controls[
                                    nextIndex
                                ];


                            if (
                                focusControl(next)
                            ) {

                                console.log(
                                    'TV RIGHT:',
                                    index,
                                    '->',
                                    nextIndex,
                                    next.className
                                );


                                return false;
                            }


                            // Couldn't focus this one.
                            // Try the following control.

                            nextIndex++;
                        }


                        console.log(
                            'TV RIGHT: END'
                        );


                        return false;
                    }


                    // =================================================
                    // LEFT
                    // =================================================

                    if (isLeft) {

                        var previousIndex =
                            index - 1;


                        while (
                            previousIndex >= 0
                        ) {

                            var previous =
                                controls[
                                    previousIndex
                                ];


                            if (
                                focusControl(previous)
                            ) {

                                console.log(
                                    'TV LEFT:',
                                    index,
                                    '->',
                                    previousIndex,
                                    previous.className
                                );


                                return false;
                            }


                            previousIndex--;
                        }


                        console.log(
                            'TV LEFT: START'
                        );


                        return false;
                    }

                },

                // =====================================================
                // VERY IMPORTANT
                //
                // true = CAPTURE PHASE
                //
                // document gets the key BEFORE Video.js / 1x.
                // =====================================================

                true
            );


            console.log(
                'Android TV document navigation installed'
            );

        })();
        """.trimIndent(),
                            null
                        )


                        // =====================================================
                        // WEBVIEW SCALE
                        // =====================================================

                        view.postDelayed({

                            view.setInitialScale(68)

                            view.scrollTo(0, 0)

                        }, 300)
                    } else {

                        // ==========================================
                        // HOME / MOVIES / SERIES / RANKING
                        // ==========================================

                        view.evaluateJavascript(
                            """
                (function() {

                    var meta = document.querySelector(
                        'meta[name="viewport"]'
                    );

                    if (!meta) {
                        meta = document.createElement('meta');
                        meta.name = 'viewport';
                        document.head.appendChild(meta);
                    }

                    meta.setAttribute(
                        'content',
                        'width=1800, initial-scale=1.0'
                    );
                    
                    var style = document.createElement('style');
                    
                    style.innerHTML = `
                        .container {
                            width: 60% !important;
                            max-width: none !important;
                            margin-left: 1% !important;
                            margin-right: 1% !important;
                        }
                    `;
                    
                    document.head.appendChild(style);

                    document.documentElement.style.overflowX = 'hidden';
                    document.body.style.overflowX = 'hidden';

                })();
                """.trimIndent(),
                            null
                        )

                    }
                }




            }
        }

        // Native WebView HTML5 fullscreen support
        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowCustomView(
                view: View?,
                callback: CustomViewCallback?
            ) {

                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                if (view == null) {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback

                webView.visibility = View.GONE

                rootView.addView(
                    view,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                view.requestFocus()
            }

            override fun onHideCustomView() {
                exitNativeFullscreen()
            }
        }

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

    inner class AndroidTVBridge {

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
    }

    private var isTVFullscreen = false

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

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            finish()
        }
    }



    override fun onSaveInstanceState(outState: Bundle) {

        webView.saveState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {

        if (customView != null) {
            exitNativeFullscreen()
        }

        webView.stopLoading()
        webView.destroy()

        super.onDestroy()
    }
}



//MainActivity
//│
//├── setup WebView
//├── setup Back
//└── connect managers