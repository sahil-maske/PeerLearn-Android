# PeerLearn

**PeerLearn** is a peer-to-peer skill-swap platform for Android where students and self-learners connect to teach and learn skills from one another. Post what you can teach, find people who can teach you what you want to learn, and connect directly — no middleman, no paid courses.

---

## ✨ Features

### Core Social Feed
- Real-time community feed with **Teach / Learn / Help** post types
- Image uploads via Cloudinary
- Like, comment, and share on posts
- Dedicated **"Need Help"** flow — post a question, others offer help directly

### Peer Matching
- Rule-based skill matching engine — compares what you want to learn against what others can teach
- Recommended Peers section on the home feed, ranked by match relevance
- One-tap **Connect** requests with real-time pending/accepted state

### Real-Time Chat
- 1-to-1 conversations with deterministic chat IDs
- Live online/last-seen presence indicators
- Icebreaker message auto-sent when a connection is accepted

### Profile & Identity
- Public vs. own-profile views
- "Can Teach" / "Wants to Learn" skill tags
- Linked accounts (Instagram, LinkedIn, GitHub, Twitter)
- Email verification and phone number linking
- Responsive layouts for tablets and foldables

### Notifications & Requests
- Dedicated notifications tab for incoming connection requests
- Accept/reject flow with live status sync
- Block/unblock users

---

## 🛠️ Tech Stack

| Layer            | Technology                                  |
|-------------------|----------------------------------------------|
| Language           | Kotlin                                       |
| UI                 | Jetpack Compose, Material 3                  |
| Architecture       | MVVM                                         |
| Backend / Database | Firebase Firestore                           |
| Auth               | Firebase Authentication                      |
| Media Storage      | Cloudinary                                   |
| Push Notifications | Firebase Cloud Messaging (FCM)               |
| Async              | Kotlin Coroutines & Flow                     |
| Image Loading      | Coil                                         |

---

## 🏗️ Architecture

The app follows an **MVVM** pattern with a clear separation between UI, state, and data:

```
ui/          → Composable screens and reusable components
viewmodel/   → ViewModels holding UI state via StateFlow
data/model/  → Data classes (User, Post, Connection, PeerSuggestion, etc.)
repository/  → Firestore/Cloudinary/matching logic, decoupled from UI
```

Key patterns used throughout the app:
- **StateFlow + `collectAsState()`** for reactive UI updates
- **Deterministic document IDs** (sorted UID pairs) for chats and connections, so both users always resolve to the same document without an extra lookup
- **Real-time Firestore listeners** (`addSnapshotListener`) for feed, chat, presence, and connection status
- **Atomic Firestore operations** (`arrayUnion`/`arrayRemove`, `increment()`) for likes and counts, to stay race-condition safe under concurrent updates

---

## 🎨 Design System

| Token            | Value       |
|-------------------|-------------|
| Primary (Teal)     | `#0F6E6E`  |
| Secondary (Purple) | `#534AB7`  |
| Accent (Amber)     | `#E8A33D`  |
| Background         | `#FAF8F5`  |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- A Firebase project with **Firestore**, **Authentication**, and **Cloud Messaging** enabled
- A Cloudinary account for media storage

### Setup

1. Clone the repository
   ```bash
   git clone https://github.com/sahil-maske/PeerLearn.git
   ```

2. Add your Firebase config
   - Download `google-services.json` from your Firebase console
   - Place it in the `app/` directory

3. Add your API keys
   - Open (or create) `local.properties` in the project root
   - Add your Cloudinary and any other required keys:
     ```
     CLOUDINARY_CLOUD_NAME=your_cloud_name
     CLOUDINARY_UPLOAD_PRESET=your_upload_preset
     ```

4. Sync Gradle and run
   ```bash
   ./gradlew build
   ```

> **Note:** `google-services.json` and `local.properties` are gitignored and must be added locally — they are never committed to this repository.

---

## 📱 Screenshots

*UI mockups shown below — built with [Stitch](https://stitch.withgoogle.com), reflecting the app's actual design system and features.*

<p align="center">
  <img src="screenshots/home_screen.png" width="200" alt="Home Screen"/>
  <img src="screenshots/chat_screen.png" width="200" alt="Chat Screen"/>
  <img src="screenshots/profile_screen.png" width="200" alt="Profile Screen"/>
  <img src="screenshots/post_screen.png" width="200" alt="Post Screen"/>
</p>

| Home Feed | Chat | Profile | Create Post |
|:---:|:---:|:---:|:---:|
| Recommended peers + community feed | Real-time chat with presence | Skills, stats, linked accounts | Teach/Learn/Help post creation |

---

## 🗺️ Roadmap

- [ ] Semantic skill matching using embeddings (beyond exact keyword match)
- [ ] Presence write-side manager (foreground/background online status)
- [ ] In-app skill-swap scheduling
- [ ] Ratings and reviews after a completed skill swap

---

## 👤 Author

**Sahil Maske**
Diploma in Computer Science Engineering (MSBTE)
[GitHub](https://github.com/sahil-maske) · sahilmaske.dev@gmail.com

---

## 📄 License

This project is currently unlicensed / for portfolio and educational purposes. Add a license file if you intend to open-source it.
