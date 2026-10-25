package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ProductWithVariants
import com.example.data.model.VariantSizes
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DarkCharcoalTextSecondary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.CeyvanaViewModel

@Composable
fun ProductsScreen(
    viewModel: CeyvanaViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsState()

    var productToEdit by remember { mutableStateOf<ProductWithVariants?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var variantToQuickEdit by remember { mutableStateOf<ProductVariantEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNewProduct = true },
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Product", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize().testTag("products_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar & Filter Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setProductSearchQuery(it) },
                    placeholder = { Text("Search products by name...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ForestGreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setProductSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips (ALL, SPICES, TEA, GROCERY ITEMS)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = (selectedCat == null),
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All Products", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("category_filter_all")
                        )
                    }

                    items(ProductCategory.all) { category ->
                        FilterChip(
                            selected = (selectedCat == category),
                            onClick = { viewModel.setCategoryFilter(category) },
                            label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("category_filter_${category.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            // Products list
            if (filteredProducts.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No products matching '$searchQuery'" else "No products found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkCharcoalText
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.product.id }) { item ->
                        ProductItemCard(
                            productWithVariants = item,
                            onEdit = { productToEdit = item },
                            onDelete = { productToDelete = item.product },
                            onQuickEditVariant = { variantToQuickEdit = it }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (isAddingNewProduct || productToEdit != null) {
        ProductEditorDialog(
            existing = productToEdit,
            onDismiss = {
                isAddingNewProduct = false
                productToEdit = null
            },
            onSave = { name, category, description, imageUri, variants ->
                viewModel.saveProduct(
                    id = productToEdit?.product?.id ?: 0L,
                    name = name,
                    category = category,
                    description = description,
                    imageUri = imageUri,
                    variants = variants
                )
                isAddingNewProduct = false
                productToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        ConfirmationDialog(
            title = "Delete Product?",
            message = "Are you sure you want to delete '${productToDelete?.name}'? If it has historical sales, it will be safely archived so your historical revenue records remain intact.",
            confirmText = "Delete",
            onConfirm = {
                productToDelete?.let { viewModel.deleteProduct(it) }
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }

    // Quick Cost & Selling Price Dialog
    if (variantToQuickEdit != null) {
        QuickVariantPricingDialog(
            variant = variantToQuickEdit!!,
            onDismiss = { variantToQuickEdit = null },
            onSave = { newCost, newSell ->
                viewModel.updateVariantCostAndPrice(variantToQuickEdit!!, newCost, newSell)
                variantToQuickEdit = null
            }
        )
    }
}

@Composable
fun QuickVariantPricingDialog(
    variant: ProductVariantEntity,
    onDismiss: () -> Unit,
    onSave: (cost: Double, sell: Double) -> Unit
) {
    var costStr by remember { mutableStateOf(variant.costPrice.toString()) }
    var sellStr by remember { mutableStateOf(variant.sellingPrice.toString()) }

    val cost = costStr.toDoubleOrNull() ?: 0.0
    val sell = sellStr.toDoubleOrNull() ?: 0.0
    val profit = sell - cost
    val marginPct = if (sell > 0) (profit / sell) * 100.0 else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enter Cost & Price: ${variant.variantName}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Update the unit cost to automatically calculate profit and profit margin.",
                    fontSize = 12.sp,
                    color = DarkCharcoalTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { input -> costStr = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Cost Price per Unit (LKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("quick_cost_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = sellStr,
                    onValueChange = { input -> sellStr = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Selling Price per Unit (LKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("quick_sell_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time automatic profit calculation card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Automatic Unit Profit:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkCharcoalText)
                            Text(
                                text = CurrencyFormatter.formatLkr(profit),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) Color(0xFF1B5E20) else Color(0xFFC62828)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Profit Margin:", fontSize = 11.sp, color = DarkCharcoalTextSecondary)
                            Text(
                                text = "${"%.1f".format(marginPct)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) Color(0xFF1B5E20) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(cost, sell) },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("save_quick_pricing_button")
            ) {
                Text("Update Pricing", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF666666))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductItemCard(
    productWithVariants: ProductWithVariants,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickEditVariant: (ProductVariantEntity) -> Unit
) {
    val product = productWithVariants.product
    val variants = productWithVariants.variants.filter { it.isActive }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Product Thumbnail
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0DAD0), RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9F7F2))
                ) {
                    if (product.imageUri.isNotBlank() && product.imageUri != "default:spice") {
                        AsyncImage(
                            model = product.imageUri,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ceyvana_brand_logo),
                            contentDescription = "Default spice logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Category
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ForestGreenContainer
                    ) {
                        Text(
                            text = product.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoalText
                    )

                    if (product.description.isNotBlank()) {
                        Text(
                            text = product.description,
                            fontSize = 11.sp,
                            color = DarkCharcoalTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Edit & Delete actions
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Product",
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Product",
                            tint = Color(0xFFB03A2E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Package Variants, Cost & Profit Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Variants, Cost & Profit per Unit:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalTextSecondary
                )
                Text(
                    text = "Tap variant to edit cost",
                    fontSize = 10.sp,
                    color = MutedGoldSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Detailed Variant List with Cost and Automatic Profit
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                variants.forEach { v ->
                    val unitProfit = v.sellingPrice - v.costPrice
                    val marginPct = if (v.sellingPrice > 0) (unitProfit / v.sellingPrice) * 100.0 else 0.0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFAF7F2),
                        border = BorderStroke(1.dp, Color(0xFFE2DCD0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuickEditVariant(v) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = v.variantName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cost: ${CurrencyFormatter.formatLkr(v.costPrice)}",
                                        fontSize = 11.sp,
                                        color = DarkCharcoalTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• Sell: ${CurrencyFormatter.formatLkr(v.sellingPrice)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkCharcoalText
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (unitProfit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = "Profit: +${CurrencyFormatter.formatLkr(unitProfit).replace("LKR ", "")} (${"%.0f".format(marginPct)}%)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unitProfit >= 0) Color(0xFF1B5E20) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class EditableVariantDraft(
    var id: Long = 0,
    var name: String,
    var costPriceStr: String,
    var sellingPriceStr: String,
    var isActive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditorDialog(
    existing: ProductWithVariants?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, description: String, imageUri: String, variants: List<ProductVariantEntity>) -> Unit
) {
    var name by remember { mutableStateOf(existing?.product?.name ?: "") }
    var category by remember { mutableStateOf(existing?.product?.category ?: ProductCategory.SPICES) }
    var description by remember { mutableStateOf(existing?.product?.description ?: "") }
    var imageUri by remember { mutableStateOf(existing?.product?.imageUri ?: "") }

    val variants = remember {
        mutableStateListOf<EditableVariantDraft>().apply {
            if (existing != null && existing.variants.isNotEmpty()) {
                addAll(existing.variants.map {
                    EditableVariantDraft(
                        id = it.id,
                        name = it.variantName,
                        costPriceStr = it.costPrice.toString(),
                        sellingPriceStr = it.sellingPrice.toString(),
                        isActive = it.isActive
                    )
                })
            } else {
                // Default 50g & 100g starting templates
                add(EditableVariantDraft(name = "50g", costPriceStr = "120.0", sellingPriceStr = "190.0"))
                add(EditableVariantDraft(name = "100g", costPriceStr = "230.0", sellingPriceStr = "360.0"))
                add(EditableVariantDraft(name = "250g", costPriceStr = "540.0", sellingPriceStr = "850.0"))
            }
        }
    }

    var selectedSizeTemplate by remember { mutableStateOf("100g") }
    var customSizeName by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "Add New Product" else "Edit Product",
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
                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        ProductCategory.all.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Product Image Selector
                Text(
                    text = "Product Image",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFDDD6CA), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF9F7F2))
                    ) {
                        if (imageUri.isNotBlank() && imageUri != "default:spice") {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Product preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ceyvana_brand_logo),
                                contentDescription = "Default emblem",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("pick_product_image_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUri.isNotBlank()) "Change Image" else "Upload Photo", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Package Variants Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Package Variants & Prices (LKR)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Variant Row Controls
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF9F5)),
                    border = BorderStroke(1.dp, Color(0xFFEBE6DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Add Variant Size:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkCharcoalTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(VariantSizes.defaults) { size ->
                                FilterChip(
                                    selected = (selectedSizeTemplate == size),
                                    onClick = { selectedSizeTemplate = size },
                                    label = { Text(size, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (selectedSizeTemplate == "Other") {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customSizeName,
                                onValueChange = { customSizeName = it },
                                placeholder = { Text("Custom size, e.g. 75g, 2kg, Gift Pack", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val variantName = if (selectedSizeTemplate == "Other") {
                                    customSizeName.ifBlank { "Custom Pack" }
                                } else {
                                    selectedSizeTemplate
                                }
                                variants.add(
                                    EditableVariantDraft(
                                        name = variantName,
                                        costPriceStr = "150.0",
                                        sellingPriceStr = "250.0",
                                        isActive = true
                                    )
                                )
                                if (selectedSizeTemplate == "Other") {
                                    customSizeName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier.align(Alignment.End).testTag("add_variant_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Size Variant", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of variants being edited
                variants.forEachIndexed { index, v ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2DDD2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Variant: ${v.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoalText
                                )

                                IconButton(
                                    onClick = { variants.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFC0392B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = v.costPriceStr,
                                    onValueChange = { input ->
                                        variants[index] = v.copy(costPriceStr = input.filter { it.isDigit() || it == '.' })
                                    },
                                    label = { Text("Cost (LKR)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = v.sellingPriceStr,
                                    onValueChange = { input ->
                                        variants[index] = v.copy(sellingPriceStr = input.filter { it.isDigit() || it == '.' })
                                    },
                                    label = { Text("Sell (LKR)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val costNum = v.costPriceStr.toDoubleOrNull() ?: 0.0
                            val sellNum = v.sellingPriceStr.toDoubleOrNull() ?: 0.0
                            val profitNum = sellNum - costNum
                            val marginNum = if (sellNum > 0) (profitNum / sellNum) * 100.0 else 0.0

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (profitNum >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Auto Calculated Profit: ${CurrencyFormatter.formatLkr(profitNum)} (${"%.1f".format(marginNum)}% margin)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (profitNum >= 0) Color(0xFF1B5E20) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entityVariants = variants.map { draft ->
                        ProductVariantEntity(
                            id = draft.id,
                            productId = existing?.product?.id ?: 0L,
                            variantName = draft.name,
                            costPrice = draft.costPriceStr.toDoubleOrNull() ?: 0.0,
                            sellingPrice = draft.sellingPriceStr.toDoubleOrNull() ?: 0.0,
                            currency = "LKR",
                            isActive = draft.isActive
                        )
                    }
                    onSave(name, category, description, imageUri, entityVariants)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Save Product", fontWeight = FontWeight.Bold)
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
