# Mag Tipid Ka - Personal Financial Tracker

Mag Tipid Ka is an offline-first personal finance and expense tracking Android application developed using modern Android engineering standards: Kotlin, Jetpack Compose with Material 3 Outlined Design System, MVVM Architecture, Clean Architecture, Room Database, SQLite, Jetpack DataStore, and Navigation Compose.

---

## 1. Executive Summary

Mag Tipid Ka is engineered to assist students, individuals, and families in managing their daily allowance and personal finances locally and privately. The application enables users to track income and expenses, calculate daily safe spending limits, manage casual borrowing and lending (IOUs), track recurring subscriptions and utility bills, monitor savings goals, view visual financial reports, and secure financial records locally without cloud servers or internet dependencies.

### Technical Highlights
- 100% Offline Architecture: Operates entirely on-device without external server dependencies or cloud data transmission.
- Philippine Peso Currency Standard: Configured with local currency formatting and flexible currency symbol customization.
- Complete Local Data Privacy: All financial records remain stored on local SQLite database storage.
- Comprehensive Backup and Restore: Exports and imports full database snapshots in structured JSON format.
- Security Integration: Multi-layered security using SHA-256 hashed 4-digit PIN lock and native Android Biometric (Fingerprint and Face) authentication.

---

## 2. Technology Stack and Prerequisites

### Core Stack
- Language: Kotlin 2.0+
- UI Framework: Jetpack Compose with Material 3 Outlined Visual Design System
- Architectural Pattern: MVVM (Model-View-ViewModel) + Clean Architecture
- Database: Room Persistence Library over SQLite (Schema Version 4 with Migrations)
- Key-Value Storage: Jetpack DataStore Preferences
- Biometric Security: AndroidX Biometric Framework
- Navigation: Navigation Compose with Slide and Fade Transitions
- Serialization Engine: Gson (JSON Backup and Restore)
- Annotation Processing: KSP (Kotlin Symbol Processing)

### Build Environment
- Android Studio: Android Studio Jellyfish (2024.1.1) or newer
- Compile SDK: 34 (Android 14)
- Minimum SDK: 26 (Android 8.0 Oreo)
- Target SDK: 34 (Android 14)
- JDK Version: Java 17+

---

## 3. Architecture Overview

The codebase is organized into Clean Architecture layers:

1. Presentation Layer: Composable UI screens, Material 3 outlined components, and ViewModels exposing unidirectional StateFlow UI states. UI components do not interact directly with Room or SQLite.
2. Domain Layer: Business logic, domain models (Transaction, Budget, SavingsGoal, Category, Debt, RecurringBill, DailySpendLimit, TipidScore, ReportData), and single-responsibility Use Cases (AddTransactionUseCase, CalculateDailySpendLimitUseCase, CalculateTipidScoreUseCase, ProcessDueRecurringBillsUseCase, GenerateMonthlyReportUseCase).
3. Data Layer: Persistence layer using Room Entities, DAOs, SQLite database migrations, DataStore key-value preferences, and repository implementations mapping between data entities and domain models.

---

## 4. Detailed Feature Specifications

### 4.1 Dashboard
- Real-time display of Current Balance, Monthly Income, Monthly Expenses, and All-Time Lifetime Totals (Total Income Received vs. Total Money Spent).
- Daily Safe Spend Limit ("Mag-Tipid Mode"): Dynamically calculates safe daily spending based on remaining monthly allowance across remaining calendar days.
- Tipid Financial Score: A 0–100 financial health index evaluating budget adherence, savings momentum, and debt safety ratio.
- Number One Expense Category Insight Banner: Highlights the highest spending category of the month with total amount and percentage share.
- Commitments and IOUs Summary: Live sub-totals for active Utang (I Owe), Pautang (Owed to Me), and Subscriptions to Pay.
- Category Spending Breakdown Donut Chart and Recent Transactions List.

### 4.2 Transaction Management and Smart Auto-Categorization
- Income and Expense entry with large focal amount input, date selection, description notes, and optional receipt photo attachment.
- Smart Offline Auto-Categorization Engine: Real-time keyword analysis automatically pre-selects matching category chips when typing descriptions (e.g., Jollibee, Jeepney, Meralco, Tuition, Shopee, Allowance).
- Interactive Category Selection: Supports 1-tap chip selection and iOS 26-inspired long-press drag-to-hover popover selection.
- Search and Filter: Filter transactions by type (All, Income, Expense), category, or search query.

### 4.3 Category Management
- Pre-populated Philippine student and personal categories covering expenses (Food and Canteen, Jeep and Transportation, School and Tuition, Books and Supplies, Projects and Printing, Rent and Dorm, Bills and Wi-Fi, Hangout and Fun, Health and Medical, Shopping, Other) and income (Allowance, Scholarship, Parents/Family, Side Hustle/Part-Time, Gifts and Pamasko, Salary, Business, Other).
- User-defined custom category creation.

### 4.4 Utang and Pautang (IOU Manager)
- Tracks casual borrowing and lending between friends, classmates, and family.
- Categorizes records into Utang (Money I Owe) and Pautang (Money Owed to Me).
- Supports partial payment recording, due date tracking, settlement progress bars, and status badges.

### 4.5 Subscriptions and Recurring Bills
- Schedule repeating utility bills and subscriptions (Meralco, Wi-Fi, Rent, Tuition, Netflix) with monthly or weekly frequencies.
- Smart Auto-Posting Approval System: Prompts user on app launch when a scheduled bill reaches its due date to approve and log the transaction into the database automatically.

### 4.6 Budget Planner
- Overall Monthly Budget: Sets total monthly allowance limits with overspending progress indicators.
- Category Budgets: Sets specific spending limits per expense category.

### 4.7 Savings Goals
- Set goal name, target savings amount, and target deadline date.
- Tracks current saved amount, remaining balance, and completion percentage.
- Supports direct money contributions toward active savings goals.

### 4.8 Financial Reports
- Monthly income, expense, and net balance analysis.
- Jetpack Compose Canvas Category Expense Donut Chart with percentage shares.
- Income vs. Expense Bar Chart comparison.
- Historical month-by-month report navigation.

### 4.9 Security and Data Management
- Multi-Layered Security: Optional 4-digit PIN lock hashed via SHA-256 and native Android Biometric (Fingerprint and Face) authentication.
- Custom Theme Accent Color Selector: Choice of 15 theme accent colors (Emerald Green, Mint Teal, Ocean Blue, Sky Cyan, Royal Blue, Deep Indigo, Sunset Purple, Violet Plum, Neon Pink, Rose Pink, Vibrant Red, Ruby Red, Crimson Red, Coral Orange, Amber Gold, Golden Yellow, Charcoal Dark).
- 100% Offline JSON Backup and Restore: Exports and imports complete database records (Transactions, Categories, Budgets, Savings Goals, Debts, Recurring Bills) using native Android Storage Access Framework.

---

## 5. Build and Test Instructions

### Building Debug APK
1. Open the project in Android Studio.
2. Ensure Gradle synchronization completes successfully.
3. Build the debug APK via command line or Android Studio Gradle panel:
   ```bash
   ./gradlew app:assembleDebug
   ```
4. Output APK location:
   `app/build/outputs/apk/debug/app-debug.apk`

### Running Unit Tests
Execute unit tests via command line:
```bash
./gradlew app:testDebugUnitTest
```

---

## 6. License and Copyright

This application is distributed under the Non-Commercial Modification License (NCML).

```text
Copyright (c) 2026 Umang620
All rights reserved.
```

### Key Terms
- Permitted Uses: Granted right to use, copy, modify, adapt, and distribute this software for personal, educational, and non-commercial purposes free of charge.
- Restrictions: Selling the software, distributing modified commercial versions, or charging access or subscription fees without prior written consent from the Copyright Holder is strictly prohibited.
- Attribution: Original copyright and license notices must be preserved in all copies or derivative works.
