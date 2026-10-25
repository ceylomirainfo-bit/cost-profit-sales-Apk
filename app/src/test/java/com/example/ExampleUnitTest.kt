package com.example

import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.LineItemDraft
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSaleLineCalculations() {
        // Line item: 3 units of 100g Chilli Powder @ Selling 360, Cost 230, Discount 80
        val draft = LineItemDraft(
            unitCostPrice = 230.0,
            unitSellingPrice = 360.0,
            quantity = 3,
            discount = 80.0
        )

        val expectedGross = 360.0 * 3 // 1080.0
        val expectedNet = 1080.0 - 80.0 // 1000.0
        val expectedCOGS = 230.0 * 3 // 690.0
        val expectedProfit = expectedNet - expectedCOGS // 310.0

        assertEquals(expectedGross, draft.grossAmount, 0.001)
        assertEquals(expectedNet, draft.netRevenue, 0.001)
        assertEquals(expectedCOGS, draft.costOfGoodsSold, 0.001)
        assertEquals(expectedProfit, draft.grossProfit, 0.001)
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = CurrencyFormatter.formatLkr(1250.5)
        assertEquals("LKR 1,250.50", formatted)

        val formattedZero = CurrencyFormatter.formatLkr(0.0)
        assertEquals("LKR 0.00", formattedZero)
    }

    @Test
    fun testDateFormatting() {
        val display = DateUtils.formatIsoToDisplay("2026-10-24")
        assertEquals("24 Oct 2026", display)
    }

    @Test
    fun testPinValidationRules() {
        // Valid 4-digit pin
        val validPin = "1234"
        val isNumericAnd4Digits = validPin.length == 4 && validPin.all { it.isDigit() }
        assertEquals(true, isNumericAnd4Digits)

        // Invalid lengths or characters
        val tooShort = "123"
        val tooLong = "12345"
        val nonNumeric = "12a4"
        assertEquals(false, tooShort.length == 4 && tooShort.all { it.isDigit() })
        assertEquals(false, tooLong.length == 4 && tooLong.all { it.isDigit() })
        assertEquals(false, nonNumeric.length == 4 && nonNumeric.all { it.isDigit() })
    }

    @Test
    fun testDateUtilsColomboTimezoneAndRanges() {
        assertEquals("Asia/Colombo", DateUtils.COLOMBO_ZONE.id)

        // Range validation
        assertEquals(true, DateUtils.isRangeValid("2026-10-01", "2026-10-31"))
        assertEquals(true, DateUtils.isRangeValid("2026-10-24", "2026-10-24"))
        assertEquals(false, DateUtils.isRangeValid("2026-10-31", "2026-10-01"))

        // Period formatting
        val formattedRange = DateUtils.formatPeriodRangeDisplay("2026-10-01", "2026-10-31")
        assertEquals("01 Oct 2026 – 31 Oct 2026", formattedRange)

        val formattedSingle = DateUtils.formatPeriodRangeDisplay("2026-10-24", "2026-10-24")
        assertEquals("24 Oct 2026", formattedSingle)
    }

    @Test
    fun testReportCalculatorAccurateFinancialMetrics() {
        // Prepare dummy sales with items
        val sale1 = com.example.data.model.SaleEntity(
            id = 1,
            saleDate = "2026-10-24",
            orderReference = "ORD-001",
            customerName = "Nimal Perera",
            totalGross = 1000.0,
            totalNet = 950.0,
            totalCost = 600.0,
            totalGrossProfit = 350.0
        )
        val line1 = com.example.data.model.SaleLineItemEntity(
            id = 1,
            saleId = 1,
            productId = 1,
            variantId = 1,
            productName = "Ceyvana Premium Chilli Powder",
            variantName = "100g",
            category = "SPICES",
            unitCostPrice = 200.0,
            unitSellingPrice = 300.0,
            quantity = 2,
            discount = 0.0,
            grossAmount = 600.0,
            netRevenue = 600.0,
            costOfGoodsSold = 400.0,
            grossProfit = 200.0
        )
        val line2 = com.example.data.model.SaleLineItemEntity(
            id = 2,
            saleId = 1,
            productId = 2,
            variantId = 2,
            productName = "Ceyvana 'Hithe Rasaya' BOPF Tea",
            variantName = "250g",
            category = "TEA",
            unitCostPrice = 200.0,
            unitSellingPrice = 400.0,
            quantity = 1,
            discount = 50.0,
            grossAmount = 400.0,
            netRevenue = 350.0,
            costOfGoodsSold = 200.0,
            grossProfit = 150.0
        )

        val saleWithItems = com.example.data.model.SaleWithItems(
            sale = sale1,
            items = listOf(line1, line2)
        )

        val report = com.example.data.model.ReportCalculator.computeReport(
            preset = com.example.data.model.ReportPeriodPreset.CUSTOM,
            startDateIso = "2026-10-01",
            endDateIso = "2026-10-31",
            salesWithItems = listOf(saleWithItems)
        )

        assertEquals(950.0, report.summary.totalRevenue, 0.001)
        assertEquals(600.0, report.summary.totalCOGS, 0.001)
        assertEquals(350.0, report.summary.totalGrossProfit, 0.001)
        assertEquals(3, report.summary.totalUnitsSold)
        assertEquals(1, report.summary.salesCount)
        assertEquals(950.0, report.summary.averageRevenuePerSale, 0.001)

        // Check Category Breakdown
        val spicesCat = report.categoryBreakdown.first { it.category == "Spices" }
        assertEquals(600.0, spicesCat.revenue, 0.001)
        assertEquals(400.0, spicesCat.costOfGoodsSold, 0.001)
        assertEquals(200.0, spicesCat.grossProfit, 0.001)
        assertEquals(2, spicesCat.unitsSold)

        val teaCat = report.categoryBreakdown.first { it.category == "Tea" }
        assertEquals(350.0, teaCat.revenue, 0.001)
        assertEquals(200.0, teaCat.costOfGoodsSold, 0.001)
        assertEquals(150.0, teaCat.grossProfit, 0.001)
        assertEquals(1, teaCat.unitsSold)
    }
}

