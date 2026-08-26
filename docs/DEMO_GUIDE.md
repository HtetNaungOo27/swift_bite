# SwiftBite Presentation Guide

## Before presenting

Restart the backend once after pulling the project. The presentation seeder is idempotent and creates the connected demo catalog, accounts, addresses, reviews, cart, orders, notifications, rider work, COD cash, and analytics data.

| App | Email | Password |
|---|---|---|
| Customer | `customer@swiftbite.demo` | `111111` |
| Restaurant | `owner@swiftbite.demo` | `111111` |
| Rider | `rider@swiftbite.demo` | `111111` |

1. Start the Ktor backend on port `8081`.
2. Confirm the emulator can reach `http://10.0.2.2:8081/`.
3. Sign in with the presentation accounts above.
4. Ensure the restaurant has at least one menu item with an image.
5. The seeded customer already has Home and Campus addresses with landmarks, Plus Codes, and map coordinates.
6. Sign in once on all three apps so FCM tokens are registered.
7. Keep three emulators available or switch Build Variants and reinstall as needed.

## Recommended demonstration

### 1. Customer discovery and checkout

1. Open SwiftBite Customer.
2. Show the restaurant-feed shimmer and search/category interface.
3. Open a restaurant and select a menu item.
4. Add it to the cart and adjust its quantity.
5. Select or create a delivery address.
6. Complete Stripe test checkout.
7. Show the animated confirmation and order reference.

### 2. Restaurant fulfillment

1. Open SwiftBite Restaurant.
2. Show the new-order notification.
3. Open the pending order.
4. Progress through Accepted, Preparing and Ready.
5. Show the menu-management grid and analytics dashboard.

### 3. Rider delivery

1. Open SwiftBite Rider.
2. Refresh available jobs.
3. Show distance, pickup/drop-off and earning estimate.
4. Accept the order.
5. Open Active Deliveries.
6. Use the bottom action to mark it picked up.
7. Mark it delivered.

### 4. Customer completion

1. Return to the Customer app.
2. Open Notifications and show the delivery updates.
3. Open Order History.
4. Show the completed delivery timeline.

## What to explain

- One Android module produces three role-specific applications.
- Shared UI, networking, authentication and notification logic reduce duplication.
- The backend—not the UI—enforces order ownership and state transitions.
- Material 3 themes automatically support light and dark mode.
- FCM connects transitions across independently installed apps.
- Compose-native shimmer, animation, semantic status icons and rider ergonomics demonstrate UX consideration.

## Troubleshooting

### Login returns 401

- Confirm the account role matches the installed flavor.
- Confirm `X-Package-Name` is present.
- Clear the app session and sign in again.

### Backend cannot be reached

- Confirm port `8081` is running.
- Use `10.0.2.2`, not `localhost`, from the Android Emulator.
- Check firewall/VPN/DNS settings.

### Notifications are empty

- An empty state is expected before events exist.
- Sign in once to register the FCM token.
- Confirm Firebase Admin initialized on the backend.
- Confirm the installed package has matching Firebase configuration.

### Rider sees no jobs

- The restaurant must mark an order `READY`.
- The order must not already have a rider.
- Refresh the Available Jobs screen.

### Images do not load

- Confirm the Supabase bucket is public.
- Confirm the backend has its API key through private configuration.
- Verify the stored public URL can be opened from the emulator.

## Safe fallback

Keep screenshots or a short recording of the complete journey in case school Wi-Fi prevents Firebase, Supabase or Stripe access. The local order flow still requires the Ktor backend.
