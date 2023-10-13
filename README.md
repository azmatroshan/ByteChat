# ByteChat
ByteChat is a real-time chat application with features like one-to-one chat and push notifications.
### [Download APK](https://drive.google.com/file/d/1UL-l2DxCjcRkCBWwMnoPfywFobA_DuTA/view?usp=sharing)

## App Preview
https://github.com/azmatroshan/ByteChat/assets/93484428/3162df02-76e8-43d3-af8e-d15182d567d1

## Built With
- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design quality, robust, testable, and maintainable apps.
  - [Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started) - Handle everything needed for in-app navigation with a single Activity.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes. 
  - [ViewBinding](https://github.com/android/databinding-samples) - Allows you to more easily write code that interacts with views. Declaratively bind observable data to UI elements.
- [Firebase](https://firebase.google.com/) -
  - [Authentication](https://firebase.google.com/docs/auth) - For Creating account with mobile number.
  - [Cloud Messaging](https://firebase.google.com/products/cloud-messaging) - For Sending Notification to client device.
  - [Cloud Storage](https://firebase.google.com/docs/storage) - For Store and serve user-generated content.
  - [Realtime Database](https://firebase.google.com/docs/database) - Flexible, scalable NoSQL cloud database to store and sync data.
- [Glide](https://bumptech.github.io/glide/) - Fast and efficient image loading for Android

## Installation and Configuration
- Clone the repository:   `git clone https://github.com/azmatroshan/ByteChat.git`
- Open the project in Android Studio
- Change the package name of app. [see how](https://stackoverflow.com/questions/16804093/android-studio-rename-package)
- Create Firebase Project. [learn how](https://firebase.google.com/docs/android/setup)
- Import the file google-service.json into your project.
- Connect to firebase authentication, database, storage and cloud messaging from your IDE.
- Run app.
