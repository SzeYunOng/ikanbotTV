#!/bin/bash
set -e

PACKAGE="com.ikanbot.tv"
ACTIVITY=".MainActivity"
ACCESSIBILITY_SERVICE="com.ikanbot.tv/.TVAccessibilityService"

echo "[1/7] Building & installing..."
./gradlew installDebug

echo "[2/7] Granting READ_LOGS..."
adb shell pm grant "$PACKAGE" android.permission.READ_LOGS

echo "[3/7] Launching ikanbotTV..."

adb shell am force-stop "$PACKAGE"
adb shell am start -n "$PACKAGE/$ACTIVITY"

# 等待新的 app process 建立
APP_PID=""

for i in {1..10}; do
    APP_PID=$(adb shell pidof "$PACKAGE" | tr -d '\r' || true)

    if [[ -n "$APP_PID" ]]; then
        break
    fi

    sleep 0.5
done

if [[ -z "$APP_PID" ]]; then
    echo "✗ ikanbotTV process not found"
    exit 1
fi

echo "→ ikanbotTV PID: $APP_PID"


echo "[4/7] Binding Accessibility..."

# 关键：
# 必须在当前 MainActivity process 建立之后重新触发 Accessibility binding
adb shell settings put secure accessibility_enabled 0

adb shell settings put secure \
    enabled_accessibility_services \
    "$ACCESSIBILITY_SERVICE"

adb shell settings put secure accessibility_enabled 1


echo "[5/7] Verifying Accessibility binding..."

ACCESSIBILITY_BOUND=false

# 最多等待 5 秒，让 AccessibilityManager 完成 bind
for i in {1..10}; do

    ACCESSIBILITY_STATE=$(adb shell dumpsys accessibility)

    if [[ "$ACCESSIBILITY_STATE" == *"Service[label=ikanbotTV"* ]] &&
       [[ "$ACCESSIBILITY_STATE" == *"com.ikanbot.tv/com.ikanbot.tv.TVAccessibilityService"* ]]; then

        ACCESSIBILITY_BOUND=true
        echo "✓ Accessibility service bound"
        break
    fi

    sleep 0.5
done

if [[ "$ACCESSIBILITY_BOUND" == false ]]; then
    echo "✗ Accessibility service failed to bind"
    exit 1
fi


echo "[6/7] Waiting for Log Access dialog..."

LOG_DIALOG_FOUND=false

# 最多等待 5 秒
for i in {1..10}; do

    FOCUS=$(adb shell dumpsys window | grep "mCurrentFocus" || true)

    if [[ "$FOCUS" == *"com.android.systemui.logcat.LogAccessDialogActivity"* ]]; then

        echo "→ Log Access dialog detected"

        # ScrollView -> Allow once
        adb shell input keyevent KEYCODE_DPAD_DOWN
        sleep 0.2

        # Confirm
        adb shell input keyevent KEYCODE_ENTER
        sleep 0.5

        LOG_DIALOG_FOUND=true
        echo "✓ Log access allowed"
        break
    fi

    sleep 0.5
done

if [[ "$LOG_DIALOG_FOUND" == false ]]; then
    echo "→ No Log Access dialog detected"
fi


echo "[7/7] Final verification..."

CURRENT_PID=$(adb shell pidof "$PACKAGE" | tr -d '\r' || true)

echo "→ Current ikanbotTV PID: $CURRENT_PID"

if [[ -z "$CURRENT_PID" ]]; then
    echo "✗ ikanbotTV is not running"
    exit 1
fi

echo ""
echo "✓ ikanbotTV ready"