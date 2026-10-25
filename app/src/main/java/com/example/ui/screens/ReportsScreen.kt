package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessReportData
import com.example.data.model.CategorySummaryItem
import com.example.data.model.DailySummaryItem
import com.example.data.model.ReportPeriodPreset
import com.example.data.model.ReportSaleDetailItem
import com.example.ui.components.DatePickerModal
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.theme.WarmIvoryBackground
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.CeyvanaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    viewModel: CeyvanaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reportData by viewModel.businessReport.collectAsState()
    val activePreset by viewModel.reportPeriodPreset.collectAsState()
    val reportStartDate by viewModel.reportStartDate.collectAsState()
    val reportEndDate by viewModel.reportEndDate.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showSingleDatePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CEYVANA SPICES",
                                color = MutedGoldSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Business Reports",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = ForestGreenContainer,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Asia/Colombo (SLST)",
                                color = ForestGreenOnContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Official accounting records, gross profit calculations, export documents, and real-time sales summaries.",
                        color = Color(0xFFE2ECE5),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MutedGoldSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reportData.formattedPeriod,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Currency: LKR",
                            color = MutedGoldSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Period Selector Module
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reporting Period",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )

                        Text(
                            text = "Reset Filters",
                            color = ForestGreenPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.resetReportFilters() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("btn_reset_filters")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Chips Row
                    val presets = listOf(
                        ReportPeriodPreset.TODAY,
                        ReportPeriodPreset.YESTERDAY,
                        ReportPeriodPreset.THIS_WEEK,
                        ReportPeriodPreset.THIS_MONTH,
                        ReportPeriodPreset.PREVIOUS_MONTH,
                        ReportPeriodPreset.CUSTOM,
                        ReportPeriodPreset.SINGLE_DATE
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = activePreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (preset == ReportPeriodPreset.CUSTOM) {
                                        viewModel.setReportPeriodPreset(preset)
                                        showStartDatePicker = true
                                    } else if (preset == ReportPeriodPreset.SINGLE_DATE) {
                                        viewModel.setReportPeriodPreset(preset)
                                        showSingleDatePicker = true
                                    } else {
                                        viewModel.setReportPeriodPreset(preset)
                                    }
                                },
                                label = {
                                    Text(
                                        text = preset.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreenContainer,
                                    selectedLabelColor = ForestGreenPrimary,
                                    selectedLeadingIconColor = ForestGreenPrimary,
                                    containerColor = Color(0xFFF5F7F5),
                                    labelColor = Color(0xFF4A554E)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) ForestGreenPrimary else Color(0xFFD6DFD8),
                                    selectedBorderColor = ForestGreenPrimary,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.testTag("preset_${preset.name.lowercase()}")
                            )
                        }
                    }

                    // Custom Date Range Pickers (shown when Custom is active)
                    AnimatedVisibility(visible = activePreset == ReportPeriodPreset.CUSTOM) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showStartDatePicker = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_custom_start_date"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("From", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            DateUtils.formatIsoToDisplay(reportStartDate),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showEndDatePicker = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_custom_end_date"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("To", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            DateUtils.formatIsoToDisplay(reportEndDate),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                    }
                                }
                            }

                            if (!DateUtils.isRangeValid(reportStartDate, reportEndDate)) {
                                Text(
                                    text = "End date cannot be earlier than start date",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

                    // Single Date Picker (shown when Single Date is active)
                    AnimatedVisibility(visible = activePreset == ReportPeriodPreset.SINGLE_DATE) {
                        Box(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedButton(
                                onClick = { showSingleDatePicker = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_pick_single_date"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ForestGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Selected Date: ${DateUtils.formatIsoToDisplay(reportStartDate)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Export, Print & Sharing Action Toolbar
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Report Actions & Sharing",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export PDF
                        Button(
                            onClick = { viewModel.exportReportPdf(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_export_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Export CSV
                        OutlinedButton(
                            onClick = { viewModel.exportReportCsv(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_export_csv")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }

                        // Print
                        OutlinedButton(
                            onClick = { viewModel.printReport(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_print_report")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }

                        // Email
                        OutlinedButton(
                            onClick = { viewModel.shareReportViaEmail(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_email_report")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }

                        // WhatsApp
                        OutlinedButton(
                            onClick = { viewModel.shareReportViaWhatsApp(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_whatsapp_report")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF1E7E34)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E7E34))
                        }
                    }
                }
            }
        }

        // 4. Executive Financial Summary Cards
        item {
            Text(
                text = "Financial Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
        }

        item {
            val sum = reportData.summary
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "NET REVENUE",
                        value = sum.formattedRevenue,
                        subtitle = "${sum.salesCount} sales recorded",
                        accentColor = ForestGreenPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_revenue")
                    )

                    KpiCard(
                        title = "GROSS PROFIT",
                        value = sum.formattedGrossProfit,
                        subtitle = "Margin: ${sum.formattedMargin}",
                        accentColor = MutedGoldSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_profit")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "COST OF GOODS (COGS)",
                        value = sum.formattedCOGS,
                        subtitle = "Direct product cost",
                        accentColor = Color(0xFF5B675F),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_cogs")
                    )

                    KpiCard(
                        title = "UNITS SOLD",
                        value = "${sum.totalUnitsSold} items",
                        subtitle = "Avg/Sale: ${sum.formattedAvgRevenue}",
                        accentColor = ForestGreenPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_units")
                    )
                }
            }
        }

        // 5. Category Performance Summary
        item {
            Text(
                text = "Category Performance",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    reportData.categoryBreakdown.forEachIndexed { idx, cat ->
                        CategoryRow(
                            item = cat,
                            totalRevenue = reportData.summary.totalRevenue
                        )
                        if (idx < reportData.categoryBreakdown.size - 1) {
                            HorizontalDivider(color = Color(0xFFF0F3F1))
                        }
                    }
                }
            }
        }

        // 6. Daily Performance Breakdown (if available)
        if (reportData.dailyBreakdown.size > 1) {
            item {
                Text(
                    text = "Daily Performance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F2), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(1.2f))
                            Text("Sales", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text("Revenue", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            Text("Gross Profit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        reportData.dailyBreakdown.forEachIndexed { idx, day ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(day.formattedDate, fontSize = 11.sp, color = Color(0xFF1E2822), modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Medium)
                                Text("${day.salesCount}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                Text(day.formattedRevenue, fontSize = 11.sp, color = Color(0xFF1E2822), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                Text(day.formattedGrossProfit, fontSize = 11.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }
                            if (idx < reportData.dailyBreakdown.size - 1) {
                                HorizontalDivider(color = Color(0xFFF5F7F5))
                            }
                        }
                    }
                }
            }
        }

        // 7. Sales Details Table / Item List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detailed Transactions & Line Items",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )

                Text(
                    text = "${reportData.detailedSales.size} line items",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        if (!reportData.hasSales) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF5F1),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No sales recorded for this period",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "There are no completed transactions between ${DateUtils.formatIsoToDisplay(reportStartDate)} and ${DateUtils.formatIsoToDisplay(reportEndDate)}.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(reportData.detailedSales) { lineItem ->
                DetailedSaleLineCard(lineItem)
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Pickers for Custom Range and Single Date
    if (showStartDatePicker) {
        DatePickerModal(
            initialDate = reportStartDate,
            onDateSelected = { newStart ->
                showStartDatePicker = false
                val newEnd = if (newStart > reportEndDate) newStart else reportEndDate
                viewModel.setCustomReportRange(newStart, newEnd)
            },
            onDismissRequest = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerModal(
            initialDate = reportEndDate,
            onDateSelected = { newEnd ->
                showEndDatePicker = false
                val newStart = if (newEnd < reportStartDate) newEnd else reportStartDate
                viewModel.setCustomReportRange(newStart, newEnd)
            },
            onDismissRequest = { showEndDatePicker = false }
        )
    }

    if (showSingleDatePicker) {
        DatePickerModal(
            initialDate = reportStartDate,
            onDateSelected = { chosenDate ->
                showSingleDatePicker = false
                viewModel.setSingleDateReport(chosenDate)
            },
            onDismissRequest = { showSingleDatePicker = false }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A7970),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF88968E)
            )
        }
    }
}

@Composable
private fun CategoryRow(
    item: CategorySummaryItem,
    totalRevenue: Double
) {
    val sharePct = if (totalRevenue > 0) (item.revenue / totalRevenue) * 100.0 else 0.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForestGreenContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = item.category,
                        color = ForestGreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${item.unitsSold} units",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.formattedRevenue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Text(
                    text = "Profit: ${item.formattedGrossProfit} (${item.formattedMargin})",
                    fontSize = 10.sp,
                    color = MutedGoldSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress line for category sales distribution
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EFEA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (sharePct / 100f).toFloat().coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ForestGreenPrimary)
            )
        }
    }
}

@Composable
private fun DetailedSaleLineCard(item: ReportSaleDetailItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F2)
                    ) {
                        Text(
                            text = item.orderReference,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.formattedDate,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ForestGreenContainer
                ) {
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2822)
            )

            Text(
                text = "Variant: ${item.variantName} • Qty: ${item.quantity} • Unit: ${item.formattedUnitPrice}",
                fontSize = 11.sp,
                color = Color(0xFF5E6D64)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF4F6F4))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Revenue", fontSize = 10.sp, color = Color.Gray)
                    Text(item.formattedRevenue, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E2822))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cost (COGS)", fontSize = 10.sp, color = Color.Gray)
                    Text(item.formattedCOGS, fontSize = 12.sp, color = Color(0xFF5E6D64))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Gross Profit", fontSize = 10.sp, color = Color.Gray)
                    Text(item.formattedGrossProfit, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                }
            }
        }
    }
}
