package com.example.data.model

import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DateUtils

enum class ReportPeriodPreset(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    PREVIOUS_MONTH("Previous Month"),
    CUSTOM("Custom Range"),
    SINGLE_DATE("Single Date")
}

data class FinancialSummary(
    val totalRevenue: Double = 0.0,
    val totalCOGS: Double = 0.0,
    val totalGrossProfit: Double = 0.0,
    val profitMarginPct: Double = 0.0,
    val totalUnitsSold: Int = 0,
    val salesCount: Int = 0,
    val averageRevenuePerSale: Double = 0.0
) {
    val formattedRevenue: String get() = CurrencyFormatter.formatLkr(totalRevenue)
    val formattedCOGS: String get() = CurrencyFormatter.formatLkr(totalCOGS)
    val formattedGrossProfit: String get() = CurrencyFormatter.formatLkr(totalGrossProfit)
    val formattedMargin: String get() = "%.1f%%".format(profitMarginPct)
    val formattedAvgRevenue: String get() = CurrencyFormatter.formatLkr(averageRevenuePerSale)
}

data class CategorySummaryItem(
    val category: String,
    val revenue: Double,
    val costOfGoodsSold: Double,
    val grossProfit: Double,
    val profitMarginPct: Double,
    val unitsSold: Int
) {
    val formattedRevenue: String get() = CurrencyFormatter.formatLkr(revenue)
    val formattedCOGS: String get() = CurrencyFormatter.formatLkr(costOfGoodsSold)
    val formattedGrossProfit: String get() = CurrencyFormatter.formatLkr(grossProfit)
    val formattedMargin: String get() = "%.1f%%".format(profitMarginPct)
}

data class DailySummaryItem(
    val dateIso: String,
    val formattedDate: String,
    val salesCount: Int,
    val revenue: Double,
    val costOfGoodsSold: Double,
    val grossProfit: Double,
    val unitsSold: Int
) {
    val formattedRevenue: String get() = CurrencyFormatter.formatLkr(revenue)
    val formattedCOGS: String get() = CurrencyFormatter.formatLkr(costOfGoodsSold)
    val formattedGrossProfit: String get() = CurrencyFormatter.formatLkr(grossProfit)
}

data class ReportSaleDetailItem(
    val saleId: Long,
    val saleDate: String,
    val formattedDate: String,
    val orderReference: String,
    val customerName: String,
    val productName: String,
    val category: String,
    val variantName: String,
    val quantity: Int,
    val unitCostPrice: Double,
    val unitSellingPrice: Double,
    val netRevenue: Double,
    val costOfGoodsSold: Double,
    val grossProfit: Double
) {
    val formattedUnitPrice: String get() = CurrencyFormatter.formatLkr(unitSellingPrice)
    val formattedUnitCost: String get() = CurrencyFormatter.formatLkr(unitCostPrice)
    val formattedRevenue: String get() = CurrencyFormatter.formatLkr(netRevenue)
    val formattedCOGS: String get() = CurrencyFormatter.formatLkr(costOfGoodsSold)
    val formattedGrossProfit: String get() = CurrencyFormatter.formatLkr(grossProfit)
}

data class BusinessReportData(
    val businessName: String = "CEYVANA SPICES",
    val title: String = "Business Management Report",
    val preset: ReportPeriodPreset,
    val startDateIso: String,
    val endDateIso: String,
    val formattedPeriod: String,
    val generatedAtTimestamp: String,
    val currency: String = "LKR",
    val summary: FinancialSummary,
    val categoryBreakdown: List<CategorySummaryItem>,
    val dailyBreakdown: List<DailySummaryItem>,
    val detailedSales: List<ReportSaleDetailItem>,
    val associatedOrderNotes: List<OrderNoteEntity> = emptyList()
) {
    val hasSales: Boolean get() = detailedSales.isNotEmpty()
}

object ReportCalculator {

    fun computeReport(
        preset: ReportPeriodPreset,
        startDateIso: String,
        endDateIso: String,
        salesWithItems: List<SaleWithItems>,
        orderNotes: List<OrderNoteEntity> = emptyList()
    ): BusinessReportData {
        // Filter out non-matching dates and cancelled records
        val matchingSales = salesWithItems.filter { saleWithItems ->
            val sale = saleWithItems.sale
            val dateMatches = sale.saleDate >= startDateIso && sale.saleDate <= endDateIso
            // Safe exclusion of cancelled transactions if flagged
            val isNotCancelled = !sale.notes.contains("CANCELLED", ignoreCase = true) &&
                    !sale.orderReference.contains("CANCELLED", ignoreCase = true)
            dateMatches && isNotCancelled
        }

        val matchingNotes = orderNotes.filter { note ->
            note.orderDate >= startDateIso && note.orderDate <= endDateIso
        }

        val detailedList = mutableListOf<ReportSaleDetailItem>()
        var totalRev = 0.0
        var totalCOGS = 0.0
        var totalProfit = 0.0
        var totalUnits = 0

        // Map line items into flattened detail items
        for (saleWithItems in matchingSales) {
            val sale = saleWithItems.sale
            for (item in saleWithItems.items) {
                totalRev += item.netRevenue
                totalCOGS += item.costOfGoodsSold
                totalProfit += item.grossProfit
                totalUnits += item.quantity

                detailedList.add(
                    ReportSaleDetailItem(
                        saleId = sale.id,
                        saleDate = sale.saleDate,
                        formattedDate = DateUtils.formatIsoToDisplay(sale.saleDate),
                        orderReference = if (sale.orderReference.isNotBlank()) sale.orderReference else "ORD-${sale.id}",
                        customerName = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer",
                        productName = item.productName,
                        category = item.category,
                        variantName = item.variantName,
                        quantity = item.quantity,
                        unitCostPrice = item.unitCostPrice,
                        unitSellingPrice = item.unitSellingPrice,
                        netRevenue = item.netRevenue,
                        costOfGoodsSold = item.costOfGoodsSold,
                        grossProfit = item.grossProfit
                    )
                )
            }
        }

        val salesCount = matchingSales.size
        val marginPct = if (totalRev > 0) (totalProfit / totalRev) * 100.0 else 0.0
        val avgRev = if (salesCount > 0) totalRev / salesCount else 0.0

        val financialSummary = FinancialSummary(
            totalRevenue = totalRev,
            totalCOGS = totalCOGS,
            totalGrossProfit = totalProfit,
            profitMarginPct = marginPct,
            totalUnitsSold = totalUnits,
            salesCount = salesCount,
            averageRevenuePerSale = avgRev
        )

        // Category breakdown: group by Spices, Tea, Grocery Items
        val standardCategories = listOf(
            ProductCategory.SPICES,
            ProductCategory.TEA,
            ProductCategory.GROCERY_ITEMS
        )

        val categoryGroups = detailedList.groupBy { it.category.uppercase().trim() }
        val categoryBreakdown = standardCategories.map { stdCat ->
            val itemsForCat = categoryGroups[stdCat] ?: emptyList()
            val catRev = itemsForCat.sumOf { it.netRevenue }
            val catCost = itemsForCat.sumOf { it.costOfGoodsSold }
            val catProfit = itemsForCat.sumOf { it.grossProfit }
            val catUnits = itemsForCat.sumOf { it.quantity }
            val catMargin = if (catRev > 0) (catProfit / catRev) * 100.0 else 0.0

            val displayLabel = when (stdCat) {
                ProductCategory.SPICES -> "Spices"
                ProductCategory.TEA -> "Tea"
                ProductCategory.GROCERY_ITEMS -> "Grocery Items"
                else -> stdCat
            }

            CategorySummaryItem(
                category = displayLabel,
                revenue = catRev,
                costOfGoodsSold = catCost,
                grossProfit = catProfit,
                profitMarginPct = catMargin,
                unitsSold = catUnits
            )
        }

        // Daily breakdown (grouped by saleDate, sorted ascending)
        val dailyMap = detailedList.groupBy { it.saleDate }.toSortedMap()
        val dailyBreakdown = dailyMap.map { (date, items) ->
            val dateRev = items.sumOf { it.netRevenue }
            val dateCost = items.sumOf { it.costOfGoodsSold }
            val dateProfit = items.sumOf { it.grossProfit }
            val dateUnits = items.sumOf { it.quantity }
            val distinctSalesOnDate = items.map { it.saleId }.distinct().size

            DailySummaryItem(
                dateIso = date,
                formattedDate = DateUtils.formatIsoToDisplay(date),
                salesCount = distinctSalesOnDate,
                revenue = dateRev,
                costOfGoodsSold = dateCost,
                grossProfit = dateProfit,
                unitsSold = dateUnits
            )
        }

        return BusinessReportData(
            businessName = "CEYVANA SPICES",
            title = "Business Management Report",
            preset = preset,
            startDateIso = startDateIso,
            endDateIso = endDateIso,
            formattedPeriod = DateUtils.formatPeriodRangeDisplay(startDateIso, endDateIso),
            generatedAtTimestamp = DateUtils.getCurrentTimestampDisplay(),
            currency = "LKR",
            summary = financialSummary,
            categoryBreakdown = categoryBreakdown,
            dailyBreakdown = dailyBreakdown,
            detailedSales = detailedList.sortedWith(compareBy({ it.saleDate }, { it.saleId })),
            associatedOrderNotes = matchingNotes
        )
    }
}
