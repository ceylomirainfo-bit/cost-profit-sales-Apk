package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.security.AuthManager
import com.example.ui.security.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ceyvana Spices", appName)
  }

  @Test
  fun `test AuthManager initial state and login flow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = AuthManager(context)

    // Ensure clean state
    authManager.logout()
    assertEquals(false, authManager.isUserLoggedIn())

    // Invalid credentials before configuration
    val invalidResult = authManager.authenticate("wrong@email.com", "wrongPass123")
    assertTrue(invalidResult is AuthResult.Error)
    assertEquals(false, authManager.isUserLoggedIn())

    // Setup secure account credentials
    val (setupOk, _) = authManager.setupAccountPassword("owner@ceyvana.lk", "owner", "SecurePass@2026")
    assertTrue(setupOk)

    // Authenticate with valid configured credentials
    val validResult = authManager.authenticate("owner@ceyvana.lk", "SecurePass@2026")
    assertTrue(validResult is AuthResult.Success)
    assertEquals(true, authManager.isUserLoggedIn())

    // Lock session
    authManager.lock()
    assertEquals(false, authManager.isUserLoggedIn())

    // Setup and verify 4-digit PIN
    val (pinSetupOk, _) = authManager.setupPin("8492")
    assertTrue(pinSetupOk)
    val pinResult = authManager.unlockWithPin("8492")
    assertTrue(pinResult is com.example.ui.security.PinResult.Success)
    assertEquals(true, authManager.isUserLoggedIn())

    // Logout
    authManager.logout()
    assertEquals(false, authManager.isUserLoggedIn())
  }

  @Test
  fun `test PDF and CSV report file generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val sale = com.example.data.model.SaleEntity(
        id = 1,
        saleDate = "2026-10-24",
        orderReference = "ORD-TEST-01",
        customerName = "Colombo Buyer",
        totalGross = 1200.0,
        totalNet = 1200.0,
        totalCost = 700.0,
        totalGrossProfit = 500.0
    )
    val item = com.example.data.model.SaleLineItemEntity(
        id = 1,
        saleId = 1,
        productId = 1,
        variantId = 1,
        productName = "Ceyvana Authentic Ceylon Cinnamon",
        variantName = "50g",
        category = "SPICES",
        unitCostPrice = 350.0,
        unitSellingPrice = 600.0,
        quantity = 2,
        discount = 0.0,
        grossAmount = 1200.0,
        netRevenue = 1200.0,
        costOfGoodsSold = 700.0,
        grossProfit = 500.0
    )
    val saleWithItems = com.example.data.model.SaleWithItems(sale, listOf(item))

    val reportData = com.example.data.model.ReportCalculator.computeReport(
        preset = com.example.data.model.ReportPeriodPreset.THIS_MONTH,
        startDateIso = "2026-10-01",
        endDateIso = "2026-10-31",
        salesWithItems = listOf(saleWithItems)
    )

    // Verify CSV generation
    val csvFile = com.example.ui.util.CsvReportGenerator.generateCsvReport(context, reportData)
    assertTrue("CSV file should exist", csvFile.exists())
    assertTrue("CSV file size should be > 0", csvFile.length() > 0)
    val csvContent = csvFile.readText()
    assertTrue("CSV should contain Ceyvana header", csvContent.contains("CEYVANA SPICES BUSINESS REPORT"))
    assertTrue("CSV should contain LKR currency", csvContent.contains("Currency,LKR"))
    assertTrue("CSV should contain cinnamon product", csvContent.contains("Ceyvana Authentic Ceylon Cinnamon"))

    // Verify PDF generation
    val pdfFile = com.example.ui.util.PdfReportGenerator.generatePdfReport(context, reportData)
    assertTrue("PDF file should exist", pdfFile.exists())
    assertTrue("PDF file size should be > 0", pdfFile.length() > 0)
  }
}

