# DailyFlow

DailyFlow is a modern Android task management application that helps users organize their daily tasks efficiently. Built with Material Design principles and Firebase integration, it provides a seamless experience for managing tasks with features like priority levels, due dates, and real-time synchronization.

## Features

- **User Authentication**
  - Secure email/password login
  - User registration
  - Persistent login sessions
  - Profile management

- **Task Management**
  - Create, edit, and delete tasks
  - Set task priorities (High, Medium, Low)
  - Add due dates with date picker
  - Mark tasks as complete/incomplete
  - Swipe gestures for quick task deletion
  - Pull-to-refresh for task list updates


## Technical Details

- **Minimum Requirements**
  - Android SDK 29 (Android 10.0) or higher
  - Google Play Services
  - Internet connection for cloud sync

- **Built With**
  - Java
  - Android SDK
  - Firebase Authentication
  - Firebase Firestore
  - Material Design Components
  - AndroidX Libraries

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Add your Firebase configuration:
   - Create a new Firebase project
   - Add an Android app to your Firebase project
   - Download `google-services.json` and place it in the `app` directory
4. Build and run the application

## Usage

1. **Getting Started**
   - Launch the app
   - Create an account or log in with existing credentials
   - Start managing your tasks!

2. **Managing Tasks**
   - Tap the floating action button to add a new task
   - Fill in task details (title, description, due date, priority)
   - Swipe left or right on a task to delete it
   - Tap a task to edit its details
   - Use the switch to mark tasks as complete/incomplete