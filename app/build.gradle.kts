name: Build Tourbillion APK

on:
  workflow_dispatch:
  push:
  pull_request:

permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Get source code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - name: Set up Gradle 8.9
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: "8.9"

      - name: Find Android project
        shell: bash
        run: |
          set -euo pipefail

          SETTINGS_FILE="$(find . -type f \( -name settings.gradle -o -name settings.gradle.kts \) -not -path './.git/*' -print -quit)"

          if [ -z "$SETTINGS_FILE" ]; then
            echo "ERROR: No settings.gradle or settings.gradle.kts file was found."
            echo "The Android project files may not be uploaded into this repository."
            exit 1
          fi

          PROJECT_DIR="$(dirname "$SETTINGS_FILE")"
          echo "PROJECT_DIR=$PROJECT_DIR" >> "$GITHUB_ENV"
          echo "Android project folder: $PROJECT_DIR"

      - name: Build debug APK
        shell: bash
        run: |
          set -euo pipefail
          cd "$PROJECT_DIR"

          if [ -f ./gradlew ]; then
            chmod +x ./gradlew
            ./gradlew --no-daemon assembleDebug
          else
            gradle --no-daemon assembleDebug
          fi

      - name: Collect APK
        shell: bash
        run: |
          set -euo pipefail
          mkdir -p "$GITHUB_WORKSPACE/apk-output"

          find "$PROJECT_DIR" -type f -path '*/build/outputs/apk/debug/*.apk' \
            -exec cp {} "$GITHUB_WORKSPACE/apk-output/" \;

          if [ -z "$(find "$GITHUB_WORKSPACE/apk-output" -name '*.apk' -print -quit)" ]; then
            echo "ERROR: The build finished without producing a debug APK."
            exit 1
          fi

      - name: Save APK
        uses: actions/upload-artifact@v4
        with:
          name: Tourbillion-debug-APK
          path: apk-output/*.apk
          if-no-files-found: error
