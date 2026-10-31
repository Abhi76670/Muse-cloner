# Muse Cloner

Android app jo phone ke apps ko **Clone Space** (Android Work Profile) me clone karta hai.
Clone hue app me data alag hota hai, bilkul fresh install jaisa.

## GitHub se debug APK banao

1. GitHub par naya repo banao aur ye saari files push karo:
   ```
   git init
   git add .
   git commit -m "Muse Cloner"
   git branch -M main
   git remote add origin https://github.com/<username>/<repo>.git
   git push -u origin main
   ```
2. Repo me **Actions** tab kholo. `Build Debug APK` workflow push par apne aap chalta hai
   (ya **Run workflow** dabao).
3. Run khatam hone par neeche **Artifacts** me `MuseCloner-debug-apk` download karo, zip kholo, `app-debug.apk` install karo.

## Use kaise karein

1. App kholo, **Space banao** dabao aur system ke steps follow karo.
2. **Space kholo** dabao. Clone Space ke andar app khulega.
3. Google login ke liye: **1. Services on**, phir **2. Account add** se Gmail add karo.
4. Kisi bhi app par **Clone** dabao, phir **Open**.

## Dhyan rakho

- Android 9 (API 28) ya upar chahiye.
- Clone Space ke accounts personal accounts se alag hote hain, isliye Gmail ek baar Space me add karna padta hai.
- Banking, Play Integrity wale apps aur kuch games clone me chalne se mana kar sakte hain.
- Kuch phones (company-managed ya custom ROM) par work profile bana nahi sakte.

## Virtual Space (ek app ke 20 clone tak)

Work Profile me har app ka sirf 1 clone banta hai. 20 clone ke liye alag Virtual Space APK banao:

1. GitHub repo me **Actions** tab, **Build Virtual Space APK**, **Run workflow**.
2. Artifact `VirtualSpace-debug-apk` download karke install karo.
3. Us app me naya space banao (har space = ek alag clone), apps install karo, aur space me **Google services (GMS)** install karo.
4. Muse Cloner ke andar **Virtual Space kholo** button isi app ko khol deta hai.

Dhyan rakho:
- Ye third-party open-source engine (BlackBox fork, Apache-2.0) hai. Isme clone hue apps ka saara data chalta hai, isliye code khud dekh lo aur `ENGINE_REF` me ek reviewed commit SHA pin karo (Settings, Secrets and variables, Actions, Variables).
- Doosra fork use karna ho to `ENGINE_REPO` variable me `owner/repo` daalo.
- Engine ka README Android 5 se 12 tak likhta hai, is fork me 14 tak. Android 15+ par chalne ki guarantee nahi.
- "Continue with Google" har app me chalega, ye guarantee nahi hai. Kai apps virtual space me Google sign-in reject kar dete hain.
- Ye workflow mere yahan test nahi hua. NDK/CMake wala build fail ho to Actions log bhej do.
