const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "demo-no-project",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("unauthenticated read fails", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users").doc("user1").collection("products").get());
});

test("authenticated user can create their own product", async () => {
  const user1Db = testEnv.authenticatedContext("user1").firestore();
  await assertSucceeds(
    user1Db.collection("users").doc("user1").collection("products").add({
      userId: "user1",
      name: "Ceyvana Premium Cinnamon",
      category: "SPICES"
    })
  );
});

test("user cannot read another user's products", async () => {
  const user2Db = testEnv.authenticatedContext("user2").firestore();
  await assertFails(
    user2Db.collection("users").doc("user1").collection("products").doc("prod1").get()
  );
});

test("user cannot write product for another user", async () => {
  const user1Db = testEnv.authenticatedContext("user1").firestore();
  await assertFails(
    user1Db.collection("users").doc("user2").collection("products").add({
      userId: "user2",
      name: "Hacked Cinnamon",
      category: "SPICES"
    })
  );
});

test("authenticated user can record their own sale", async () => {
  const user1Db = testEnv.authenticatedContext("user1").firestore();
  await assertSucceeds(
    user1Db.collection("users").doc("user1").collection("sales").add({
      userId: "user1",
      date: "2026-10-25",
      discount: 0
    })
  );
});

test("unauthenticated sale write fails", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthedDb.collection("users").doc("user1").collection("sales").add({
      userId: "user1",
      date: "2026-10-25"
    })
  );
});
