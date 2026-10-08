# 🌿 WalkQuest AI

> **AI that plans your escape from the screen.**

WalkQuest AI is an open-source AI-powered outdoor quest generator built for **Hacktoberfest 2026 Week 1 — Touch Grass**.

Instead of keeping users on the screen, WalkQuest uses local AI to create a short outdoor quest and encourages the user to put the phone away and go outside.

## ✨ What It Does

1. Describe what you need right now.
2. Local AI generates a structured outdoor quest.
3. The app shows exactly three outdoor activities.
4. Start the quest and put the phone away.
5. A countdown tracks the outdoor session.
6. Come back and reflect on your mood.
7. Completed quests and outdoor minutes are stored locally.

## 🧠 AI at the Core

- **Ollama** — local AI inference
- **Gemma 3:1B** — open-weight AI model
- **Spring Boot 4** — REST backend
- **Structured JSON** — reliable quest generation
- **HTML / CSS / JavaScript** — frontend

The AI turns a natural-language request into a structured outdoor plan.
## 🏗️ Architecture

```text
User
  ↓
WalkQuest Web UI
  ↓
Spring Boot REST API
  ↓
QuestService
  ↓
Ollama
  ↓
Gemma 3:1B
  ↓
Structured Quest JSON
  ↓
Outdoor Quest
```

## 🌳 Touch Grass Experience

> **The best AI interaction is sometimes the one that ends quickly.**

The screen is the shortest part of the experience.

```text
Describe what you need
        ↓
Generate Quest
        ↓
Read the quest
        ↓
Put phone away
        ↓
Go outside
        ↓
Complete the activities
        ↓
Come back
        ↓
Reflect on your mood
```

## 📵 Go Outside Mode

After generating a quest, the user can start the outdoor session.

The app provides:

- A clear phone-away message
- A countdown timer
- An outdoor-focused experience
- An **I'M BACK** action
- A completion and mood-reflection screen

## 📊 Progress Tracking

WalkQuest uses browser LocalStorage to track:

- Quests completed
- Minutes spent outside

No account is required.

## 🛡️ Safety

The AI prompt encourages practical and safe outdoor activities.

Generated quests should avoid:

- Dangerous climbing
- Unsafe roads or traffic
- Fire
- Unsafe swimming
- Restricted areas
- Picking or damaging plants
- Other clearly unsafe activities

The goal is simple, practical and safe outdoor exploration.
## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Backend language |
| Spring Boot 4 | REST API |
| Ollama | Local AI inference |
| Gemma 3:1B | Open-weight AI model |
| HTML / CSS / JavaScript | Frontend |
| LocalStorage | Progress tracking |
| Maven | Build tool |

## 🚀 Run Locally

### 1. Install and run Ollama

Make sure Ollama is installed and running.

```bash
ollama pull gemma3:1b
ollama list
```

### 2. Start WalkQuest

From the project directory on Windows:

```bash
./mvnw.cmd spring-boot:run
```

Or use the included launcher:

```text
run-walkquest.bat
```

The launcher searches for an available port between 8080 and 8089.

### 3. Open the App

Open the localhost URL shown by Spring Boot.

## 🔌 API

Generate an outdoor quest with:

```http
POST /api/quest
Content-Type: application/json
```

Example request:

```json
{
  "request": "Mere paas 30 minute hain, mood fresh karna hai"
}
```

The API returns a structured quest containing a title, duration and three activities.

## 🌍 Why Open-Source AI?

WalkQuest uses a local open-weight model through Ollama instead of depending entirely on a hosted proprietary AI API.

This makes the core AI experience:

- Local
- Reproducible
- Experiment-friendly
- Easier to modify
- More privacy-friendly

Open-source AI gives developers more freedom to understand, experiment with and adapt the AI layer.

## 💡 Project Philosophy

> **AI should sometimes help you leave the screen, not stay on it.**

The AI creates the plan. Then the user goes outside.

## 📁 Project Structure

```text
walkquest/
├── src/
│   ├── main/
│   │   ├── java/com/shitanshu/walkquest/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   └── service/
│   │   └── resources/static/index.html
│   └── test/
├── .gitignore
├── pom.xml
├── mvnw
├── mvnw.cmd
└── run-walkquest.bat
```

## 🚧 Future Ideas

- Weather-aware quests
- Location-aware outdoor suggestions
- More outdoor activity types
- Quest history
- Streaks and achievements
- Community-created quests

## 🏆 Hacktoberfest 2026

Built for **Hacktoberfest 2026 — Week 1: Touch Grass**.

Challenge tags:

`devchallenge` `hf26challenge`

## 📄 License

This project is open source and intended for experimentation, learning and contribution.
