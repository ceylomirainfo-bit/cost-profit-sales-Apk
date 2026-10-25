package com.example.rules

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CeyvanaRulesTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUser_cannotReadProducts() = runTest {
    auth.signOut()
    try {
      firestore.collection("users").document("user1").collection("products").get().await()
      fail("Expected PERMISSION_DENIED for unauthenticated read")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }

  @Test
  fun authenticatedUser_canCreateProduct() = runTest {
    val uid = signInTestUser("owner@ceyvana.lk")
    val productData = hashMapOf(
      "userId" to uid,
      "name" to "Ceyvana Premium Roasted Curry Powder",
      "category" to "SPICES",
      "description" to "Traditional blend",
      "isArchived" to false
    )

    val docRef = firestore.collection("users").document(uid).collection("products").add(productData).await()
    assertNotNull(docRef.id)

    val snapshot = docRef.get().await()
    assertTrue(snapshot.exists())
    assertEquals("Ceyvana Premium Roasted Curry Powder", snapshot.getString("name"))
  }

  @Test
  fun authenticatedUser_cannotReadOtherUsersProducts() = runTest {
    val aliceUid = signInTestUser("alice@ceyvana.lk")
    val productData = hashMapOf(
      "userId" to aliceUid,
      "name" to "Alice Spices",
      "category" to "SPICES"
    )
    val docRef = firestore.collection("users").document(aliceUid).collection("products").add(productData).await()

    // Sign in as Bob
    val bobUid = signInTestUser("bob@ceyvana.lk")
    try {
      firestore.collection("users").document(aliceUid).collection("products").document(docRef.id).get().await()
      fail("Expected PERMISSION_DENIED when Bob reads Alice's products")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }

  @Test
  fun authenticatedUser_canCreateSale() = runTest {
    val uid = signInTestUser("sales@ceyvana.lk")
    val saleData = hashMapOf(
      "userId" to uid,
      "date" to "2026-10-25",
      "discount" to 0.0,
      "isCancelled" to false,
      "orderReference" to "ORD-1001"
    )

    val docRef = firestore.collection("users").document(uid).collection("sales").add(saleData).await()
    assertNotNull(docRef.id)

    val snapshot = docRef.get().await()
    assertTrue(snapshot.exists())
    assertEquals("ORD-1001", snapshot.getString("orderReference"))
  }

  @Test
  fun unauthenticatedUser_cannotWriteSale() = runTest {
    auth.signOut()
    val saleData = hashMapOf(
      "userId" to "unauthed_user",
      "date" to "2026-10-25"
    )
    try {
      firestore.collection("users").document("unauthed_user").collection("sales").add(saleData).await()
      fail("Expected PERMISSION_DENIED for unauthenticated sale write")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }

  @Test
  fun authenticatedUser_canCreateOrderNote() = runTest {
    val uid = signInTestUser("manager@ceyvana.lk")
    val orderNoteData = hashMapOf(
      "userId" to uid,
      "date" to "2026-10-25",
      "reference" to "MEMO-501",
      "orderDetails" to "10x 100g Cinnamon quills",
      "totalQuantity" to 10.0,
      "isArchived" to false
    )

    val docRef = firestore.collection("users").document(uid).collection("order_notes").add(orderNoteData).await()
    assertNotNull(docRef.id)

    val snapshot = docRef.get().await()
    assertTrue(snapshot.exists())
    assertEquals("MEMO-501", snapshot.getString("reference"))
  }
}
