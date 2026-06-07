#!/bin/bash
# FxZ Client — Build Script
# Run this after setting ANDROID_HOME

echo "=== FxZ Client Build Script ==="

if [ -z "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME not set"
    echo "Set it with: export ANDROID_HOME=/path/to/Android/Sdk"
    exit 1
fi

echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "✅ local.properties set to: $ANDROID_HOME"

echo "Building debug APK..."
./gradlew assembleDebug --stacktrace

echo ""
echo "APK location:"
find . -name "*.apk" -path "*/debug/*" 2>/dev/null
