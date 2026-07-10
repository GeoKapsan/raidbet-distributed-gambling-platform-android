# RaidBet

A sleek, modern mobile gambling app for Android, built as the frontend companion to a fully distributed backend system developed for the **Distributed Systems (3664)** course at the **Athens University of Economics and Business**.

The backend runs across multiple machines simultaneously, handles many players at once, and resolves every bet using cryptographically secure random numbers. All built from scratch in Java with no external libraries.

> **Backend repo:** https://github.com/GeoKapsan/distributed-gambling-platform-android

---

## Screenshots

> _Add your screenshots here once the app is running._

---

## What You Can Do

### 🔐 Sign In
- Log in with your username and password

### 🏠 Browse the Lobby
- Browse all available games in a clean card list.
- **Filter games** by any combination of:
  - Risk level
  - Betting category
  - Minimum star rating
- Reset filters and start fresh with one tap
- Rate the games

### 💰 Manage Your Balance
- Check your current balance at any time from the dashboard

### 🎮 Play a Game
- Tap any game card to jump straight into it
- Place a bet using a slider
- Hit **Spin** to play

### 🏆 See Your Results
Every round ends with a clear result screen:
- **Lost**, **Won** or **Jackpot**
- Jump straight into another round or head back to the lobby

---

## The Games
All the game feature a spinning mechanism but they differ in chances of winning and betting categories. Only 3 games have a specific accompanying logo.

---

## How the Backend Works

The app talks to a distributed backend system that runs across multiple machines at the same time.

### When you search for games
- Your filters (risk level, category, stars) are sent to a central **Master** server. 
- The Master splits the search across all available **Worker** nodes simultaneously.
- Each Worker checks its own portion of the game catalogue.
- All results are collected by a **Reducer**, merged into a single clean list, and sent back to you.
- This is called **MapReduce**, and it means searches are fast no matter how many games the platform has.

### When you place a bet
- The Master routes your bet directly to the Worker that holds that specific game. 
- Every bet outcome is decided by a dedicated **Secure Random Generator**; a separate server that continuously produces cryptographically secure random numbers so no result can be predicted or manipulated.

### Scaling
- The number of Worker nodes is completely flexible. The platform can run on one machine or spread across many without any code changes. Games are automatically distributed across all nodes so the load is always balanced.

### Manager side
Alongside the player experience, the backend also supports a **Manager mode** where an admin can:
- Add new games or remove existing ones
- Edit game properties such as betting limits and odds
- View profit and loss statistics broken down by game
- View statistics per individual player and game provider

---

## Built With

**Frontend (this repo)**
- Android (Java)
- Material Components
- Space Grotesk font
- AndroidX CardView

**Backend**
- Java (standard library only, no external dependencies)
- Raw TCP sockets for all communication
- Custom MapReduce implementation
- Master / Worker / Reducer architecture
- Cryptographically secure random number generation

---

## Course Context

Built as the final project for the **Distributed Systems (3664)** course — a 3rd year mandatory course for the [Department of Informatics](https://www.dept.aueb.gr/cs) at the [Athens University of Economics and Business](https://www.aueb.gr).

---

## License

MIT License — free to use, modify, and distribute.
