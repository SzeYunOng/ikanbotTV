package com.ikanbot.tv.javascript

object PageLayoutScripts {

    fun playPageSetup(): String {
        return """
            (function() {

                // =============================================
                // VIEWPORT
                // =============================================

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


                // =============================================
                // PAGE OVERFLOW
                // =============================================

                document.documentElement.style.overflowX =
                    'hidden';

                document.body.style.overflowX =
                    'hidden';

            })();
        """.trimIndent()
    }

    fun installPlayStyle(css: String): String {

        val quotedCss =
            org.json.JSONObject.quote(css)

        return """
        (function() {
            var homeStyle =
                document.getElementById(
                    'android-tv-home-style'
                );

            if (homeStyle) {
                homeStyle.remove();
            }

            var style =
                document.getElementById(
                    'android-tv-play-style'
                );

            if (!style) {

                style =
                    document.createElement(
                        'style'
                    );

                style.id =
                    'android-tv-play-style';

                document.head.appendChild(
                    style
                );
            }

            style.innerHTML =
                $quotedCss;

            console.log(
                'TV PLAY STYLE INSTALLED'
            );

        })();
    """.trimIndent()
    }
    fun homePageSetup(): String {
        return """
        (function() {
        
            var playStyle =
                document.getElementById(
                    'android-tv-play-style'
                );

            if (playStyle) {
                playStyle.remove();
            }

            // =============================================
            // VIEWPORT
            // =============================================

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


            // =============================================
            // HOME / MOVIES / SERIES STYLE
            // =============================================

            var style =
                document.getElementById(
                    'android-tv-home-style'
                );

            if (!style) {
                style =
                    document.createElement('style');

                style.id =
                    'android-tv-home-style';

                document.head.appendChild(style);
            }

            style.innerHTML = `

                .container {
                    width: 60% !important;
                    max-width: none !important;
                    margin-left: 1% !important;
                    margin-right: 1% !important;
                }

            `;


            // =============================================
            // PAGE OVERFLOW
            // =============================================

            document.documentElement.style.overflowX =
                'hidden';

            document.body.style.overflowX =
                'hidden';

        })();
    """.trimIndent()
    }

    fun searchPageSetup(): String {
        return """
    (function () {

        // =========================================
        // BACKGROUND
        // =========================================

        document.documentElement.style.background =
            '#ffffff';

        document.body.style.background =
            '#ffffff';


        // =========================================
        // WARNING
        // =========================================

        const notice =
            document.querySelector(
                '.alert.alert-warning'
            );

        if (notice) {
            notice.style.display = 'none';
        }


        // =========================================
        // SEARCH TITLE
        // =========================================

        const searchTitle =
            [...document.querySelectorAll('h1')]
                .find(el =>
                    el.innerText
                        .trim()
                        .startsWith('搜索：')
                );

        if (searchTitle) {
            searchTitle.style.marginTop = '0px';
            searchTitle.style.marginBottom = '6px';
            searchTitle.style.fontSize = '24px';
        }


        // =========================================
        // TOP NAV
        // =========================================

        const navContainer =
            document.querySelector('.nav-container');

        const navLeft =
            document.querySelector('.nav-left');

        const navRight =
            document.querySelector('.nav-right');

        if (navContainer) {
            navContainer.style.setProperty(
                'display',
                'flex',
                'important'
            );

            navContainer.style.setProperty(
                'align-items',
                'center',
                'important'
            );
        }

        if (navLeft) {
            navLeft.style.setProperty(
                'flex',
                '0 0 70px',
                'important'
            );
        }

        if (navRight) {
            navRight.style.setProperty(
                'flex',
                '1 1 auto',
                'important'
            );

            navRight.style.setProperty(
                'width',
                'auto',
                'important'
            );

            navRight.style.setProperty(
                'min-width',
                '0',
                'important'
            );

            navRight.style.setProperty(
                'margin-left',
                '20px',
                'important'
            );

            navRight.style.setProperty(
                'margin-right',
                '20px',
                'important'
            );
        }


        // =========================================
        // SEARCH FORM
        // =========================================

        const form =
            document.querySelector(
                '.nav-right form'
            );

        const searchWindow =
            document.querySelector(
                '.green_window'
            );

        const input =
            document.querySelector('#q');

        const searchButton =
            document.querySelector(
                '#search_btn'
            );

        if (
            form &&
            searchWindow &&
            input &&
            searchButton
        ) {

            form.style.setProperty(
                'display',
                'flex',
                'important'
            );

            form.style.setProperty(
                'align-items',
                'center',
                'important'
            );

            form.style.setProperty(
                'position',
                'static',
                'important'
            );


            // input outer
            searchWindow.style.setProperty(
                'position',
                'static',
                'important'
            );

            searchWindow.style.setProperty(
                'flex',
                '0 0 74%',
                'important'
            );

            searchWindow.style.setProperty(
                'width',
                '74%',
                'important'
            );

            searchWindow.style.setProperty(
                'height',
                '34px',
                'important'
            );

            searchWindow.style.setProperty(
                'min-height',
                '34px',
                'important'
            );

            searchWindow.style.setProperty(
                'padding',
                '0',
                'important'
            );

            searchWindow.style.setProperty(
                'border',
                '1px solid #d8d8d8',
                'important'
            );

            searchWindow.style.setProperty(
                'background',
                '#ffffff',
                'important'
            );

            searchWindow.style.setProperty(
                'box-sizing',
                'border-box',
                'important'
            );


            // actual input
            input.style.setProperty(
                'width',
                '100%',
                'important'
            );

            input.style.setProperty(
                'max-width',
                '100%',
                'important'
            );

            input.style.setProperty(
                'min-width',
                '0',
                'important'
            );

            input.style.setProperty(
                'height',
                '32px',
                'important'
            );

            input.style.setProperty(
                'line-height',
                '32px',
                'important'
            );

            input.style.setProperty(
                'padding-left',
                '12px',
                'important'
            );

            input.style.setProperty(
                'padding-right',
                '12px',
                'important'
            );

            input.style.setProperty(
                'border',
                '0',
                'important'
            );

            input.style.setProperty(
                'outline',
                'none',
                'important'
            );

            input.style.setProperty(
                'background',
                'transparent',
                'important'
            );

            input.style.setProperty(
                'font-size',
                '15px',
                'important'
            );

            input.style.setProperty(
                'box-sizing',
                'border-box',
                'important'
            );


            // search button
            searchButton.style.setProperty(
                'position',
                'static',
                'important'
            );

            searchButton.style.setProperty(
                'display',
                'block',
                'important'
            );

            searchButton.style.setProperty(
                'visibility',
                'visible',
                'important'
            );

            searchButton.style.setProperty(
                'flex',
                '0 0 70px',
                'important'
            );

            searchButton.style.setProperty(
                'width',
                '70px',
                'important'
            );

            searchButton.style.setProperty(
                'min-width',
                '70px',
                'important'
            );

            searchButton.style.setProperty(
                'height',
                '34px',
                'important'
            );

            searchButton.style.setProperty(
                'margin-left',
                '8px',
                'important'
            );

            searchButton.style.setProperty(
                'background',
                '#eeeeee',
                'important'
            );

            searchButton.style.setProperty(
                'color',
                '#333333',
                'important'
            );

            searchButton.style.setProperty(
                'border',
                '1px solid #d8d8d8',
                'important'
            );
        }


        // =========================================
        // NAV PILLS
        // =========================================

        const navs =
            [...document.querySelectorAll(
                'ul.nav.nav-pills'
            )];

        navs.forEach(nav => {

            if (nav.offsetParent === null) return;

            nav.style.setProperty(
                'display',
                'flex',
                'important'
            );

            nav.style.setProperty(
                'align-items',
                'center',
                'important'
            );

            nav.style.setProperty(
                'width',
                '100%',
                'important'
            );

            nav.style.setProperty(
                'padding-left',
                '0px',
                'important'
            );

            nav.style.setProperty(
                'padding-right',
                '20px',
                'important'
            );

            nav.style.setProperty(
                'box-sizing',
                'border-box',
                'important'
            );

            [...nav.children].forEach(li => {

                li.style.float = 'none';
                li.style.whiteSpace = 'nowrap';

                const text =
                    li.innerText.trim();

                if (text === '播放历史') {
                    li.style.marginLeft = '428px';
                }
            });
        });


        // =========================================
        // RESULT GRID
        // =========================================

        const firstMedia =
            document.querySelector('.media');

        if (!firstMedia) {
            return;
        }

        const list =
            firstMedia.parentElement;

        const resultColumn =
            list.parentElement;

        if (resultColumn) {
            resultColumn.style.width = '100%';
            resultColumn.style.maxWidth = 'none';
            resultColumn.style.float = 'none';
        }

        list.style.display = 'grid';

        list.style.gridTemplateColumns =
            '460px 460px';

        list.style.columnGap = '10px';
        list.style.rowGap = '2px';

        list.style.justifyContent = 'start';

        list.style.width = '100%';
        list.style.maxWidth = '100%';
        list.style.paddingLeft = '0px';


        // =========================================
        // RESULT ITEMS
        // =========================================

        document.querySelectorAll('.media')
            .forEach(media => {

                media.style.width = '460px';
                media.style.maxWidth = '460px';
                media.style.minWidth = '460px';

                media.style.display = 'flex';
                media.style.alignItems = 'flex-start';

                media.style.marginTop = '0';
                media.style.marginBottom = '0';

                media.style.paddingTop = '0';
                media.style.paddingBottom = '0';

                media.style.minHeight = '0';
                media.style.height = 'auto';

                media.style.overflow = 'visible';


                const body =
                    media.querySelector('.media-body');

                if (body) {
                    body.style.flex = '0 0 400px';
                    body.style.width = '400px';
                    body.style.maxWidth = '400px';
                    body.style.minWidth = '0';

                    body.style.height = 'auto';
                    body.style.minHeight = '0';
                    body.style.overflow = 'visible';

                    body.style.paddingLeft = '10px';
                }


                const title =
                    media.querySelector('.title-text');

                if (title) {
                    title.style.fontSize = '17px';
                    title.style.lineHeight = '1.2';
                }


                media.querySelectorAll('.small')
                    .forEach(el => {

                        el.style.display = 'block';

                        el.style.maxWidth = '300px';

                        el.style.fontSize = '13px';
                        el.style.lineHeight = '1.12';

                        el.style.marginTop = '2px';
                        el.style.marginBottom = '0';

                        el.style.whiteSpace = 'normal';
                        el.style.overflow = 'visible';

                        el.style.overflowWrap =
                            'break-word';

                        el.style.wordBreak =
                            'normal';

                        el.style.maxHeight = 'none';
                        el.style.height = 'auto';
                    });

            });


        // =========================================
        // PAGE MORE
        // =========================================

        const pageMore =
            document.querySelector('.page-more');

        if (pageMore) {

            pageMore.style.position = 'static';

            pageMore.style.width = '100px';
            pageMore.style.maxWidth = '100px';

            pageMore.style.marginLeft = '360px';
            pageMore.style.marginRight = '0';

            pageMore.style.textAlign = 'center';

            pageMore.style.marginTop = '30px';
            pageMore.style.marginBottom = '0px';


            const resultContainer =
                pageMore.closest('.container');

            if (resultContainer) {
                resultContainer.style.marginBottom =
                    '0px';

                resultContainer.style.paddingBottom =
                    '0px';

                resultContainer.style.background =
                    '#ffffff';
            }
        }


        console.log(
            'TV SEARCH LAYOUT APPLIED'
        );

    })();
    """.trimIndent()
    }
}

//PageLayoutScripts
//├── Home / Movie / TV
//├── Search
//├── Billboard
//└── Play layout
