#!/usr/bin/env bash
set -euo pipefail
echo "=== Listing Studio cloud build START ==="
if [ ! -d /usr/lib/jvm/java-17-openjdk-amd64 ]; then
  sudo apt-get update -qq
  sudo apt-get install -y -qq openjdk-17-jdk-headless
fi
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version 2>&1 | head -1
export ANDROID_SDK_ROOT="$HOME/android-sdk"
if [ ! -d "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin" ]; then
  rm -rf /tmp/cmt && mkdir -p /tmp/cmt "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  curl -sL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/cmt/cmt.zip
  unzip -q /tmp/cmt/cmt.zip -d /tmp/cmt
  mv /tmp/cmt/cmdline-tools/* "$ANDROID_SDK_ROOT/cmdline-tools/latest/"
fi
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null
cd "$HOME"
rm -rf listing-studio
git clone --depth 1 https://github.com/ahdoot/listing-studio.git
cd listing-studio
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
chmod +x gradlew
echo ">> gradle assembleDebug"
./gradlew assembleDebug --no-daemon
ls -la app/build/outputs/apk/debug/app-debug.apk
echo "=== BUILD COMPLETE ==="
