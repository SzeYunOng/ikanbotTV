package com.ikanbot.tv.javascript

object VideoPlayerScripts {

    fun install(): String {
        return """
            (function() {
            
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
                    
                        // ========================================
                        // Search / Gboard owns remote
                        // ========================================

                        if (window.tvSearchInputMode) {
                            return;
                        }

    
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
                        // PLAYBACK SPEED - OK / ENTER
                        // =====================================================

                        var playbackRateButton =
                            player.querySelector(
                                '.vjs-playback-rate'
                            );

                        if (
                            playbackRateButton &&
                            (
                                current === playbackRateButton ||
                                playbackRateButton.contains(current)
                            ) &&
                            (
                                e.key === 'Enter' ||
                                e.keyCode === 13
                            )
                        ) {

                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();


                            var video =
                                player.querySelector('video');

                            if (!video) {
                                return false;
                            }


                            // =============================================
                            // AVAILABLE TV PLAYBACK SPEEDS
                            // =============================================

                            var playbackRates = [
                                0.5,
                                0.75,
                                1,
                                1.25,
                                1.5,
                                2
                            ];


                            var currentRate =
                                video.playbackRate || 1;


                            var currentRateIndex =
                                playbackRates.findIndex(
                                    function(rate) {

                                        return Math.abs(
                                            rate - currentRate
                                        ) < 0.01;
                                    }
                                );


                            // Unknown rate -> start from 1x
                            if (currentRateIndex === -1) {

                                currentRateIndex =
                                    playbackRates.indexOf(1);
                            }


                            var nextRateIndex =
                                currentRateIndex + 1;


                            // Last speed -> return to 0.5x
                            if (
                                nextRateIndex >=
                                playbackRates.length
                            ) {

                                nextRateIndex = 0;
                            }


                            var nextRate =
                                playbackRates[
                                    nextRateIndex
                                ];


                            // =============================================
                            // APPLY SPEED
                            // =============================================

                            video.playbackRate =
                                nextRate;


                            // =============================================
                            // UPDATE VIDEO.JS BUTTON TEXT
                            // =============================================

                            var rateValue =
                                playbackRateButton.querySelector(
                                    '.vjs-playback-rate-value'
                                );

                            if (rateValue) {

                                rateValue.textContent =
                                    nextRate + 'x';
                            }


                            console.log(
                                'TV PLAYBACK RATE:',
                                currentRate,
                                '->',
                                nextRate
                            );


                            // Keep focus on 1x button
                            focusControl(
                                playbackRateButton
                            );


                            return false;
                        }
                        
                        // =====================================================
                        // PICTURE IN PICTURE - OK / ENTER
                        // =====================================================

                        var pipButton =
                            player.querySelector(
                                '.vjs-picture-in-picture-control'
                            );

                        if (
                            pipButton &&
                            (
                                current === pipButton ||
                                pipButton.contains(current)
                            ) &&
                            (
                                e.key === 'Enter' ||
                                e.keyCode === 13
                            )
                        ) {

                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();

                            console.log(
                                'TV PIP OK'
                            );

                            if (
                                typeof window.AndroidTV !== 'undefined' &&
                                typeof window.AndroidTV.enterPictureInPicture === 'function'
                            ) {

                                console.log(
                                    'TV PIP -> ANDROID'
                                );

                                window.AndroidTV.enterPictureInPicture();

                            } else {

                                console.log(
                                    'AndroidTV PiP bridge not found'
                                );
                            }

                            return false;
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
    
    
                            // =================================================
                            // RIGHT END -> WRAP TO FIRST CONTROL
                            // =================================================
                            
                            var firstIndex = 0;
                            
                            while (
                                firstIndex < controls.length
                            ) {
                            
                                var firstControl =
                                    controls[firstIndex];
                            
                                if (
                                    focusControl(firstControl)
                                ) {
                            
                                    console.log(
                                        'TV RIGHT WRAP:',
                                        index,
                                        '->',
                                        firstIndex,
                                        firstControl.className
                                    );
                            
                                    return false;
                                }
                            
                                firstIndex++;
                            }
                            
                            console.log(
                                'TV RIGHT: NO FOCUSABLE CONTROL'
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
    
    
                            // =================================================
                            // LEFT START -> WRAP TO LAST CONTROL
                            // =================================================
                            
                            var lastIndex =
                                controls.length - 1;
                            
                            while (
                                lastIndex >= 0
                            ) {
                            
                                var lastControl =
                                    controls[lastIndex];
                            
                                if (
                                    focusControl(lastControl)
                                ) {
                            
                                    console.log(
                                        'TV LEFT WRAP:',
                                        index,
                                        '->',
                                        lastIndex,
                                        lastControl.className
                                    );
                            
                                    return false;
                                }
                            
                                lastIndex--;
                            }
                            
                            console.log(
                                'TV LEFT: NO FOCUSABLE CONTROL'
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
        """.trimIndent()
    }
}




//VideoPlayerScripts
//├── Video Screen focus
//├── Control Bar
//├── LEFT / RIGHT
//├── Progress
//├── Playback Speed
// ├── PiP
//└── Fullscreen button