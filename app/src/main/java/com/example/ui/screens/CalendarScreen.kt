package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderNoteEntity
import com.example.data.model.SaleWithItems
import com.example.ui.components.StatSummaryCard
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DarkCharcoalTextSecondary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.CeyvanaViewModel
import com.example.ui.viewmodel.MainNavigationSection
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CeyvanaViewModel,
    onNavigateToSection: (MainNavigationSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDateStr by viewModel.selectedBusinessDate.collectAsState()
    val saleDates by viewModel.distinctSaleDates.collectAsState()
    val orderDates by viewModel.distinctOrderDates.collectAsState()

    val dateSales by viewModel.selectedDateSales.collectAsState()
    val dateNotes by viewModel.selectedDateOrderNotes.collectAsState()

    val selectedLocalDate = remember(selectedDateStr) { DateUtils.parseLocalDate(selectedDateStr) }
    var displayedYearMonth by remember { mutableStateOf(YearMonth.from(selectedLocalDate)) }

    val actualToday = remember { LocalDate.now() }

    val daysInMonth = displayedYearMonth.lengthOfMonth()
    val firstDayOfWeek = displayedYearMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sun, 1 = Mon ...

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize().testTag("calendar_screen")
    ) {
        // Month Navigation & Controls Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displayedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = "Selected: ${DateUtils.formatIsoToDisplay(selectedDateStr)}",
                                fontSize = 11.sp,
                                color = MutedGoldSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shortcut buttons: Today & Initial Launch Date (24 Oct 2026)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                val launch = LocalDate.of(2026, 10, 24)
                                viewModel.setSelectedDate(DateUtils.toIsoString(launch))
                                displayedYearMonth = YearMonth.from(launch)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("calendar_launch_date_button")
                        ) {
                            Text("24 Oct 2026", fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                viewModel.setDateToToday()
                                displayedYearMonth = YearMonth.from(actualToday)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("calendar_today_button")
                        ) {
                            Icon(imageVector = Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Today", fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                            Text(
                                text = day,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF777777),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar month grid
                    val totalCells = firstDayOfWeek + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - firstDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = displayedYearMonth.atDay(dayNumber)
                                    val cellDateIso = DateUtils.toIsoString(cellDate)
                                    val isSelected = (cellDateIso == selectedDateStr)
                                    val isActualToday = (cellDate == actualToday)
                                    val hasSales = saleDates.contains(cellDateIso)
                                    val hasOrders = orderDates.contains(cellDateIso)

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> ForestGreenPrimary
                                                    isActualToday -> Color(0xFFE5F1EB)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .then(
                                                if (isActualToday && !isSelected) {
                                                    Modifier.border(1.5.dp, ForestGreenPrimary, RoundedCornerShape(10.dp))
                                                } else Modifier
                                            )
                                            .clickable {
                                                viewModel.setSelectedDate(cellDateIso)
                                            }
                                            .testTag("calendar_day_$cellDateIso")
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isActualToday) FontWeight.Bold else FontWeight.Medium,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isActualToday -> ForestGreenPrimary
                                                    else -> DarkCharcoalText
                                                }
                                            )

                                            // Record Indicator Dots
                                            if (hasSales || hasOrders) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    if (hasSales) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color(0xFFE5C16C) else ForestGreenPrimary)
                                                        )
                                                    }
                                                    if (hasOrders) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else MutedGoldSecondary)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ForestGreenPrimary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sales Logged", fontSize = 10.sp, color = DarkCharcoalTextSecondary)

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MutedGoldSecondary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Orders/Notes", fontSize = 10.sp, color = DarkCharcoalTextSecondary)
                    }
                }
            }
        }

        // Selected Date Summary Title & Action
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Performance on ${DateUtils.formatIsoToDisplay(selectedDateStr)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                )

                Button(
                    onClick = { onNavigateToSection(MainNavigationSection.DAILY_SALES) },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("calendar_record_sale_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Sale", fontSize = 12.sp)
                }
            }
        }

        // Daily Totals for Selected Date
        item {
            val totalRev = dateSales.sumOf { it.sale.totalNet }
            val totalCost = dateSales.sumOf { it.sale.totalCost }
            val totalProfit = dateSales.sumOf { it.sale.totalGrossProfit }
            val units = dateSales.sumOf { it.items.sumOf { item -> item.quantity } }
            val margin = if (totalRev > 0) (totalProfit / totalRev) * 100.0 else 0.0

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatSummaryCard(
                    title = "Daily Revenue",
                    value = CurrencyFormatter.formatLkr(totalRev),
                    icon = Icons.Default.AttachMoney,
                    iconColor = ForestGreenPrimary,
                    iconBackground = Color(0xFFE5F1EB),
                    subtext = "${dateSales.size} transactions",
                    modifier = Modifier.weight(1f)
                )

                StatSummaryCard(
                    title = "Daily Gross Profit",
                    value = CurrencyFormatter.formatLkr(totalProfit),
                    icon = Icons.Default.TrendingUp,
                    iconColor = MutedGoldSecondary,
                    iconBackground = Color(0xFFFBF1D9),
                    subtext = "Cost: ${CurrencyFormatter.formatLkr(totalCost)} (${"%.1f".format(margin)}%)",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sales for Selected Date
        item {
            Text(
                text = "Sales on this Date (${dateSales.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
            )
        }

        if (dateSales.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEAE5DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No sales recorded on this calendar day.",
                        fontSize = 12.sp,
                        color = DarkCharcoalTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(dateSales) { saleWithItems ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8E3D8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (saleWithItems.sale.orderReference.isNotBlank())
                                    saleWithItems.sale.orderReference
                                else "Sale #${saleWithItems.sale.id}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DarkCharcoalText
                            )
                            val itemsStr = saleWithItems.items.joinToString(", ") { "${it.quantity}x ${it.productName}" }
                            Text(itemsStr, fontSize = 11.sp, color = DarkCharcoalTextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatLkr(saleWithItems.sale.totalNet),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = "Profit: ${CurrencyFormatter.formatLkr(saleWithItems.sale.totalGrossProfit)}",
                                fontSize = 10.sp,
                                color = MutedGoldSecondary
                            )
                        }
                    }
                }
            }
        }

        // Orders & Notes for Selected Date
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Orders & Notes on this Date (${dateNotes.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                )

                TextButton(
                    onClick = { onNavigateToSection(MainNavigationSection.ORDERS_NOTES) },
                    modifier = Modifier.testTag("calendar_add_note_button")
                ) {
                    Text("+ Add Note", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (dateNotes.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEAE5DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No customer orders or notes logged for this calendar date.",
                        fontSize = 12.sp,
                        color = DarkCharcoalTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(dateNotes) { note ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8E3D8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (note.customerName.isNotBlank()) note.customerName else (note.orderReference.ifBlank { "Order Note" }),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DarkCharcoalText
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ForestGreenContainer
                            ) {
                                Text(
                                    text = note.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (note.orderDetails.isNotBlank()) {
                            Text(
                                text = "Details: ${note.orderDetails}",
                                fontSize = 12.sp,
                                color = DarkCharcoalTextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (note.notes.isNotBlank()) {
                            Text(
                                text = "Instructions: ${note.notes}",
                                fontSize = 11.sp,
                                color = DarkCharcoalTextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
