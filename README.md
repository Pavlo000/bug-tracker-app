# Bug Tracker App

Offline-first Android bug tracker with **Room** local storage, **Retrofit** remote sync, and automatic retry via **WorkManager**.

## Requirements

- Android Studio or VS Code with Kotlin extensions
- Android SDK (API 35)
- JDK 17+
- Android emulator (or physical device)
- Node.js (for mock API server)

## Quick Start

### 1. Start the mock backend

```bash
cd mock-api
npm install
npm start
```

The API runs at `http://localhost:3000`. The Android emulator reaches it at `http://10.0.2.2:3000`.

### 2. Build and run the app

**Option A — Android Studio (recommended)**

1. Open this folder in Android Studio.
2. Start an emulator.
3. Run the `app` configuration.

**Option B — VS Code + command line**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew installDebug
adb shell am start -n com.uopeople.bugtracker/.ui.MainActivity
```

### 3. Test offline sync

1. Create issues while the mock API is running — they sync and show **Synced**.
2. Stop the mock API (`Ctrl+C`) or disable emulator Wi‑Fi.
3. Create/edit/delete issues — they stay local with **Pending upload/update/delete**.
4. Restart the mock API or re-enable Wi‑Fi — WorkManager retries with exponential backoff.

## Architecture

| Layer | Components |
|-------|------------|
| UI | Jetpack Compose, ViewModel, Navigation |
| Data | Room (`IssueEntity`, `IssueDao`), Repository |
| Network | Retrofit (`IssueApiService`), Gson |
| Sync | `SyncWorker` (WorkManager), `NetworkMonitor`, retry up to 5 times |

## Room Schema

`issues` table: `id`, `remoteId`, `title`, `description`, `priority`, `status`, `createdAt`, `syncStatus`, `retryCount`, `lastSyncAttempt`, `syncErrorMessage`

## Git Workflow (Assignment Q3)

Branches used in this project:

- `main` — stable releases
- `feature/room-database` — Room schema and DAO
- `feature/retrofit-sync` — Retrofit + sync layer
- `feature/ui` — Compose UI
- `hotfix/sync-retry-limit` — improved retry handling

Tags: `v1.0` (initial release), `v1.1` (hotfix release)

Remote: `https://github.com/Pavlo000/bug-tracker-app.git`
