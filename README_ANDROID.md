# 📱 BSNL Lineman Native Android APK — Setup & Build Guide

This directory contains the source code for the **BSNL Lineman Native Android Application**.

## 🌟 Key Capabilities
1. **24/7 Background Location Service**: Uses an Android **Foreground Location Service** (`LocationService.java`). Sends high-accuracy GPS location pings every 15 seconds to your Python Flask server.
2. **RAM-Clear Proof**: Runs continuously in the background even if the lineman closes the app, swipes it away, or clears phone RAM.
3. **Auto-Start on Boot**: Automatically initializes tracking when the lineman turns on their phone in the morning (`BootReceiver.java`).
4. **No Admin Access**: Contains ONLY the Lineman Tasks view and ONU Location Capture tool.
5. **Configurable Server IP**: Includes a floating gear icon ⚙️ in the app to easily set your Flask server IP address (LAN IP e.g. `http://192.168.1.5:5000` or Tailscale IP).

---

## 🛠️ How to Build the APK

### Option A: Build Using Android Studio (Recommended & Easiest)
1. Download & Install [Android Studio](https://developer.android.com/studio).
2. Open Android Studio → Select **Open an existing project**.
3. Choose the `lineman_android_app` folder (`c:\Users\hp\Desktop\FibreManagerWeb.tar\FibreManagerWeb\lineman_android_app`).
4. Wait for Gradle sync to complete.
5. Go to menu bar: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Android Studio will generate the `.apk` file at:  
   `lineman_android_app/app/build/outputs/apk/debug/app-debug.apk`

---

### Option B: Build Using Command Line (Gradle)
If you have Android SDK & Java installed on your machine:
```bash
cd lineman_android_app
gradlew assembleDebug
```
The output `.apk` file will be generated in `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📲 How to Install & Configure on Lineman's Phone

1. Transfer `app-debug.apk` to the lineman's Android phone.
2. Tap the `.apk` file to install it (*Allow "Install from Unknown Sources" if prompted*).
3. **First-Time Setup (ONCE ONLY)**:
   - Open the **BSNL Lineman** app.
   - When Android asks for Location permissions, select **"Allow all the time"** (or "Allow while using app", then tap "Allow all the time" in Settings).
   - Tap the ⚙️ **Settings icon** at top right to enter your computer's IP address (e.g. `http://192.168.1.5:5000` or Tailscale IP `http://100.x.x.x:5000`).
4. **Done!** The lineman can now do their daily tasks, capture ONU locations, and their device will report 24/7 background location pings to your Admin Dashboard!
