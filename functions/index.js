const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Helper: send FCM to a user by uid
async function notifyUser(uid, title, body) {
  if (!uid) return;
  const userDoc = await db.collection("users").doc(uid).get();
  const token = userDoc.data()?.fcmToken;
  if (!token) return;
  await admin.messaging().send({
    token,
    notification: { title, body },
    android: { priority: "high" },
  });
}

// Trigger 1: New care request → notify the agency admin
exports.onCareRequestCreated = functions.firestore
  .document("careRequests/{requestId}")
  .onCreate(async (snap) => {
    const req = snap.data();
    const agencyId = req.agencyId;
    if (!agencyId) return;
    const usersSnap = await db.collection("users")
      .where("agencyId", "==", agencyId)
      .where("role", "==", "AGENCY")
      .limit(1)
      .get();
    if (usersSnap.empty) return;
    const adminUid = usersSnap.docs[0].id;
    await notifyUser(
      adminUid,
      "New care request",
      `${req.familyName} needs a caregiver for ${req.patientName}`
    );
  });

// Trigger 2: Care request assigned → notify the family
exports.onCareRequestAssigned = functions.firestore
  .document("careRequests/{requestId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();
    if (before.status === after.status) return;
    if (after.status !== "ASSIGNED") return;
    const familyUid = after.familyUid;
    await notifyUser(
      familyUid,
      "Caregiver assigned",
      `${after.assignedCaregiverName} has been assigned to care for ${after.patientName}`
    );
  });

// Trigger 3: Shift report filed → notify the family
exports.onShiftReportCreated = functions.firestore
  .document("reports/{reportId}")
  .onCreate(async (snap) => {
    const report = snap.data();
    const bookingsSnap = await db.collection("bookings")
      .where("caregiverUid", "==", report.caregiverId ?? "")
      .where("status", "in", ["IN_PROGRESS", "CONFIRMED"])
      .limit(1)
      .get();
    if (bookingsSnap.empty) return;
    const booking = bookingsSnap.docs[0].data();
    const customerUid = booking.customerUid;
    await notifyUser(
      customerUid,
      "Shift report filed",
      `${report.caregiverName} has submitted today's care report`
    );
  });
