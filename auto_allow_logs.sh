#!/bin/bash

(
    echo "Waiting for Log Access dialog..."

    for i in {1..30}
    do
        CURRENT=$(adb shell dumpsys window | grep "mCurrentFocus")

        if echo "$CURRENT" | grep -q "com.android.systemui.logcat.LogAccessDialogActivity"; then
            echo "Log Access dialog detected."

            adb shell input tap 950 851

            echo "Log Access allowed."
            exit 0
        fi

        sleep 1
    done

    echo "Log Access dialog not detected."
) >/tmp/ikanbot_auto_allow.log 2>&1 &

exit 0