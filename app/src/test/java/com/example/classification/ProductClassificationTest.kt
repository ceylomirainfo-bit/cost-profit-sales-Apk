package com.example.classification

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ProductType
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ProductWithVariants
import com.example.data.model.ReportCalculator
import com.example.data.model.ReportPeriodPreset
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.example.data.model.SaleWithItems
import com.example.data.repository.CeyvanaRepository
import com.example.ui.util.CsvReportGenerator
import com.example.ui.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductClassificationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: CeyvanaRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CeyvanaRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCreatePremiumProduct() = runTest {
        val premiumProduct = ProductEntity(
            name = "Ceyvana Authentic Ceylon Cinnamon",
            category = ProductCategory.SPICES,
            productType = ProductType.PREMIUM,
            description = "True Alba grade Ceylon cinnamon quills hand-peeled in Galle",
            isArchived = false
        )
        val variants = listOf(
            ProductVariantEntity(
                productId = 0,
                variantName = "100g",
                costPrice = 850.00,
                sellingPrice = 1450.00,
                currency = "LKR",
                isActive = true
            )
        )

        val id = repository.addProductWithVariants(premiumProduct, variants)
        assertTrue("Generated ID should be positive", id > 0)

        val retrieved = repository.getProductWithVariants(id)
        assertNotNull(retrieved)
        assertEquals("Ceyvana Authentic Ceylon Cinnamon", retrieved!!.product.name)
        assertEquals(ProductType.PREMIUM, retrieved.product.productType)
        assertTrue(ProductType.isPremium(retrieved.product.productType))
        assertEquals(1, retrieved.variants.size)
        assertEquals(1450.00, retrieved.variants[0].sellingPrice, 0.001)
    }

    @Test
    fun testCreateNormalProduct() = runTest {
        val normalProduct = ProductEntity(
            name = "Standard White Pepper Powder",
            category = ProductCategory.SPICES,
            productType = ProductType.NORMAL,
            description = "Finely ground white pepper for everyday culinary seasoning",
            isArchived = false
        )
        val variants = listOf(
            ProductVariantEntity(
                productId = 0,
                variantName = "50g",
                costPrice = 200.00,
                sellingPrice = 350.00,
                currency = "LKR",
                isActive = true
            )
        )

        val id = repository.addProductWithVariants(normalProduct, variants)
        assertTrue("Generated ID should be positive", id > 0)

        val retrieved = repository.getProductWithVariants(id)
        assertNotNull(retrieved)
        assertEquals("Standard White Pepper Powder", retrieved!!.product.name)
        assertEquals(ProductType.NORMAL, retrieved.product.productType)
        assertFalse(ProductType.isPremium(retrieved.product.productType))
    }

    @Test
    fun testEditAndReclassifyProduct() = runTest {
        // Start as Normal
        val product = ProductEntity(
            name = "Ceylon Cardamom Pods",
            category = ProductCategory.SPICES,
            productType = ProductType.NORMAL,
            description = "Green cardamom pods",
            isArchived = false
        )
        val variants = listOf(
            ProductVariantEntity(
                productId = 0,
                variantName = "50g",
                costPrice = 600.00,
                sellingPrice = 950.00,
                currency = "LKR",
                isActive = true
            )
        )
        val id = repository.addProductWithVariants(product, variants)

        val initial = repository.getProductWithVariants(id)
        assertNotNull(initial)
        assertEquals(ProductType.NORMAL, initial!!.product.productType)

        // Reclassify to Premium
        val reclassified = initial.product.copy(
            name = "Ceyvana Royal Reserve Cardamom Pods",
            productType = ProductType.PREMIUM
        )
        repository.updateProductAndVariants(reclassified, initial.variants)

        val updated = repository.getProductWithVariants(id)
        assertNotNull(updated)
        assertEquals("Ceyvana Royal Reserve Cardamom Pods", updated!!.product.name)
        assertEquals(ProductType.PREMIUM, updated.product.productType)
        assertTrue(ProductType.isPremium(updated.product.productType))
    }

    @Test
    fun testFilteringAndSearchingByGroupAndCategory() = runTest {
        val p1 = ProductEntity(name = "Premium Chilli Powder", category = ProductCategory.SPICES, productType = ProductType.PREMIUM)
        val p2 = ProductEntity(name = "Normal Chilli Powder", category = ProductCategory.SPICES, productType = ProductType.NORMAL)
        val p3 = ProductEntity(name = "Premium Silver Tips Tea", category = ProductCategory.TEA, productType = ProductType.PREMIUM)
        val p4 = ProductEntity(name = "Normal Broken Orange Pekoe Tea", category = ProductCategory.TEA, productType = ProductType.NORMAL)

        val v = listOf(ProductVariantEntity(productId = 0, variantName = "100g", costPrice = 100.0, sellingPrice = 200.0))

        repository.addProductWithVariants(p1, v)
        repository.addProductWithVariants(p2, v)
        repository.addProductWithVariants(p3, v)
        repository.addProductWithVariants(p4, v)

        val all = repository.activeProducts.first()
        assertEquals(4, all.size)

        // 1. Filter by Premium only
        val premiumOnly = all.filter { it.product.productType.equals(ProductType.PREMIUM, ignoreCase = true) }
        assertEquals(2, premiumOnly.size)
        assertTrue(premiumOnly.all { it.product.productType == ProductType.PREMIUM })

        // 2. Filter by Normal only
        val normalOnly = all.filter { it.product.productType.equals(ProductType.NORMAL, ignoreCase = true) }
        assertEquals(2, normalOnly.size)
        assertTrue(normalOnly.all { it.product.productType == ProductType.NORMAL })

        // 3. Combined Filter: Premium + Spices
        val premiumSpices = all.filter {
            it.product.productType.equals(ProductType.PREMIUM, ignoreCase = true) &&
                    it.product.category.equals(ProductCategory.SPICES, ignoreCase = true)
        }
        assertEquals(1, premiumSpices.size)
        assertEquals("Premium Chilli Powder", premiumSpices[0].product.name)

        // 4. Combined Filter: Premium + Tea + Search "Silver"
        val premiumTeaSearch = all.filter {
            it.product.productType.equals(ProductType.PREMIUM, ignoreCase = true) &&
                    it.product.category.equals(ProductCategory.TEA, ignoreCase = true) &&
                    it.product.name.contains("Silver", ignoreCase = true)
        }
        assertEquals(1, premiumTeaSearch.size)
        assertEquals("Premium Silver Tips Tea", premiumTeaSearch[0].product.name)
    }

    @Test
    fun testGroupBasedFinancialCalculations() = runTest {
        // Create 1 Premium product and 1 Normal product
        val premProd = ProductEntity(name = "Ceyvana Premium Saffron", category = ProductCategory.SPICES, productType = ProductType.PREMIUM)
        val normProd = ProductEntity(name = "Standard Mustard Seeds", category = ProductCategory.SPICES, productType = ProductType.NORMAL)

        val v1 = listOf(ProductVariantEntity(productId = 0, variantName = "10g", costPrice = 1000.0, sellingPrice = 2500.0))
        val v2 = listOf(ProductVariantEntity(productId = 0, variantName = "100g", costPrice = 80.0, sellingPrice = 150.0))

        val premId = repository.addProductWithVariants(premProd, v1)
        val normId = repository.addProductWithVariants(normProd, v2)

        // Sale with 2 Premium units and 3 Normal units
        val lineItems = listOf(
            SaleLineItemEntity(
                saleId = 0,
                productId = premId,
                variantId = 1,
                productName = "Ceyvana Premium Saffron",
                variantName = "10g",
                category = ProductCategory.SPICES,
                productType = ProductType.PREMIUM,
                unitCostPrice = 1000.0,
                unitSellingPrice = 2500.0,
                quantity = 2,
                discount = 200.0, // 5000 - 200 = 4800 net revenue
                grossAmount = 5000.0,
                netRevenue = 4800.0,
                costOfGoodsSold = 2000.0, // 2 * 1000 = 2000
                grossProfit = 2800.0 // 4800 - 2000 = 2800
            ),
            SaleLineItemEntity(
                saleId = 0,
                productId = normId,
                variantId = 2,
                productName = "Standard Mustard Seeds",
                variantName = "100g",
                category = ProductCategory.SPICES,
                productType = ProductType.NORMAL,
                unitCostPrice = 80.0,
                unitSellingPrice = 150.0,
                quantity = 3,
                discount = 0.0,
                grossAmount = 450.0,
                netRevenue = 450.0,
                costOfGoodsSold = 240.0, // 3 * 80 = 240
                grossProfit = 210.0 // 450 - 240 = 210
            )
        )

        val sale = SaleEntity(
            saleDate = "2026-10-26",
            orderReference = "ORD-TEST-001",
            customerName = "Mrs. Perera",
            notes = "Test order with premium and normal items",
            orderDiscount = 200.0,
            totalGross = 5450.0,
            totalNet = 5250.0,
            totalCost = 2240.0,
            totalGrossProfit = 3010.0
        )

        val saleId = repository.recordSaleWithItems(sale, lineItems)
        assertTrue(saleId > 0)

        val sales = repository.allSales.first()
        assertEquals(1, sales.size)

        // Compute report
        val report = ReportCalculator.computeReport(
            preset = ReportPeriodPreset.TODAY,
            startDateIso = "2026-10-26",
            endDateIso = "2026-10-26",
            salesWithItems = sales
        )

        assertEquals(5250.0, report.summary.totalRevenue, 0.001)
        assertEquals(2240.0, report.summary.totalCOGS, 0.001)
        assertEquals(3010.0, report.summary.totalGrossProfit, 0.001)

        val premBreakdown = report.productTypeBreakdown.find { it.productType == ProductType.PREMIUM }
        val normBreakdown = report.productTypeBreakdown.find { it.productType == ProductType.NORMAL }

        assertNotNull(premBreakdown)
        assertNotNull(normBreakdown)

        // Verify Premium numbers
        assertEquals(4800.0, premBreakdown!!.revenue, 0.001)
        assertEquals(2000.0, premBreakdown.costOfGoodsSold, 0.001)
        assertEquals(2800.0, premBreakdown.grossProfit, 0.001)
        assertEquals(2, premBreakdown.unitsSold)

        // Verify Normal numbers
        assertEquals(450.0, normBreakdown!!.revenue, 0.001)
        assertEquals(240.0, normBreakdown.costOfGoodsSold, 0.001)
        assertEquals(210.0, normBreakdown.grossProfit, 0.001)
        assertEquals(3, normBreakdown.unitsSold)
    }

    @Test
    fun testHistoricalSalesRemainUnchangedAfterReclassification() = runTest {
        // Step 1: Create a Premium Product
        val prod = ProductEntity(
            name = "Speciality Ceylon Vanilla Pods",
            category = ProductCategory.SPICES,
            productType = ProductType.PREMIUM
        )
        val v = listOf(ProductVariantEntity(productId = 0, variantName = "20g", costPrice = 500.0, sellingPrice = 1200.0))
        val prodId = repository.addProductWithVariants(prod, v)

        // Step 2: Record a sale at the time it was Premium
        val saleItem = SaleLineItemEntity(
            saleId = 0,
            productId = prodId,
            variantId = 1,
            productName = prod.name,
            variantName = "20g",
            category = prod.category,
            productType = prod.productType, // Snapshotted as PREMIUM
            unitCostPrice = 500.0,
            unitSellingPrice = 1200.0,
            quantity = 2,
            discount = 0.0,
            grossAmount = 2400.0,
            netRevenue = 2400.0,
            costOfGoodsSold = 1000.0,
            grossProfit = 1400.0
        )
        val sale = SaleEntity(
            saleDate = "2026-10-25",
            orderReference = "HIST-001",
            customerName = "Mr. Silva",
            totalGross = 2400.0,
            totalNet = 2400.0,
            totalCost = 1000.0,
            totalGrossProfit = 1400.0
        )
        repository.recordSaleWithItems(sale, listOf(saleItem))

        // Step 3: Owner reclassifies product to NORMAL weeks later
        val updatedProd = prod.copy(id = prodId, productType = ProductType.NORMAL)
        repository.updateProductAndVariants(updatedProd, v)

        // Verify product itself is now NORMAL
        val currentProd = repository.getProductWithVariants(prodId)
        assertEquals(ProductType.NORMAL, currentProd!!.product.productType)

        // Step 4: Verify historical sale line item still retains PREMIUM snapshot
        val historicalSales = repository.allSales.first()
        val historicalItem = historicalSales[0].items[0]
        assertEquals("Historical sale line item must keep snapshotted productType", ProductType.PREMIUM, historicalItem.productType)

        // Step 5: Verify historical financial report maintains exact numbers
        val report = ReportCalculator.computeReport(
            preset = ReportPeriodPreset.CUSTOM,
            startDateIso = "2026-10-25",
            endDateIso = "2026-10-25",
            salesWithItems = historicalSales
        )
        val premSummary = report.productTypeBreakdown.find { it.productType == ProductType.PREMIUM }
        assertNotNull(premSummary)
        assertEquals(2400.0, premSummary!!.revenue, 0.001)
        assertEquals(1000.0, premSummary.costOfGoodsSold, 0.001)
        assertEquals(1400.0, premSummary.grossProfit, 0.001)
    }

    @Test
    fun testRoomMigration1To2PreservesExistingRecords() = runTest {
        val dbFile = File(context.filesDir, "test_migration_v1_to_v2.db")
        if (dbFile.exists()) dbFile.delete()

        // 1. Create a version 1 database using raw SQLite
        val openHelper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.name)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS products (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                name TEXT NOT NULL,
                                category TEXT NOT NULL,
                                description TEXT NOT NULL,
                                imageUri TEXT NOT NULL,
                                isArchived INTEGER NOT NULL
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS product_variants (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                productId INTEGER NOT NULL,
                                variantName TEXT NOT NULL,
                                costPrice REAL NOT NULL,
                                sellingPrice REAL NOT NULL,
                                currency TEXT NOT NULL,
                                isActive INTEGER NOT NULL
                            )
                        """.trimIndent())

                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS sale_line_items (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                saleId INTEGER NOT NULL,
                                productId INTEGER NOT NULL,
                                variantId INTEGER NOT NULL,
                                productName TEXT NOT NULL,
                                variantName TEXT NOT NULL,
                                category TEXT NOT NULL,
                                unitCostPrice REAL NOT NULL,
                                unitSellingPrice REAL NOT NULL,
                                quantity INTEGER NOT NULL,
                                discount REAL NOT NULL,
                                grossAmount REAL NOT NULL,
                                netRevenue REAL NOT NULL,
                                costOfGoodsSold REAL NOT NULL,
                                grossProfit REAL NOT NULL
                            )
                        """.trimIndent())

                        // Insert v1 sample records
                        db.execSQL("INSERT INTO products (id, name, category, description, imageUri, isArchived) VALUES (1, 'Legacy Cinnamon', 'Spices', 'Old Stock', '', 0)")
                        db.execSQL("INSERT INTO product_variants (id, productId, variantName, costPrice, sellingPrice, currency, isActive) VALUES (1, 1, '100g', 300.0, 500.0, 'LKR', 1)")
                        db.execSQL("INSERT INTO sale_line_items (id, saleId, productId, variantId, productName, variantName, category, unitCostPrice, unitSellingPrice, quantity, discount, grossAmount, netRevenue, costOfGoodsSold, grossProfit) VALUES (1, 1, 1, 1, 'Legacy Cinnamon', '100g', 'Spices', 300.0, 500.0, 1, 0.0, 500.0, 500.0, 300.0, 200.0)")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val rawDb = openHelper.writableDatabase
        rawDb.close()

        // 2. Open via Room AppDatabase version 2 applying MIGRATION_1_2
        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.name)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val productDao = migratedDb.productDao()
        val retrieved = productDao.getProductWithVariants(1)
        assertNotNull("Legacy product must be preserved after migration", retrieved)
        assertEquals("Legacy Cinnamon", retrieved!!.product.name)
        assertEquals("Existing product must safely default to NORMAL without crashes", ProductType.NORMAL, retrieved.product.productType)
        assertEquals(1, retrieved.variants.size)

        migratedDb.close()
        dbFile.delete()
    }

    @Test
    fun testPdfAndCsvExportsIncludeProductClassification() = runTest {
        val premProd = ProductEntity(name = "Ceyvana Royal Cardamom", category = ProductCategory.SPICES, productType = ProductType.PREMIUM)
        val normProd = ProductEntity(name = "Ceyvana Table Salt", category = ProductCategory.GROCERY_ITEMS, productType = ProductType.NORMAL)

        val v = listOf(ProductVariantEntity(productId = 0, variantName = "100g", costPrice = 100.0, sellingPrice = 250.0))
        val p1Id = repository.addProductWithVariants(premProd, v)
        val p2Id = repository.addProductWithVariants(normProd, v)

        val lineItems = listOf(
            SaleLineItemEntity(
                saleId = 0,
                productId = p1Id,
                variantId = 1,
                productName = "Ceyvana Royal Cardamom",
                variantName = "100g",
                category = ProductCategory.SPICES,
                productType = ProductType.PREMIUM,
                unitCostPrice = 100.0,
                unitSellingPrice = 250.0,
                quantity = 5,
                discount = 50.0,
                grossAmount = 1250.0,
                netRevenue = 1200.0,
                costOfGoodsSold = 500.0,
                grossProfit = 700.0
            ),
            SaleLineItemEntity(
                saleId = 0,
                productId = p2Id,
                variantId = 2,
                productName = "Ceyvana Table Salt",
                variantName = "100g",
                category = ProductCategory.GROCERY_ITEMS,
                productType = ProductType.NORMAL,
                unitCostPrice = 20.0,
                unitSellingPrice = 60.0,
                quantity = 10,
                discount = 0.0,
                grossAmount = 600.0,
                netRevenue = 600.0,
                costOfGoodsSold = 200.0,
                grossProfit = 400.0
            )
        )

        val sale = SaleEntity(
            saleDate = "2026-10-26",
            orderReference = "ORD-REP-100",
            customerName = "Restaurant Cinnamon Grand",
            totalGross = 1850.0,
            totalNet = 1800.0,
            totalCost = 700.0,
            totalGrossProfit = 1100.0
        )

        repository.recordSaleWithItems(sale, lineItems)
        val sales = repository.allSales.first()

        val report = ReportCalculator.computeReport(
            preset = ReportPeriodPreset.TODAY,
            startDateIso = "2026-10-26",
            endDateIso = "2026-10-26",
            salesWithItems = sales
        )

        // 1. Verify CSV contains product classification section and productType column
        val csvFile = CsvReportGenerator.generateCsvReport(context, report)
        assertTrue(csvFile.exists())
        val csvContent = csvFile.readText()
        assertTrue(csvContent.contains("--- PRODUCT CLASSIFICATION BREAKDOWN ---"))
        assertTrue(csvContent.contains("Premium Products"))
        assertTrue(csvContent.contains("Normal Products"))
        assertTrue(csvContent.contains("Product Type"))
        assertTrue(csvContent.contains("PREMIUM"))
        assertTrue(csvContent.contains("NORMAL"))

        // 2. Verify PDF file generates successfully and contains data
        val pdfFile = PdfReportGenerator.generatePdfReport(context, report)
        assertTrue(pdfFile.exists())
        assertTrue("PDF file size should be positive", pdfFile.length() > 0)
    }
}
