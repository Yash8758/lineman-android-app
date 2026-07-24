# 🌐 Step-by-Step Guide: How to Build APK Online Using GitHub (Free & No Installation)

Follow these simple 4 steps to get your **BSNL Lineman `.apk` file** compiled online for free using GitHub:

---

### Step 1: Create a Free GitHub Account
If you don't have one, go to [github.com](https://github.com/) and create a free account.

---

### Step 2: Create a New Repository
1. On GitHub, click the **`+`** icon at the top right $\rightarrow$ select **New repository**.
2. Name it: `lineman-android-app`.
3. Keep it **Public** or **Private**.
4. Click **Create repository**.

---

### Step 3: Upload the `lineman_android_app` Folder
Upload all files inside `lineman_android_app` to your new GitHub repository:
- You can drag & drop the files directly on the GitHub website, OR push using Git commands:
  ```bash
  cd lineman_android_app
  git init
  git add .
  git commit -m "Initial Lineman App Code"
  git branch -M main
  git remote add origin https://github.com/YOUR_USERNAME/lineman-android-app.git
  git push -u origin main
  ```

---

### Step 4: Download Your Ready APK File! 📲
1. As soon as files are uploaded/pushed, go to the **Actions** tab on your GitHub repository.
2. You will see a workflow running named **"Build Lineman Android APK"** ⏳ (takes ~1 to 2 minutes).
3. Once completed with a green checkmark ✅, click on the workflow run.
4. Scroll down to the **Artifacts** section at the bottom $\rightarrow$ click on **`BSNL-Lineman-App`**.
5. GitHub will download a `.zip` file containing your ready **`app-debug.apk`**!

---

### 📲 Install on Lineman's Phone
1. Transfer `app-debug.apk` to the lineman's phone.
2. Install it and grant Location permission **"Allow all the time"** (ONCE).
3. Tap ⚙️ Settings at top right to enter your computer's IP (e.g. `http://192.168.1.5:5000` or Tailscale IP).
