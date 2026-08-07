# DoseFlow - Medication & Hydration Tracker

DoseFlow is a modern, privacy-first Android application built with **Kotlin** and **Jetpack Compose** designed to help users effortlessly track their daily medication routines and water intake goals.

---

## 🌟 Core Features

1. **Today Dashboard**:
   - Real-time list of scheduled medication doses for today with quick actions (`Take`, `Skip`, `Snooze 15m`, `Snooze 30m`).
   - Interactive water tracker progress ring supporting rapid logging (e.g. +250ml, +500ml) against daily hydration goals.
   - Snackbar feedback with immediate **Undo** support for medication and water actions.

2. **Medications Manager**:
   - Add, edit, and manage prescriptions with custom dosing schedules (Daily, Weekdays, Custom Intervals).
   - Stock remaining level tracking with low-stock alerts.
   - Customizable medication icon types (`pill`, `capsule`, `syrup`, `injection`) and color-coded tags.
   - Alarm scheduling and test notification triggers.

3. **Weekly Analytics Report**:
   - Adherence score calculation (last 7 days).
   - 7-day visual trend analysis chart combining water intake and medication consistency.
   - Habit consistency insights and motivational summaries.

4. **History & CSV Export**:
   - Detailed audit logs of all past medication and water actions.
   - Built-in CSV export tool for easy record sharing with healthcare providers.

5. **Settings & Customization**:
   - Configurable daily water intake goals and reminder intervals.
   - Dark OLED-optimized UI theme with sleek Material 3 design and dynamic color accents.
   - Integrated notification scheduling via AlarmManager and BroadcastReceivers.

---

## 🏗️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose & Material 3
- **Architecture**: MVVM (Model-View-ViewModel) with Clean separation of concerns
- **Local Persistence**: Room Database (`MedicationEntity`, `WaterLogEntity`, `MedicationLogEntity`) with Kotlin Flows
- **Asynchronous Processing**: Kotlin Coroutines & Flow
- **Notifications & Alarms**: Android AlarmManager, NotificationCompat, and custom BroadcastReceiver handlers
- **Analytics & Telemetry**: Firebase Analytics event logging

---

## 🚀 Getting Started

1. Open the project in Android Studio or AI Studio Build.
2. Ensure `compileSdk` and `targetSdk` are configured to the latest stable Android SDK.
3. Build and run using Gradle.
