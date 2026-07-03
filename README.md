# PeerLearn 🎓

**Peer-to-Peer Student Learning Network** — An Android app that connects students to learn from each other, powered by AI-driven skill matching.

> Built as a solo project by a 2nd-year CSE Diploma student, demonstrating real-world Android development with modern architecture and AI integration.

---

## 🚩 The Problem

- Students struggle to find peers with complementary skills
- Traditional tutoring is expensive and not peer-oriented
- No dedicated platform exists for student-to-student skill exchange

## 💡 The Solution

PeerLearn lets students list skills they **can teach** and skills they **want to learn**, then uses AI to intelligently match them with compatible peers — followed by real-time chat to connect and start learning.

- 🤖 **AI-powered skill matching** using Google's Gemini API
- 💬 **Real-time chat** to connect matched peers
- 👤 **Simple profile system** to showcase skills

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Backend | Firebase (Auth, Firestore, FCM) |
| AI | Google Gemini API |

---

## 📱 Core Screens

### Home Screen
Discovery hub showing AI-curated peer suggestions based on the user's skill profile, with skill filter chips and a floating action button to post learning requests.

### Profile Screen
Displays and allows editing of user skills, bio, and availability — with fields for skills taught (`teaches`) and skills wanted (`wants`).

### Chat Screen
Real-time 1-to-1 messaging between matched peers using Firestore's live listeners.

---

## 🧠 How AI Matching Works

1. Fetch candidate peers from Firestore (rough filter on overlapping skills)
2. Send user profile + candidate list to Gemini API
3. Gemini ranks candidates by skill compatibility (0–100 score)
4. Display ranked list on the Home Screen

```kotlin
val model = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GEMINI_API_KEY
)
val response = model.generateContent(prompt)
val ranked = parseMatchJson(response.text)
```

---

## 🎨 Design

Login and onboarding use a **glassmorphism UI** — blurred translucent cards on a dark gradient background — for a premium first impression.

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── model/         # User, Message, ChatRoom data classes
│   ├── repository/    # FirestoreRepository, AuthRepository
│   └── remote/        # GeminiApiService
├── ui/
│   ├── home/          # HomeScreen + HomeViewModel
│   ├── profile/       # ProfileScreen + ProfileViewModel
│   ├── chat/          # ChatScreen + ChatViewModel
│   └── auth/          # LoginScreen + AuthViewModel
├── navigation/        # NavGraph.kt
└── di/                # Hilt modules
```

---

## 🗺️ Roadmap

- [x] **Phase 1 — Foundation:** Firebase setup, navigation, glassmorphism login
- [ ] **Phase 2 — Core Features:** Profile editing, Firestore peer query, Gemini matching
- [ ] **Phase 3 — Chat:** Real-time messaging, chat list, push notifications
- [ ] **Phase 4 — Polish & Ship:** UI refinement, user seeding, public demo

---

## 🚀 Getting Started

```bash
git clone https://github.com/sahil-maske/PeerLearn-Android.git
```

1. Open in Android Studio
2. Add your `google-services.json` (Firebase config)
3. Add your Gemini API key to `local.properties`:
   ```
   GEMINI_API_KEY=your_key_here
   ```
4. Run on an emulator or device

---

## 👤 Author

Built by **Sahil Maske** — 2nd Year CSE Diploma Student
[GitHub](https://github.com/sahil-maske)
