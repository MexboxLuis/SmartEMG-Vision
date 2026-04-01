# Welcome to SmartEMG Vision 🦾

[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Python](https://img.shields.io/badge/Python-14354C?style=flat-square&logo=python&logoColor=white)](https://www.python.org/)
[![TensorFlow](https://img.shields.io/badge/TensorFlow-%23FF6F00.svg?style=flat-square&logo=TensorFlow&logoColor=white)](https://www.tensorflow.org/)

Welcome to the **SmartEMG Vision** repository. This Android application serves as a real-time simulator designed to assist individuals with reduced mobility. It integrates electromyography (EMG) signal classification with AI-powered computer vision to facilitate contextual interactions with the user's environment.

This project was developed at the **Instituto Politécnico Nacional (IPN)** as a prototype for accessible technology.

🎥 **[Watch the Video Demo Here](#)**  
📄 **[Download the Project PDF Here](#)** 

---

## 📚 About The Project

| Feature                | Details |
| ---------------------- | ------- |
| 🎯 **Purpose**         | To assist individuals with motor disabilities by translating muscle signals into commands interacting with visually detected objects. |
| ⚙️ **Architecture**     | Client-Server architecture. The Android app sends camera frames and commands via HTTP to local Python servers running the AI models. |
| 🧠 **AI Integration**   | Utilizes a custom TensorFlow/Keras neural network for EMG signal classification and YOLOv8 for real-time object detection. |
| 🔄 **Core Operations** | Real-time camera feed analysis, bounding box rendering, simulated EMG signal processing, and contextual UI action suggestions. |

---

## 🚀 Tech Stack

### Android & UI

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)

- **Kotlin & Jetpack Compose:** The UI is built using Compose, providing reactive animations and dynamic state management based on server responses.
- **CameraX:** Captures real-time image frames from the device camera for continuous analysis.
- **OkHttp:** Manages asynchronous multi-part HTTP requests to send image frames and receive predictions from the backend.

### Backend & AI Models

![Flask](https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white)
![YOLO](https://img.shields.io/badge/YOLOv8-00FFFF?style=for-the-badge&logo=yolo&logoColor=black)

- **Python & Flask:** Two lightweight local servers (`server.py` and `smg_predict.py`) handle incoming requests, process data, and return JSON responses.
- **Ultralytics YOLOv8:** Processes incoming JPEG frames to detect and locate specific object classes.
- **TensorFlow & Scikit-learn:** A pre-trained `.keras` model classifies simulated EMG signals into grasp types using standardized data.

---

## 🔧 Highlighted Features

| Feature | Description |
|--------|------------|
| **Real-Time Object Detection** | The app overlays custom bounding boxes on the camera preview, identifying objects and confidence scores. |
| **Contextual Suggestions** | Based on detected objects, the app proposes contextual actions. |
| **EMG Grasp Simulation** | Simulates and validates EMG signals through communication with the backend. |
| **Visual Feedback System** | Provides immediate feedback indicating whether the predicted movement matches the intended action. |

---

## 📸 Screenshots

- ![Welcome Screen](assets/WelcomeScreen.jpeg)
- ![Camera Preview & YOLO Boxes](assets/YoloDetection.jpeg)
- ![Action Suggestions](assets/ActionSuggestions.jpeg)
- ![Prediction Result](assets/PredictionResult.jpeg)

---

## 🛠️ How to Run Locally

### 1. Backend Setup (Python)

```bash
git clone https://github.com/MexboxLuis/SMARTEMG-Vision.git
cd SMARTEMG-Vision/app/src/main/java/com/example/smartemgvision/model
```

### Install dependencies

```bash
pip install flask ultralytics numpy opencv-python pandas tensorflow scikit-learn
```

### Start servers

```bash
python server.py
```

```bash
python smg_predict.py
```

---

### 2. Android App Setup

- Open Android Studio and load the project.
- Sync Gradle files.
- Run on emulator or device.
- Grant camera permissions.

---

## 🤝 Authors

- [Luis Alfredo](https://github.com/MexboxLuis)
- [Iker Antonio](https://github.com/uumaaa)

