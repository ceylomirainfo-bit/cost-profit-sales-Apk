package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.OrderNoteEntity
import com.example.data.model.SaleWithItems
import com.example.ui.components.StatSummaryCard
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DarkCharcoalTextSecondary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.CeyvanaViewModel
import com.example.ui.viewmodel.MainNavigationSection

@Composable
fun DashboardScreen(
    viewModel: CeyvanaViewModel,
    onNavigateToSection: (MainNavigationSection) -> Unit,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val dateSales by viewModel.selectedDateSales.collectAsState()
    val orderNotes by viewModel.allOrderNotes.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize().testTag("dashboard_screen")
    ) {
        // Hero Brand Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.ceyvana_hero),
                        contentDescription = "Ceyvana Premium Spices Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay for contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xEE123C2C),
                                        Color(0x99123C2C),
                                        Color(0x44123C2C)
                                    )
                                )
                            )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MutedGoldSecondary),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ceyvana_brand_logo),
                                contentDescription = "Ceyvana Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "CEYVANA SPICES",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pure Origin. Timeless Flavor.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2C98C)
                            )
                            Text(
                                text = "Business Intelligence & Daily Sales",
                                fontSize = 10.sp,
                                color = Color(0xFFD6E8DE)
                            )
                        }
                    }
                }
            }
        }

        // Date Context Strip
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(BorderStroke(1.dp, Color(0xFFEBE6DC)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = "REPORTING FOR DATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoalTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = summary.selectedDateDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onOpenDatePicker,
                        modifier = Modifier.testTag("dashboard_change_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Date", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedButton(
                        onClick = { onNavigateToSection(MainNavigationSection.REPORTS) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("dashboard_open_reports_button")
                    ) {
                        Text("Reports", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    }
                }
            }
        }

        // 4 Summary Cards Grid for Selected Date
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatSummaryCard(
                        title = "Today's Revenue",
                        value = CurrencyFormatter.formatLkr(summary.dateRevenue),
                        icon = Icons.Default.AttachMoney,
                        iconColor = ForestGreenPrimary,
                        iconBackground = Color(0xFFE5F1EB),
                        subtext = "Net after discounts",
                        testTag = "card_today_revenue",
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        title = "Today's Gross Profit",
                        value = CurrencyFormatter.formatLkr(summary.dateGrossProfit),
                        icon = Icons.Default.TrendingUp,
                        iconColor = MutedGoldSecondary,
                        iconBackground = Color(0xFFFBF1D9),
                        subtext = "Cost: ${CurrencyFormatter.formatLkr(summary.dateCost)} (${"%.1f".format(summary.dateProfitMarginPct)}% margin)",
                        testTag = "card_today_profit",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatSummaryCard(
                        title = "Today's Units Sold",
                        value = "${summary.dateUnitsSold} units",
                        icon = Icons.Default.Inventory,
                        iconColor = Color(0xFF2980B9),
                        iconBackground = Color(0xFFEBF5FB),
                        subtext = "Across all variants",
                        testTag = "card_today_units",
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        title = "Today's Number of Sales",
                        value = "${summary.dateSalesCount}",
                        icon = Icons.Default.Receipt,
                        iconColor = Color(0xFF8E44AD),
                        iconBackground = Color(0xFFF4ECF7),
                        subtext = "Transactions logged",
                        testTag = "card_today_sales_count",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Automatic Cost & Profit Calculation Equation Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4EE)),
                    border = BorderStroke(1.dp, Color(0xFFE5DFD4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AUTOMATIC PROFIT CALCULATION (${summary.selectedDateDisplay})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Net Revenue", fontSize = 11.sp, color = DarkCharcoalTextSecondary)
                                Text(
                                    text = CurrencyFormatter.formatLkr(summary.dateRevenue),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                            Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalTextSecondary)
                            Column {
                                Text("Product Cost (COGS)", fontSize = 11.sp, color = DarkCharcoalTextSecondary)
                                Text(
                                    text = CurrencyFormatter.formatLkr(summary.dateCost),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB03A2E)
                                )
                            }
                            Text("=", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalTextSecondary)
                            Column {
                                Text("Gross Profit", fontSize = 11.sp, color = DarkCharcoalTextSecondary)
                                Text(
                                    text = CurrencyFormatter.formatLkr(summary.dateGrossProfit),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedGoldSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // All-Time Lifetime Performance Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF163E2F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ALL-TIME BUSINESS TOTALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Revenue", fontSize = 11.sp, color = Color(0xFFB5C6BC))
                            Text(
                                text = CurrencyFormatter.formatLkr(summary.totalRevenue),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text("Total Product Cost", fontSize = 11.sp, color = Color(0xFFB5C6BC))
                            Text(
                                text = CurrencyFormatter.formatLkr(summary.totalCost),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFAB91)
                            )
                        }
                        Column {
                            Text("Total Profit", fontSize = 11.sp, color = Color(0xFFB5C6BC))
                            Text(
                                text = CurrencyFormatter.formatLkr(summary.totalGrossProfit),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5C16C)
                            )
                        }
                        Column {
                            Text("Units", fontSize = 11.sp, color = Color(0xFFB5C6BC))
                            Text(
                                text = "${summary.totalUnitsSold}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Recent Sales for Selected Date
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sales on ${summary.selectedDateDisplay}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                )

                TextButton(
                    onClick = { onNavigateToSection(MainNavigationSection.DAILY_SALES) },
                    modifier = Modifier.testTag("view_all_sales_button")
                ) {
                    Text("Record / View Sales", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }

        if (dateSales.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEAE5DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No sales recorded for this date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkCharcoalText
                        )
                        Text(
                            text = "All figures reflect saved records. Tap below to record today's sales.",
                            fontSize = 12.sp,
                            color = DarkCharcoalTextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        Button(
                            onClick = { onNavigateToSection(MainNavigationSection.DAILY_SALES) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("dashboard_record_sale_cta")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Sale for this Date", fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(dateSales.take(5)) { saleWithItems ->
                DashboardSaleItemCard(saleWithItems)
            }
        }

        // Latest Business Orders & Notes
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Latest Orders & Notes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                )

                TextButton(
                    onClick = { onNavigateToSection(MainNavigationSection.ORDERS_NOTES) },
                    modifier = Modifier.testTag("view_all_notes_button")
                ) {
                    Text("All Orders & Notes", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }

        if (orderNotes.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEAE5DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "No customer orders or notes logged yet",
                            fontSize = 13.sp,
                            color = DarkCharcoalTextSecondary
                        )
                        TextButton(
                            onClick = { onNavigateToSection(MainNavigationSection.ORDERS_NOTES) }
                        ) {
                            Text("+ Add Order or Note", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(orderNotes.take(3)) { note ->
                DashboardNoteCard(note)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardSaleItemCard(saleWithItems: SaleWithItems) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8E3D8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (saleWithItems.sale.orderReference.isNotBlank())
                            saleWithItems.sale.orderReference
                        else
                            "Sale #${saleWithItems.sale.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkCharcoalText
                    )
                    if (saleWithItems.sale.customerName.isNotBlank()) {
                        Text(
                            text = " • ${saleWithItems.sale.customerName}",
                            fontSize = 13.sp,
                            color = DarkCharcoalTextSecondary
                        )
                    }
                }

                Text(
                    text = CurrencyFormatter.formatLkr(saleWithItems.sale.totalNet),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ForestGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items summary
            val itemNames = saleWithItems.items.joinToString(", ") { "${it.quantity}x ${it.productName} (${it.variantName})" }
            Text(
                text = itemNames,
                fontSize = 12.sp,
                color = DarkCharcoalTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gross Profit: ${CurrencyFormatter.formatLkr(saleWithItems.sale.totalGrossProfit)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedGoldSecondary
                )
                Text(
                    text = "${saleWithItems.items.sumOf { it.quantity }} units",
                    fontSize = 11.sp,
                    color = DarkCharcoalTextSecondary
                )
            }
        }
    }
}

@Composable
private fun DashboardNoteCard(note: OrderNoteEntity) {
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (note.customerName.isNotBlank()) note.customerName else (note.orderReference.ifBlank { "Note" }),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DarkCharcoalText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (note.status) {
                            "Completed" -> Color(0xFFE8F5E9)
                            "Confirmed" -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFF8E1)
                        }
                    ) {
                        Text(
                            text = note.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (note.status) {
                                "Completed" -> Color(0xFF2E7D32)
                                "Confirmed" -> Color(0xFF1565C0)
                                else -> Color(0xFFF57F17)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (note.orderDetails.isNotBlank()) {
                    Text(
                        text = note.orderDetails,
                        fontSize = 11.sp,
                        color = DarkCharcoalTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = note.orderDate,
                fontSize = 11.sp,
                color = DarkCharcoalTextSecondary
            )
        }
    }
}
