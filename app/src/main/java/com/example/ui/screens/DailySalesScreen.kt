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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCategory
import com.example.data.model.ProductWithVariants
import com.example.data.model.SaleEntity
import com.example.data.model.SaleWithItems
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DarkCharcoalTextSecondary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.CeyvanaViewModel
import com.example.ui.viewmodel.LineItemDraft

@Composable
fun DailySalesScreen(
    viewModel: CeyvanaViewModel,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedBusinessDate.collectAsState()
    val salesForDate by viewModel.selectedDateSales.collectAsState()
    val allProducts by viewModel.products.collectAsState()

    var showNewSaleDialog by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<SaleEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_sales_screen")
    ) {
        // Header with Active Date & "+ Record New Sale" Button
        Surface(
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Sales on ${DateUtils.formatIsoToDisplay(selectedDate)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    Text(
                        text = "${salesForDate.size} transactions • LKR ${
                            CurrencyFormatter.formatLkr(salesForDate.sumOf { it.sale.totalNet }).replace("LKR ", "")
                        } revenue",
                        fontSize = 12.sp,
                        color = DarkCharcoalTextSecondary
                    )
                }

                Button(
                    onClick = { showNewSaleDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("record_sale_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Sale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Transactions List for Date
        if (salesForDate.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No sales recorded for this date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkCharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'New Sale' to record multi-line orders with real-time revenue & gross profit calculations.",
                        fontSize = 12.sp,
                        color = DarkCharcoalTextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(salesForDate, key = { it.sale.id }) { saleWithItems ->
                    SaleDetailCard(
                        saleWithItems = saleWithItems,
                        onDelete = { saleToDelete = saleWithItems.sale }
                    )
                }
            }
        }
    }

    // Record New Sale Dialog (supports multi-line product items)
    if (showNewSaleDialog) {
        RecordSaleDialog(
            initialDate = selectedDate,
            availableProducts = allProducts,
            onDismiss = { showNewSaleDialog = false },
            onSave = { date, customer, orderRef, notes, drafts ->
                viewModel.recordSaleTransaction(
                    date = date,
                    customerName = customer,
                    orderReference = orderRef,
                    notes = notes,
                    lineDrafts = drafts,
                    onSuccess = { showNewSaleDialog = false }
                )
            }
        )
    }

    // Delete Sale Confirmation
    if (saleToDelete != null) {
        ConfirmationDialog(
            title = "Delete Sale Record?",
            message = "Are you sure you want to delete this sale record? Revenue and profit figures will update automatically.",
            confirmText = "Delete Sale",
            onConfirm = {
                saleToDelete?.let { viewModel.deleteSaleTransaction(it) }
                saleToDelete = null
            },
            onDismiss = { saleToDelete = null }
        )
    }
}

@Composable
private fun SaleDetailCard(
    saleWithItems: SaleWithItems,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sale = saleWithItems.sale

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8E3D8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (sale.orderReference.isNotBlank()) sale.orderReference else "Sale #${sale.id}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoalText
                    )
                    if (sale.customerName.isNotBlank()) {
                        Text(
                            text = " • ${sale.customerName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkCharcoalTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyFormatter.formatLkr(sale.totalNet),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_sale_${sale.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFB03A2E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Gross Profit & Units Summary
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gross Profit: ${CurrencyFormatter.formatLkr(sale.totalGrossProfit)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedGoldSecondary
                )
                Text(
                    text = "${saleWithItems.items.sumOf { it.quantity }} units sold",
                    fontSize = 12.sp,
                    color = DarkCharcoalTextSecondary
                )
            }

            if (sale.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${sale.notes}",
                    fontSize = 11.sp,
                    color = DarkCharcoalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand/Collapse line items
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (expanded) "Hide Item Breakdown (${saleWithItems.items.size} lines)" else "Show Item Breakdown (${saleWithItems.items.size} lines)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestGreenPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Expanded Item Lines
            if (expanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF7F2), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    saleWithItems.items.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.quantity}x ${item.productName} (${item.variantName})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkCharcoalText
                                )
                                Text(
                                    text = "@ ${CurrencyFormatter.formatLkr(item.unitSellingPrice)} (Cost: ${CurrencyFormatter.formatLkr(item.unitCostPrice)})",
                                    fontSize = 10.sp,
                                    color = DarkCharcoalTextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatLkr(item.netRevenue),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoalText
                                )
                                Text(
                                    text = "Profit: ${CurrencyFormatter.formatLkr(item.grossProfit)}",
                                    fontSize = 10.sp,
                                    color = MutedGoldSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSaleDialog(
    initialDate: String,
    availableProducts: List<ProductWithVariants>,
    onDismiss: () -> Unit,
    onSave: (date: String, customer: String, orderRef: String, notes: String, drafts: List<LineItemDraft>) -> Unit
) {
    var saleDate by remember { mutableStateOf(initialDate) }
    var customerName by remember { mutableStateOf("") }
    var orderReference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val lineDrafts = remember {
        mutableStateListOf<LineItemDraft>().apply {
            // Prepopulate 1 starting line item if products exist
            if (availableProducts.isNotEmpty()) {
                val firstProd = availableProducts.first()
                val firstVar = firstProd.variants.firstOrNull { it.isActive }
                add(
                    LineItemDraft(
                        product = firstProd,
                        variant = firstVar,
                        unitCostPrice = firstVar?.costPrice ?: 0.0,
                        unitSellingPrice = firstVar?.sellingPrice ?: 0.0,
                        quantity = 1,
                        discount = 0.0
                    )
                )
            }
        }
    }

    // Real-time calculation totals across all line drafts
    val totalGross = lineDrafts.sumOf { it.grossAmount }
    val totalDiscount = lineDrafts.sumOf { it.discount }
    val totalNetRevenue = lineDrafts.sumOf { it.netRevenue }
    val totalCOGS = lineDrafts.sumOf { it.costOfGoodsSold }
    val totalGrossProfit = lineDrafts.sumOf { it.grossProfit }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Daily Sale",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Sale Date
                OutlinedTextField(
                    value = saleDate,
                    onValueChange = { saleDate = it },
                    label = { Text("Sale Date (YYYY-MM-DD) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sale_date_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Customer & Order Ref
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer (Optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("customer_name_input")
                    )
                    OutlinedTextField(
                        value = orderReference,
                        onValueChange = { orderReference = it },
                        label = { Text("Order Ref (Optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("order_ref_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Line Items Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sale Line Items (${lineDrafts.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )

                    TextButton(
                        onClick = {
                            if (availableProducts.isNotEmpty()) {
                                val p = availableProducts.first()
                                val v = p.variants.firstOrNull { it.isActive }
                                lineDrafts.add(
                                    LineItemDraft(
                                        product = p,
                                        variant = v,
                                        unitCostPrice = v?.costPrice ?: 0.0,
                                        unitSellingPrice = v?.sellingPrice ?: 0.0,
                                        quantity = 1,
                                        discount = 0.0
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("add_sale_line_item_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Item", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Line Items list
                lineDrafts.forEachIndexed { index, draft ->
                    SaleLineItemEditor(
                        draft = draft,
                        availableProducts = availableProducts,
                        onUpdate = { updated -> lineDrafts[index] = updated },
                        onRemove = { lineDrafts.removeAt(index) },
                        canRemove = lineDrafts.size > 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Order Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Sale Notes / Delivery Info (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Calculation Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5EE)),
                    border = BorderStroke(1.dp, Color(0xFFE2DDD2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ORDER CALCULATION SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gross Amount:", fontSize = 12.sp, color = DarkCharcoalTextSecondary)
                            Text(CurrencyFormatter.formatLkr(totalGross), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (totalDiscount > 0) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Discount:", fontSize = 12.sp, color = DarkCharcoalTextSecondary)
                                Text("-${CurrencyFormatter.formatLkr(totalDiscount)}", fontSize = 12.sp, color = Color(0xFFB03A2E))
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cost of Goods Sold (COGS):", fontSize = 12.sp, color = DarkCharcoalTextSecondary)
                            Text(CurrencyFormatter.formatLkr(totalCOGS), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFDCD6CA)))
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Net Revenue:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            Text(CurrencyFormatter.formatLkr(totalNetRevenue), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gross Profit:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MutedGoldSecondary)
                            Text(CurrencyFormatter.formatLkr(totalGrossProfit), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MutedGoldSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(saleDate, customerName, orderReference, notes, lineDrafts)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("submit_sale_button")
            ) {
                Text("Save Sale", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF666666))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleLineItemEditor(
    draft: LineItemDraft,
    availableProducts: List<ProductWithVariants>,
    onUpdate: (LineItemDraft) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    var productExpanded by remember { mutableStateOf(false) }
    var variantExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2DDD2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Product Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = productExpanded,
                onExpandedChange = { productExpanded = !productExpanded }
            ) {
                OutlinedTextField(
                    value = draft.product?.product?.name ?: "Select Product",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Product *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = productExpanded,
                    onDismissRequest = { productExpanded = false }
                ) {
                    availableProducts.forEach { prod ->
                        DropdownMenuItem(
                            text = {
                                Text("${prod.product.name} (${prod.product.category})", fontSize = 13.sp)
                            },
                            onClick = {
                                val firstVar = prod.variants.firstOrNull { it.isActive }
                                onUpdate(
                                    draft.copy(
                                        product = prod,
                                        variant = firstVar,
                                        unitCostPrice = firstVar?.costPrice ?: 0.0,
                                        unitSellingPrice = firstVar?.sellingPrice ?: 0.0
                                    )
                                )
                                productExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Variant Selector Dropdown
            val activeVariants = draft.product?.variants?.filter { it.isActive } ?: emptyList()
            ExposedDropdownMenuBox(
                expanded = variantExpanded,
                onExpandedChange = { variantExpanded = !variantExpanded }
            ) {
                OutlinedTextField(
                    value = draft.variant?.variantName ?: "Select Package Size",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Package Variant *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = variantExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = variantExpanded,
                    onDismissRequest = { variantExpanded = false }
                ) {
                    activeVariants.forEach { v ->
                        DropdownMenuItem(
                            text = {
                                Text("${v.variantName} - Sell: ${CurrencyFormatter.formatLkr(v.sellingPrice)}")
                            },
                            onClick = {
                                onUpdate(
                                    draft.copy(
                                        variant = v,
                                        unitCostPrice = v.costPrice,
                                        unitSellingPrice = v.sellingPrice
                                    )
                                )
                                variantExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: Quantity and Discount
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quantity Stepper
                Column {
                    Text("Quantity *", fontSize = 11.sp, color = DarkCharcoalTextSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, Color(0xFFDDD6CA), RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (draft.quantity > 1) onUpdate(draft.copy(quantity = draft.quantity - 1)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${draft.quantity}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onUpdate(draft.copy(quantity = draft.quantity + 1)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Discount (optional)
                OutlinedTextField(
                    value = if (draft.discount == 0.0) "" else draft.discount.toString(),
                    onValueChange = {
                        val num = it.toDoubleOrNull() ?: 0.0
                        onUpdate(draft.copy(discount = num))
                    },
                    placeholder = { Text("0") },
                    label = { Text("Discount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Line",
                            tint = Color(0xFFC0392B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Unit Cost Price (LKR) and Unit Selling Price (LKR)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = draft.unitCostPrice.toString(),
                    onValueChange = {
                        val num = it.toDoubleOrNull() ?: 0.0
                        onUpdate(draft.copy(unitCostPrice = num))
                    },
                    label = { Text("Unit Cost (LKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("line_cost_input")
                )

                OutlinedTextField(
                    value = draft.unitSellingPrice.toString(),
                    onValueChange = {
                        val num = it.toDoubleOrNull() ?: 0.0
                        onUpdate(draft.copy(unitSellingPrice = num))
                    },
                    label = { Text("Unit Sell (LKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("line_sell_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Automatic Profit Calculation Banner for this Line Item
            val lineMargin = if (draft.netRevenue > 0) (draft.grossProfit / draft.netRevenue) * 100.0 else 0.0
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (draft.grossProfit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Cost: ${CurrencyFormatter.formatLkr(draft.costOfGoodsSold)} • Rev: ${CurrencyFormatter.formatLkr(draft.netRevenue)}",
                        fontSize = 11.sp,
                        color = DarkCharcoalTextSecondary
                    )
                    Text(
                        text = "Line Profit: ${CurrencyFormatter.formatLkr(draft.grossProfit)} (${"%.1f".format(lineMargin)}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (draft.grossProfit >= 0) Color(0xFF1B5E20) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}
