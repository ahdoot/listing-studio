# Listing Studio — AI Product Photo App (PhotoRoom-style, for eBay & marketplaces)

An Android app that turns raw product snapshots into clean, listing-ready photos using
Google's **Gemini 2.5 Flash Image** model. Built for sellers who list on eBay, Amazon,
Etsy, Poshmark, Depop, and Mercari.

## Features

- **White Background** — removes the background, drops the product on pure white
- **Studio Background** — professional studio backdrop with soft lighting
- **Add Shadow** — realistic drop shadow so items don't look "cut out"
- **Remove Wrinkles** — smooths creases in clothing/fabric
- **Ghost Mannequin** — invisible-mannequin look for apparel
- **Enhance / Upscale** — sharpen and clean up the shot
- **Marketplace export presets** — one tap to save a square, white-background JPG sized for
  eBay (1600px), Amazon (2000px), Etsy (2000px), or Poshmark/Depop/Mercari (1200px)
- Saved to **Gallery ▸ Pictures/ListingStudio**

You bring your own Gemini API key (free tier available). It's stored only on your device.

---

## Getting the APK — three ways

### Option A — Cloud build via GitHub (no software to install) ⭐ easiest
1. Create a free GitHub account and a new repository.
2. Upload this whole folder to it (or `git push`).
3. GitHub Actions builds the APK automatically (see the **Actions** tab).
4. Open the finished run → **Artifacts** → download **ListingStudio-debug-apk**.
5. Copy the `.apk` to your phone and install it (enable "Install unknown apps").

The build recipe is already included at `.github/workflows/build-apk.yml`.

### Option B — Android Studio (one click)
1. Install [Android Studio](https://developer.android.com/studio) (free).
2. **File ▸ Open** this folder. Let it sync (downloads Gradle + SDK automatically).
3. Plug in your phone (USB debugging on) or start an emulator.
4. Press **Run ▶**. The app installs and launches.

### Option C — Command line
Requires JDK 17 and the Android SDK. Then:
```
./gradlew assembleDebug
```
APK appears at `app/build/outputs/apk/debug/app-debug.apk`.

---

## First run
1. Open the app → tap the **key** chip (or the gear icon).
2. Get a free key at https://aistudio.google.com/apikey and paste it in.
3. **Pick photo** → tap an AI tool → **Export** for your marketplace.

## Notes
- The app needs internet (the AI runs in Google's cloud, not on the phone).
- Gemini image generation is billed per image on paid tiers; the free tier has daily limits.
- `minSdk` 26 (Android 8.0+).

## Project layout
```
app/src/main/java/com/listingstudio/app/
  MainActivity.kt            app entry
  model/Tools.kt             AI tool prompts + marketplace presets
  data/Settings.kt           stores the API key (DataStore)
  data/GeminiClient.kt       Gemini image API calls
  data/ImageOps.kt           load / square-canvas resize / save to gallery
  ui/EditorViewModel.kt      state + actions
  ui/AppScreen.kt            Compose UI
  ui/Theme.kt                colors
```
