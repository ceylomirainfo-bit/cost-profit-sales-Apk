package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.BusinessReportData
import java.io.File
import java.net.URLEncoder

object ReportSharingUtils {

    fun buildTextSummary(reportData: BusinessReportData, isWhatsAppFormat: Boolean = false): String {
        val bold = if (isWhatsAppFormat) "*" else ""
        val bullet = "•"

        val sb = StringBuilder()
        sb.append("${bold}CEYVANA SPICES - Business Report${bold}\n")
        sb.append("Period: ${reportData.formattedPeriod}\n")
        sb.append("Generated: ${reportData.generatedAtTimestamp}\n")
        sb.append("Currency: LKR\n\n")

        sb.append("${bold}FINANCIAL HIGHLIGHTS:${bold}\n")
        sb.append("$bullet Net Revenue: ${reportData.summary.formattedRevenue}\n")
        sb.append("$bullet Cost of Goods Sold (COGS): ${reportData.summary.formattedCOGS}\n")
        sb.append("$bullet Gross Profit: ${reportData.summary.formattedGrossProfit} (${reportData.summary.formattedMargin})\n")
        sb.append("$bullet Total Units Sold: ${reportData.summary.totalUnitsSold} items\n")
        sb.append("$bullet Sales Transactions: ${reportData.summary.salesCount} sales\n")
        sb.append("$bullet Average Ticket: ${reportData.summary.formattedAvgRevenue}\n\n")

        if (reportData.categoryBreakdown.isNotEmpty()) {
            sb.append("${bold}CATEGORY BREAKDOWN:${bold}\n")
            for (cat in reportData.categoryBreakdown) {
                sb.append("$bullet ${cat.category}: ${cat.formattedRevenue} | Profit: ${cat.formattedGrossProfit} (${cat.formattedMargin}) | Units: ${cat.unitsSold}\n")
            }
            sb.append("\n")
        }

        if (reportData.dailyBreakdown.size > 1) {
            sb.append("${bold}DAILY ACTIVITY:${bold}\n")
            for (day in reportData.dailyBreakdown.take(7)) {
                sb.append("$bullet ${day.formattedDate}: ${day.formattedRevenue} (${day.salesCount} sales)\n")
            }
            if (reportData.dailyBreakdown.size > 7) {
                sb.append("... and ${reportData.dailyBreakdown.size - 7} more days in report attachment.\n")
            }
            sb.append("\n")
        }

        sb.append("Managed with care by Ceyvana Spices Business Management System.")
        return sb.toString()
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareViaEmail(context: Context, reportData: BusinessReportData) {
        val pdfFile = PdfReportGenerator.generatePdfReport(context, reportData)
        val pdfUri = getFileUri(context, pdfFile)
        val subject = "Ceyvana Spices Business Report - ${reportData.formattedPeriod}"
        val bodyText = buildTextSummary(reportData, isWhatsAppFormat = false)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Send Business Report via Email")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareViaWhatsApp(context: Context, reportData: BusinessReportData) {
        val pdfFile = PdfReportGenerator.generatePdfReport(context, reportData)
        val pdfUri = getFileUri(context, pdfFile)
        val message = buildTextSummary(reportData, isWhatsAppFormat = true)

        try {
            // Attempt to send directly via WhatsApp with attachment
            val whatsAppIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(whatsAppIntent)
        } catch (_: Exception) {
            // Fallback: Open WhatsApp Web API link or general chooser
            try {
                val encodedText = URLEncoder.encode(message, "UTF-8")
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
                // Final fallback: general share chooser
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Share Report via WhatsApp"))
            }
        }
    }

    fun sharePdfFile(context: Context, reportData: BusinessReportData) {
        val pdfFile = PdfReportGenerator.generatePdfReport(context, reportData)
        val pdfUri = getFileUri(context, pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Ceyvana Spices Report (${reportData.formattedPeriod})")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Export / Share PDF Report"))
    }

    fun shareCsvFile(context: Context, reportData: BusinessReportData) {
        val csvFile = CsvReportGenerator.generateCsvReport(context, reportData)
        val csvUri = getFileUri(context, csvFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Ceyvana Spices CSV Report (${reportData.formattedPeriod})")
            putExtra(Intent.EXTRA_STREAM, csvUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Export / Share CSV Report"))
    }
}
