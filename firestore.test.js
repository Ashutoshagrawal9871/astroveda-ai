const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      preferredLanguage: "en",
      aiMemoryEnabled: true,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user isolation: Bob cannot read Alice's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      preferredLanguage: "en",
      aiMemoryEnabled: true,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("User can create and read own Kundli profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("kundli_profiles").doc("chart_1").set({
      id: "chart_1",
      userId: ALICE_UID,
      name: "Alice Kundli",
      birthDate: "1995-10-24",
      birthTime: "06:45",
      birthPlace: "New Delhi, India",
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("kundli_profiles").doc("chart_1").get()
  );
});

test("Bob cannot read Alice's Kundli profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("kundli_profiles").doc("chart_1").set({
      id: "chart_1",
      userId: ALICE_UID,
      name: "Alice Kundli",
      birthDate: "1995-10-24",
      birthTime: "06:45",
      birthPlace: "New Delhi, India",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("kundli_profiles").doc("chart_1").get()
  );
});

test("Schema validation: Invalid language fails user profile creation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      preferredLanguage: "es", // only 'en' and 'hi' allowed
      aiMemoryEnabled: true,
    })
  );
});

test("User Profile: stores complete birth details for Kundli calculation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice Seeker",
      preferredLanguage: "en",
      aiMemoryEnabled: true,
      birthDate: "1994-11-20",
      birthTime: "07:15",
      birthPlace: "Varanasi, Uttar Pradesh, India",
      latitude: 25.3176,
      longitude: 82.9739,
      timezoneOffset: 5.5,
      gender: "female",
      isTimeApproximate: false,
    })
  );
  const doc = await aliceDb.collection("users").doc(ALICE_UID).get();
  assertSucceeds(doc);
});

