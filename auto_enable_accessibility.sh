#!/bin/bash
set -e

SERVICE="com.ikanbot.tv/.TVAccessibilityService"

echo "Enabling ikanbotTV Accessibility..."

adb shell settings put secure \
    enabled_accessibility_services "$SERVICE"

adb shell settings put secure \
    accessibility_enabled 1

SERVICES=$(adb shell settings get secure enabled_accessibility_services | tr -d '\r')
ENABLED=$(adb shell settings get secure accessibility_enabled | tr -d '\r')

echo "Services: $SERVICES"
echo "Enabled:  $ENABLED"

if [[ "$SERVICES" == *"$SERVICE"* ]] && [[ "$ENABLED" == "1" ]]; then
    echo "✓ Accessibility enabled"
    exit 0
fi

echo "✗ Accessibility failed"
exit 1