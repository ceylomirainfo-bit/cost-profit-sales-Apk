package com.example.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.AppDatabase
import com.example.data.model.OrderNoteEntity
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ReportCalculator
import com.example.data.model.ReportPeriodPreset
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.example.data.remote.CeyvanaFirestoreRepository
import com.example.data.repository.CeyvanaRepository
import com.example.ui.util.CsvReportGenerator
import com.example.ui.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * End-to-end test suite for:
 * 1. Data persistence across app close and reopen (cold restart simulation).
 * 2. Cloud synchronization to Firestore when configured.
 * 3. PDF report generation, verifying structure, headers, totals, and line items.
 * 4. CSV report generation for spreadsheet verification.
 */
class DataPersistenceAndReportsRobolectricTest : FirestoreEmulatorTestBase() {

    private lateinit var context: Context
    private var testDbFile: File? = null

    @Before
    fun initContext() {
        context = ApplicationProvider.getApplicationContext<Context>()
        testDbFile = File(context.filesDir, "test_persistence_restart.db")
        if (testDbFile?.exists() == true) {
            testDbFile?.delete()
        }
    }

    @After
    fun cleanUpDb() {
        testDbFile?.let {
            if (it.exists()) it.delete()
        }
    }

    @Test
    fun testAppRestart_recordsPersistAcrossCloseAndReopen() = runTest {
        // --- SESSION 1: APP OPENED & DATA WRITTEN ---
        val db1 = Room.databaseBuilder(context, AppDatabase::class.java, testDbFile!!.absolutePath)
            .allowMainThreadQueries()
            .build()
        val repo1 = CeyvanaRepository(db1)

        // 1. Add product with variants
        val product = ProductEntity(
            name = "Ceyvana Ceylon Black Pepper",
            category = ProductCategory.SPICES,
            description = "Grade A whole black peppercorns from Matale",
            isArchived = false
        )
        val variants = listOf(
            ProductVariantEntity(
                productId = 0,
                variantName = "100g",
                costPrice = 320.00,
                sellingPrice = 580.00,
                currency = "LKR",
                isActive = true
            ),
            ProductVariantEntity(
                productId = 0,
                variantName = "250g",
                costPrice = 750.00,
                sellingPrice = 1350.00,
                currency = "LKR",
                isActive = true
            )
        )
        val prodId = repo1.addProductWithVariants(product, variants)
        assertTrue(prodId > 0)

        // 2. Record a sale
        val sale = SaleEntity(
            saleDate = "2026-10-26",
            orderReference = "ORD-PERSIST-101",
            customerName = "Galle Fort Cafe",
            notes = "Recurring wholesale spice order",
            orderDiscount = 60.00,
            totalGross = 1160.00,
            totalNet = 1100.00,
            totalCost = 640.00,
            totalGrossProfit = 460.00
        )
        val lineItem = SaleLineItemEntity(
            saleId = 0,
            productId = prodId,
            variantId = 1,
            productName = "Ceyvana Ceylon Black Pepper",
            variantName = "100g",
            category = ProductCategory.SPICES,
            unitCostPrice = 320.00,
            unitSellingPrice = 580.00,
            quantity = 2,
            discount = 60.00,
            grossAmount = 1160.00,
            netRevenue = 1100.00,
            costOfGoodsSold = 640.00,
            grossProfit = 460.00
        )
        val saleId = repo1.recordSale(sale, listOf(lineItem))
        assertTrue(saleId > 0)

        // 3. Add an order note
        val note = OrderNoteEntity(
            orderDate = "2026-10-26",
            orderReference = "ORD-PERSIST-101",
            customerName = "Galle Fort Cafe",
            orderDetails = "2x 100g Black Pepper",
            notes = "Delivery scheduled for Monday morning",
            status = "Confirmed"
        )
        db1.orderNoteDao().insertOrderNote(note)

        // --- SIMULATE APP CLOSE / PROCESS TERMINATION ---
        db1.close()

        // --- SESSION 2: APP REOPENED ---
        val db2 = Room.databaseBuilder(context, AppDatabase::class.java, testDbFile!!.absolutePath)
            .allowMainThreadQueries()
            .build()
        val repo2 = CeyvanaRepository(db2)

        // Verify products persisted
        val productsAfterRestart = repo2.allProducts.first { it.isNotEmpty() }
        val retrievedProduct = productsAfterRestart.firstOrNull { it.product.id == prodId }
        assertNotNull("Product must persist across restart", retrievedProduct)
        assertEquals("Ceyvana Ceylon Black Pepper", retrievedProduct!!.product.name)
        assertEquals(2, retrievedProduct.variants.size)
        assertEquals("100g", retrievedProduct.variants[0].variantName)
        assertEquals(580.00, retrievedProduct.variants[0].sellingPrice, 0.001)

        // Verify sales persisted
        val salesAfterRestart = repo2.getSalesForDate("2026-10-26").first { it.isNotEmpty() }
        assertEquals(1, salesAfterRestart.size)
        val retrievedSale = salesAfterRestart[0]
        assertEquals("ORD-PERSIST-101", retrievedSale.sale.orderReference)
        assertEquals("Galle Fort Cafe", retrievedSale.sale.customerName)
        assertEquals(1100.00, retrievedSale.sale.totalNet, 0.001)
        assertEquals(460.00, retrievedSale.sale.totalGrossProfit, 0.001)
        assertEquals(1, retrievedSale.items.size)
        assertEquals("Ceyvana Ceylon Black Pepper", retrievedSale.items[0].productName)

        // Verify order notes persisted
        val notesAfterRestart = repo2.getOrderNotesForDate("2026-10-26").first { it.isNotEmpty() }
        assertEquals(1, notesAfterRestart.size)
        assertEquals("Galle Fort Cafe", notesAfterRestart[0].customerName)
        assertEquals("Confirmed", notesAfterRestart[0].status)

        db2.close()
    }

    @Test
    fun testCloudSynchronization_syncsDataToFirestore() = runTest {
        // Authenticate test user with Firebase Auth emulator
        val uid = signInTestUser("ceyvanasync@ceyvana.lk")
        assertNotNull("Authenticated user ID must not be null", uid)

        // In-memory Room database for sync testing
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val firestoreRepo = CeyvanaFirestoreRepository(firestore)
        val repository = CeyvanaRepository(inMemoryDb, firestoreRepo)

        // Add product and record sale locally
        val product = ProductEntity(
            id = 42,
            name = "Ceyvana Premium Ceylon Cinnamon",
            category = ProductCategory.SPICES,
            description = "True Ceylon Alba Grade Cinnamon",
            isArchived = false
        )
        val variant = ProductVariantEntity(
            id = 101,
            productId = 42,
            variantName = "50g",
            costPrice = 400.00,
            sellingPrice = 750.00,
            currency = "LKR",
            isActive = true
        )
        inMemoryDb.productDao().insertProduct(product)
        inMemoryDb.productDao().insertVariant(variant)

        val sale = SaleEntity(
            id = 77,
            saleDate = "2026-10-26",
            orderReference = "ORD-CLOUD-077",
            customerName = "Kandy Luxury Tea & Spice",
            notes = "Synced to Firestore",
            orderDiscount = 50.00,
            totalGross = 750.00,
            totalNet = 700.00,
            totalCost = 400.00,
            totalGrossProfit = 300.00
        )
        val lineItem = SaleLineItemEntity(
            id = 1,
            saleId = 77,
            productId = 42,
            variantId = 101,
            productName = "Ceyvana Premium Ceylon Cinnamon",
            variantName = "50g",
            category = ProductCategory.SPICES,
            unitCostPrice = 400.00,
            unitSellingPrice = 750.00,
            quantity = 1,
            discount = 50.00,
            grossAmount = 750.00,
            netRevenue = 700.00,
            costOfGoodsSold = 400.00,
            grossProfit = 300.00
        )
        inMemoryDb.saleDao().insertSale(sale)
        inMemoryDb.saleDao().insertSaleLineItems(listOf(lineItem))

        val orderNote = OrderNoteEntity(
            id = 15,
            orderDate = "2026-10-26",
            orderReference = "ORD-CLOUD-077",
            customerName = "Kandy Luxury Tea & Spice",
            orderDetails = "1x 50g Ceylon Cinnamon",
            notes = "Cloud backup test",
            status = "Completed"
        )
        inMemoryDb.orderNoteDao().insertOrderNote(orderNote)

        // Trigger full sync to Firestore
        repository.syncAllToFirestore()

        // Verify documents exist in Firestore emulator
        val productDoc = firestore.collection("users").document(uid).collection("products").document("42").get().await()
        assertTrue("Product document must exist in Firestore", productDoc.exists())
        assertEquals("Ceyvana Premium Ceylon Cinnamon", productDoc.getString("name"))
        assertEquals("SPICES", productDoc.getString("category"))

        val saleDoc = firestore.collection("users").document(uid).collection("sales").document("77").get().await()
        assertTrue("Sale document must exist in Firestore", saleDoc.exists())
        assertEquals("ORD-CLOUD-077", saleDoc.getString("orderReference"))
        assertEquals(700.00, saleDoc.getDouble("totalNet") ?: 0.0, 0.001)
        assertEquals(300.00, saleDoc.getDouble("totalGrossProfit") ?: 0.0, 0.001)

        val noteDoc = firestore.collection("users").document(uid).collection("order_notes").document("15").get().await()
        assertTrue("Order note document must exist in Firestore", noteDoc.exists())
        assertEquals("Kandy Luxury Tea & Spice", noteDoc.getString("customerName"))

        inMemoryDb.close()
    }

    @Test
    fun testPdfAndCsvReportExport_verifiesTotalsAndContents() {
        val sale = SaleEntity(
            id = 88,
            saleDate = "2026-10-26",
            orderReference = "ORD-EXPORT-088",
            customerName = "Colombo Spice House",
            totalGross = 5000.00,
            totalNet = 4750.00,
            totalCost = 3000.00,
            totalGrossProfit = 1750.00
        )
        val item1 = SaleLineItemEntity(
            id = 1,
            saleId = 88,
            productId = 1,
            variantId = 1,
            productName = "Ceyvana Premium Chilli Powder",
            variantName = "100g",
            category = ProductCategory.SPICES,
            unitCostPrice = 300.00,
            unitSellingPrice = 500.00,
            quantity = 6,
            discount = 150.00,
            grossAmount = 3000.00,
            netRevenue = 2850.00,
            costOfGoodsSold = 1800.00,
            grossProfit = 1050.00
        )
        val item2 = SaleLineItemEntity(
            id = 2,
            saleId = 88,
            productId = 2,
            variantId = 2,
            productName = "Ceylomira Traditional Black Tea",
            variantName = "500g",
            category = ProductCategory.TEA,
            unitCostPrice = 600.00,
            unitSellingPrice = 1000.00,
            quantity = 2,
            discount = 100.00,
            grossAmount = 2000.00,
            netRevenue = 1900.00,
            costOfGoodsSold = 1200.00,
            grossProfit = 700.00
        )
        val saleWithItems = com.example.data.model.SaleWithItems(sale, listOf(item1, item2))

        val reportData = ReportCalculator.computeReport(
            preset = ReportPeriodPreset.SINGLE_DATE,
            startDateIso = "2026-10-26",
            endDateIso = "2026-10-26",
            salesWithItems = listOf(saleWithItems)
        )

        // 1. VERIFY PDF REPORT GENERATION
        val pdfFile = PdfReportGenerator.generatePdfReport(context, reportData)
        assertTrue("PDF file must exist", pdfFile.exists())
        assertTrue("PDF file size must be > 0", pdfFile.length() > 0)
        assertTrue("File name must indicate Ceyvana report", pdfFile.name.contains("Ceyvana_Spices_Report_20261026"))

        // Verify PDF Header Magic Bytes (%PDF-)
        val pdfBytes = pdfFile.readBytes()
        val pdfHeader = String(pdfBytes.take(8).toByteArray())
        assertTrue("PDF must start with valid PDF header: $pdfHeader", pdfHeader.startsWith("%PDF-"))

        // Check text content inside generated PDF stream
        val pdfString = String(pdfBytes)
        assertTrue("PDF must contain Ceyvana Spices brand name", pdfString.contains("CEYVANA SPICES"))
        assertTrue("PDF must contain period date", pdfString.contains("26 Oct 2026"))
        assertTrue("PDF must contain LKR currency", pdfString.contains("LKR"))
        assertTrue("PDF must contain Net Revenue total", pdfString.contains("4,750.00"))
        assertTrue("PDF must contain Gross Profit total", pdfString.contains("1,750.00"))
        assertTrue("PDF must contain COGS total", pdfString.contains("3,000.00"))

        // 2. VERIFY CSV REPORT GENERATION
        val csvFile = CsvReportGenerator.generateCsvReport(context, reportData)
        assertTrue("CSV file must exist", csvFile.exists())
        assertTrue("CSV file size must be > 0", csvFile.length() > 0)
        val csvContent = csvFile.readText()

        assertTrue("CSV must contain business header", csvContent.contains("CEYVANA SPICES BUSINESS REPORT"))
        assertTrue("CSV must contain Currency", csvContent.contains("Currency,LKR"))
        assertTrue("CSV must contain financial totals", csvContent.contains("Total Net Revenue,4750.00"))
        assertTrue("CSV must contain COGS", csvContent.contains("Total Cost of Goods Sold (COGS),3000.00"))
        assertTrue("CSV must contain Gross Profit", csvContent.contains("Total Gross Profit,1750.00"))
        assertTrue("CSV must contain Chilli line item", csvContent.contains("Ceyvana Premium Chilli Powder"))
        assertTrue("CSV must contain Black Tea line item", csvContent.contains("Ceylomira Traditional Black Tea"))
        assertTrue("CSV must contain order reference", csvContent.contains("ORD-EXPORT-088"))
    }
}
