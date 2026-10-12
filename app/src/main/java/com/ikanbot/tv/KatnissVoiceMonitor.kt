package com.ikanbot.tv

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

class KatnissVoiceMonitor(
    private val onVoiceResult: (String) -> Unit,
    private val onAssistantFinished: () -> Unit
) {
    companion object {
        private var activeMonitor: KatnissVoiceMonitor? = null
    }

    private var process: Process? = null
    private var workerThread: Thread? = null

    @Volatile
    private var running = false

    private var ikanbotWasForeground = false

    private val finalResultPattern =
        Pattern.compile(
            """state\[FINAL_RESULT\].*recognized_speech:\s*"([^"]+)""""
        )

    @Synchronized
    fun start() {

        Log.d(
            "TV_KATNISS",
            "Monitor start() called"
        )

        val oldMonitor = activeMonitor

        if (
            oldMonitor != null &&
            oldMonitor !== this
        ) {
            Log.d(
                "TV_KATNISS",
                "Stopping previous monitor instance"
            )

            oldMonitor.stop()
        }

        activeMonitor = this

        if (running) {
            return
        }

        running = true

        workerThread = Thread {

            try {
                process =
                    Runtime.getRuntime().exec(
                        arrayOf(
                            "logcat",
                            "-v",
                            "brief"
                        )
                    )

                val reader =
                    BufferedReader(
                        InputStreamReader(
                            process!!.inputStream
                        )
                    )

                while (running) {

                    val line =
                        reader.readLine()
                            ?: break
                    if (
                        ikanbotWasForeground &&
                        (
                                line.contains(
                                    "ASSISTANT_TV_SEARCH_RESULTS_PAGE_RENDERED"
                                ) ||
                                        line.contains(
                                            "OnConversationTurnFinished"
                                        )
                                )
                    ) {
                        Log.d(
                            "TV_KATNISS",
                            "Assistant finished detected"
                        )

                        ikanbotWasForeground = false

                        onAssistantFinished()

                        continue
                    }

                    if (
                        line.contains(
                            "Foreground app name from running tasks: com.ikanbot.tv"
                        )
                    ) {
                        ikanbotWasForeground = true

                        Log.d(
                            "TV_KATNISS",
                            "ikanbot foreground confirmed"
                        )

                        continue
                    }

                    if (
                        ikanbotWasForeground &&
                        line.contains(
                            "state[FINAL_RESULT]"
                        )
                    ) {
                        val matcher =
                            finalResultPattern.matcher(
                                line
                            )

                        if (matcher.find()) {

                            val text =
                                matcher.group(1)
                                    ?.trim()

                            if (
                                !text.isNullOrBlank()
                            ) {
                                Log.d(
                                    "TV_KATNISS",
                                    "FINAL = $text"
                                )

                                // Keep this true until Assistant actually finishes.
                                // ikanbotWasForeground = false

                                onVoiceResult(text)
                            }
                        }
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    "TV_KATNISS",
                    "Monitor error",
                    e
                )
            }
        }

        workerThread?.start()
    }

    fun stop() {
        running = false

        process?.destroy()
        process = null

        workerThread?.interrupt()
        workerThread = null

        if (activeMonitor === this) {
            activeMonitor = null
        }

        Log.d(
            "TV_KATNISS",
            "Monitor stopped"
        )
    }
}