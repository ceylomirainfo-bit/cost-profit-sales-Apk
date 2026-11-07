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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.ProductType
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
    val selectedProductType by viewModel.selectedProductTypeFilter.collectAsState()
    val allCount by viewModel.allProductCount.collectAsState()
    val premCount by viewModel.premiumProductCount.collectAsState()
    val normCount by viewModel.normalProductCount.collectAsState()

    var productToEdit by remember { mutableStateOf<ProductWithVariants?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var variantToQuickEdit by remember { mutableStateOf<ProductVariantEntity?>(null) }
    var showReloadCatalogueConfirm by remember { mutableStateOf(false) }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setProductSearchQuery(it) },
                        placeholder = { Text("Search products by name...", fontSize = 13.sp, color = Color(0xFF5A4C44), fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ForestGreenPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setProductSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF3E2316))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkCharcoalText,
                            unfocusedTextColor = DarkCharcoalText,
                            focusedPlaceholderColor = Color(0xFF5A4C44),
                            unfocusedPlaceholderColor = Color(0xFF5A4C44),
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = Color(0xFF8C7E74),
                            focusedLeadingIconColor = ForestGreenPrimary,
                            unfocusedLeadingIconColor = ForestGreenPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_search_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2ECE4),
                        border = BorderStroke(1.dp, Color(0xFFE2D7C9))
                    ) {
                        IconButton(
                            onClick = { showReloadCatalogueConfirm = true },
                            modifier = Modifier.testTag("reload_official_pricelist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Load Official Price List",
                                tint = ForestGreenPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Product Classification Tabs (ALL, PREMIUM, NORMAL)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF2ECE4),
                    border = BorderStroke(1.dp, Color(0xFFE2D7C9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_classification_tabs")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // All Products Tab
                        val isAllSelected = (selectedProductType == null)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) ForestGreenPrimary else Color.Transparent)
                                .clickable { viewModel.setProductTypeFilter(null) }
                                .padding(vertical = 8.dp)
                                .testTag("product_tab_all")
                        ) {
                            Text(
                                text = "All ($allCount)",
                                fontSize = 12.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) Color(0xFFFAF6EE) else Color(0xFF285641)
                            )
                        }

                        // Premium Products Tab
                        val isPremSelected = (selectedProductType == ProductType.PREMIUM)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPremSelected) ForestGreenPrimary else Color.Transparent)
                                .clickable { viewModel.setProductTypeFilter(ProductType.PREMIUM) }
                                .padding(vertical = 8.dp)
                                .testTag("product_tab_premium")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isPremSelected) Color(0xFFFFD700) else Color(0xFFB8860B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Premium ($premCount)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isPremSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPremSelected) Color(0xFFFAF6EE) else Color(0xFF285641)
                                )
                            }
                        }

                        // Normal Products Tab
                        val isNormSelected = (selectedProductType == ProductType.NORMAL)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNormSelected) Color(0xFF1E523D) else Color.Transparent)
                                .clickable { viewModel.setProductTypeFilter(ProductType.NORMAL) }
                                .padding(vertical = 8.dp)
                                .testTag("product_tab_normal")
                        ) {
                            Text(
                                text = "Normal ($normCount)",
                                fontSize = 12.sp,
                                fontWeight = if (isNormSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNormSelected) Color(0xFFFAF6EE) else Color(0xFF285641)
                            )
                        }
                    }
                }

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
                            label = {
                                Text(
                                    text = "All Categories",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCat == null) Color.White else ForestGreenPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF3F8F5),
                                labelColor = ForestGreenPrimary,
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (selectedCat == null) ForestGreenPrimary else Color(0xFF8BA89B)),
                            modifier = Modifier.testTag("category_filter_all")
                        )
                    }

                    items(ProductCategory.all) { category ->
                        val isCatSelected = (selectedCat == category)
                        FilterChip(
                            selected = isCatSelected,
                            onClick = { viewModel.setCategoryFilter(category) },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCatSelected) Color.White else ForestGreenPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF3F8F5),
                                labelColor = ForestGreenPrimary,
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (isCatSelected) ForestGreenPrimary else Color(0xFF8BA89B)),
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
            onSave = { name, category, productType, description, imageUri, variants ->
                viewModel.saveProduct(
                    id = productToEdit?.product?.id ?: 0L,
                    name = name,
                    category = category,
                    productType = productType,
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

    // Reload Official Price List Confirmation Dialog
    if (showReloadCatalogueConfirm) {
        ConfirmationDialog(
            title = "Load Official Price List?",
            message = "This will synchronize all official Sri Lankan spice products (Chilli, Turmeric, Curry Powders, Goraka, Black Pepper, Tea, Soya, Papadam, Cinnamon) with official pack sizes (25g - 1kg) and verified LKR selling prices. Any product with past sales will remain untouched.",
            confirmText = "Load Prices",
            onConfirm = {
                viewModel.reloadOfficialPriceList()
                showReloadCatalogueConfirm = false
            },
            onDismiss = { showReloadCatalogueConfirm = false }
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
                    label = { Text("Cost Price per Unit (LKR) *", fontWeight = FontWeight.SemiBold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkCharcoalText,
                        unfocusedTextColor = DarkCharcoalText,
                        focusedLabelColor = ForestGreenPrimary,
                        unfocusedLabelColor = Color(0xFF4A3E37),
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = Color(0xFF8C7E74)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("quick_cost_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = sellStr,
                    onValueChange = { input -> sellStr = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Selling Price per Unit (LKR) *", fontWeight = FontWeight.SemiBold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkCharcoalText,
                        unfocusedTextColor = DarkCharcoalText,
                        focusedLabelColor = ForestGreenPrimary,
                        unfocusedLabelColor = Color(0xFF4A3E37),
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = Color(0xFF8C7E74)
                    ),
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
                Text("Cancel", color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
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

                // Name, Category & Classification
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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

                        val isPremium = product.productType.equals(ProductType.PREMIUM, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPremium) ForestGreenPrimary else Color(0xFFE2EFE7),
                            border = BorderStroke(1.dp, if (isPremium) Color(0xFFB8860B) else Color(0xFFC7DBD0)),
                            modifier = Modifier.testTag("product_type_badge_${product.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isPremium) Color(0xFFFFD700) else ForestGreenPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isPremium) "Premium" else "Normal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) Color(0xFFFAF6EE) else ForestGreenPrimary
                                )
                            }
                        }
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
                    color = DarkCharcoalText
                )
                Text(
                    text = "Tap variant to edit cost",
                    fontSize = 10.sp,
                    color = Color(0xFF285641),
                    fontWeight = FontWeight.SemiBold
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
    onSave: (name: String, category: String, productType: String, description: String, imageUri: String, variants: List<ProductVariantEntity>) -> Unit
) {
    var name by remember { mutableStateOf(existing?.product?.name ?: "") }
    var category by remember { mutableStateOf(existing?.product?.category ?: ProductCategory.SPICES) }
    var productType by remember { mutableStateOf(existing?.product?.productType ?: "") }
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
                    label = { Text("Product Name *", fontWeight = FontWeight.SemiBold) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkCharcoalText,
                        unfocusedTextColor = DarkCharcoalText,
                        focusedLabelColor = ForestGreenPrimary,
                        unfocusedLabelColor = Color(0xFF4A3E37),
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = Color(0xFF8C7E74)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product Type (Classification: Premium vs Normal)
                Text(
                    text = "Product Type (Required) *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                )
                Text(
                    text = "Select whether this item belongs to the Premium collection or Normal range.",
                    fontSize = 11.sp,
                    color = DarkCharcoalTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Premium Option Card
                    val isPremiumSelected = (productType == ProductType.PREMIUM)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPremiumSelected) Color(0xFFFAF6EE) else Color(0xFFFBF9F5),
                        border = BorderStroke(
                            if (isPremiumSelected) 2.dp else 1.dp,
                            if (isPremiumSelected) ForestGreenPrimary else Color(0xFFC7DBD0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { productType = ProductType.PREMIUM }
                            .testTag("select_product_type_premium")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isPremiumSelected) Color(0xFFB8860B) else ForestGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Premium",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isPremiumSelected) ForestGreenPrimary else DarkCharcoalText
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ceylon export grade, special harvest & packaging",
                                fontSize = 10.sp,
                                color = DarkCharcoalTextSecondary,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // Normal Option Card
                    val isNormalSelected = (productType == ProductType.NORMAL)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isNormalSelected) Color(0xFFF3F8F5) else Color(0xFFFBF9F5),
                        border = BorderStroke(
                            if (isNormalSelected) 2.dp else 1.dp,
                            if (isNormalSelected) Color(0xFF1E523D) else Color(0xFFC7DBD0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { productType = ProductType.NORMAL }
                            .testTag("select_product_type_normal")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Normal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isNormalSelected) Color(0xFF1E523D) else DarkCharcoalText
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Standard culinary essentials & regular packaging",
                                fontSize = 10.sp,
                                color = DarkCharcoalTextSecondary,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                if (productType.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please select a product type before saving",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product Category", fontWeight = FontWeight.SemiBold) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkCharcoalText,
                            unfocusedTextColor = DarkCharcoalText,
                            focusedLabelColor = ForestGreenPrimary,
                            unfocusedLabelColor = Color(0xFF4A3E37),
                            focusedTrailingIconColor = ForestGreenPrimary,
                            unfocusedTrailingIconColor = Color(0xFF4A3E37),
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = Color(0xFF8C7E74)
                        ),
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
                                text = { Text(cat, fontWeight = FontWeight.Medium) },
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
                    label = { Text("Description", fontWeight = FontWeight.SemiBold) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkCharcoalText,
                        unfocusedTextColor = DarkCharcoalText,
                        focusedLabelColor = ForestGreenPrimary,
                        unfocusedLabelColor = Color(0xFF4A3E37),
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = Color(0xFF8C7E74)
                    ),
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
                                val isSelected = (selectedSizeTemplate == size)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSizeTemplate = size },
                                    label = {
                                        Text(
                                            text = size,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else ForestGreenPrimary
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(0xFFF3F8F5),
                                        labelColor = ForestGreenPrimary,
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFF8BA89B))
                                )
                            }
                        }

                        if (selectedSizeTemplate == "Other") {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customSizeName,
                                onValueChange = { customSizeName = it },
                                placeholder = { Text("Custom size, e.g. 75g, 2kg, Gift Pack", fontSize = 12.sp, color = Color(0xFF5A4C44)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = DarkCharcoalText,
                                    unfocusedTextColor = DarkCharcoalText,
                                    focusedPlaceholderColor = Color(0xFF5A4C44),
                                    unfocusedPlaceholderColor = Color(0xFF5A4C44),
                                    focusedBorderColor = ForestGreenPrimary,
                                    unfocusedBorderColor = Color(0xFF8C7E74)
                                ),
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
                            Text("+ Add Size Variant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    label = { Text("Cost (LKR)", fontWeight = FontWeight.SemiBold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = DarkCharcoalText,
                                        unfocusedTextColor = DarkCharcoalText,
                                        focusedLabelColor = ForestGreenPrimary,
                                        unfocusedLabelColor = Color(0xFF4A3E37),
                                        focusedBorderColor = ForestGreenPrimary,
                                        unfocusedBorderColor = Color(0xFF8C7E74)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = v.sellingPriceStr,
                                    onValueChange = { input ->
                                        variants[index] = v.copy(sellingPriceStr = input.filter { it.isDigit() || it == '.' })
                                    },
                                    label = { Text("Sell (LKR)", fontWeight = FontWeight.SemiBold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = DarkCharcoalText,
                                        unfocusedTextColor = DarkCharcoalText,
                                        focusedLabelColor = ForestGreenPrimary,
                                        unfocusedLabelColor = Color(0xFF4A3E37),
                                        focusedBorderColor = ForestGreenPrimary,
                                        unfocusedBorderColor = Color(0xFF8C7E74)
                                    ),
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
                    onSave(name, category, productType, description, imageUri, entityVariants)
                },
                enabled = name.isNotBlank() && productType.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFD6EAE0),
                    disabledContentColor = Color(0xFF4A6B5B)
                ),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Save Product", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
