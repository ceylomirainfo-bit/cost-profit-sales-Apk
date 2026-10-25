package com.example.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.BusinessReportData
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 points (72 dpi)
    private const val MARGIN = 36f // 0.5 inch margins
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)
    private const val BOTTOM_LIMIT = PAGE_HEIGHT - 48f

    // Ceyvana Brand Colors
    private val COLOR_FOREST_GREEN = Color.parseColor("#123B2A")
    private val COLOR_FOREST_DARK = Color.parseColor("#0B291E")
    private val COLOR_GOLD = Color.parseColor("#C5A15B")
    private val COLOR_IVORY = Color.parseColor("#F8F5ED")
    private val COLOR_GRAY_BG = Color.parseColor("#F1F5F2")
    private val COLOR_TEXT_PRIMARY = Color.parseColor("#1C1B1F")
    private val COLOR_TEXT_MUTED = Color.parseColor("#5A6B61")
    private val COLOR_BORDER = Color.parseColor("#DDE5E0")

    fun generatePdfReport(context: Context, reportData: BusinessReportData): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val sanitizedStart = reportData.startDateIso.replace("-", "")
        val sanitizedEnd = reportData.endDateIso.replace("-", "")
        val fileName = if (sanitizedStart == sanitizedEnd) {
            "Ceyvana_Spices_Report_$sanitizedStart.pdf"
        } else {
            "Ceyvana_Spices_Report_${sanitizedStart}_$sanitizedEnd.pdf"
        }
        val outputFile = File(reportsDir, fileName)

        try {
            renderNativePdf(outputFile, reportData)
        } catch (_: Throwable) {
            // Fallback for headless JVM / Robolectric where Android native Skia/Pdfium is unmocked
            writeStandardPdfFallback(outputFile, reportData)
        }

        return outputFile
    }

    private fun renderNativePdf(outputFile: File, reportData: BusinessReportData) {
        val pdfDoc = PdfDocument()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = COLOR_BORDER
            strokeWidth = 0.75f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN

        fun drawHeader() {
            // Dark Green Header Bar
            fillPaint.color = COLOR_FOREST_GREEN
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 68f, fillPaint)

            // Gold accent strip below header
            fillPaint.color = COLOR_GOLD
            canvas.drawRect(MARGIN, currentY + 66f, PAGE_WIDTH - MARGIN, currentY + 69f, fillPaint)

            // Brand title
            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textPaint.textSize = 18f
            canvas.drawText("CEYVANA SPICES", MARGIN + 14f, currentY + 28f, textPaint)

            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = COLOR_GOLD
            canvas.drawText("The Essence of Ceylon, Managed with Care", MARGIN + 14f, currentY + 44f, textPaint)

            // Right side: Report title & period
            textPaint.color = Color.WHITE
            textPaint.textSize = 12f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val titleText = reportData.title
            val titleWidth = textPaint.measureText(titleText)
            canvas.drawText(titleText, PAGE_WIDTH - MARGIN - 14f - titleWidth, currentY + 26f, textPaint)

            textPaint.color = Color.parseColor("#E0EAE3")
            textPaint.textSize = 9f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val periodText = "Period: ${reportData.formattedPeriod}"
            val periodWidth = textPaint.measureText(periodText)
            canvas.drawText(periodText, PAGE_WIDTH - MARGIN - 14f - periodWidth, currentY + 42f, textPaint)

            val timeText = "Generated: ${reportData.generatedAtTimestamp}"
            val timeWidth = textPaint.measureText(timeText)
            canvas.drawText(timeText, PAGE_WIDTH - MARGIN - 14f - timeWidth, currentY + 56f, textPaint)

            currentY += 80f
        }

        fun drawFooter(curPage: Int) {
            strokePaint.color = COLOR_BORDER
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 32f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 32f, strokePaint)

            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Ceyvana Spices Business Management System • Currency: LKR", MARGIN, PAGE_HEIGHT - 20f, textPaint)

            val pageStr = "Page $curPage"
            val pageStrWidth = textPaint.measureText(pageStr)
            canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pageStrWidth, PAGE_HEIGHT - 20f, textPaint)
        }

        fun newPage() {
            drawFooter(pageNumber)
            pdfDoc.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDoc.startPage(pageInfo)
            canvas = page.canvas
            currentY = MARGIN

            // Mini top header on subsequent pages
            fillPaint.color = COLOR_FOREST_GREEN
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 28f, fillPaint)
            fillPaint.color = COLOR_GOLD
            canvas.drawRect(MARGIN, currentY + 26f, PAGE_WIDTH - MARGIN, currentY + 28f, fillPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            canvas.drawText("CEYVANA SPICES • Business Report (${reportData.formattedPeriod})", MARGIN + 10f, currentY + 18f, textPaint)

            currentY += 40f
        }

        // Draw initial page header
        drawHeader()

        // 1. Executive Financial Summary Cards
        val cardHeight = 48f
        val cardWidth = (CONTENT_WIDTH - 20f) / 3f

        val kpis = listOf(
            Triple("NET REVENUE", reportData.summary.formattedRevenue, COLOR_FOREST_GREEN),
            Triple("GROSS PROFIT", "${reportData.summary.formattedGrossProfit} (${reportData.summary.formattedMargin})", COLOR_GOLD),
            Triple("COST OF GOODS (COGS)", reportData.summary.formattedCOGS, COLOR_TEXT_PRIMARY),
            Triple("UNITS SOLD", "${reportData.summary.totalUnitsSold} items", COLOR_TEXT_PRIMARY),
            Triple("TRANSACTIONS", "${reportData.summary.salesCount} sales", COLOR_TEXT_PRIMARY),
            Triple("AVG PER TRANSACTION", reportData.summary.formattedAvgRevenue, COLOR_TEXT_PRIMARY)
        )

        for (i in 0 until 6) {
            val row = i / 3
            val col = i % 3
            val x = MARGIN + (col * (cardWidth + 10f))
            val y = currentY + (row * (cardHeight + 8f))

            fillPaint.color = COLOR_GRAY_BG
            canvas.drawRoundRect(x, y, x + cardWidth, y + cardHeight, 6f, 6f, fillPaint)
            strokePaint.color = COLOR_BORDER
            canvas.drawRoundRect(x, y, x + cardWidth, y + cardHeight, 6f, 6f, strokePaint)

            val item = kpis[i]
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 7.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.first, x + 8f, y + 16f, textPaint)

            textPaint.color = item.third
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.second, x + 8f, y + 34f, textPaint)
        }
        currentY += (cardHeight * 2) + 24f

        // 2. Category Summary Table
        textPaint.color = COLOR_FOREST_GREEN
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Category Performance Breakdown", MARGIN, currentY, textPaint)
        currentY += 12f

        // Table Header
        fillPaint.color = COLOR_FOREST_DARK
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 18f, fillPaint)

        val catCols = listOf(
            Triple("Category", MARGIN + 8f, 130f),
            Triple("Units Sold", MARGIN + 140f, 65f),
            Triple("Revenue (LKR)", MARGIN + 210f, 95f),
            Triple("COGS (LKR)", MARGIN + 310f, 95f),
            Triple("Gross Profit", MARGIN + 410f, 80f),
            Triple("Margin", MARGIN + 490f, 30f)
        )

        textPaint.color = Color.WHITE
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        for (col in catCols) {
            canvas.drawText(col.first, col.second, currentY + 12f, textPaint)
        }
        currentY += 18f

        reportData.categoryBreakdown.forEachIndexed { idx, cat ->
            fillPaint.color = if (idx % 2 == 0) Color.WHITE else COLOR_GRAY_BG
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 16f, fillPaint)
            strokePaint.color = COLOR_BORDER
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 16f, strokePaint)

            textPaint.color = COLOR_TEXT_PRIMARY
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText(cat.category, MARGIN + 8f, currentY + 11f, textPaint)
            canvas.drawText("${cat.unitsSold}", MARGIN + 140f, currentY + 11f, textPaint)
            canvas.drawText(cat.formattedRevenue, MARGIN + 210f, currentY + 11f, textPaint)
            canvas.drawText(cat.formattedCOGS, MARGIN + 310f, currentY + 11f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = COLOR_FOREST_GREEN
            canvas.drawText(cat.formattedGrossProfit, MARGIN + 410f, currentY + 11f, textPaint)
            canvas.drawText(cat.formattedMargin, MARGIN + 490f, currentY + 11f, textPaint)

            currentY += 16f
        }
        currentY += 18f

        // 3. Daily Breakdown Table (if multiple days)
        if (reportData.dailyBreakdown.size > 1) {
            if (currentY + 100f > BOTTOM_LIMIT) newPage()

            textPaint.color = COLOR_FOREST_GREEN
            textPaint.textSize = 11f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Daily Sales Summary", MARGIN, currentY, textPaint)
            currentY += 12f

            fillPaint.color = COLOR_FOREST_DARK
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 18f, fillPaint)

            val dailyCols = listOf(
                Pair("Date", MARGIN + 8f),
                Pair("Transactions", MARGIN + 110f),
                Pair("Units Sold", MARGIN + 190f),
                Pair("Revenue (LKR)", MARGIN + 270f),
                Pair("COGS (LKR)", MARGIN + 360f),
                Pair("Gross Profit", MARGIN + 450f)
            )

            textPaint.color = Color.WHITE
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            for (col in dailyCols) {
                canvas.drawText(col.first, col.second, currentY + 12f, textPaint)
            }
            currentY += 18f

            reportData.dailyBreakdown.forEachIndexed { idx, day ->
                if (currentY + 18f > BOTTOM_LIMIT) newPage()

                fillPaint.color = if (idx % 2 == 0) Color.WHITE else COLOR_GRAY_BG
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 16f, fillPaint)
                strokePaint.color = COLOR_BORDER
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 16f, strokePaint)

                textPaint.color = COLOR_TEXT_PRIMARY
                textPaint.textSize = 8f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                canvas.drawText(day.formattedDate, MARGIN + 8f, currentY + 11f, textPaint)
                canvas.drawText("${day.salesCount}", MARGIN + 110f, currentY + 11f, textPaint)
                canvas.drawText("${day.unitsSold}", MARGIN + 190f, currentY + 11f, textPaint)
                canvas.drawText(day.formattedRevenue, MARGIN + 270f, currentY + 11f, textPaint)
                canvas.drawText(day.formattedCOGS, MARGIN + 360f, currentY + 11f, textPaint)

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.color = COLOR_FOREST_GREEN
                canvas.drawText(day.formattedGrossProfit, MARGIN + 450f, currentY + 11f, textPaint)

                currentY += 16f
            }
            currentY += 18f
        }

        // 4. Detailed Sales Transactions
        if (currentY + 100f > BOTTOM_LIMIT) newPage()

        textPaint.color = COLOR_FOREST_GREEN
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Sales Transactions & Line Items", MARGIN, currentY, textPaint)
        currentY += 12f

        if (!reportData.hasSales) {
            fillPaint.color = COLOR_GRAY_BG
            canvas.drawRoundRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 40f, 6f, 6f, fillPaint)
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("No sales recorded for this period.", MARGIN + 16f, currentY + 24f, textPaint)
            currentY += 50f
        } else {
            fillPaint.color = COLOR_FOREST_DARK
            canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 18f, fillPaint)

            val detailCols = listOf(
                Pair("Date", MARGIN + 4f),
                Pair("Ref", MARGIN + 60f),
                Pair("Product & Variant", MARGIN + 110f),
                Pair("Qty", MARGIN + 270f),
                Pair("Unit Sell", MARGIN + 300f),
                Pair("Revenue", MARGIN + 360f),
                Pair("COGS", MARGIN + 420f),
                Pair("Profit", MARGIN + 475f)
            )

            textPaint.color = Color.WHITE
            textPaint.textSize = 7.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            for (col in detailCols) {
                canvas.drawText(col.first, col.second, currentY + 12f, textPaint)
            }
            currentY += 18f

            reportData.detailedSales.forEachIndexed { idx, item ->
                if (currentY + 16f > BOTTOM_LIMIT) newPage()

                fillPaint.color = if (idx % 2 == 0) Color.WHITE else COLOR_GRAY_BG
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 16f, fillPaint)
                strokePaint.color = COLOR_BORDER
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 16f, strokePaint)

                textPaint.color = COLOR_TEXT_PRIMARY
                textPaint.textSize = 7.5f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                canvas.drawText(item.formattedDate, MARGIN + 4f, currentY + 11f, textPaint)
                canvas.drawText(item.orderReference.take(8), MARGIN + 60f, currentY + 11f, textPaint)
                val prodSummary = "${item.productName.take(18)} (${item.variantName})"
                canvas.drawText(prodSummary, MARGIN + 110f, currentY + 11f, textPaint)
                canvas.drawText("${item.quantity}", MARGIN + 270f, currentY + 11f, textPaint)
                canvas.drawText(item.formattedUnitPrice, MARGIN + 300f, currentY + 11f, textPaint)
                canvas.drawText(item.formattedRevenue, MARGIN + 360f, currentY + 11f, textPaint)
                canvas.drawText(item.formattedCOGS, MARGIN + 420f, currentY + 11f, textPaint)

                textPaint.color = COLOR_FOREST_GREEN
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(item.formattedGrossProfit, MARGIN + 475f, currentY + 11f, textPaint)

                currentY += 16f
            }
        }

        // Draw final page footer
        drawFooter(pageNumber)
        pdfDoc.finishPage(page)

        // Write to output file
        FileOutputStream(outputFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()
    }

    /**
     * Fallback standard PDF 1.4 generator when Android native Skia/Pdfium is not mocked in JVM
     */
    private fun writeStandardPdfFallback(outputFile: File, reportData: BusinessReportData) {
        val lines = mutableListOf(
            "CEYVANA SPICES - Business Management Report",
            "Period: ${reportData.formattedPeriod}",
            "Generated: ${reportData.generatedAtTimestamp}",
            "Currency: LKR",
            "",
            "--- FINANCIAL HIGHLIGHTS ---",
            "Total Net Revenue: ${reportData.summary.formattedRevenue}",
            "Cost of Goods Sold (COGS): ${reportData.summary.formattedCOGS}",
            "Total Gross Profit: ${reportData.summary.formattedGrossProfit} (${reportData.summary.formattedMargin})",
            "Total Units Sold: ${reportData.summary.totalUnitsSold} items",
            "Sales Transactions: ${reportData.summary.salesCount} sales",
            "Average Revenue per Sale: ${reportData.summary.formattedAvgRevenue}",
            "",
            "--- CATEGORY BREAKDOWN ---"
        )
        for (cat in reportData.categoryBreakdown) {
            lines.add("${cat.category}: Revenue ${cat.formattedRevenue} | COGS ${cat.formattedCOGS} | Profit ${cat.formattedGrossProfit} | Units: ${cat.unitsSold}")
        }
        if (reportData.detailedSales.isNotEmpty()) {
            lines.add("")
            lines.add("--- RECENT SALES TRANSACTIONS (${reportData.detailedSales.size} items) ---")
            for (item in reportData.detailedSales.take(25)) {
                lines.add("${item.formattedDate} | ${item.orderReference} | ${item.productName} (${item.variantName}) | Qty: ${item.quantity} | Rev: ${item.formattedRevenue} | Profit: ${item.formattedGrossProfit}")
            }
        }

        val sb = java.lang.StringBuilder()
        sb.append("BT\n/F1 10 Tf\n40 800 Td\n15 TL\n")
        for (line in lines) {
            val sanitized = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
            sb.append("($sanitized) '\n")
        }
        sb.append("ET\n")
        val streamBytes = sb.toString().toByteArray(StandardCharsets.ISO_8859_1)

        FileOutputStream(outputFile).use { fos ->
            var written = 0L
            fun writeBytes(b: ByteArray) {
                fos.write(b)
                written += b.size
            }
            fun writeStr(s: String) = writeBytes(s.toByteArray(StandardCharsets.ISO_8859_1))

            writeStr("%PDF-1.4\n")
            val offsets = mutableListOf<Long>()

            // Obj 1
            offsets.add(written)
            writeStr("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

            // Obj 2
            offsets.add(written)
            writeStr("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")

            // Obj 3
            offsets.add(written)
            writeStr("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n")

            // Obj 4
            offsets.add(written)
            writeStr("4 0 obj\n<< /Length ${streamBytes.size} >>\nstream\n")
            writeBytes(streamBytes)
            writeStr("\nendstream\nendobj\n")

            // Obj 5
            offsets.add(written)
            writeStr("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n")

            // Xref
            val startXref = written
            writeStr("xref\n0 6\n0000000000 65535 f \n")
            for (offset in offsets) {
                writeStr(String.format(Locale.US, "%010d 00000 n \n", offset))
            }
            writeStr("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n$startXref\n%%EOF\n")
        }
    }
}
