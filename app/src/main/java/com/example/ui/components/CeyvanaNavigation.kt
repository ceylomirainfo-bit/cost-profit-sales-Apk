package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MutedGoldSecondary
import com.example.ui.viewmodel.MainNavigationSection

data class NavigationTabItem(
    val section: MainNavigationSection,
    val icon: ImageVector,
    val tag: String
)

val navigationItems = listOf(
    NavigationTabItem(MainNavigationSection.DASHBOARD, Icons.Default.Dashboard, "nav_dashboard"),
    NavigationTabItem(MainNavigationSection.PRODUCTS, Icons.Default.Category, "nav_products"),
    NavigationTabItem(MainNavigationSection.DAILY_SALES, Icons.Default.ReceiptLong, "nav_daily_sales"),
    NavigationTabItem(MainNavigationSection.REPORTS, Icons.Default.Assessment, "nav_reports"),
    NavigationTabItem(MainNavigationSection.CALENDAR, Icons.Default.CalendarMonth, "nav_calendar"),
    NavigationTabItem(MainNavigationSection.ORDERS_NOTES, Icons.AutoMirrored.Filled.Assignment, "nav_orders_notes")
)

@Composable
fun CeyvanaBottomNavigation(
    currentSection: MainNavigationSection,
    onSectionSelected: (MainNavigationSection) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = ForestGreenPrimary,
        tonalElevation = 8.dp,
        modifier = modifier.navigationBarsPadding()
    ) {
        navigationItems.forEach { item ->
            val selected = (currentSection == item.section)
            NavigationBarItem(
                selected = selected,
                onClick = { onSectionSelected(item.section) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.section.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.section.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForestGreenPrimary,
                    selectedTextColor = ForestGreenPrimary,
                    indicatorColor = ForestGreenContainer,
                    unselectedIconColor = Color(0xFF6B726C),
                    unselectedTextColor = Color(0xFF6B726C)
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}

@Composable
fun CeyvanaNavigationRail(
    currentSection: MainNavigationSection,
    onSectionSelected: (MainNavigationSection) -> Unit,
    onLockClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = Color.White,
        contentColor = ForestGreenPrimary,
        modifier = modifier
    ) {
        navigationItems.forEach { item ->
            val selected = (currentSection == item.section)
            NavigationRailItem(
                selected = selected,
                onClick = { onSectionSelected(item.section) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.section.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.section.title,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = ForestGreenPrimary,
                    selectedTextColor = ForestGreenPrimary,
                    indicatorColor = ForestGreenContainer,
                    unselectedIconColor = Color(0xFF6B726C),
                    unselectedTextColor = Color(0xFF6B726C)
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = false,
            onClick = onLockClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Dashboard",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Lock",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = NavigationRailItemDefaults.colors(
                unselectedIconColor = ForestGreenPrimary,
                unselectedTextColor = ForestGreenPrimary
            ),
            modifier = Modifier.testTag("rail_lock_button")
        )

        NavigationRailItem(
            selected = false,
            onClick = onLogoutClick,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Sign Out",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = NavigationRailItemDefaults.colors(
                unselectedIconColor = Color(0xFF8D4024),
                unselectedTextColor = Color(0xFF8D4024)
            ),
            modifier = Modifier.testTag("rail_logout_button")
        )
    }
}
