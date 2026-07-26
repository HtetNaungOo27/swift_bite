# FoodHub Android

Android client for FoodHub, built with Kotlin and Jetpack Compose. It connects to the FoodHub Ktor backend for authentication and food-delivery features.

## Requirements

- Android Studio with Android Gradle Plugin 9.2.1 support
- JDK 21 (the Gradle daemon toolchain is configured for Java 21)
- Android SDK 36 and the compile SDK requested by Android Studio (API 37.1 in the current project)
- An Android device or emulator running Android 7.0 / API 24 or newer
- The `food_delivery_ktor` backend running locally

## Set up on a team member's computer

1. Clone the repository and use the shared development branch:

   ```bash
   git clone <FOODHUB_ANDROID_REPOSITORY_URL>
   cd FoodHubAndroid
   git switch developing
   ```

2. Open the cloned folder in Android Studio.
3. Allow Android Studio to use the project's Gradle wrapper and sync the project. Install any Android SDK packages that Android Studio requests.
4. Start the Ktor backend on port `8080` before running the app.
5. Choose a device and run the `app` configuration.

No `local.properties` file should be shared. Android Studio creates it automatically with each member's Android SDK path.

## Connect to the backend

The app currently uses this Retrofit base URL:

```text
http://10.0.2.2:8080/
```

`10.0.2.2` is the Android Emulator alias for the development computer's localhost.

- Android Emulator: keep `http://10.0.2.2:8080/`.
- Physical Android device: connect the phone and computer to the same network, then replace `10.0.2.2` in `app/src/main/java/com/example/foodhub_android/data/NetworkModule.kt` with the computer's LAN IP address, for example `http://192.168.1.20:8080/`.
- If Android blocks cleartext HTTP on a physical device, use a local HTTPS endpoint or add an appropriate debug-only network security configuration.

## Useful commands

On macOS or Linux:

```bash
./gradlew test
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

## Branch workflow

- `main`: stable, reviewed code
- `developing`: integration branch for team development
- Create feature branches from `developing`, then open pull requests back into `developing`.

Example:

```bash
git switch developing
git pull
git switch -c feature/login-improvements
```

Do not commit `local.properties`, Android Studio workspace files, build output, API keys, or passwords.
