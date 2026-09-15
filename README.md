# Homify Grocery Manager

**Homify-GroceryManager** is a smart home grocery management Android app designed to simplify daily household inventory tracking.  
Built using **Kotlin**, **XML**, and **Room Database**, it helps users manage groceries efficiently, monitor expiry dates, and receive timely reminders before items expire or run out.

---

### ✨ Features
- 🛒 **Add & Manage Items** – Easily add grocery items with name, quantity, and expiry date.  
- ⏰ **Smart Reminders** – Receive notifications before groceries expire or are expected to finish.  
- 📅 **Expiry & Consumption Tracking** – Track both expiry dates and expected consumption durations.  
- 🔔 **Custom Notification System** – Personalized reminders using WorkManager & Notification API.  
- 💾 **Offline Storage** – Powered by Room Database for local persistence.  
- 🧭 **Clean & Intuitive UI** – Built with Material Design components and XML layouts.  

---

### 🧰 **Tech Stack**
| Layer | Technology Used |
|-------|------------------|
| Language | Kotlin |
| UI | XML (Material Design 3) |
| Database | Room (SQLite) |
| Architecture | MVVM |
| Background Tasks | WorkManager |
| Notifications | Android Notification API |
| Tools | Android Studio, Gradle |

---

### 🖼️ **App Flow**
**Home → Add Item → Smart Reminder → Notification Alert**
---
### ScreenShots 
<img width="540" height="1200" alt="image" src="https://github.com/user-attachments/assets/ed4266d1-2620-4149-a82f-a2703cb2991f" />
<img width="540" height="1200" alt="image" src="https://github.com/user-attachments/assets/2b25163e-4381-4a9a-951f-c68e4bbc243b" />
<img width="540" height="1200" alt="image" src="https://github.com/user-attachments/assets/16e40da0-6863-4a2b-8682-54521e44d388" />
 <img width="540" height="1200" alt="image" src="https://github.com/user-attachments/assets/2ef6762f-7e59-43fb-90ba-5a3c716e3db2" />

---

## Technical decisions

**WorkManager over AlarmManager.** Expiry reminders have to survive reboots and Doze,
and they do not need to fire at an exact second. WorkManager handles the persistence
and the battery constraints; AlarmManager would have meant handling both myself.

**Room over raw SQLite.** The app is offline-first, so every read hits the local
database. Room gives compile-time query checking and removes the cursor boilerplate,
which matters more than the small overhead when the queries are this simple.

**MVVM.** State survives configuration changes without extra work, and the reminder
scheduling stays out of the UI layer where it would be hard to test.

**No backend.** Nothing here needs a server. Adding one would mean accounts, sync and
a privacy story for a grocery list, in exchange for nothing the user asked for.
