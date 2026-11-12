# CB350C Mileage Tracker
Author: **Shivam Gauns**

An offline native Android application to log full-tank refills, calculate actual mileage, compare optional motorcycle dashboard mileage, track fuel cost, show a mileage trend chart, edit/delete entries, and export CSV.

## Build an APK
1. Install Android Studio with Android SDK Platform 35.
2. Open this folder as a project, allow Gradle sync (requires internet for Android Gradle Plugin).
3. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Find `app/build/outputs/apk/debug/app-debug.apk`, transfer it to your phone, and install it.

## Formula
Actual mileage = distance since the previous full-tank fill / litres required to refill to the same level. The dashboard reading is optional and is stored separately. Overall mileage uses total distance / total litres, not the average of individual mileages.

## Notes
- Includes one editable sample record from the supplied photos: 395.8 km, 11.74 L, ₹113.80/L, dated 2026-11-12 (date is a placeholder; edit it to the actual refuelling date).
- Data is saved locally on your device using SQLite. Export CSV regularly for backup.
- No cloud account, ads, or internet permissions.
