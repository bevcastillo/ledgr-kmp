# Ledgr

An offline-first expense tracker for **Android and iOS**, built with **Kotlin Multiplatform** and **Compose Multiplatform**. UI, logic, database, and navigation are fully shared.

> 🚧 Work in progress

## Features

- Add, edit, and delete expenses with categories and notes
- Running total, with form validation
- Local storage that persists across restarts

## Tech stack

Kotlin · Compose Multiplatform · Material 3 · Room (KMP) · Koin · Navigation Compose with type-safe routes · Coroutines & Flow · KSP

## Architecture

MVVM with a layered setup: **Compose UI → ViewModel (`StateFlow`) → Repository → Room DAO**.

- One immutable UI state per screen, with stateful/stateless composables for easy previews
- Money stored as `Long` cents to avoid floating-point errors
- Platform-specific code limited to the database builder, via `expect`/`actual`

## Getting started

Requires macOS, Android Studio with the Kotlin Multiplatform plugin, and Xcode.

- **Android:** run the `androidApp` configuration
- **iOS:** run the `iosApp` configuration, or open `iosApp/iosApp.xcodeproj` in Xcode

## Roadmap

- [ ] Unit tests
- [ ] Monthly summary and charts
- [ ] Adaptive layout for tablets
- [ ] CI with GitHub Actions
