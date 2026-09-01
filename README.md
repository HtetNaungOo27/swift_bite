# SwiftBite

SwiftBite is a multi-role food-delivery platform built as a Kotlin school project. One Android codebase produces three applications—Customer, Restaurant, and Rider—which communicate with a shared Ktor backend.

The implemented journey is:
# SwiftBite

SwiftBite is a multi-role food-delivery platform built as a Kotlin school project. One Android codebase produces three applications—Customer, Restaurant, and Rider—which communicate with a shared Ktor backend.

The implemented journey is:

```text
Customer places order
        ↓
Restaurant accepts → prepares → marks ready
        ↓
Rider accepts → picks up → delivers
        ↓
Customer receives status updates and order history
```

## Applications

| Flavor | Application ID | Display name | Purpose |
|---|---|---|---|
| `customer` | `com.example.foodhub_android` | SwiftBite | Discover restaurants, order food and follow delivery progress |
| `restaurant` | `com.example.foodhub_android.restaurant` | SwiftBite Restaurant | Manage a restaurant, menu, orders and analytics |
| `rider` | `com.example.foodhub_android.rider` | SwiftBite Rider | Accept and complete deliveries |

## Main features

### Customer

- Email/password and Google authentication
- Restaurant discovery, categories and search
- Restaurant menus and food details
- Favorites and quantity selection
- Cart, address selection and Stripe checkout
- Manual delivery-address creation
- Active-order and order-history views
- Visual order-progress timeline
- Restaurant ratings and reviews
- Push-notification inbox and deep links
- Animated payment-success confirmation

### Restaurant

- Owner authentication and restaurant profile
- Status-filtered order list
- Controlled order transitions: Pending → Accepted → Preparing → Ready
- Order details and customer delivery information
- Menu grid and Add Menu Item flow
- Image picker, multipart upload and Supabase image storage
- Business dashboard with completed orders, revenue, average order value, order-stage chart and top-selling items
- Push notifications for new orders and rider assignment

### Rider

- Rider authentication
- Available delivery jobs with distance and earning estimates
- Accept or reject jobs
- Active-delivery list
- Pickup and drop-off details
- Assigned → Out for delivery → Delivered transitions
- Delivery-failure reporting
- Bottom-anchored, oversized actions for one-handed use
- Push notifications and order deep links

### Shared experience

- Material 3 light and dark themes
- Separate Customer, Restaurant and Rider color identities
- MVVM with `ViewModel`, Coroutines and Flow
- Hilt dependency injection
- Retrofit/OkHttp networking and standardized API errors
- JWT session management and expired-session recovery
- Loading shimmer, empty states, error states and retry actions
- Accessible status pills with semantic colors and icons
- Firebase Cloud Messaging

## Technology

- Kotlin and Jetpack Compose
- Material 3 and Navigation Compose
- Hilt and KSP
- Kotlin Coroutines and Flow
- Retrofit, OkHttp and Gson
- Coil image loading
- Firebase Authentication integration and Cloud Messaging
- Stripe Payment Sheet
- Ktor backend, Exposed ORM and JWT authentication
- Supabase Storage for menu images

See [Architecture](docs/ARCHITECTURE.md) for data flow and source-set details.

## Requirements

- Android Studio compatible with Android Gradle Plugin 9.2.1
- JDK 21 for the current Gradle environment
- Android SDK requested by the project (`compileSdk 37.1`, `targetSdk 36`)
- Android 7.0/API 24 or newer emulator/device
- Local `food_delivery_ktor` backend
- Firebase configuration for each application ID
- Stripe and Supabase development credentials for payment/image features

## Local setup

1. Clone the Android and backend repositories.
2. Open `FoodHubAndroid` in Android Studio and allow Gradle sync to finish.
3. Do not share `local.properties`; Android Studio creates it with the local SDK path.
4. Configure Firebase, Stripe and Supabase secrets as described below.
5. Start the backend on port `8081`.
6. In Android Studio, select `customerDebug`, `restaurantDebug`, or `riderDebug` in **Build Variants**.
7. Run the `app` configuration on an emulator.

The Android Emulator reaches the computer through:

```text
http://10.0.2.2:8081/
```

For a physical device, replace `10.0.2.2` in `NetworkModule.kt` with the computer's LAN address and keep both devices on the same network.

## External-service configuration

### Firebase

Register all three Android package names in the same Firebase project and configure the correct SHA-1/SHA-256 fingerprints. Keep `google-services.json` out of public repositories if it contains project configuration you do not intend to publish.

Firebase is used for:

- Google authentication configuration
- FCM device tokens
- Order and delivery push notifications

### Supabase

Create a public storage bucket such as `foodhub-images`. The Ktor server uploads compressed menu images and returns public URLs. Never commit a Supabase secret/service-role key. Load it from a local environment variable or ignored configuration file.

### Stripe

Use Stripe test keys for development. Secret keys and webhook secrets belong only on the backend. The Android client receives only the publishable key and temporary payment-session values.

### Google Maps

Maps are used for delivery-pin selection and foreground rider navigation. While an online rider keeps the Jobs screen active, the app publishes fresh high-accuracy coordinates every 30 seconds. SwiftBite does not collect background location. Add the restricted Android Maps key to the user-level Gradle properties file (do not commit it):

```properties
MAPS_API_KEY=your_restricted_android_key
```

Restrict the key to **Maps SDK for Android** and the three SwiftBite package names/SHA fingerprints. Without this property, the address form still opens but the map tiles cannot load.

## Build and verification

```bash
./gradlew :app:compileCustomerDebugKotlin \
  :app:compileRestaurantDebugKotlin \
  :app:compileRiderDebugKotlin
```

Backend:

```bash
cd ../food_delivery_ktor
./gradlew compileKotlin
```

## Order-state contract

| State | Updated by | Meaning |
|---|---|---|
| `PENDING_ACCEPTANCE` | Backend | Customer placed an order |
| `ACCEPTED` | Restaurant | Restaurant accepted it |
| `PREPARING` | Restaurant | Kitchen is preparing it |
| `READY` | Restaurant | Available to riders |
| `ASSIGNED` | Rider/backend | A rider accepted the job |
| `OUT_FOR_DELIVERY` | Rider | Rider picked up the food |
| `DELIVERED` | Rider | Delivery completed |
| `DELIVERY_FAILED` | Rider | Delivery could not be completed |
| `REJECTED` | Restaurant | Restaurant rejected the order |
| `CANCELLED` | System/customer scope | Order was cancelled |

The backend validates role ownership and permitted transitions. Clients must not skip states.

## Security rules

- Never commit API secrets, service-account files, keystores or passwords.
- Keep authorization headers redacted in logs.
- Use HTTPS and production secret storage outside local development.
- Use separate debug and production Firebase/Stripe/Supabase configurations before release.
- Rotate any credential that has been pasted into chat, source control or screenshots.

## Known scope limitations

SwiftBite is designed for a school demonstration, not production deployment. Current intentional limitations include:

- No background rider tracking or customer-facing moving-rider map
- No customer–rider chat
- Refund processing is limited to supported Stripe cancellation paths
- No promotion/coupon engine
- Limited automated UI/end-to-end tests
- Local cleartext backend URL for emulator development

## Presentation

Follow [Demo Guide](docs/DEMO_GUIDE.md) for a reliable three-role presentation sequence.

```text
Customer places order
        ↓
Restaurant accepts → prepares → marks ready
        ↓
Rider accepts → picks up → delivers
        ↓
Customer receives status updates and order history
```

## Applications

| Flavor | Application ID | Display name | Purpose |
|---|---|---|---|
| `customer` | `com.example.foodhub_android` | SwiftBite | Discover restaurants, order food and follow delivery progress |
| `restaurant` | `com.example.foodhub_android.restaurant` | SwiftBite Restaurant | Manage a restaurant, menu, orders and analytics |
| `rider` | `com.example.foodhub_android.rider` | SwiftBite Rider | Accept and complete deliveries |

## Main features

### Customer

- Email/password and Google authentication
- Restaurant discovery, categories and search
- Restaurant menus and food details
- Favorites and quantity selection
- Cart, address selection and Stripe checkout
- Manual delivery-address creation
- Active-order and order-history views
- Visual order-progress timeline
- Restaurant ratings and reviews
- Push-notification inbox and deep links
- Animated payment-success confirmation

### Restaurant

- Owner authentication and restaurant profile
- Status-filtered order list
- Controlled order transitions: Pending → Accepted → Preparing → Ready
- Order details and customer delivery information
- Menu grid and Add Menu Item flow
- Image picker, multipart upload and Supabase image storage
- Business dashboard with completed orders, revenue, average order value, order-stage chart and top-selling items
- Push notifications for new orders and rider assignment

### Rider

- Rider authentication
- Available delivery jobs with distance and earning estimates
- Accept or reject jobs
- Active-delivery list
- Pickup and drop-off details
- Assigned → Out for delivery → Delivered transitions
- Delivery-failure reporting
- Bottom-anchored, oversized actions for one-handed use
- Push notifications and order deep links

### Shared experience

- Material 3 light and dark themes
- Separate Customer, Restaurant and Rider color identities
- MVVM with `ViewModel`, Coroutines and Flow
- Hilt dependency injection
- Retrofit/OkHttp networking and standardized API errors
- JWT session management and expired-session recovery
- Loading shimmer, empty states, error states and retry actions
- Accessible status pills with semantic colors and icons
- Firebase Cloud Messaging

## Technology

- Kotlin and Jetpack Compose
- Material 3 and Navigation Compose
- Hilt and KSP
- Kotlin Coroutines and Flow
- Retrofit, OkHttp and Gson
- Coil image loading
- Firebase Authentication integration and Cloud Messaging
- Stripe Payment Sheet
- Ktor backend, Exposed ORM and JWT authentication
- Supabase Storage for menu images

See [Architecture](docs/ARCHITECTURE.md) for data flow and source-set details.

## Requirements

- Android Studio compatible with Android Gradle Plugin 9.2.1
- JDK 21 for the current Gradle environment
- Android SDK requested by the project (`compileSdk 37.1`, `targetSdk 36`)
- Android 7.0/API 24 or newer emulator/device
- Local `food_delivery_ktor` backend
- Firebase configuration for each application ID
- Stripe and Supabase development credentials for payment/image features

## Local setup

1. Clone the Android and backend repositories.
2. Open `FoodHubAndroid` in Android Studio and allow Gradle sync to finish.
3. Do not share `local.properties`; Android Studio creates it with the local SDK path.
4. Configure Firebase, Stripe and Supabase secrets as described below.
5. Start the backend on port `8081`.
6. In Android Studio, select `customerDebug`, `restaurantDebug`, or `riderDebug` in **Build Variants**.
7. Run the `app` configuration on an emulator.

The Android Emulator reaches the computer through:

```text
http://10.0.2.2:8081/
```

For a physical device, replace `10.0.2.2` in `NetworkModule.kt` with the computer's LAN address and keep both devices on the same network.

## External-service configuration

### Firebase

Register all three Android package names in the same Firebase project and configure the correct SHA-1/SHA-256 fingerprints. Keep `google-services.json` out of public repositories if it contains project configuration you do not intend to publish.

Firebase is used for:

- Google authentication configuration
- FCM device tokens
- Order and delivery push notifications

### Supabase

Create a public storage bucket such as `foodhub-images`. The Ktor server uploads compressed menu images and returns public URLs. Never commit a Supabase secret/service-role key. Load it from a local environment variable or ignored configuration file.

### Stripe

Use Stripe test keys for development. Secret keys and webhook secrets belong only on the backend. The Android client receives only the publishable key and temporary payment-session values.

## Build and verification

```bash
./gradlew :app:compileCustomerDebugKotlin \
  :app:compileRestaurantDebugKotlin \
  :app:compileRiderDebugKotlin
```

Backend:

```bash
cd ../food_delivery_ktor
./gradlew compileKotlin
```

## Order-state contract

| State | Updated by | Meaning |
|---|---|---|
| `PENDING_ACCEPTANCE` | Backend | Customer placed an order |
| `ACCEPTED` | Restaurant | Restaurant accepted it |
| `PREPARING` | Restaurant | Kitchen is preparing it |
| `READY` | Restaurant | Available to riders |
| `ASSIGNED` | Rider/backend | A rider accepted the job |
| `OUT_FOR_DELIVERY` | Rider | Rider picked up the food |
| `DELIVERED` | Rider | Delivery completed |
| `DELIVERY_FAILED` | Rider | Delivery could not be completed |
| `REJECTED` | Restaurant | Restaurant rejected the order |
| `CANCELLED` | System/customer scope | Order was cancelled |

The backend validates role ownership and permitted transitions. Clients must not skip states.

## Security rules

- Never commit API secrets, service-account files, keystores or passwords.
- Keep authorization headers redacted in logs.
- Use HTTPS and production secret storage outside local development.
- Use separate debug and production Firebase/Stripe/Supabase configurations before release.
- Rotate any credential that has been pasted into chat, source control or screenshots.

## Known scope limitations

SwiftBite is designed for a school demonstration, not production deployment. Current intentional limitations include:

- No background rider tracking or customer-facing moving-rider map
- No customer–rider chat
- Refund processing is limited to supported Stripe cancellation paths
- No promotion/coupon engine
- Limited automated UI/end-to-end tests
- Local cleartext backend URL for emulator development

## Presentation

Follow [Demo Guide](docs/DEMO_GUIDE.md) for a reliable three-role presentation sequence.
