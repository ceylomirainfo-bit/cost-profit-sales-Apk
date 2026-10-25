package com.example.ui.util

import android.content.Context
import com.example.data.model.BusinessReportData
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvReportGenerator {

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    fun generateCsvReport(context: Context, reportData: BusinessReportData): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val sanitizedStart = reportData.startDateIso.replace("-", "")
        val sanitizedEnd = reportData.endDateIso.replace("-", "")
        val fileName = if (sanitizedStart == sanitizedEnd) {
            "Ceyvana_Spices_Report_$sanitizedStart.csv"
        } else {
            "Ceyvana_Spices_Report_${sanitizedStart}_$sanitizedEnd.csv"
        }
        val outputFile = File(reportsDir, fileName)

        OutputStreamWriter(FileOutputStream(outputFile), StandardCharsets.UTF_8).use { writer ->
            // UTF-8 BOM for Microsoft Excel auto-detection
            writer.write("\uFEFF")

            // 1. Report Metadata
            writer.write("CEYVANA SPICES BUSINESS REPORT\n")
            writer.write("Report Title,${escapeCsv(reportData.title)}\n")
            writer.write("Reporting Period,${escapeCsv(reportData.formattedPeriod)}\n")
            writer.write("Start Date (ISO),${escapeCsv(reportData.startDateIso)}\n")
            writer.write("End Date (ISO),${escapeCsv(reportData.endDateIso)}\n")
            writer.write("Generated Timestamp,${escapeCsv(reportData.generatedAtTimestamp)}\n")
            writer.write("Currency,LKR\n\n")

            // 2. Financial Summary
            writer.write("--- FINANCIAL SUMMARY ---\n")
            writer.write("Metric,Value (LKR / Count)\n")
            writer.write("Total Net Revenue,${"%.2f".format(reportData.summary.totalRevenue)}\n")
            writer.write("Total Cost of Goods Sold (COGS),${"%.2f".format(reportData.summary.totalCOGS)}\n")
            writer.write("Total Gross Profit,${"%.2f".format(reportData.summary.totalGrossProfit)}\n")
            writer.write("Profit Margin (%),${"%.2f".format(reportData.summary.profitMarginPct)}%\n")
            writer.write("Total Units Sold,${reportData.summary.totalUnitsSold}\n")
            writer.write("Total Sales Transactions,${reportData.summary.salesCount}\n")
            writer.write("Average Revenue Per Sale,${"%.2f".format(reportData.summary.averageRevenuePerSale)}\n\n")

            // 3. Category Breakdown
            writer.write("--- CATEGORY BREAKDOWN ---\n")
            writer.write("Category,Units Sold,Revenue (LKR),COGS (LKR),Gross Profit (LKR),Profit Margin (%)\n")
            for (cat in reportData.categoryBreakdown) {
                writer.write("${escapeCsv(cat.category)},${cat.unitsSold},${"%.2f".format(cat.revenue)},${"%.2f".format(cat.costOfGoodsSold)},${"%.2f".format(cat.grossProfit)},${"%.2f".format(cat.profitMarginPct)}%\n")
            }
            writer.write("\n")

            // 4. Daily Breakdown
            if (reportData.dailyBreakdown.isNotEmpty()) {
                writer.write("--- DAILY PERFORMANCE BREAKDOWN ---\n")
                writer.write("Date,Formatted Date,Transactions,Units Sold,Revenue (LKR),COGS (LKR),Gross Profit (LKR)\n")
                for (day in reportData.dailyBreakdown) {
                    writer.write("${escapeCsv(day.dateIso)},${escapeCsv(day.formattedDate)},${day.salesCount},${day.unitsSold},${"%.2f".format(day.revenue)},${"%.2f".format(day.costOfGoodsSold)},${"%.2f".format(day.grossProfit)}\n")
                }
                writer.write("\n")
            }

            // 5. Detailed Sales Transactions
            writer.write("--- DETAILED SALES TRANSACTIONS & LINE ITEMS ---\n")
            writer.write("Sale Date,Order Reference,Customer Name,Product Name,Category,Variant/Size,Quantity,Unit Selling Price (LKR),Unit Cost Price (LKR),Net Revenue (LKR),Cost of Goods Sold (LKR),Gross Profit (LKR)\n")
            for (item in reportData.detailedSales) {
                writer.write(
                    listOf(
                        escapeCsv(item.saleDate),
                        escapeCsv(item.orderReference),
                        escapeCsv(item.customerName),
                        escapeCsv(item.productName),
                        escapeCsv(item.category),
                        escapeCsv(item.variantName),
                        item.quantity.toString(),
                        "%.2f".format(item.unitSellingPrice),
                        "%.2f".format(item.unitCostPrice),
                        "%.2f".format(item.netRevenue),
                        "%.2f".format(item.costOfGoodsSold),
                        "%.2f".format(item.grossProfit)
                    ).joinToString(",") + "\n"
                )
            }
        }

        return outputFile
    }
}
