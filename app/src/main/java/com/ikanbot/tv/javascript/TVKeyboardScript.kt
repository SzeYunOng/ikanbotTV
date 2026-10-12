package com.ikanbot.tv.javascript

object TVKeyboardScripts {

    fun install(): String = """
        (function() {

            if (window.tvKeyboardInstalled) {
                return;
            }

            window.tvKeyboardInstalled = true;
            window.tvKeyboardOpen = false;

            var targetInput = null;
            var currentIndex = 0;

            var rows = [
                ['1','2','3','4','5','6','7','8','9','0'],
                ['Q','W','E','R','T','Y','U','I','O','P'],
                ['A','S','D','F','G','H','J','K','L'],
                ['Z','X','C','V','B','N','M'],
                ['SPACE','BACKSPACE','SEARCH','CLOSE']
            ];

            // ========================================
            // Keyboard container
            // ========================================

            var keyboard =
                document.createElement('div');

            keyboard.id = 'tv-keyboard';

            rows.forEach(function(row, rowIndex) {

                var rowElement =
                    document.createElement('div');

                rowElement.className =
                    'tv-keyboard-row';

                row.forEach(function(value, columnIndex) {

                    var key =
                        document.createElement('button');

                    key.type = 'button';

                    key.className =
                        'tv-keyboard-key';

                    key.dataset.value = value;
                    key.dataset.row = rowIndex;
                    key.dataset.column = columnIndex;

                    if (value === 'SPACE') {
                        key.textContent = 'Space';
                        key.classList.add('tv-key-wide');
                    }
                    else if (value === 'BACKSPACE') {
                        key.textContent = '⌫';
                    }
                    else if (value === 'SEARCH') {
                        key.textContent = '搜索';
                        key.classList.add('tv-key-search');
                    }
                    else if (value === 'CLOSE') {
                        key.textContent = '关闭';
                    }
                    else {
                        key.textContent = value;
                    }

                    key.setAttribute(
                        'tabindex',
                        '-1'
                    );

                    rowElement.appendChild(key);
                });

                keyboard.appendChild(rowElement);
            });

            document.documentElement.appendChild(
                keyboard
            );

            // ========================================
            // CSS
            // ========================================

            var style =
                document.createElement('style');

            style.textContent = `

                #tv-keyboard {
                    position: fixed;

                    left: 50%;
                    bottom: 40px;

                    transform: translateX(-50%);

                    width: 900px;

                    padding: 24px;

                    background: rgba(20,20,20,0.97);

                    border-radius: 14px;

                    z-index: 2147483646;

                    display: none;

                    box-sizing: border-box;
                }

                #tv-keyboard.tv-keyboard-visible {
                    display: block;
                }

                .tv-keyboard-row {
                    display: flex;
                    justify-content: center;
                    gap: 10px;

                    margin-bottom: 10px;
                }

                .tv-keyboard-key {
                    min-width: 70px;
                    height: 58px;

                    padding: 0 14px;

                    border: 2px solid #666;
                    border-radius: 7px;

                    background: #333;
                    color: white;

                    font-size: 24px;

                    box-sizing: border-box;
                }

                .tv-keyboard-key:focus {
                    outline: 4px solid #FFBF00 !important;
                    outline-offset: 2px;

                    background: #555;
                }

                .tv-key-wide {
                    min-width: 220px;
                }

                .tv-key-search {
                    min-width: 120px;
                }

            `;

            document.head.appendChild(style);

            // ========================================
            // Helpers
            // ========================================

            function getKeys() {
                return Array.from(
                    keyboard.querySelectorAll(
                        '.tv-keyboard-key'
                    )
                );
            }

            function focusKey(key) {

                if (!key) {
                    return;
                }

                getKeys().forEach(function(item) {
                    item.setAttribute(
                        'tabindex',
                        '-1'
                    );
                });

                key.setAttribute(
                    'tabindex',
                    '0'
                );

                key.focus();

                currentIndex =
                    getKeys().indexOf(key);
            }

            function getCurrentKey() {
                return getKeys()[currentIndex];
            }

            function findDirectionalKey(
                current,
                direction
            ) {

                var keys = getKeys();

                var currentRect =
                    current.getBoundingClientRect();

                var cx =
                    currentRect.left +
                    currentRect.width / 2;

                var cy =
                    currentRect.top +
                    currentRect.height / 2;

                var best = null;
                var bestScore = Infinity;

                keys.forEach(function(candidate) {

                    if (candidate === current) {
                        return;
                    }

                    var rect =
                        candidate.getBoundingClientRect();

                    var x =
                        rect.left +
                        rect.width / 2;

                    var y =
                        rect.top +
                        rect.height / 2;

                    var dx = x - cx;
                    var dy = y - cy;

                    if (
                        direction === 'left' &&
                        dx >= -1
                    ) {
                        return;
                    }

                    if (
                        direction === 'right' &&
                        dx <= 1
                    ) {
                        return;
                    }

                    if (
                        direction === 'up' &&
                        dy >= -1
                    ) {
                        return;
                    }

                    if (
                        direction === 'down' &&
                        dy <= 1
                    ) {
                        return;
                    }

                    var primary;
                    var secondary;

                    if (
                        direction === 'left' ||
                        direction === 'right'
                    ) {
                        primary = Math.abs(dx);
                        secondary = Math.abs(dy);
                    } else {
                        primary = Math.abs(dy);
                        secondary = Math.abs(dx);
                    }

                    var score =
                        primary +
                        secondary * 2;

                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                });

                return best;
            }

            function updateInput(value) {

                if (!targetInput) {
                    return;
                }

                var oldValue =
                    targetInput.value;

                targetInput.value = value;

                // Important for website/framework
                targetInput.dispatchEvent(
                    new Event(
                        'input',
                        {
                            bubbles: true
                        }
                    )
                );

                console.log(
                    'TV KEYBOARD INPUT:',
                    oldValue,
                    '->',
                    value
                );
            }

            // ========================================
            // Key actions
            // ========================================

            function activateKey(key) {

                if (!key || !targetInput) {
                    return;
                }

                var value =
                    key.dataset.value;

                if (value === 'SPACE') {

                    updateInput(
                        targetInput.value + ' '
                    );

                    return;
                }

                if (value === 'BACKSPACE') {

                    updateInput(
                        targetInput.value.slice(
                            0,
                            -1
                        )
                    );

                    return;
                }

                if (value === 'SEARCH') {

                    var form =
                        targetInput.closest('form');

                    var searchButton =
                        form
                            ? form.querySelector(
                                '#search_btn'
                              )
                            : null;

                    if (
                        targetInput.value.trim() &&
                        form
                    ) {
                        if (
                            typeof form.requestSubmit ===
                            'function'
                        ) {
                            form.requestSubmit(
                                searchButton || undefined
                            );
                        }
                        else if (searchButton) {
                            searchButton.click();
                        }
                        else {
                            form.submit();
                        }
                    }

                    return;
                }

                if (value === 'CLOSE') {
                    window.TVKeyboard.close();
                    return;
                }

                updateInput(
                    targetInput.value + value
                );
            }

            // ========================================
            // Public API
            // ========================================

            window.TVKeyboard = {

                open: function(input) {

                    if (!input) {
                        return;
                    }

                    targetInput = input;

                    window.tvKeyboardOpen = true;

                    // This is our remote keyboard,
                    // not Gboard.
                    window.tvSearchInputMode = true;

                    keyboard.classList.add(
                        'tv-keyboard-visible'
                    );

                    var firstKey =
                        getKeys()[0];

                    currentIndex = 0;

                    focusKey(firstKey);

                    console.log(
                        'TV KEYBOARD OPEN'
                    );
                },

                close: function() {

                    keyboard.classList.remove(
                        'tv-keyboard-visible'
                    );

                    window.tvKeyboardOpen = false;
                    window.tvSearchInputMode = false;

                    if (targetInput) {

                        var area =
                            targetInput.closest(
                                '.green_window'
                            );

                        targetInput.setAttribute(
                            'tabindex',
                            '-1'
                        );

                        if (area) {
                            area.focus();
                        }
                    }

                    console.log(
                        'TV KEYBOARD CLOSED'
                    );
                }
            };

            // ========================================
            // DPAD
            // ========================================

            document.addEventListener(
                'keydown',
                function(e) {

                    if (!window.tvKeyboardOpen) {
                        return;
                    }

                    var key =
                        document.activeElement;

                    if (
                        !key ||
                        !key.classList.contains(
                            'tv-keyboard-key'
                        )
                    ) {
                        key = getCurrentKey();
                        focusKey(key);
                    }

                    var direction = null;

                    if (
                        e.key === 'ArrowLeft' ||
                        e.keyCode === 37
                    ) {
                        direction = 'left';
                    }

                    else if (
                        e.key === 'ArrowRight' ||
                        e.keyCode === 39
                    ) {
                        direction = 'right';
                    }

                    else if (
                        e.key === 'ArrowUp' ||
                        e.keyCode === 38
                    ) {
                        direction = 'up';
                    }

                    else if (
                        e.key === 'ArrowDown' ||
                        e.keyCode === 40
                    ) {
                        direction = 'down';
                    }

                    if (direction) {

                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        var next =
                            findDirectionalKey(
                                key,
                                direction
                            );

                        if (next) {
                            focusKey(next);
                        }

                        return false;
                    }

                    if (
                        e.key === 'Enter' ||
                        e.keyCode === 13
                    ) {

                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        activateKey(key);

                        return false;
                    }

                },
                true
            );

            console.log(
                'TV KEYBOARD INSTALLED'
            );

        })();
    """.trimIndent()
}