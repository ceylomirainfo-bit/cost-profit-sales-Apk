package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CeyvanaBottomNavigation
import com.example.ui.components.CeyvanaNavigationRail
import com.example.ui.components.CeyvanaTopBar
import com.example.ui.components.DatePickerModal
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DailySalesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.OrdersNotesScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmIvoryBackground
import com.example.ui.viewmodel.CeyvanaViewModel
import com.example.ui.viewmodel.MainNavigationSection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CeyvanaApp()
            }
        }
    }
}

@Composable
fun CeyvanaApp(
    viewModel: CeyvanaViewModel = viewModel()
) {
    val currentSection by viewModel.currentSection.collectAsState()
    val selectedDate by viewModel.selectedBusinessDate.collectAsState()
    val userNotification by viewModel.userNotification.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Display reactive notifications
    LaunchedEffect(userNotification) {
        userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    // Android Hardware / Gesture Back navigation
    BackHandler(enabled = !isAppLocked && currentSection != MainNavigationSection.DASHBOARD) {
        viewModel.navigateTo(MainNavigationSection.DASHBOARD)
    }

    if (isAppLocked) {
        LockScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletOrExpanded = maxWidth >= 720.dp

            if (isTabletOrExpanded) {
                // Tablet / Desktop layout with sidebar NavigationRail
                Row(modifier = Modifier.fillMaxSize().background(WarmIvoryBackground)) {
                    CeyvanaNavigationRail(
                        currentSection = currentSection,
                        onSectionSelected = { section -> viewModel.navigateTo(section) },
                        onLockClick = { viewModel.lockApp() },
                        onLogoutClick = { viewModel.logout() }
                    )

                    Scaffold(
                        topBar = {
                            CeyvanaTopBar(
                                selectedDate = selectedDate,
                                onDateClick = { showDatePickerDialog = true },
                                onTodayClick = { viewModel.setDateToToday() },
                                onLockClick = { viewModel.lockApp() },
                                onLogoutClick = { viewModel.logout() }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = WarmIvoryBackground,
                        modifier = Modifier.weight(1f)
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            AppScreenContent(
                                section = currentSection,
                                viewModel = viewModel,
                                onOpenDatePicker = { showDatePickerDialog = true }
                            )
                        }
                    }
                }
            } else {
                // Handheld mobile layout with BottomNavigationBar
                Scaffold(
                    topBar = {
                        CeyvanaTopBar(
                            selectedDate = selectedDate,
                            onDateClick = { showDatePickerDialog = true },
                            onTodayClick = { viewModel.setDateToToday() },
                            onLockClick = { viewModel.lockApp() },
                            onLogoutClick = { viewModel.logout() }
                        )
                    },
                    bottomBar = {
                        CeyvanaBottomNavigation(
                            currentSection = currentSection,
                            onSectionSelected = { section -> viewModel.navigateTo(section) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = WarmIvoryBackground,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppScreenContent(
                            section = currentSection,
                            viewModel = viewModel,
                            onOpenDatePicker = { showDatePickerDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Global Date Picker Modal
    if (showDatePickerDialog) {
        DatePickerModal(
            initialDate = selectedDate,
            onDateSelected = { newDate ->
                viewModel.setSelectedDate(newDate)
                showDatePickerDialog = false
            },
            onDismissRequest = { showDatePickerDialog = false }
        )
    }
}

@Composable
private fun AppScreenContent(
    section: MainNavigationSection,
    viewModel: CeyvanaViewModel,
    onOpenDatePicker: () -> Unit
) {
    when (section) {
        MainNavigationSection.DASHBOARD -> {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSection = { newSec -> viewModel.navigateTo(newSec) },
                onOpenDatePicker = onOpenDatePicker
            )
        }
        MainNavigationSection.PRODUCTS -> {
            ProductsScreen(viewModel = viewModel)
        }
        MainNavigationSection.DAILY_SALES -> {
            DailySalesScreen(
                viewModel = viewModel,
                onOpenDatePicker = onOpenDatePicker
            )
        }
        MainNavigationSection.REPORTS -> {
            ReportsScreen(viewModel = viewModel)
        }
        MainNavigationSection.CALENDAR -> {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToSection = { newSec -> viewModel.navigateTo(newSec) }
            )
        }
        MainNavigationSection.ORDERS_NOTES -> {
            OrdersNotesScreen(viewModel = viewModel)
        }
    }
}
