# Syn Counter

A small, native Android app for tracking daily my personal adherence to a famous food optimsation/diet system.
This is in no way affiliated with, or endorsed by, any diet plan organisation and is a personal project.
It stores each day's total in a private on-device SQLite database and lets you view the 30 most recently saved days as a graph. The daily target is saved on the device.

The app does not request network, location, contacts, camera, or other phone permissions. App data is excluded from Android backup.
Code written use GitHub Copilot, via VSCode, then built and compiled in Android Studio.

## Licencing
Original app source is licensed under GPL-3.0-only; third-party components are identified in THIRD-PARTY-NOTICES.md.

## Requirements

- Android Studio (current stable release)
- Android SDK Platform 35 and Build Tools (installable from Android Studio's SDK Manager)
- JDK 17 (Android Studio includes a compatible JDK)
- An Android phone with Android 8.0 (API 26) or later, or an Android emulator

## Open, build, and run

1. Open Android Studio and choose **Open**.
2. Select this project folder, `android_syn_counter`.
3. If prompted, install Android SDK Platform 35 and accept the Gradle sync.
4. Choose an emulator in the device selector, or connect an Android phone with USB debugging enabled.
5. Press **Run** (the green triangle) to build and install the app.

To build a debug APK from a terminal in the project folder, make sure `JAVA_HOME`
points to JDK 17. Android Studio's bundled JDK is commonly at
`C:\Program Files\Android\Android Studio\jbr`; in PowerShell, set it for the
current terminal (adjust the path if Android Studio is installed elsewhere):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

The APK is written to `app\build\outputs\apk\debug\app-debug.apk`.

## Install on your phone

### With Android Studio

Connect the phone over USB, enable USB debugging in Android's Developer options, select the device in Android Studio, and press **Run**.

### Install the APK

Build the APK using the command above, then install it with a connected phone and Android Platform Tools (`adb`):

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Alternatively, transfer `app\build\outputs\apk\debug\app-debug.apk` to the phone and open it there. Android may ask you to allow installs from that file app.

## Quick test checklist

1. On the Counter tab, tap **+ 0.5** twice and confirm the total is `1.0`; tap **− 0.5** and confirm it is `0.5`.
2. Set a target, save it, and confirm the “Syns below target” value changes.
3. Choose another date, change its total, then return to today and verify the two dates have separate totals.
4. Open Trends and switch between **Daily total** and **Below target**.
5. Close and reopen the app and confirm the totals and target remain saved.

## Use

- Tap the date to choose which day's total to view or change.
- Use **+ 0.5** and **− 0.5** to adjust that date's saved total. Totals cannot go below zero.
- Set a daily target and tap **Save target**.
- Open **Trends** to switch between daily totals and the difference from your target.

Totals for dates without saved entries appear as zero. The chart plots saved dates only.
