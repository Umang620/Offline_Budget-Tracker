# Mag Tipid Ka

**Mag Tipid Ka** is an offline-first personal budget and expense tracking Android application built using modern Android development principles: **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM Architecture**, **Clean Architecture**, **Room Database**, **SQLite**, **DataStore**, and **Navigation Compose**.

---

## 1. Executive Summary

"Mag Tipid Ka" is designed to help students, individuals, and families manage their finances locally and privately. The application allows users to track income and expenses, set monthly and category-specific budgets, monitor savings goals, view visual financial reports, and secure their financial data locally without relying on cloud services or external servers.

### Key Highlights
- **100% Offline Core Functionality**: Operates completely without internet connection or external servers.
- **Philippine Peso (PHP) Currency Standard**: Built with local currency formatting (`₱`).
- **Data Privacy & Security**: Financial records remain stored locally on device. Optional 4-digit PIN lock protection hashed via SHA-256.
- **Local Data Backup & Restore**: Export and import complete financial data in structured JSON format.

---

## 2. Technology Stack & Prerequisites

### Technical Architecture
- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3 Design System
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Local Database**: Room Database on top of SQLite
- **Preferences Storage**: Jetpack DataStore Preferences
- **Navigation**: Navigation Compose
- **Serialization Engine**: Gson (JSON Backup & Restore)
- **Annotation Processing**: KSP (Kotlin Symbol Processing)

### Development Requirements
- **Android Studio**: Android Studio Jellyfish (2024.1.1) or newer
- **Compile SDK**: 34 (Android 14)
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **JDK**: Java 17+

---

## 3. Application Architecture

The application strictly follows Clean Architecture separation into three primary layers:

```text
Presentation Layer (Composables, ViewModels, UI State)
       │
       ▼
Domain Layer (Domain Models, Use Cases, Repository Interfaces)
       │
       ▼
Data Layer (Room DAOs, Database, DataStore, Repositories, Serialization)
```

1. **Presentation Layer**: Contains Composable UI screens, Material 3 components, and ViewModels exposing unidirectional StateFlow UI states. UI components do not interact directly with Room or SQLite.
2. **Domain Layer**: Contains business logic, domain models (`Transaction`, `Budget`, `SavingsGoal`, `Category`, `ReportData`), and single-responsibility Use Cases (`AddTransactionUseCase`, `CalculateBudgetProgressUseCase`, `GenerateMonthlyReportUseCase`, etc.).
3. **Data Layer**: Manages data persistence using Room Entities, DAOs, SQLite database migrations, DataStore key-value preferences, and repository implementations mapping between data entities and domain models.

---

## 4. Feature Specifications

### 4.1 Dashboard
- Real-time financial summary displaying **Current Balance**, **Total Income**, and **Total Expenses**.
- Overall monthly budget progress card with progress percentage and remaining allowance.
- Recent transactions list with category icons and quick access to transaction details.
- Category spending breakdown pie chart.

### 4.2 Transaction Tracker
- Add, Edit, and Delete transactions for both **Income** and **Expense**.
- Form inputs include amount, category selection, optional description note, and date selection.
- Filter transactions by type (All, Income, Expense), category, or custom date ranges.
- Instant search by description or category name.

### 4.3 Category Management
- Ships with pre-populated expense categories (*Food*, *Transportation*, *Bills*, *School*, *Shopping*, *Entertainment*, *Health*, *Other*) and income categories (*Allowance*, *Salary*, *Business*, *Investment*, *Other*).
- Users can create custom categories directly within transaction entry.

### 4.4 Budget Management
- **Overall Monthly Budget**: Set total monthly spending allowance.
- **Category Budgets**: Set specific spending limits per expense category.
- **Budget Exceeded Warning**: Visual alerts and progress indicators highlight when budget thresholds are reached or exceeded.

### 4.5 Savings Goals
- Set goal name, target savings amount, and target deadline date.
- Tracks current saved amount, remaining balance, and completion percentage (e.g., `Saved: ₱8,000 / Target: ₱15,000 (53.3%)`).
- Allows adding money contributions towards active savings goals.

### 4.6 Financial Reports
- Monthly income, expense, and net balance analysis.
- Custom Jetpack Compose Canvas **Category Expense Donut Chart** with percentage shares.
- Custom **Income vs. Expense Bar Chart** comparison.
- Month-by-month historical report navigation.

### 4.7 Security & Offline Data Management
- **PIN Lock Protection**: Optional 4-digit PIN lock prompt on app startup. PIN hashes are stored securely via DataStore using SHA-256.
- **JSON Backup & Restore**: Export complete local financial records to a `.json` backup file or restore previously exported backups using native Android Storage Access Framework.
- **Theme Selection**: Supports System Default, Light Mode, and Dark Mode.

---

## 5. Building & Installation Instructions

### Building Debug APK
1. Open the project directory in Android Studio.
2. Ensure Gradle synchronization completes successfully.
3. Build the debug APK via terminal or Android Studio Gradle panel:
   ```bash
   ./gradlew app:assembleDebug
   ```
4. The output APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

### Running Unit Tests
To execute local unit tests:
```bash
./gradlew app:testDebugUnitTest
```

---

## 6. License & Copyright

This application is distributed under the **Non-Commercial Modification License (NCML)**.

```text
Copyright (c) 2026 Umang620
All rights reserved.
```

### Key Terms
- **Permitted Uses**: You are permitted to use, copy, modify, adapt, and distribute this software for personal, educational, and non-commercial purposes free of charge.
- **Restrictions**: Selling the software, distributing modified commercial versions, or charging access/subscription fees without prior written consent from the Copyright Holder is strictly prohibited.
- **Attribution**: Original copyright and license notices must be preserved in all copies or derivative works.
