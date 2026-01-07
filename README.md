# 🛡️ SkillVault

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-green.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVI--Clean-orange.svg)]()
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

**SkillVault** is a secure, offline-first credential manager built with a focus on modern Android security standards and high-performance UI. It serves as a showcase for production-grade development practices.

---

## 📸 Preview
| List Screen | Add Credential | Dark Mode |
| :---: | :---: | :---: |
| ![List](https://drive.google.com/file/d/1qWbTY1wsjhmxuib4WhkGfOv3MkVbxCr7/view?usp=drive_link) | ![Add](https://drive.google.com/file/d/11UiV0oy-eBvfc7HoJXNm3cDpMO1yV6Hk/view?usp=drive_link) | ![Dark](https://drive.google.com/file/d/1-nwtcboKLv6A91dKHNNrLwaLfaZOeSiV/view?usp=drive_link) |

---

## 🚀 Key Features
* **Encrypted Storage:** All credentials stored locally using AES-256 encryption via Room.
* **Security Overlay:** Automatic screen masking in recent apps to prevent data leaks.
* **Smart Suggestions:** UX-optimized account and username autocomplete.
* **Biometric Ready:** (Optional: mention if you added this).
* **Material You:** Full support for dynamic colors and adaptive icons.

---

## 🏗️ Architecture
This project follows **Clean Architecture** principles with a unidirectional data flow (MVI).



* **Presentation Layer:** Jetpack Compose with MVI pattern (State, Intent, Effect).
* **Domain Layer:** Pure Kotlin business logic and UseCases.
* **Data Layer:** Room database with Repository pattern for local persistence.

---

## 🛠️ Tech Stack
* **UI:** Jetpack Compose, Material 3
* **DI:** Hilt (Dependency Injection)
* **Local DB:** Room Persistence Library
* **Async:** Kotlin Coroutines & Flow
* **Testing:** JUnit 5, MockK, Turbine (for Flow testing)

---

## ⚙️ Setup & Installation
Since this is a security-focused app, it requires local configuration for signing:

1. Clone the repository.
2. Create a `local.properties` file in the root directory.
3. Add your keystore details (see `local.properties.example` for reference).
4. Build the project using Android Studio Hedgehog or higher.

---

## 🧪 Testing
Run all tests using the following command:
```bash
./gradlew test
