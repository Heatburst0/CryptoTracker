# 🪙 Crypto Tracker App

A modern, **Offline-First** cryptocurrency tracking application built with **Native Android (Kotlin)** and **Jetpack Compose**. This app demonstrates advanced Android architecture by implementing a "Single Source of Truth" data strategy using **Paging 3**, **Room Database**, and **RemoteMediator**.

The app seamlessly handles network connectivity issues, allowing users to browse cached cryptocurrency data even when offline, and automatically syncs with the CoinGecko API when connectivity is restored.

## ✨ Key Features

* **Offline-First Architecture:** Users can view the coin list and details without an internet connection. Data is cached locally in a Room database.
* **Infinite Scrolling:** Implemented using **Paging 3** for efficient memory usage and smooth scrolling through thousands of coins.
* **Smart Sync:** Uses `RemoteMediator` to manage data flow. The UI *only* observes the local database, while the Mediator fetches fresh data from the API in the background.
* **Search Functionality:** Real-time search with debouncing to filter coins by name or symbol.
* **Robust Error Handling:** Custom UI logic to handle "Split-Screen" errors, ensuring a smooth transition between cached data and network errors without flashing UI glitches.
* **Dependency Injection:** Fully modularized code using **Hilt**.

## 🛠 Tech Stack & Libraries

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** Clean Architecture (MVVM + Repository Pattern)
* **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
* **Network:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
* **Serialization:** [Moshi](https://github.com/square/moshi)
* **Local Database:** [Room](https://developer.android.com/training/data-storage/room)
* **Pagination:** [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3) (with `RemoteMediator`)
* **Image Loading:** [Coil](https://coil-kt.github.io/coil/) (configured for aggressive disk caching)
* **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
* **Asynchrony:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)

## 🏗 Architecture

The app follows the **Single Source of Truth (SSOT)** principle. The UI never communicates directly with the Network.

```mermaid
graph TD
    UI[Jetpack Compose UI] -->|Observes| VM[ViewModel]
    VM -->|Collects Flow| Repo[Repository]
    Repo -->|Returns PagingData| Mediator[RemoteMediator]
    
    subgraph Data Layer
        Mediator -->|1. Fetch Data| API[CoinGecko API]
        Mediator -->|2. Save Data| DB[(Room Database)]
        DB -->|3. Emit Updates| Repo
    end


    
## 🔎 How It Works

**The Trigger:**  
The UI observes the Database via a `PagingSource`.

**The Check:**  
When the user scrolls to the bottom, Paging 3 checks if more data exists in the Database.

**The Fetch:**  
If the Database is empty (or the end is reached), `RemoteMediator` triggers.

**The Sync:**  
`RemoteMediator` calls the API, saves the new page to Room, and updates the `remote_keys` table.

**The Update:**  
Room automatically emits the new data to the UI.

---

## 🚀 Getting Started

### Clone the repository

```bash
git clone https://github.com/yourusername/crypto-tracker.git
```

### Open in Android Studio  
Open the project folder in Android Studio (Ladybug or newer recommended).

### Sync Gradle  
Allow the project to download dependencies.

### Run the App  
Select an emulator or physical device and click **Run**.

---

## 📂 Project Structure

```
com.example.cryptotracker
├── common             # Constants, Resource Wrappers
├── data
│   ├── local          # Room DAO, Database, Entities
│   ├── remote         # Retrofit API, DTOs, RemoteMediator
│   └── repository     # Repository Implementation
├── di                 # Hilt Modules (AppModule)
├── domain
│   ├── model          # Clean Domain Models
│   ├── repository     # Repository Interfaces
│   └── use_case       # Business Logic (GetCoins, SearchCoins)
└── presentation
    ├── coin_detail    # Detail Screen & ViewModel
    ├── coin_list      # List Screen & ViewModel
    └── theme          # Compose Theme & Colors
```

## 🔗 API Reference

This project uses the free **CoinGecko API**.

**Endpoint:** `/coins/markets`

> The free tier has a rate limit of ~10–30 calls/minute.  
> The app handles **HTTP 429 (Too Many Requests)** gracefully.

---

## 📄 License

This project is licensed under the **MIT License** — see the `LICENSE` file for details.
