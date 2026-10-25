package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderNoteEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DarkCharcoalTextSecondary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.CeyvanaViewModel

val orderStatuses = listOf("Pending", "Confirmed", "Packed", "Completed", "Cancelled")

@Composable
fun OrdersNotesScreen(
    viewModel: CeyvanaViewModel,
    modifier: Modifier = Modifier
) {
    val allNotes by viewModel.allOrderNotes.collectAsState()
    val selectedDate by viewModel.selectedBusinessDate.collectAsState()

    var statusFilter by remember { mutableStateOf<String?>(null) }
    var isAddingNote by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<OrderNoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<OrderNoteEntity?>(null) }

    val filteredNotes = allNotes.filter { note ->
        statusFilter == null || note.status.equals(statusFilter, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNote = true },
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_order_note_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Order or Note")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Order / Note", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize().testTag("orders_notes_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status Filters Row
            Surface(
                color = Color.White,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = (statusFilter == null),
                            onClick = { statusFilter = null },
                            label = { Text("All (${allNotes.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all_orders")
                        )
                    }

                    items(orderStatuses) { status ->
                        val count = allNotes.count { it.status.equals(status, ignoreCase = true) }
                        FilterChip(
                            selected = (statusFilter == status),
                            onClick = { statusFilter = status },
                            label = { Text("$status ($count)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_order_$status")
                        )
                    }
                }
            }

            // Orders list
            if (filteredNotes.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (statusFilter != null) "No orders with status '$statusFilter'" else "No orders or business notes yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkCharcoalText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record customer special requests, pending orders, and delivery instructions.",
                            fontSize = 12.sp,
                            color = DarkCharcoalTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        OrderNoteItemCard(
                            note = note,
                            onStatusChange = { newStatus -> viewModel.updateOrderStatus(note, newStatus) },
                            onEdit = { noteToEdit = note },
                            onDelete = { noteToDelete = note }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (isAddingNote || noteToEdit != null) {
        OrderNoteEditorDialog(
            existing = noteToEdit,
            defaultDate = selectedDate,
            onDismiss = {
                isAddingNote = false
                noteToEdit = null
            },
            onSave = { date, ref, cust, details, notesText, status ->
                viewModel.saveOrderNote(
                    id = noteToEdit?.id ?: 0L,
                    date = date,
                    orderReference = ref,
                    customerName = cust,
                    orderDetails = details,
                    notes = notesText,
                    status = status
                )
                isAddingNote = false
                noteToEdit = null
            }
        )
    }

    // Delete confirmation
    if (noteToDelete != null) {
        ConfirmationDialog(
            title = "Delete Order / Note?",
            message = "Are you sure you want to delete this order record? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                noteToDelete?.let { viewModel.deleteOrderNote(it) }
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderNoteItemCard(
    note: OrderNoteEntity,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8E3D8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${note.id}")
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
                        text = if (note.customerName.isNotBlank()) note.customerName else (note.orderReference.ifBlank { "Order #${note.id}" }),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoalText
                    )
                    if (note.orderReference.isNotBlank() && note.customerName.isNotBlank()) {
                        Text(
                            text = " (${note.orderReference})",
                            fontSize = 12.sp,
                            color = DarkCharcoalTextSecondary
                        )
                    }
                }

                // Interactive Status Pill Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusMenuExpanded,
                    onExpandedChange = { statusMenuExpanded = !statusMenuExpanded }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (note.status) {
                            "Completed" -> Color(0xFFE8F5E9)
                            "Confirmed" -> Color(0xFFE3F2FD)
                            "Packed" -> Color(0xFFF3E5F5)
                            "Cancelled" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFFFF8E1)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .clickable { statusMenuExpanded = true }
                    ) {
                        Text(
                            text = "${note.status} ▾",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (note.status) {
                                "Completed" -> Color(0xFF2E7D32)
                                "Confirmed" -> Color(0xFF1565C0)
                                "Packed" -> Color(0xFF7B1FA2)
                                "Cancelled" -> Color(0xFFC62828)
                                else -> Color(0xFFF57F17)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        orderStatuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s, fontSize = 12.sp) },
                                onClick = {
                                    onStatusChange(s)
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Order Date
            Text(
                text = "Order Date: ${DateUtils.formatIsoToDisplay(note.orderDate)}",
                fontSize = 11.sp,
                color = ForestGreenPrimary,
                fontWeight = FontWeight.SemiBold
            )

            // Order details & items
            if (note.orderDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Items / Quantities:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalTextSecondary
                )
                Text(
                    text = note.orderDetails,
                    fontSize = 13.sp,
                    color = DarkCharcoalText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Notes / special instructions
            if (note.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFAF7F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Special Instructions / Delivery Notes:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGoldSecondary
                        )
                        Text(
                            text = note.notes,
                            fontSize = 12.sp,
                            color = DarkCharcoalText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions row
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_order_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_order_${note.id}")
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderNoteEditorDialog(
    existing: OrderNoteEntity?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (date: String, ref: String, cust: String, details: String, notesText: String, status: String) -> Unit
) {
    var date by remember { mutableStateOf(existing?.orderDate ?: defaultDate) }
    var orderRef by remember { mutableStateOf(existing?.orderReference ?: "") }
    var customer by remember { mutableStateOf(existing?.customerName ?: "") }
    var orderDetails by remember { mutableStateOf(existing?.orderDetails ?: "") }
    var notesText by remember { mutableStateOf(existing?.notes ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "Pending") }

    var statusMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "New Customer Order / Note" else "Edit Order / Note",
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
                // Order Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Order Date (YYYY-MM-DD) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("order_date_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Order Reference
                OutlinedTextField(
                    value = orderRef,
                    onValueChange = { orderRef = it },
                    label = { Text("Order Ref / Number (e.g. ORD-1024)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("order_ref_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Customer Name
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Customer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Order Details (Products & Variants included)
                OutlinedTextField(
                    value = orderDetails,
                    onValueChange = { orderDetails = it },
                    label = { Text("Products & Variants Included") },
                    placeholder = { Text("e.g. 5x 100g Ceylon Cinnamon, 2x 250g BOPF Tea") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("order_details_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Special Notes / Delivery Instructions
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Special Instructions / Delivery Notes") },
                    placeholder = { Text("e.g. Pack in gift bag, delivery to Kandy by Tuesday") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("order_notes_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusMenuExpanded,
                    onExpandedChange = { statusMenuExpanded = !statusMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Order Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        orderStatuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(date, orderRef, customer, orderDetails, notesText, status)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("save_order_button")
            ) {
                Text("Save Order / Note", fontWeight = FontWeight.Bold)
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
