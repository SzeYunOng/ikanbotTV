package com.ikanbot.tv.javascript.styles

object PlayPageStyles {

    fun css(): String = """

        .container {
            width: 75% !important;
            max-width: none !important;
            margin-left: 12.5% !important;
            margin-right: 12.5% !important;
        }

        /* =========================================
           VIDEO LAYOUT
           ========================================= */

        #m-player {
            width: 75% !important;
            height: 0 !important;
            padding-bottom: 42.1875% !important;
            margin-left: 12.5% !important;
            margin-right: 12.5% !important;
        }

        #player-wrap {
            width: 100% !important;
        }

        #ikanbot-player {
            width: 100% !important;
            aspect-ratio: 16 / 9 !important;
            height: auto !important;
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
           ========================================= */

        #ikanbot-player.tv-native-fullscreen:focus {
            outline: none !important;
            box-shadow: none !important;
        }

    """.trimIndent()
}