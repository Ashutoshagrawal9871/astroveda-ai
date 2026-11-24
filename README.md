<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Jyotish AI — Vedic AI Astrologer & Kundli

Android app (Kotlin + Jetpack Compose) with server-verified Google Play Billing,
Firebase backend, and AI chat/voice astrologer features.

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio and choose **Open** on this directory.
2. Allow Android Studio to fix any incompatibilities as it imports the project.
3. Copy `.env.example` to `.env` and set `GEMINI_API_KEY` (and `BACKEND_PROXY_URL` if using the Cloud Functions proxy).
4. Add your `google-services.json` (Firebase Console → Project settings → Your apps → Android) to `app/` — required for Google Sign-In and Firestore.
5. Run the app on an emulator or a physical device signed into a Google account with Play Store access.

## Payments Architecture (Google Play Billing v8)

All digital goods are sold **exclusively through Google Play Billing** (Play Payments Policy).
There are no direct UPI/Paytm/card flows in the app.

Flow: `PlayBillingManager` launches the Play purchase sheet → client POSTs the purchase token to the
backend `/api/billing/verify-play-purchase` → the Cloud Function verifies it against the
Google Play Developer API (`purchases.products.get` / `purchases.subscriptionsv2.get`) →
Firestore `users/{uid}/billing/entitlements` (server-authoritative) is updated in a transaction →
the app mirrors entitlements into local Room state via `GET /api/billing/entitlements`.

**Fail-closed:** if the backend is unreachable or verification fails, no entitlement is granted and
the purchase stays unacknowledged/unconsumed so it re-verifies automatically on the next restore.

### Product IDs (must match Google Play Console exactly)

| Product ID | Type | Price | Grant |
|---|---|---|---|
| `sub_elite_monthly` | Subscription | ₹399 / month | 200 chat answers refreshed every 30 days, ad-free |
| `sub_elite_annual` | Subscription | ₹3,999 / year | 200 chat answers refreshed every 30 days, ad-free |
| `payg_chat_quick` | In-app product (consumable) | ₹29 | 8 chat answers |
| `payg_chat_15` | In-app product (consumable) | ₹49 | 18 chat answers |
| `payg_chat_35` | In-app product (consumable) | ₹99 | 45 chat answers |
| `payg_chat_100` | In-app product (consumable) | ₹199 | 110 chat answers |

## Play Console Setup Checklist

1. **Create the app** in Google Play Console with the exact application ID `com.aistudio.astrokundli.vxmjqa`.
2. **Monetize → Products → Subscriptions**: create `sub_elite_monthly` and `sub_elite_annual`
   with a base plan (auto-renewing, monthly / yearly) and activate them.
3. **Monetize → Products → In-app products**: create the four `payg_chat_*` products as
   consumables, set prices, and activate them.
4. **Service account for verification**: Play Console → Setup → API access → link a Google Cloud
   project → create a service account with Play Console permissions (View financial data + Manage orders),
   download its JSON key, and grant the account access to this app in Play Console.
5. **Deploy the Cloud Functions** (see `functions/`):
   ```bash
   cd functions && npm install
   firebase functions:secrets:set PLAY_SERVICE_ACCOUNT_JSON   # paste the service-account JSON
   firebase deploy --only functions
   ```
   Set `ANDROID_PACKAGE_NAME=com.aistudio.astrokundli.vxmjqa` (defaults to that value) and
   `BACKEND_PROXY_URL` in the app's `.env` to the deployed function URL.
6. **Licence testers** (Play Console → Setup → License testing): add your Google account, then test
   purchases against the real Play purchase sheet (test purchases are free and still hit the
   Play Developer API).
7. **Release build signing** — the release build reads environment variables (or place `my-upload-key.jks` in the repo root):
   ```bash
   export KEYSTORE_PATH=/path/to/my-upload-key.jks
   export STORE_PASSWORD=...
   export KEY_PASSWORD=...
   ./gradlew :app:bundleRelease
   ```
   Upload `app/build/outputs/bundle/release/app-release.aab` to a Play Console internal-testing track.
8. **Store listing / policy**: complete Data safety, privacy policy URL, content rating, and
   target audience forms. Billing library version is 8.0.0+ (required by Play from Aug 2026).

## Testing

Pure JVM unit tests (billing catalog, entitlements, transaction filtering):

```bash
./gradlew :app:testDebugUnitTest
```

Firestore security-rule and repository tests need the Firebase emulators (Firestore on 8085, Auth on 9099):

```bash
firebase emulators:exec --only firestore,auth --project demo-no-project "./gradlew :app:testDebugUnitTest"
```

## Notes

- Release builds use the `release` signing config (env vars above). Debug builds use the standard
  auto-generated `~/.android/debug.keystore`.
- If you published via AI Studio before, request an upload key reset:
  https://support.google.com/googleplay/android-developer/answer/9842756
