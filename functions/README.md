# Jyotish AI - Secure Backend Proxy Service

This directory contains the **Firebase Cloud Functions (v2)** backend proxy service that securely mediates client requests to **Google Gemini AI**.

---

## 🔒 Security Architecture

1. **Zero Client Secrets**:
   The Android application (`Jyotish AI`) does **not** store or package `GEMINI_API_KEY`.
2. **Server-Side Verification**:
   The backend proxy verifies Firebase Authentication tokens, rate limits queries, checks entitlements, and injects server-side API keys directly into upstream requests to:
   - `generativelanguage.googleapis.com` (Gemini 2.5 Flash / Pro)
3. **Google Play Verification & Entitlement Ledger**:
   Purchases are verified server-side with Google Play Developer API and recorded in Firestore.

---

## 🚀 Available Endpoints

- `GET /api/health`: Health status and backend proxy service verification.
- `POST /api/chat`: Gemini Astrology Chat Proxy with Devi Priya Vedic persona and Kundli context.
- `POST /api/voice/session`: Standalone Gemini Live Voice Session initialization.
- `POST /api/voice/end`: Server-side voice session termination and minute deduction.
- `POST /api/transcribe`: Audio transcription using Gemini Multimodal Audio.
- `POST /api/billing/verify-play-purchase`: Google Play purchase token verification and idempotent entitlement crediting.

---

## ⚙️ Deployment & Secrets Configuration

### 1. Configure Secrets in Firebase / Google Cloud
Run:
```bash
firebase functions:secrets:set GEMINI_API_KEY
```

### 2. Local Testing (Firebase Emulator)
```bash
cd functions
npm install
npm run serve
```
Local proxy endpoint: `http://10.0.2.2:5001/<your-project-id>/asia-southeast1/api` (for Android emulator)

### 3. Deploy to Production
```bash
firebase deploy --only functions
```
Once deployed, copy your Cloud Function URL (e.g. `https://<region>-<project-id>.cloudfunctions.net/api`) into the app's settings or `.env` `BACKEND_PROXY_URL`.
