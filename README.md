# 🌿 PAPAIA Mobile
### Papaya Farmer Management System with AI-Powered Disease Detection

![Kotlin](https://img.shields.io/badge/Kotlin-Android-blue)
![Firebase](https://img.shields.io/badge/Firebase-Firestore-orange)
![Node.js](https://img.shields.io/badge/Node.js-Express-green)
![TensorFlow](https://img.shields.io/badge/TensorFlow-AI-yellow)
![License](https://img.shields.io/badge/License-MIT-red)

PAPAIA Mobile is an Android application designed to assist papaya farmers in monitoring the health of their crops using Artificial Intelligence. The application allows farmers to capture or upload images of papaya leaves and fruits for disease detection while providing farm management tools such as weather monitoring, activity tracking, announcements, appointments, notifications, and agricultural recommendations.

The mobile application works together with the **PAPAIA Web Admin System**, enabling farm owners to monitor all registered farmers, disease reports, analytics, and overall farm health in real-time.

---

# 📖 Table of Contents

- Overview
- Features
- System Architecture
- Screenshots
- Technologies Used
- Project Structure
- Installation Guide
- Configuration
- Running the Application
- API Documentation
- Firebase Configuration
- Machine Learning Integration
- User Roles
- Future Improvements
- Contributors
- License

---

# 🌱 Overview

PAPAIA is an AI-powered Papaya Farmer Management System developed as a capstone project. It combines Android development, cloud computing, machine learning, and web technologies to help farmers identify papaya diseases at an early stage.

The application uses:

- Android (Kotlin)
- Firebase Firestore
- Firebase Storage
- Node.js REST API
- TensorFlow CNN Model
- Retrofit
- CameraX

The AI model identifies common papaya diseases by analyzing images captured by the mobile application.

---

# ✨ Features

## 👨‍🌾 Farmer Features

- Secure Login & Registration
- Email Verification
- Forgot Password
- User Profile Management
- Scan Papaya Leaves/Fruits
- AI Disease Detection
- Camera Capture
- Gallery Image Upload
- Scan History
- Daily Agricultural Tips
- Weather Information
- Fertilizer Recommendations
- Disease Information
- Notifications
- Appointment Requests
- Farm Announcements
- Complaint Submission
- Activity Logs

---

## 🤖 AI Disease Detection

The application can detect:

- Healthy
- Anthracnose
- Powdery Mildew
- Ring Spot Virus

After prediction, the app displays:

- Disease Name
- Confidence Score
- Description
- Symptoms
- Recommended Treatment
- Prevention Methods

---

## ☁ Cloud Features

- Firestore Database
- Firebase Authentication
- Firebase Storage
- Real-time Data Synchronization

---

# 🏗 System Architecture

```
                 +--------------------+
                 | Android Mobile App |
                 +---------+----------+
                           |
                     Retrofit API
                           |
                +----------+----------+
                | Node.js Express API |
                +----------+----------+
                           |
        +------------------+------------------+
        |                                     |
Firebase Firestore                 TensorFlow Model
        |                                     |
Firebase Storage                Disease Prediction
```

---

# 📱 Screenshots

Screenshot will be attached (to be follow):

```
screenshots/

login.png

dashboard.png

scan.png

prediction.png

history.png

profile.png

```

Example:

```markdown
## Login

![Login](screenshots/login.png)

## Dashboard

![Dashboard](screenshots/dashboard.png)

## Disease Detection

![Prediction](screenshots/prediction.png)
```

---

# 🛠 Technologies Used

## Mobile

- Kotlin
- Android Studio
- XML Layouts
- CameraX
- Material Design

## Backend

- Node.js
- Express.js
- JWT Authentication
- Multer
- TensorFlow
- REST API

## Database

- Firebase Firestore

## Storage

- Firebase Storage

## Networking

- Retrofit2
- Gson

## AI

- TensorFlow
- CNN Model
- Google Colab (Training)

---

# 📂 Project Structure

```
PAPAIA-Mobile/
│
├── app/
│   ├── activities/
│   ├── adapters/
│   ├── api/
│   ├── fragments/
│   ├── models/
│   ├── repository/
│   ├── utils/
│   ├── services/
│   └── viewmodels/
│
├── res/
│   ├── drawable/
│   ├── layout/
│   ├── mipmap/
│   ├── values/
│   └── navigation/
│
├── screenshots/
│
├── gradle/
│
├── README.md
│
└── build.gradle
```

---

# ⚙ Requirements

Before installing, make sure you have:

- Android Studio Hedgehog or newer
- Android SDK 34+
- Kotlin 1.9+
- Gradle 8+
- JDK 17
- Git
- Firebase Project
- Node.js Backend Running

---

# 🚀 Installation Guide

## 1 Clone Repository

```bash
git clone https://github.com/yourusername/PAPAIA-Mobile.git
```

---

## 2 Open Android Studio

Open Android Studio.

Choose

```
Open Existing Project
```

Select the cloned repository.

---

## 3 Sync Gradle

Wait for Android Studio to download all dependencies.

---

## 4 Configure Firebase

Create a Firebase project.

Enable:

- Firestore
- Storage
- Authentication

Download

```
google-services.json
```

Place it inside

```
app/google-services.json
```

---

## 5 Configure API

Open

```
ApiClient.kt
```

Change the base URL.

Example:

```kotlin
private const val BASE_URL =
"https://your-api-url.onrender.com/api/"
```

For local testing:

```kotlin
http://10.0.2.2:5000/api/
```

---

## 6 Build Project

Click

```
Build > Make Project
```

or

```
Run
```

---

# 🔐 Authentication

The application uses JWT authentication.

Workflow:

```
Login

↓

Receive JWT

↓

Save Token (SharedPreferences)

↓

Include Authorization Header

↓

Access Protected APIs
```

---

# 🌐 API Endpoints

## Authentication

| Method | Endpoint |
|----------|------------|
| POST | /login |
| POST | /register |
| POST | /verify-otp |
| POST | /forgot-password |
| POST | /reset-password |

---

## User

| Method | Endpoint |
|----------|------------|
| GET | /profile |
| PUT | /profile |

---

## Prediction

| Method | Endpoint |
|----------|------------|
| POST | /predict |

---

## History

| Method | Endpoint |
|----------|------------|
| GET | /history |

---

## Announcement

| Method | Endpoint |
|----------|------------|
| GET | /announcements |

---

## Appointment

| Method | Endpoint |
|----------|------------|
| GET | /appointments |
| POST | /appointments |

---

## Complaints

| Method | Endpoint |
|----------|------------|
| POST | /complaints |

---

# 🧠 Machine Learning

The disease detection model was trained using TensorFlow.

Dataset includes:

- Healthy
- Anthracnose
- Powdery Mildew
- Ring Spot Virus

Workflow:

```
Camera

↓

Image Processing

↓

REST API

↓

TensorFlow Model

↓

Prediction

↓

JSON Response

↓

Display Result
```

---

# 👥 User Roles

## Farmer

- Scan crops
- View predictions
- Submit complaints
- Book appointments
- Receive announcements
- View weather
- Track activities

---

## Farm Owner (Web System)

Available only in the Web Admin application.

- Manage farmers
- Monitor disease statistics
- View analytics
- Manage announcements
- Manage appointments
- View all scan history

---

# 🔮 Future Improvements

- Offline AI Prediction (TensorFlow Lite)
- Push Notifications
- GPS Farm Mapping
- Pest Detection
- Multi-language Support
- Image Quality Detection
- Dark Mode
- Offline Sync
- Weather Forecast Alerts

---

# 🤝 Contributing

Contributions are welcome.

1. Fork the repository.

2. Create a new branch.

```bash
git checkout -b feature/new-feature
```

3. Commit changes.

```bash
git commit -m "Added new feature"
```

4. Push branch.

```bash
git push origin feature/new-feature
```

5. Open a Pull Request.

---

# 👨‍💻 Developers

**Went Ruzel Igot**
- Full Stack Developer
- Mobile Developer
- AI Integration

Capstone Project
University of Cebu – Banilad

---

# 📄 License

This project was developed for educational and research purposes as a university capstone project.

---

# ⭐ Support

If you find this project helpful, consider giving it a ⭐ on GitHub.

It helps others discover the project and supports future development.
