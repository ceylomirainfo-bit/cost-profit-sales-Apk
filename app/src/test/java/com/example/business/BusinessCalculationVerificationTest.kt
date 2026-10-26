package com.example.business

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ProductWithVariants
import com.example.data.model.ReportCalculator
import com.example.data.model.ReportPeriodPreset
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.example.data.model.SaleWithItems
import com.example.data.repository.CeyvanaRepository
import com.example.ui.viewmodel.LineItemDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BusinessCalculationVerificationTest {

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
    fun testManualVersusSystemCalculations_multiItemSaleWithDiscounts() = runTest {
        // =========================================================================
        // STEP 1: CREATE PRODUCTS AND VARIANTS
        // =========================================================================
        // Product 1: Custom Spice product
        val spiceProduct = ProductEntity(
            name = "Ceyvana Royal Cardamom Pods",
            category = ProductCategory.SPICES,
            description = "High grade green cardamom from Nuwara Eliya",
            isArchived = false
        )
        val spiceVariants = listOf(
            ProductVariantEntity(
                productId = 0,
                variantName = "50g",
                costPrice = 450.00,
                sellingPrice = 750.00,
                currency = "LKR",
                isActive = true
            ),
            ProductVariantEntity(
                productId = 0,
                variantName = "100g",
                costPrice = 850.00,
                sellingPrice = 1400.00,
                currency = "LKR",
                isActive = true
            )
        )
        val spiceProdId = repository.addProductWithVariants(spiceProduct, spiceVariants)
        assertTrue("Spice product should be inserted", spiceProdId > 0)

        // Product 2: Tea product
        val teaProduct = ProductEntity(
            name = "Ceyvana 'Hithe Rasaya' BOPF Tea",
            category = ProductCategory.TEA,
            description = "Finest Ceylon BOPF",
            isArchived = false
        )
        val teaVariants = listOf(
            ProductVariantEntity(
                productId = 0,
                variantName = "250g",
                costPrice = 600.00,
                sellingPrice = 1100.00,
                currency = "LKR",
                isActive = true
            )
        )
        val teaProdId = repository.addProductWithVariants(teaProduct, teaVariants)
        assertTrue("Tea product should be inserted", teaProdId > 0)

        // Retrieve created products with variants from database
        val activeProducts = repository.activeProducts.first { it.isNotEmpty() }
        val retrievedSpice = activeProducts.first { it.product.id == spiceProdId }
        val retrievedTea = activeProducts.first { it.product.id == teaProdId }

        val variantSpice50g = retrievedSpice.variants.first { it.variantName == "50g" }
        val variantSpice100g = retrievedSpice.variants.first { it.variantName == "100g" }
        val variantTea250g = retrievedTea.variants.first { it.variantName == "250g" }

        // =========================================================================
        // STEP 2: DEFINE MULTI-ITEM SALE DRAFTS WITH DISCOUNTS
        // =========================================================================
        // Item 1: 3x Royal Cardamom 50g (Unit Cost: 450.00, Unit Sell: 750.00, Discount: 150.00)
        val draft1 = LineItemDraft(
            product = retrievedSpice,
            variant = variantSpice50g,
            unitCostPrice = variantSpice50g.costPrice,
            unitSellingPrice = variantSpice50g.sellingPrice,
            quantity = 3,
            discount = 150.00
        )

        // Item 2: 2x Royal Cardamom 100g (Unit Cost: 850.00, Unit Sell: 1400.00, Discount: 200.00)
        val draft2 = LineItemDraft(
            product = retrievedSpice,
            variant = variantSpice100g,
            unitCostPrice = variantSpice100g.costPrice,
            unitSellingPrice = variantSpice100g.sellingPrice,
            quantity = 2,
            discount = 200.00
        )

        // Item 3: 4x Hithe Rasaya BOPF Tea 250g (Unit Cost: 600.00, Unit Sell: 1100.00, Discount: 400.00)
        val draft3 = LineItemDraft(
            product = retrievedTea,
            variant = variantTea250g,
            unitCostPrice = variantTea250g.costPrice,
            unitSellingPrice = variantTea250g.sellingPrice,
            quantity = 4,
            discount = 400.00
        )

        // =========================================================================
        // STEP 3: MANUAL CALCULATIONS VS LINE ITEM DRAFT
        // =========================================================================
        // Manual Item 1:
        // Gross = 750.00 * 3 = 2,250.00 LKR
        // Discount = 150.00 LKR
        // Net Revenue = 2,250.00 - 150.00 = 2,100.00 LKR
        // COGS = 450.00 * 3 = 1,350.00 LKR
        // Gross Profit = 2,100.00 - 1,350.00 = 750.00 LKR
        assertEquals(2250.00, draft1.grossAmount, 0.001)
        assertEquals(2100.00, draft1.netRevenue, 0.001)
        assertEquals(1350.00, draft1.costOfGoodsSold, 0.001)
        assertEquals(750.00, draft1.grossProfit, 0.001)

        // Manual Item 2:
        // Gross = 1,400.00 * 2 = 2,800.00 LKR
        // Discount = 200.00 LKR
        // Net Revenue = 2,800.00 - 200.00 = 2,600.00 LKR
        // COGS = 850.00 * 2 = 1,700.00 LKR
        // Gross Profit = 2,600.00 - 1,700.00 = 900.00 LKR
        assertEquals(2800.00, draft2.grossAmount, 0.001)
        assertEquals(2600.00, draft2.netRevenue, 0.001)
        assertEquals(1700.00, draft2.costOfGoodsSold, 0.001)
        assertEquals(900.00, draft2.grossProfit, 0.001)

        // Manual Item 3:
        // Gross = 1,100.00 * 4 = 4,400.00 LKR
        // Discount = 400.00 LKR
        // Net Revenue = 4,400.00 - 400.00 = 4,000.00 LKR
        // COGS = 600.00 * 4 = 2,400.00 LKR
        // Gross Profit = 4,000.00 - 2,400.00 = 1,600.00 LKR
        assertEquals(4400.00, draft3.grossAmount, 0.001)
        assertEquals(4000.00, draft3.netRevenue, 0.001)
        assertEquals(2400.00, draft3.costOfGoodsSold, 0.001)
        assertEquals(1600.00, draft3.grossProfit, 0.001)

        // =========================================================================
        // STEP 4: AGGREGATE ORDER TOTALS (MANUAL VS APPLICATION)
        // =========================================================================
        val drafts = listOf(draft1, draft2, draft3)

        // Manual Total Expected:
        // Expected Total Gross = 2,250 + 2,800 + 4,400 = 9,450.00 LKR
        // Expected Total Discount = 150 + 200 + 400 = 750.00 LKR
        // Expected Net Revenue = 9,450 - 750 = 8,700.00 LKR
        // Expected Total COGS = 1,350 + 1,700 + 2,400 = 5,450.00 LKR
        // Expected Gross Profit = 8,700 - 5,450 = 3,250.00 LKR (also 750 + 900 + 1,600 = 3,250.00 LKR)
        // Expected Units Sold = 3 + 2 + 4 = 9 units
        val expectedGross = 9450.00
        val expectedDiscount = 750.00
        val expectedNet = 8700.00
        val expectedCost = 5450.00
        val expectedProfit = 3250.00
        val expectedUnits = 9

        val calculatedGross = drafts.sumOf { it.grossAmount }
        val calculatedDiscount = drafts.sumOf { it.discount }
        val calculatedNet = drafts.sumOf { it.netRevenue }
        val calculatedCost = drafts.sumOf { it.costOfGoodsSold }
        val calculatedProfit = drafts.sumOf { it.grossProfit }
        val calculatedUnits = drafts.sumOf { it.quantity }

        assertEquals(expectedGross, calculatedGross, 0.001)
        assertEquals(expectedDiscount, calculatedDiscount, 0.001)
        assertEquals(expectedNet, calculatedNet, 0.001)
        assertEquals(expectedCost, calculatedCost, 0.001)
        assertEquals(expectedProfit, calculatedProfit, 0.001)
        assertEquals(expectedUnits, calculatedUnits)

        // Verify Gross Profit Formula Equivalence:
        // Formula 1: Sum of item profits = 750 + 900 + 1,600 = 3,250.00
        // Formula 2: Total Net Revenue - Total COGS = 8,700 - 5,450 = 3,250.00
        assertEquals(calculatedNet - calculatedCost, calculatedProfit, 0.001)

        // =========================================================================
        // STEP 5: RECORD SALE IN DATABASE AND VERIFY PERSISTENCE ACCURACY
        // =========================================================================
        val testDate = "2026-10-26"
        val saleEntity = SaleEntity(
            saleDate = testDate,
            orderReference = "ORD-VERIFY-001",
            customerName = "Heritage Spice Emporium",
            notes = "Wholesale sample pack with loyalty discount",
            orderDiscount = calculatedDiscount,
            totalGross = calculatedGross,
            totalNet = calculatedNet,
            totalCost = calculatedCost,
            totalGrossProfit = calculatedProfit
        )

        val lineItemEntities = drafts.map { draft ->
            SaleLineItemEntity(
                saleId = 0,
                productId = draft.product!!.product.id,
                variantId = draft.variant!!.id,
                productName = draft.product.product.name,
                variantName = draft.variant.variantName,
                category = draft.product.product.category,
                unitCostPrice = draft.unitCostPrice,
                unitSellingPrice = draft.unitSellingPrice,
                quantity = draft.quantity,
                discount = draft.discount,
                grossAmount = draft.grossAmount,
                netRevenue = draft.netRevenue,
                costOfGoodsSold = draft.costOfGoodsSold,
                grossProfit = draft.grossProfit
            )
        }

        val saleId = repository.recordSale(saleEntity, lineItemEntities)
        assertTrue("Sale should be successfully recorded", saleId > 0)

        // Read back from database
        val savedSales = repository.getSalesForDate(testDate).first { it.isNotEmpty() }
        assertEquals(1, savedSales.size)
        val savedSaleWithItems = savedSales.first()

        assertEquals("ORD-VERIFY-001", savedSaleWithItems.sale.orderReference)
        assertEquals(3, savedSaleWithItems.items.size)
        assertEquals(expectedGross, savedSaleWithItems.sale.totalGross, 0.001)
        assertEquals(expectedNet, savedSaleWithItems.sale.totalNet, 0.001)
        assertEquals(expectedCost, savedSaleWithItems.sale.totalCost, 0.001)
        assertEquals(expectedProfit, savedSaleWithItems.sale.totalGrossProfit, 0.001)

        // =========================================================================
        // STEP 6: VERIFY REPORT CALCULATOR AND CATEGORY BREAKDOWN
        // =========================================================================
        val report = ReportCalculator.computeReport(
            preset = ReportPeriodPreset.SINGLE_DATE,
            startDateIso = testDate,
            endDateIso = testDate,
            salesWithItems = savedSales
        )

        assertNotNull(report)
        val fin = report.summary
        assertEquals("Total Revenue in Report must match manual calculation", 8700.00, fin.totalRevenue, 0.001)
        assertEquals("Total COGS in Report must match manual calculation", 5450.00, fin.totalCOGS, 0.001)
        assertEquals("Total Gross Profit in Report must match manual calculation", 3250.00, fin.totalGrossProfit, 0.001)
        assertEquals(9, fin.totalUnitsSold)
        assertEquals(1, fin.salesCount)
        assertEquals(8700.00, fin.averageRevenuePerSale, 0.001)

        // Margin Percentage: (3,250.00 / 8,700.00) * 100 = 37.356...%
        val expectedMarginPct = (3250.00 / 8700.00) * 100.0
        assertEquals(expectedMarginPct, fin.profitMarginPct, 0.01)

        // Category breakdown:
        // Spices:
        // - Units: 3 + 2 = 5
        // - Revenue: 2,100 + 2,600 = 4,700.00
        // - COGS: 1,350 + 1,700 = 3,050.00
        // - Gross Profit: 4,700 - 3,050 = 1,650.00
        val spiceCategory = report.categoryBreakdown.first { it.category.equals("Spices", ignoreCase = true) }
        assertEquals(5, spiceCategory.unitsSold)
        assertEquals(4700.00, spiceCategory.revenue, 0.001)
        assertEquals(3050.00, spiceCategory.costOfGoodsSold, 0.001)
        assertEquals(1650.00, spiceCategory.grossProfit, 0.001)

        // Tea:
        // - Units: 4
        // - Revenue: 4,000.00
        // - COGS: 2,400.00
        // - Gross Profit: 1,600.00
        val teaCategory = report.categoryBreakdown.first { it.category.equals("Tea", ignoreCase = true) }
        assertEquals(4, teaCategory.unitsSold)
        assertEquals(4000.00, teaCategory.revenue, 0.001)
        assertEquals(2400.00, teaCategory.costOfGoodsSold, 0.001)
        assertEquals(1600.00, teaCategory.grossProfit, 0.001)

        // Grocery Items: (0 sales)
        val groceryCategory = report.categoryBreakdown.first { it.category.equals("Grocery Items", ignoreCase = true) }
        assertEquals(0, groceryCategory.unitsSold)
        assertEquals(0.0, groceryCategory.revenue, 0.001)
        assertEquals(0.0, groceryCategory.costOfGoodsSold, 0.001)
        assertEquals(0.0, groceryCategory.grossProfit, 0.001)
    }

    @Test
    fun testDiscountEdgeCases_noDoubleSubtractionAndFullDiscount() {
        // Line with 100% discount (promotional item)
        val promoItem = LineItemDraft(
            unitCostPrice = 200.00,
            unitSellingPrice = 500.00,
            quantity = 2,
            discount = 1000.00 // 100% discount
        )
        // Gross: 1000.00, Discount: 1000.00 -> Net: 0.00, COGS: 400.00 -> Gross Profit: -400.00
        assertEquals(1000.00, promoItem.grossAmount, 0.001)
        assertEquals(0.00, promoItem.netRevenue, 0.001)
        assertEquals(400.00, promoItem.costOfGoodsSold, 0.001)
        assertEquals(-400.00, promoItem.grossProfit, 0.001)

        // Ensure orderDiscount stores the discount amount for reporting,
        // but totalNet is computed strictly as sum of line net revenues so discounts are NEVER double-subtracted:
        val normalItem = LineItemDraft(
            unitCostPrice = 300.00,
            unitSellingPrice = 600.00,
            quantity = 1,
            discount = 50.00
        )
        // normalItem: Net: 550.00, COGS: 300.00, Profit: 250.00
        val items = listOf(promoItem, normalItem)
        val totalNet = items.sumOf { it.netRevenue } // 0.00 + 550.00 = 550.00
        val totalDiscount = items.sumOf { it.discount } // 1000.00 + 50.00 = 1050.00
        val totalGross = items.sumOf { it.grossAmount } // 1000.00 + 600.00 = 1600.00

        assertEquals(550.00, totalNet, 0.001)
        assertEquals(totalGross - totalDiscount, totalNet, 0.001)
    }
}
