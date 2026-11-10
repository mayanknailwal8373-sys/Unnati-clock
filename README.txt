UNNATI CLOCK - Android (Kotlin) - alarm core
1. Android Studio (Koala+) > Open > UnnatiClock folder. Gradle sync (internet chahiye).
2. Intro MP3: already included (res/raw/fixed_intro_ringtone.mp3). Badalna ho to same naam se replace karein.
3. Krishna images: app/src/main/res/drawable-nodpi/krishna_1..5.png already included (replace with your own if you like).
4. Run on a REAL phone: Build > Build APK(s).
5. Phone par: Notifications allow, "Alarms & reminders" allow, Battery > Unrestricted. Full-screen alert Android 14 par Settings > Apps > Special access se allow karein.
Opening screen: SplashActivity (fireworks + daily motivation) included.
Not included yet: weather, stopwatch/timer, world clock, love notes, sleep music, festival themes, alarm edit/history, settings screen (snooze/volume/vibration defaults Store mein hain).
Untested - real-device testing zaroori hai.

GitHub se APK (computer par kuch install nahi):
1. github.com par naya repository banayein > "uploading an existing file" se is zip ke andar ki saari files upload karein
   (.github folder samet; na dikhe to Add file > Create new file, naam: .github/workflows/build.yml, aur build.yml ka text paste karein).
2. Repo > Actions tab > "Build APK" > Run workflow (push par apne aap bhi chalta hai). ~5-8 min.
3. Run khulne par neeche Artifacts > UnnatiClock-debug-apk download karein > zip kholkar APK phone par install karein.
