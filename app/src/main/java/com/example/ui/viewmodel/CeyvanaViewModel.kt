package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.OrderNoteEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ProductWithVariants
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.example.data.model.SaleWithItems
import com.example.data.model.BusinessReportData
import com.example.data.model.ReportCalculator
import com.example.data.model.ReportPeriodPreset
import com.example.data.repository.CeyvanaRepository
import com.example.ui.security.AuthManager
import com.example.ui.security.AuthResult
import com.example.ui.util.CsvReportGenerator
import com.example.ui.util.DateUtils
import com.example.ui.util.PdfReportGenerator
import com.example.ui.util.ReportPrintHelper
import com.example.ui.util.ReportSharingUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavigationSection(val title: String) {
    DASHBOARD("Dashboard"),
    PRODUCTS("Products"),
    DAILY_SALES("Daily Sales"),
    REPORTS("Reports"),
    CALENDAR("Calendar"),
    ORDERS_NOTES("Orders & Notes")
}

data class LineItemDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val product: ProductWithVariants? = null,
    val variant: ProductVariantEntity? = null,
    val unitCostPrice: Double = 0.0,
    val unitSellingPrice: Double = 0.0,
    val quantity: Int = 1,
    val discount: Double = 0.0
) {
    val grossAmount: Double
        get() = unitSellingPrice * quantity

    val netRevenue: Double
        get() = (grossAmount - discount).coerceAtLeast(0.0)

    val costOfGoodsSold: Double
        get() = unitCostPrice * quantity

    val grossProfit: Double
        get() = netRevenue - costOfGoodsSold
}

data class DashboardSummary(
    val selectedDate: String,
    val selectedDateDisplay: String,
    val dateRevenue: Double = 0.0,
    val dateCost: Double = 0.0,
    val dateGrossProfit: Double = 0.0,
    val dateProfitMarginPct: Double = 0.0,
    val dateUnitsSold: Int = 0,
    val dateSalesCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalGrossProfit: Double = 0.0,
    val totalProfitMarginPct: Double = 0.0,
    val totalUnitsSold: Int = 0,
    val totalSalesCount: Int = 0
)

class CeyvanaViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepository: com.example.data.remote.CeyvanaFirestoreRepository =
        com.example.data.remote.CeyvanaFirestoreRepository(application)
    private val repository: CeyvanaRepository
    val authManager: AuthManager = AuthManager(application)

    // Track actual Sri Lankan today and update dynamically (including overnight changes)
    private val _actualTodayDate = MutableStateFlow(DateUtils.getActualTodayIso())
    val actualTodayDate: StateFlow<String> = _actualTodayDate.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = CeyvanaRepository(database, firestoreRepository)
        viewModelScope.launch {
            repository.ensureCatalogueInitialized()
            if (com.google.firebase.Firebase.auth.currentUser != null) {
                repository.syncAllToFirestore()
            }
        }
        // Periodic check to update actual Sri Lankan today automatically when calendar day changes
        viewModelScope.launch {
            while (true) {
                delay(30_000) // check every 30 seconds
                val currentSriLankaToday = DateUtils.getActualTodayIso()
                if (currentSriLankaToday != _actualTodayDate.value) {
                    val wasOnToday = (_selectedBusinessDate.value == _actualTodayDate.value)
                    _actualTodayDate.value = currentSriLankaToday
                    if (wasOnToday) {
                        _selectedBusinessDate.value = currentSriLankaToday
                    }
                }
            }
        }
    }

    fun syncFullDatabase() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            repository.syncAllToFirestore()
            _isCloudSyncing.value = false
            notifyUser("Cloud synchronization complete")
        }
    }

    fun onGoogleSignInSuccess(email: String, displayName: String?) {
        authManager.onGoogleAuthSuccess(email, displayName)
        _currentUserEmail.value = email
        _currentUsername.value = displayName ?: email.substringBefore("@")
        _isAppLocked.value = false
        notifyUser("Signed in as $email. Cloud sync enabled.")
        syncFullDatabase()
    }

    // Active Navigation Section
    private val _currentSection = MutableStateFlow(MainNavigationSection.DASHBOARD)
    val currentSection: StateFlow<MainNavigationSection> = _currentSection.asStateFlow()

    fun navigateTo(section: MainNavigationSection) {
        _currentSection.value = section
    }

    // Selected Business Date: dynamically initialized to actual Sri Lanka today
    private val _selectedBusinessDate = MutableStateFlow(DateUtils.getActualTodayIso())
    val selectedBusinessDate: StateFlow<String> = _selectedBusinessDate.asStateFlow()

    fun setSelectedDate(isoDate: String) {
        _selectedBusinessDate.value = isoDate
    }

    fun setDateToToday() {
        _selectedBusinessDate.value = DateUtils.getActualTodayIso()
    }

    // App Security & Authentication State
    // Defaults to locked on first launch if unauthenticated
    private val _isAppLocked = MutableStateFlow(!authManager.isUserLoggedIn())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(authManager.getCurrentEmail())
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUsername = MutableStateFlow(authManager.getCurrentUsername())
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    fun authenticateWithCredentials(identifier: String, passwordAttempt: String): AuthResult {
        val result = authManager.authenticate(identifier, passwordAttempt)
        if (result is AuthResult.Success) {
            _currentUserEmail.value = result.email
            _currentUsername.value = result.username
            _isAppLocked.value = false
            notifyUser("Welcome back to Ceyvana Spices")
        }
        return result
    }

    fun lockApp() {
        authManager.lock()
        _isAppLocked.value = true
        notifyUser("Dashboard Locked")
    }

    fun logout() {
        authManager.logout()
        try {
            com.google.firebase.Firebase.auth.signOut()
        } catch (_: Exception) {}
        _currentUserEmail.value = ""
        _currentUsername.value = ""
        _isAppLocked.value = true
        _currentSection.value = MainNavigationSection.DASHBOARD
        notifyUser("Signed out of Ceyvana Spices")
    }

    fun hasConfiguredPin(): Boolean = authManager.hasConfiguredPin()

    fun setupPin(newPin: String): Pair<Boolean, String> {
        val (ok, message) = authManager.setupPin(newPin)
        notifyUser(message)
        return Pair(ok, message)
    }

    fun unlockWithPin(pin: String): com.example.ui.security.PinResult {
        val result = authManager.unlockWithPin(pin)
        if (result is com.example.ui.security.PinResult.Success) {
            _isAppLocked.value = false
            notifyUser("Dashboard Unlocked")
        }
        return result
    }

    fun changeOwnerPin(oldPin: String, newPin: String): Boolean {
        val (ok, message) = authManager.changePin(oldPin, newPin)
        notifyUser(message)
        return ok
    }

    fun setupAccountPassword(email: String, username: String, pass: String): Pair<Boolean, String> {
        val (ok, message) = authManager.setupAccountPassword(email, username, pass)
        notifyUser(message)
        return Pair(ok, message)
    }

    // Toast/Snackbar notifications
    private val _userNotification = MutableStateFlow<String?>(null)
    val userNotification: StateFlow<String?> = _userNotification.asStateFlow()

    fun clearNotification() {
        _userNotification.value = null
    }

    fun notifyUser(message: String) {
        _userNotification.value = message
    }

    // Products
    val products: StateFlow<List<ProductWithVariants>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    val filteredProducts: StateFlow<List<ProductWithVariants>> = combine(
        products,
        productSearchQuery,
        selectedCategoryFilter
    ) { list, query, cat ->
        list.filter { item ->
            val matchesCat = (cat == null || item.product.category.equals(cat, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    item.product.name.contains(query, ignoreCase = true) ||
                    item.product.description.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sales Data
    val allSales: StateFlow<List<SaleWithItems>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctSaleDates: StateFlow<List<String>> = repository.saleDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateSales: StateFlow<List<SaleWithItems>> = combine(
        allSales,
        selectedBusinessDate
    ) { sales, date ->
        sales.filter { it.sale.saleDate == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders & Notes
    val allOrderNotes: StateFlow<List<OrderNoteEntity>> = repository.allOrderNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctOrderDates: StateFlow<List<String>> = repository.orderDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateOrderNotes: StateFlow<List<OrderNoteEntity>> = combine(
        allOrderNotes,
        selectedBusinessDate
    ) { notes, date ->
        notes.filter { it.orderDate == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Summary Calculations
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        allSales,
        selectedBusinessDate
    ) { sales, date ->
        var dateRev = 0.0
        var dateCost = 0.0
        var dateProfit = 0.0
        var dateUnits = 0
        var dateCount = 0

        var totRev = 0.0
        var totCost = 0.0
        var totProfit = 0.0
        var totUnits = 0
        val totCount = sales.size

        for (saleWithItems in sales) {
            val isCurrentDate = (saleWithItems.sale.saleDate == date)
            val net = saleWithItems.sale.totalNet
            val cost = saleWithItems.sale.totalCost
            val profit = saleWithItems.sale.totalGrossProfit
            val units = saleWithItems.items.sumOf { it.quantity }

            totRev += net
            totCost += cost
            totProfit += profit
            totUnits += units

            if (isCurrentDate) {
                dateRev += net
                dateCost += cost
                dateProfit += profit
                dateUnits += units
                dateCount += 1
            }
        }

        val dateMargin = if (dateRev > 0) (dateProfit / dateRev) * 100.0 else 0.0
        val totMargin = if (totRev > 0) (totProfit / totRev) * 100.0 else 0.0

        DashboardSummary(
            selectedDate = date,
            selectedDateDisplay = DateUtils.formatIsoToDisplay(date),
            dateRevenue = dateRev,
            dateCost = dateCost,
            dateGrossProfit = dateProfit,
            dateProfitMarginPct = dateMargin,
            dateUnitsSold = dateUnits,
            dateSalesCount = dateCount,
            totalRevenue = totRev,
            totalCost = totCost,
            totalGrossProfit = totProfit,
            totalProfitMarginPct = totMargin,
            totalUnitsSold = totUnits,
            totalSalesCount = totCount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardSummary(DateUtils.INITIAL_BUSINESS_DATE, DateUtils.formatIsoToDisplay(DateUtils.INITIAL_BUSINESS_DATE))
    )

    fun updateVariantCostAndPrice(variant: ProductVariantEntity, newCostPrice: Double, newSellingPrice: Double) {
        if (newCostPrice < 0 || newSellingPrice < 0) {
            notifyUser("Prices cannot be negative")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateVariant(
                    variant.copy(costPrice = newCostPrice, sellingPrice = newSellingPrice)
                )
                notifyUser("Updated ${variant.variantName} cost: LKR ${"%.2f".format(newCostPrice)}, sell: LKR ${"%.2f".format(newSellingPrice)}")
            } catch (e: Exception) {
                notifyUser("Failed to update pricing: ${e.message}")
            }
        }
    }

    // Product Actions
    fun saveProduct(
        id: Long = 0,
        name: String,
        category: String,
        description: String,
        imageUri: String,
        variants: List<ProductVariantEntity>
    ) {
        if (name.isBlank()) {
            notifyUser("Product name cannot be empty")
            return
        }
        if (variants.isEmpty()) {
            notifyUser("Please add at least one variant size")
            return
        }

        viewModelScope.launch {
            try {
                if (id == 0L) {
                    val newProduct = ProductEntity(
                        name = name.trim(),
                        category = category,
                        description = description.trim(),
                        imageUri = imageUri,
                        isArchived = false
                    )
                    repository.addProductWithVariants(newProduct, variants)
                    notifyUser("Product added successfully")
                } else {
                    val updatedProduct = ProductEntity(
                        id = id,
                        name = name.trim(),
                        category = category,
                        description = description.trim(),
                        imageUri = imageUri,
                        isArchived = false
                    )
                    repository.updateProductAndVariants(updatedProduct, variants)
                    notifyUser("Product updated successfully")
                }
            } catch (e: Exception) {
                notifyUser("Error saving product: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                val deleted = repository.deleteOrArchiveProduct(product)
                if (deleted) {
                    notifyUser("Product deleted successfully")
                } else {
                    notifyUser("Product has historical sales and was safely archived")
                }
            } catch (e: Exception) {
                notifyUser("Failed to delete product: ${e.message}")
            }
        }
    }

    // Sale Recording
    fun recordSaleTransaction(
        date: String,
        customerName: String,
        orderReference: String,
        notes: String,
        lineDrafts: List<LineItemDraft>,
        onSuccess: () -> Unit = {}
    ) {
        if (lineDrafts.isEmpty()) {
            notifyUser("Please add at least one line item to the sale")
            return
        }

        for (draft in lineDrafts) {
            if (draft.product == null || draft.variant == null) {
                notifyUser("Please select a product and package variant for each item")
                return
            }
            if (draft.quantity < 1) {
                notifyUser("Quantity must be a positive whole number")
                return
            }
            if (draft.unitSellingPrice < 0 || draft.unitCostPrice < 0) {
                notifyUser("Price cannot be negative")
                return
            }
        }

        viewModelScope.launch {
            try {
                var totalGross = 0.0
                var totalNet = 0.0
                var totalCost = 0.0
                var totalProfit = 0.0

                val lineItems = lineDrafts.map { draft ->
                    val gross = draft.grossAmount
                    val net = draft.netRevenue
                    val cost = draft.costOfGoodsSold
                    val profit = draft.grossProfit

                    totalGross += gross
                    totalNet += net
                    totalCost += cost
                    totalProfit += profit

                    val prod = draft.product!!
                    val variant = draft.variant!!

                    // Snapshot data preservation
                    SaleLineItemEntity(
                        saleId = 0,
                        productId = prod.product.id,
                        variantId = variant.id,
                        productName = prod.product.name,
                        variantName = variant.variantName,
                        category = prod.product.category,
                        unitCostPrice = draft.unitCostPrice,
                        unitSellingPrice = draft.unitSellingPrice,
                        quantity = draft.quantity,
                        discount = draft.discount,
                        grossAmount = gross,
                        netRevenue = net,
                        costOfGoodsSold = cost,
                        grossProfit = profit
                    )
                }

                val sale = SaleEntity(
                    saleDate = date,
                    orderReference = orderReference.trim(),
                    customerName = customerName.trim(),
                    notes = notes.trim(),
                    orderDiscount = lineDrafts.sumOf { it.discount },
                    totalGross = totalGross,
                    totalNet = totalNet,
                    totalCost = totalCost,
                    totalGrossProfit = totalProfit
                )

                repository.recordSale(sale, lineItems)
                notifyUser("Sale recorded successfully")
                onSuccess()
            } catch (e: Exception) {
                notifyUser("Failed to save sale: ${e.message}")
            }
        }
    }

    fun deleteSaleTransaction(sale: SaleEntity) {
        viewModelScope.launch {
            try {
                repository.deleteSale(sale)
                notifyUser("Sale transaction deleted")
            } catch (e: Exception) {
                notifyUser("Failed to delete sale: ${e.message}")
            }
        }
    }

    // Orders & Notes Actions
    fun saveOrderNote(
        id: Long = 0,
        date: String,
        orderReference: String,
        customerName: String,
        orderDetails: String,
        notes: String,
        status: String
    ) {
        viewModelScope.launch {
            try {
                val entity = OrderNoteEntity(
                    id = id,
                    orderDate = date,
                    orderReference = orderReference.trim(),
                    customerName = customerName.trim(),
                    orderDetails = orderDetails.trim(),
                    notes = notes.trim(),
                    status = status
                )
                if (id == 0L) {
                    repository.addOrderNote(entity)
                    notifyUser("Order/Note added successfully")
                } else {
                    repository.updateOrderNote(entity)
                    notifyUser("Order/Note updated successfully")
                }
            } catch (e: Exception) {
                notifyUser("Failed to save note: ${e.message}")
            }
        }
    }

    fun updateOrderStatus(note: OrderNoteEntity, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderNote(note.copy(status = newStatus))
                notifyUser("Order status updated to $newStatus")
            } catch (e: Exception) {
                notifyUser("Failed to update status: ${e.message}")
            }
        }
    }

    fun deleteOrderNote(note: OrderNoteEntity) {
        viewModelScope.launch {
            try {
                repository.deleteOrderNote(note)
                notifyUser("Order/Note record deleted")
            } catch (e: Exception) {
                notifyUser("Failed to delete note: ${e.message}")
            }
        }
    }

    // --- BUSINESS REPORTING MODULE ---
    private val _reportPeriodPreset = MutableStateFlow(ReportPeriodPreset.THIS_MONTH)
    val reportPeriodPreset: StateFlow<ReportPeriodPreset> = _reportPeriodPreset.asStateFlow()

    private val initialMonthRange = DateUtils.getThisMonthRangeIso()
    private val _reportStartDate = MutableStateFlow(initialMonthRange.first)
    val reportStartDate: StateFlow<String> = _reportStartDate.asStateFlow()

    private val _reportEndDate = MutableStateFlow(initialMonthRange.second)
    val reportEndDate: StateFlow<String> = _reportEndDate.asStateFlow()

    val businessReport: StateFlow<BusinessReportData> = combine(
        allSales,
        allOrderNotes,
        _reportPeriodPreset,
        _reportStartDate,
        _reportEndDate
    ) { sales, notes, preset, start, end ->
        ReportCalculator.computeReport(
            preset = preset,
            startDateIso = start,
            endDateIso = end,
            salesWithItems = sales,
            orderNotes = notes
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportCalculator.computeReport(
            ReportPeriodPreset.THIS_MONTH,
            initialMonthRange.first,
            initialMonthRange.second,
            emptyList()
        )
    )

    fun setReportPeriodPreset(preset: ReportPeriodPreset) {
        _reportPeriodPreset.value = preset
        when (preset) {
            ReportPeriodPreset.TODAY -> {
                val today = DateUtils.getActualTodayIso()
                _reportStartDate.value = today
                _reportEndDate.value = today
            }
            ReportPeriodPreset.YESTERDAY -> {
                val yesterday = DateUtils.getYesterdayIso()
                _reportStartDate.value = yesterday
                _reportEndDate.value = yesterday
            }
            ReportPeriodPreset.THIS_WEEK -> {
                val (start, end) = DateUtils.getThisWeekRangeIso()
                _reportStartDate.value = start
                _reportEndDate.value = end
            }
            ReportPeriodPreset.THIS_MONTH -> {
                val (start, end) = DateUtils.getThisMonthRangeIso()
                _reportStartDate.value = start
                _reportEndDate.value = end
            }
            ReportPeriodPreset.PREVIOUS_MONTH -> {
                val (start, end) = DateUtils.getPreviousMonthRangeIso()
                _reportStartDate.value = start
                _reportEndDate.value = end
            }
            ReportPeriodPreset.CUSTOM, ReportPeriodPreset.SINGLE_DATE -> {
                // Preserves current custom bounds until explicitly updated
            }
        }
    }

    fun setCustomReportRange(startIso: String, endIso: String) {
        if (!DateUtils.isRangeValid(startIso, endIso)) {
            notifyUser("Start date cannot be after end date")
            return
        }
        _reportPeriodPreset.value = ReportPeriodPreset.CUSTOM
        _reportStartDate.value = startIso
        _reportEndDate.value = endIso
        notifyUser("Report range set: ${DateUtils.formatPeriodRangeDisplay(startIso, endIso)}")
    }

    fun setSingleDateReport(dateIso: String) {
        _reportPeriodPreset.value = ReportPeriodPreset.SINGLE_DATE
        _reportStartDate.value = dateIso
        _reportEndDate.value = dateIso
        notifyUser("Report period set to ${DateUtils.formatIsoToDisplay(dateIso)}")
    }

    fun resetReportFilters() {
        setReportPeriodPreset(ReportPeriodPreset.THIS_MONTH)
        notifyUser("Report period reset to This Month")
    }

    fun exportReportPdf(context: android.content.Context) {
        try {
            val report = businessReport.value
            val file = PdfReportGenerator.generatePdfReport(context, report)
            ReportSharingUtils.sharePdfFile(context, report)
            notifyUser("Exported PDF: ${file.name}")
        } catch (e: Exception) {
            notifyUser("Failed to export PDF: ${e.message}")
        }
    }

    fun exportReportCsv(context: android.content.Context) {
        try {
            val report = businessReport.value
            val file = CsvReportGenerator.generateCsvReport(context, report)
            ReportSharingUtils.shareCsvFile(context, report)
            notifyUser("Exported CSV: ${file.name}")
        } catch (e: Exception) {
            notifyUser("Failed to export CSV: ${e.message}")
        }
    }

    fun printReport(context: android.content.Context) {
        val report = businessReport.value
        ReportPrintHelper.printReport(context, report) { _, msg ->
            notifyUser(msg)
        }
    }

    fun shareReportViaEmail(context: android.content.Context) {
        try {
            val report = businessReport.value
            ReportSharingUtils.shareViaEmail(context, report)
            notifyUser("Opening email share dialog")
        } catch (e: Exception) {
            notifyUser("Failed to share via email: ${e.message}")
        }
    }

    fun shareReportViaWhatsApp(context: android.content.Context) {
        try {
            val report = businessReport.value
            ReportSharingUtils.shareViaWhatsApp(context, report)
            notifyUser("Opening WhatsApp share")
        } catch (e: Exception) {
            notifyUser("Failed to share via WhatsApp: ${e.message}")
        }
    }
}
