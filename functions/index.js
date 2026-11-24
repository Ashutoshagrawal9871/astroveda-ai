import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import { onRequest } from "firebase-functions/v2/https";
import admin from "firebase-admin";
import { GoogleGenAI } from "@google/genai";
import { google } from "googleapis";

dotenv.config();

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

const app = express();
app.use(cors({ origin: true }));
app.use(express.json({ limit: "15mb" }));

// Android application ID that purchased tokens must belong to.
const ANDROID_PACKAGE_NAME =
  process.env.ANDROID_PACKAGE_NAME || "com.aistudio.astrokundli.vxmjqa";

// System instruction for AI Astrologer Devi Priya
const DEVI_PRIYA_SYSTEM_INSTRUCTION = `You are Devi Priya, a revered, warm, and highly erudite AI Vedic Astrologer (Jyotishi) with deep knowledge of Parashari Jyotish, Jaimini, and Brihat Samhita.
TRUST & SAFETY DISCLOSURE MANDATE:
1. Clearly state that Jyotish is an interpretive cosmic science of probabilities, NOT guaranteed or fatalistic prediction.
2. Never provide medical diagnosis, psychiatric advice, legal counsel, or guaranteed financial investment tips.
3. Never use fear-based tactics (like threatening Rahu/Ketu/Shani doom) or pressure to purchase rituals. Empower the querent with clarity, karma yoga, and peace of mind.
4. Maintain transparency that you are an AI persona, not a human astrologer.`;

/**
 * Middleware: Verify Firebase Authentication Token
 * Validates the caller's Firebase ID Token, ensuring all operations are bound to an authentic user UID.
 */
async function authenticateUser(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(401).json({
      error: "Unauthorized: Missing or invalid Authorization header. A valid Firebase ID token is required.",
    });
  }

  const idToken = authHeader.split("Bearer ")[1].trim();
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error("Token verification failed:", error.message);
    return res.status(401).json({
      error: "Unauthorized: Invalid or expired Firebase ID token.",
      details: error.message,
    });
  }
}

/**
 * Health check endpoint (Public)
 */
app.get(["/api/health", "/health", "/"], (req, res) => {
  res.json({
    status: "online",
    service: "Jyotish AI Firebase Cloud Functions Production Proxy",
    timestamp: new Date().toISOString(),
    geminiConfigured: !!(process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== "MY_GEMINI_API_KEY"),
    playVerificationConfigured: !!(process.env.PLAY_SERVICE_ACCOUNT_JSON || process.env.GOOGLE_APPLICATION_CREDENTIALS),
  });
});

/**
 * Product Mapping: Server-Side Source of Truth for Google Play Billing Products.
 * These IDs must exactly match the products configured in Google Play Console.
 */
const PLAY_PRODUCTS = {
  sub_elite_monthly: {
    type: "SUBSCRIPTION",
    title: "Elite Monthly",
    priceInr: 399,
    durationDays: 30,
    chatCredits: 200, // Monthly refreshed allowance
    voiceMinutes: 0,
    isAdFree: true,
  },
  sub_elite_annual: {
    type: "SUBSCRIPTION",
    title: "Elite Annual",
    priceInr: 3999,
    durationDays: 365,
    chatCredits: 200, // Monthly refreshed allowance
    voiceMinutes: 0,
    isAdFree: true,
  },
  payg_chat_quick: {
    type: "CONSUMABLE",
    title: "8 Quick Answers",
    priceInr: 29,
    chatCredits: 8,
  },
  payg_chat_15: {
    type: "CONSUMABLE",
    title: "18 Chat Answers",
    priceInr: 49,
    chatCredits: 18,
  },
  payg_chat_35: {
    type: "CONSUMABLE",
    title: "45 Chat Answers",
    priceInr: 99,
    chatCredits: 45,
  },
  payg_chat_100: {
    type: "CONSUMABLE",
    title: "110 Chat Answers",
    priceInr: 199,
    chatCredits: 110,
  },
};

const SUBSCRIPTION_MONTHLY_ALLOWANCE = 200;
const THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000;

const DEFAULT_ENTITLEMENTS = {
  planType: "PAY_AS_YOU_USE",
  expiryMillis: 0,
  chatCredits: 0,
  voiceMinutes: 0,
  isAdFree: false,
  monthlyAllowanceRefreshedTimestamp: 0,
};

/**
 * Builds an authorized Google Play Developer API (androidpublisher) client.
 * Prefers PLAY_SERVICE_ACCOUNT_JSON (Secret Manager) and falls back to
 * Application Default Credentials of the Cloud Functions service account.
 * The service account must be granted access in Google Play Console.
 */
function getAndroidPublisher() {
  const scopes = ["https://www.googleapis.com/auth/androidpublisher"];
  const raw = process.env.PLAY_SERVICE_ACCOUNT_JSON;
  const credentials =
    raw && raw.trim().startsWith("{") ? JSON.parse(raw) : undefined;
  const auth = new google.auth.GoogleAuth({ credentials, scopes });
  return google.androidpublisher({ version: "v3", auth });
}

/**
 * Verifies a purchase token directly with the Google Play Developer API.
 * This is the ONLY trusted source of purchase truth — client payloads are never trusted.
 */
async function verifyPlayPurchase(productId, purchaseToken, productConfig) {
  const androidPublisher = getAndroidPublisher();
  const packageName = ANDROID_PACKAGE_NAME;

  if (productConfig.type === "SUBSCRIPTION") {
    const response = await androidPublisher.purchases.subscriptionsv2.get({
      packageName,
      token: purchaseToken,
    });
    const subscription = response.data || {};
    // CANCELED still grants access until expiryTime (user turned off auto-renew);
    // the expiry check below revokes access once the paid period ends.
    const validStates = [
      "SUBSCRIPTION_STATE_ACTIVE",
      "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
      "SUBSCRIPTION_STATE_CANCELED",
    ];
    if (!validStates.includes(subscription.subscriptionState)) {
      return {
        ok: false,
        reason: `Subscription is not active on Google Play (state: ${subscription.subscriptionState || "unknown"}).`,
      };
    }
    const lineItems = subscription.lineItems || [];
    const lineItem =
      lineItems.find((item) => item.productId === productId) || lineItems[0];
    if (!lineItem || lineItem.productId !== productId) {
      return {
        ok: false,
        reason: "Purchased subscription does not match the requested product.",
      };
    }
    const expiryMillis = lineItem.expiryTime
      ? new Date(lineItem.expiryTime).getTime()
      : 0;
    if (!expiryMillis || expiryMillis <= Date.now()) {
      return { ok: false, reason: "Subscription has already expired." };
    }
    return {
      ok: true,
      expiryMillis,
      orderId: subscription.latestOrderId || "",
    };
  }

  const response = await androidPublisher.purchases.products.get({
    packageName,
    productId,
    token: purchaseToken,
  });
  const purchase = response.data || {};
  if (purchase.purchaseState !== 0) {
    return {
      ok: false,
      reason: `One-time product purchase is not completed (state: ${purchase.purchaseState}).`,
    };
  }
  if (purchase.consumptionState === 1) {
    return { ok: false, reason: "Purchase token was already consumed." };
  }
  return { ok: true, orderId: purchase.orderId || "" };
}

/**
 * Lazily refreshes the monthly AI answer allowance for active subscribers.
 * Elite subscriptions include 200 answers refreshed every 30 days.
 */
function refreshMonthlyAllowanceIfDue(entitlements, now) {
  const isSubscriptionPlan =
    entitlements.planType && entitlements.planType !== "PAY_AS_YOU_USE";
  const isActive = isSubscriptionPlan && entitlements.expiryMillis > now;
  if (!isActive) return { entitlements, changed: false };

  const lastRefresh = entitlements.monthlyAllowanceRefreshedTimestamp || 0;
  if (now - lastRefresh >= THIRTY_DAYS_MS) {
    return {
      entitlements: {
        ...entitlements,
        chatCredits: SUBSCRIPTION_MONTHLY_ALLOWANCE,
        monthlyAllowanceRefreshedTimestamp: now,
      },
      changed: true,
    };
  }
  return { entitlements, changed: false };
}

/**
 * POST /api/billing/verify-play-purchase
 * Authenticated endpoint: Verifies a Google Play purchase token against the
 * Google Play Developer API, enforces idempotency, and atomically credits
 * user entitlements in Firestore.
 */
app.post(
  ["/api/billing/verify-play-purchase", "/billing/verify-play-purchase"],
  authenticateUser,
  async (req, res) => {
    const uid = req.user.uid;
    const { purchaseToken, productId, orderId } = req.body || {};

    if (!purchaseToken || !productId) {
      return res.status(400).json({
        verified: false,
        error: "Missing purchaseToken or productId in request body.",
      });
    }

    const productConfig = PLAY_PRODUCTS[productId];
    if (!productConfig) {
      return res.status(400).json({
        verified: false,
        error: `Unrecognized Google Play product ID: ${productId}`,
      });
    }

    // 1. Verify the purchase token with Google Play before trusting anything.
    let verification;
    try {
      verification = await verifyPlayPurchase(
        productId,
        purchaseToken,
        productConfig
      );
    } catch (error) {
      console.error("Google Play verification API error:", error.message);
      return res.status(503).json({
        verified: false,
        error:
          "Google Play purchase verification is temporarily unavailable or not configured. No credits were granted.",
        details: error.message,
      });
    }

    if (!verification.ok) {
      console.warn(
        `[Billing] Rejected purchase for UID ${uid}, product ${productId}: ${verification.reason}`
      );
      return res.status(400).json({
        verified: false,
        status: "REJECTED",
        message: verification.reason,
      });
    }

    const db = admin.firestore();
    const tokenDocRef = db
      .collection("redeemed_play_tokens")
      .doc(purchaseToken.replace(/[\/\.]/g, "_"));
    const userEntitlementsRef = db
      .collection("users")
      .doc(uid)
      .collection("billing")
      .doc("entitlements");
    const auditLedgerRef = db.collection("billing_audit_ledger").doc();
    const resolvedOrderId =
      orderId || verification.orderId || "PLAY_" + Date.now();

    try {
      let finalEntitlements;
      await db.runTransaction(async (transaction) => {
        // 2. Idempotency Check: Verify token has not already been redeemed
        const tokenDoc = await transaction.get(tokenDocRef);
        if (tokenDoc.exists) {
          throw new Error("TOKEN_ALREADY_REDEEMED");
        }

        // 3. Fetch or initialize current user entitlements
        const entitlementsDoc = await transaction.get(userEntitlementsRef);
        const current = entitlementsDoc.exists
          ? entitlementsDoc.data()
          : { ...DEFAULT_ENTITLEMENTS };

        const now = Date.now();
        let updatedEntitlements = { ...current };

        if (productConfig.type === "SUBSCRIPTION") {
          updatedEntitlements.planType =
            productId === "sub_elite_annual" ? "ELITE_ANNUAL" : "ELITE_MONTHLY";
          updatedEntitlements.expiryMillis =
            verification.expiryMillis ||
            now + productConfig.durationDays * 24 * 60 * 60 * 1000;
          updatedEntitlements.chatCredits = productConfig.chatCredits;
          updatedEntitlements.voiceMinutes = productConfig.voiceMinutes || 0;
          updatedEntitlements.isAdFree = true;
          updatedEntitlements.monthlyAllowanceRefreshedTimestamp = now;
        } else {
          // Consumable products add to the current credit balance
          updatedEntitlements.chatCredits =
            (updatedEntitlements.chatCredits || 0) +
            (productConfig.chatCredits || 0);
          if (productConfig.voiceMinutes) {
            updatedEntitlements.voiceMinutes =
              (updatedEntitlements.voiceMinutes || 0) +
              productConfig.voiceMinutes;
          }
        }

        finalEntitlements = updatedEntitlements;

        // 4. Mark token redeemed
        transaction.set(tokenDocRef, {
          redeemedBy: uid,
          productId,
          orderId: resolvedOrderId,
          redeemedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        // 5. Update user's source of truth entitlements
        transaction.set(userEntitlementsRef, updatedEntitlements);

        // 6. Append durable audit entry (purchase token stays server-side only)
        transaction.set(auditLedgerRef, {
          uid,
          action: "PLAY_PURCHASE_VERIFIED",
          productId,
          orderId: resolvedOrderId,
          purchaseToken,
          granted: {
            type: productConfig.type,
            chatCredits: productConfig.chatCredits || 0,
            isAdFree: !!productConfig.isAdFree,
          },
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
        });
      });

      console.log(
        `[Billing] Play purchase verified for UID ${uid}, product: ${productId}`
      );
      return res.json({
        verified: true,
        status: "ACTIVATED",
        productId,
        orderId: resolvedOrderId,
        entitlements: finalEntitlements,
        message: "Purchase successfully verified and entitlements credited.",
      });
    } catch (error) {
      if (error.message === "TOKEN_ALREADY_REDEEMED") {
        // Purchase was verified before (e.g. reinstall or restore).
        // Return the current server-side entitlements so the client re-syncs.
        const doc = await userEntitlementsRef.get();
        const current = doc.exists
          ? doc.data()
          : { ...DEFAULT_ENTITLEMENTS };
        return res.status(409).json({
          verified: true,
          status: "ALREADY_PROCESSED",
          entitlements: current,
          message:
            "Purchase token was already verified and credited previously.",
        });
      }
      console.error("Error processing Play purchase transaction:", error);
      return res.status(500).json({
        verified: false,
        error: "Failed to verify purchase with server.",
      });
    }
  }
);

/**
 * GET /api/billing/entitlements
 * Retrieves the user's verified server-side balances and plan state.
 */
app.get(
  ["/api/billing/entitlements", "/billing/entitlements"],
  authenticateUser,
  async (req, res) => {
    try {
      const uid = req.user.uid;
      const db = admin.firestore();
      const doc = await db
        .collection("users")
        .doc(uid)
        .collection("billing")
        .doc("entitlements")
        .get();

      if (!doc.exists) {
        return res.json({ ...DEFAULT_ENTITLEMENTS });
      }

      let data = doc.data();
      const now = Date.now();
      // Keep the monthly allowance fresh even if the user is not chatting yet.
      const refreshed = refreshMonthlyAllowanceIfDue(data, now);
      if (refreshed.changed) {
        data = refreshed.entitlements;
        await doc.ref.set(data);
      }
      return res.json(data);
    } catch (error) {
      return res.status(500).json({ error: error.message });
    }
  }
);

/**
 * GET /api/billing/history
 * Returns the user's verified purchase ledger (sanitized for client display).
 */
app.get(
  ["/api/billing/history", "/billing/history"],
  authenticateUser,
  async (req, res) => {
    try {
      const uid = req.user.uid;
      const db = admin.firestore();
      const snapshot = await db
        .collection("billing_audit_ledger")
        .where("uid", "==", uid)
        .limit(100)
        .get();

      const orders = snapshot.docs
        .map((doc) => {
          const entry = doc.data();
          const config = PLAY_PRODUCTS[entry.productId] || {};
          return {
            orderId: entry.orderId || "",
            productId: entry.productId || "",
            title: config.title || entry.productId || "Digital Purchase",
            amountInr: config.priceInr || 0,
            isSubscription: config.type === "SUBSCRIPTION",
            timestampMillis:
              (entry.timestamp && entry.timestamp.toMillis
                ? entry.timestamp.toMillis()
                : 0) || 0,
            status: "VERIFIED_ACTIVE",
            paymentMethod: "Google Play",
          };
        })
        .sort((a, b) => b.timestampMillis - a.timestampMillis)
        .slice(0, 50);

      return res.json({ orders });
    } catch (error) {
      console.error("Error fetching purchase history:", error);
      return res.status(500).json({ error: error.message });
    }
  }
);

/**
 * POST /api/chat
 * Secure, quota-enforced Gemini Astrology Chat Proxy.
 * Enforces server-side authentication, quota deductions, and safety constraints.
 */
app.post(["/api/chat", "/chat"], authenticateUser, async (req, res) => {
  try {
    const uid = req.user.uid;
    const {
      message,
      kundli,
      chatHistory = [],
      language = "en",
      isVoiceOrVideo = false,
      useSearchGrounding = false,
    } = req.body || {};

    if (!message || typeof message !== "string" || !message.trim()) {
      return res.status(400).json({ error: "Missing or invalid 'message' field in request body." });
    }

    if (message.length > 8000) {
      return res.status(400).json({ error: "Message exceeds maximum allowed length of 8000 characters." });
    }

    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey || apiKey === "MY_GEMINI_API_KEY") {
      return res.status(503).json({
        error: "GEMINI_API_KEY is not configured on the backend server. Please contact support.",
      });
    }

    // Atomically reserve one chat credit before calling Gemini so two concurrent
    // requests can never spend the same credit (lost-update race).
    const db = admin.firestore();
    const userBillingRef = db.collection("users").doc(uid).collection("billing").doc("entitlements");
    let isSubActive = false;
    let chatCreditsRemaining = 0;

    try {
      const charge = await db.runTransaction(async (transaction) => {
        const billingDoc = await transaction.get(userBillingRef);
        let billingData = billingDoc.exists
          ? billingDoc.data()
          : { ...DEFAULT_ENTITLEMENTS };

        // Refresh the subscriber monthly allowance if a new 30-day period began.
        const now = Date.now();
        const refreshed = refreshMonthlyAllowanceIfDue(billingData, now);
        if (refreshed.changed) billingData = refreshed.entitlements;

        const subActive =
          !!billingData.planType &&
          billingData.planType !== "PAY_AS_YOU_USE" &&
          (billingData.expiryMillis || 0) > now;
        const credits = billingData.chatCredits || 0;

        if (credits <= 0) {
          const quotaError = new Error("NO_CREDITS");
          quotaError.code = "NO_CREDITS";
          quotaError.subActive = subActive;
          throw quotaError;
        }

        transaction.set(userBillingRef, {
          ...billingData,
          chatCredits: credits - 1,
        });
        return { subActive, remaining: credits - 1 };
      });
      isSubActive = charge.subActive;
      chatCreditsRemaining = charge.remaining;
    } catch (error) {
      if (error.code === "NO_CREDITS") {
        return res.status(403).json({
          error: "NO_CREDITS",
          message: error.subActive
            ? "Your monthly AI answer allowance is fully used. It refreshes automatically in your next billing period, or you can purchase additional answer packs."
            : "You have no active subscription or chat credits remaining. Please subscribe to Elite Plan or purchase chat credits to continue.",
        });
      }
      throw error;
    }

    const refundChatCredit = async () => {
      try {
        await userBillingRef.set(
          { chatCredits: admin.firestore.FieldValue.increment(1) },
          { merge: true }
        );
      } catch (refundError) {
        console.error(`[Chat] Failed to refund reserved credit for UID ${uid}:`, refundError);
      }
    };

    // Build authentic Kundli context
    let chartContext = "No birth chart provided.";
    if (kundli && kundli.birthDate) {
      chartContext = `USER'S KUNDLI (BIRTH CHART):
- Name: ${kundli.personName || "Querent"}
- Birth Date & Time: ${kundli.birthDate} at ${kundli.birthTime || "12:00"} (${kundli.isTimeApproximate ? "Approximate/Unknown Time" : "Accurate Time"})
- Birth Place: ${kundli.birthPlace || "Unknown"}
- Lagna (Ascendant): ${kundli.lagnaSignName || "Mesha"}
- Rashi (Moon Sign): ${kundli.rashiSignName || "Vrishabha"}
- Nakshatra: ${kundli.nakshatra || "Rohini"} (Pada ${kundli.nakshatraPada || 1})
- Current Mahadasha: ${kundli.currentMahadasha || "Jupiter"}, Antardasha: ${kundli.currentAntardasha || "Saturn"}
- Manglik Status: ${kundli.manglikStatus || "Non-Manglik"}
- Key Placements: ${Array.isArray(kundli.planets) ? kundli.planets.map(p => `${p.name} in ${p.signName} (House ${p.house}, ${p.dignity || "Neutral"})`).join("; ") : "N/A"}`;
    }

    const langDirective = language === "hi"
      ? "Speak in graceful, authentic Hindi (हिंदी) with occasional uplifting Sanskrit mantras."
      : "Speak in warm, compassionate English with Vedic astrological concepts clearly explained.";

    const modeDirective = isVoiceOrVideo
      ? "Spoken live voice/video format: keep answer concise (2-4 natural sentences) for interactive turn-taking."
      : "Comprehensive written format: cite relevant bhavas (houses), planetary dashas, and provide practical sattvic remedies.";

    const systemPrompt = `${DEVI_PRIYA_SYSTEM_INSTRUCTION}\n\n${langDirective}\n${modeDirective}\n\n${chartContext}`;

    const contents = [];
    const recentTurns = Array.isArray(chatHistory) ? chatHistory.slice(-6) : [];
    for (const turn of recentTurns) {
      const role = turn.role === "user" ? "user" : "model";
      contents.push({ role, parts: [{ text: turn.text || "" }] });
    }
    contents.push({ role: "user", parts: [{ text: message }] });

    const ai = new GoogleGenAI({ apiKey });
    const model = "gemini-2.5-flash";

    const config = {
      systemInstruction: systemPrompt,
      temperature: 0.7,
      topP: 0.95,
      maxOutputTokens: isVoiceOrVideo ? 250 : 1024,
    };

    if (useSearchGrounding) {
      config.tools = [{ googleSearch: {} }];
    }

    let replyText = "";
    try {
      const response = await ai.models.generateContent({ model, contents, config });
      replyText = response.text || "";
    } catch (generationError) {
      await refundChatCredit();
      throw generationError;
    }

    if (!replyText) {
      await refundChatCredit();
      return res.status(500).json({ error: "No response text generated by Gemini model." });
    }

    return res.json({
      status: "success",
      astrologer: "Devi Priya",
      reply: replyText.trim(),
      language,
      chatCreditsRemaining,
      planActive: isSubActive,
    });
  } catch (error) {
    console.error("Error in /api/chat proxy:", error);
    return res.status(500).json({
      error: "Internal server error while processing chat.",
      message: error.message,
    });
  }
});

/**
 * POST /api/account/delete
 * Authenticated endpoint: Fully deletes the user's Firestore document, subcollections (kundli_profiles, chat_messages, billing),
 * audit ledger entries, and Firebase Auth account (GDPR & Google Play Account Deletion compliance).
 */
app.post(["/api/account/delete", "/account/delete"], authenticateUser, async (req, res) => {
  const uid = req.user.uid;
  const db = admin.firestore();

  try {
    // 1. Recursively delete the user document and every subcollection (profiles, chats,
    //    billing). recursiveDelete pages internally, avoiding Firestore's 500-write batch cap.
    await db.recursiveDelete(db.collection("users").doc(uid));

    // 2. Delete audit ledger records in bounded batches.
    const auditSnap = await db.collection("billing_audit_ledger").where("uid", "==", uid).get();
    const auditRefs = auditSnap.docs.map((doc) => doc.ref);
    for (let i = 0; i < auditRefs.length; i += 400) {
      const batch = db.batch();
      auditRefs.slice(i, i + 400).forEach((ref) => batch.delete(ref));
      await batch.commit();
    }

    // 3. Delete the Firebase Auth user record. An already-deleted user is treated as
    //    success so retries after a partial failure stay idempotent.
    try {
      await admin.auth().deleteUser(uid);
    } catch (authError) {
      if (authError.code !== "auth/user-not-found") throw authError;
    }

    console.log(`[Account] Successfully deleted all data and auth record for UID ${uid}`);
    return res.json({ success: true, message: "Account and all associated data permanently deleted." });
  } catch (error) {
    console.error(`[Account] Error deleting account for UID ${uid}:`, error);
    return res.status(500).json({ error: "Failed to delete account and data.", details: error.message });
  }
});


// Export as Firebase Cloud Function v2 with Secret Manager bindings
export const api = onRequest(
  {
    cors: true,
    maxInstances: 10,
    secrets: ["GEMINI_API_KEY", "PLAY_SERVICE_ACCOUNT_JSON"],
  },
  app
);

// For local direct execution with `node index.js`
if (process.env.NODE_ENV !== "production" && process.env.RUN_LOCAL === "true") {
  const PORT = process.env.PORT || 5001;
  app.listen(PORT, () => {
    console.log(`Jyotish AI Backend Proxy running locally on port ${PORT}`);
  });
}
