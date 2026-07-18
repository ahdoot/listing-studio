#!/usr/bin/env bash
set -euo pipefail
echo "=== Listing Studio cloud build START ==="

# 1) JDK 17
if [ ! -d /usr/lib/jvm/java-17-openjdk-amd64 ]; then
  echo ">> installing JDK 17"
  sudo apt-get update -qq
  sudo apt-get install -y -qq openjdk-17-jdk-headless
fi
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version 2>&1 | head -1

# 2) Android SDK command-line tools + packages
export ANDROID_SDK_ROOT="$HOME/android-sdk"
if [ ! -d "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin" ]; then
  echo ">> installing Android cmdline-tools"
  rm -rf /tmp/cmt && mkdir -p /tmp/cmt "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  curl -sL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/cmt/cmt.zip
  unzip -q /tmp/cmt/cmt.zip -d /tmp/cmt
  mv /tmp/cmt/cmdline-tools/* "$ANDROID_SDK_ROOT/cmdline-tools/latest/"
fi
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
echo ">> accepting licenses + installing packages"
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null

# 3) clone + build
cd "$HOME"
rm -rf listing-studio
git clone --depth 1 https://github.com/ahdoot/listing-studio.git
cd listing-studio
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
chmod +x gradlew
echo ">> gradle assembleDebug (first run downloads Gradle + deps, be patient)"
./gradlew assembleDebug --no-daemon
APK="app/build/outputs/apk/debug/app-debug.apk"
ls -la "$APK"

# 4) publish APK as a GitHub Release asset
if [ -n "${GH_TOKEN:-}" ]; then
  echo ">> creating release + uploading APK"
  curl -s -X DELETE -H "Authorization: token $GH_TOKEN" \
    "https://api.github.com/repos/ahdoot/listing-studio/releases/tags/v1.0" >/dev/null 2>&1 || true
  REL=$(curl -s -H "Authorization: token $GH_TOKEN" -H "Accept: application/vnd.github+json" \
    https://api.github.com/repos/ahdoot/listing-studio/releases \
    -d '{"tag_name":"v1.0","name":"Listing Studio v1.0","body":"Debug APK build"}')
  UP=$(echo "$REL" | grep -m1 upload_url | sed -E 's/.*"(https[^"{]+).*/\1/')
  echo "upload_url=$UP"
  curl -s -H "Authorization: token $GH_TOKEN" \
    -H "Content-Type: application/vnd.android.package-archive" \
    --data-binary @"$APK" "${UP}?name=app-debug.apk" | grep -m1 browser_download_url || true
fi
echo "=== BUILD COMPLETE ==="
