package com.ikanbot.tv.javascript

object SearchNavigationScripts {

    fun install(): String = """
        (function() {

            if (window.tvSearchNavigationInstalled) {
                return;
            }

            window.tvSearchNavigationInstalled = true;
            window.tvSearchInputMode = false;

            // ========================================
            // Find Search structure
            // ========================================

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

            var searchArea =
                input
                    ? input.closest('.green_window')
                    : null;

            if (
                !searchButton ||
                !form ||
                !input ||
                !searchArea
            ) {
                console.log(
                    'TV SEARCH: structure not found'
                );
                return;
            }

            console.log(
                'TV SEARCH: structure found'
            );

            // ========================================
            // Setup
            // ========================================

            searchArea.classList.add(
                'tv-search-focus'
            );

            input.classList.add(
                'tv-search-input'
            );

            searchButton.classList.add(
                'tv-search-button'
            );

            // Remote navigation initially focuses
            // wrapper, NOT real input.
            searchArea.setAttribute(
                'tabindex',
                '0'
            );

            input.setAttribute(
                'tabindex',
                '-1'
            );

            searchButton.setAttribute(
                'tabindex',
                '0'
            );

            // ========================================
            // Focus styles
            // ========================================

            var style =
                document.createElement('style');

            style.id =
                'tv-search-navigation-style';

            style.textContent = `

                .tv-search-focus {
                    position: relative !important;
                    outline: none !important;
                }
                
                .tv-search-focus:focus {
                    outline: none !important;
                }
                
                .tv-search-focus:focus::after {
                    content: "";
                    position: absolute;
                
                    left: 0;
                    top: 0;
                
                    width: calc(100% - 66px);
                    height: 100%;
                
                    box-sizing: border-box;
                
                    border: 4px solid #FFBF00;
                    border-radius: 4px;
                
                    pointer-events: none;
                }
                
                .tv-search-button {
                    outline: none !important;
                }
                
                .tv-search-button:focus {
                    outline: 4px solid #FFBF00 !important;
                    outline-offset: 3px !important;
                }
                
                .tv-search-input:focus {
                    outline: none !important;
                }

            `;

            document.head.appendChild(style);

            // ========================================
            // Remote Search Input Mode
            // ========================================
            function enterRemoteInputMode(showKeyboard) {

                console.log(
                    'TV SEARCH: REMOTE INPUT MODE'
                );
            
                window.tvSearchInputMode = true;
            
                input.setAttribute(
                    'tabindex',
                    '0'
                );
            
                input.focus();
            
                console.log(
                    'TV SEARCH: INPUT FOCUSED',
                    document.activeElement === input
                );
            
                if (
                    showKeyboard &&
                    typeof window.AndroidTV !== 'undefined' &&
                    typeof window.AndroidTV.showKeyboard === 'function'
                ) {
            
                    console.log(
                        'TV SEARCH: REQUEST GBOARD'
                    );
            
                    window.AndroidTV.showKeyboard();
                }
            }
            
            function exitInputMode() {

                console.log(
                    'TV SEARCH: EXIT INPUT MODE'
                );

                window.tvSearchInputMode = false;

                input.setAttribute(
                    'tabindex',
                    '-1'
                );

                input.blur();

                searchArea.focus();
            }

            // ========================================
            // Search Area
            // ========================================

            searchArea.addEventListener(
                'keydown',
                function(e) {

                    // OK / Enter
                    if (
                        e.key === 'Enter' ||
                        e.keyCode === 13
                    ) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        enterRemoteInputMode(true);

                        return false;
                    }

                    // RIGHT -> Search Button
                    if (
                        e.key === 'ArrowRight' ||
                        e.keyCode === 39
                    ) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        searchArea.blur();
                        searchButton.focus();

                        return false;
                    }

                },
                true
            );

            // ========================================
            // Search Button
            // ========================================

            searchButton.addEventListener(
                'keydown',
                function(e) {

                    // LEFT -> Search Area
                    if (
                        e.key === 'ArrowLeft' ||
                        e.keyCode === 37
                    ) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        searchButton.blur();
                        searchArea.focus();

                        return false;
                    }

                    // IMPORTANT:
                    // Don't intercept Enter here.
                    //
                    // The original button is:
                    // type="submit"
                    //
                    // So Enter / Remote OK should use
                    // the website's normal form submit.

                },
                true
            );

            // ========================================
            // Mouse
            // ========================================

            input.addEventListener(
                'mousedown',
                function() {

                    console.log(
                        'TV SEARCH: MOUSE INPUT'
                    );

                    window.tvSearchInputMode = true;

                    input.setAttribute(
                        'tabindex',
                        '0'
                    );

                    // IMPORTANT:
                    // Do NOT call showKeyboard().
                },
                true
            );

            // ========================================
            // Real Search Input / Gboard
            // ========================================
            
            input.addEventListener(
                'keydown',
                function(e) {
            
                    console.log(
                        'TV SEARCH INPUT KEY:',
                        e.key,
                        e.keyCode
                    );
                    
            
                    // IMPORTANT:
                    //
                    // While the real input / Gboard is active,
                    // DON'T intercept:
                    //
                    // LEFT / RIGHT / UP / DOWN
                    // ENTER / OK
                    // Number keys
                    //
                    // Let Android / Gboard handle them.
            
                    // Only ESC exits Search Input Mode.
                    if (
                        e.key === 'Escape' ||
                        e.keyCode === 27
                    ) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
            
                        exitInputMode();
            
                        return false;
                    }
                },
                true
            );

            // ========================================
            // Empty Search protection
            // ========================================

            form.addEventListener(
                'submit',
                function(e) {

                    var value =
                        input.value.trim();

                    if (!value) {
                        e.preventDefault();

                        console.log(
                            'TV SEARCH: EMPTY'
                        );

                        return false;
                    }

                    console.log(
                        'TV SEARCH: SUBMIT',
                        value
                    );

                    // Do NOT preventDefault here.
                    // Let ikanbot perform its
                    // original /search?q=... action.
                }
            );

            console.log(
                'TV SEARCH NAVIGATION INSTALLED'
            );
            var searchInput =
                document.querySelector(
                    'input[name="q"]'
                );

            var searchButton =
                document.querySelector(
                    '#search_btn'
                );

            if (
                searchInput &&
                searchButton &&
                !searchInput.dataset.tvEnterSearchInstalled
            ) {

                searchInput.dataset.tvEnterSearchInstalled =
                    '1';

                searchInput.addEventListener(
                    'keydown',
                    function(e) {

                        if (e.key === 'Enter') {

                            e.preventDefault();

                            console.log(
                                'TV SEARCH ENTER'
                            );

                            searchButton.click();
                        }
                    }
                );
            }

        })();
    """.trimIndent()
}