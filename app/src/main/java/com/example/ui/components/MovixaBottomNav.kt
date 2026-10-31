package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MovixaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AppNavTab

@Composable
fun MovixaBottomNav(
    currentTab: AppNavTab,
    downloadsCount: Int,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground.copy(alpha = 0.85f),
                        DarkBackground
                    )
                )
            )
            .navigationBarsPadding()
            .padding(vertical = 6.dp)
            .testTag("movixa_bottom_nav"),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavTabItem(
            tab = AppNavTab.HOME,
            selected = currentTab == AppNavTab.HOME,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            onClick = { onTabSelected(AppNavTab.HOME) }
        )
        NavTabItem(
            tab = AppNavTab.SEARCH,
            selected = currentTab == AppNavTab.SEARCH,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            onClick = { onTabSelected(AppNavTab.SEARCH) }
        )
        NavTabItem(
            tab = AppNavTab.NEW_POPULAR,
            selected = currentTab == AppNavTab.NEW_POPULAR,
            selectedIcon = Icons.Filled.Whatshot,
            unselectedIcon = Icons.Outlined.Whatshot,
            onClick = { onTabSelected(AppNavTab.NEW_POPULAR) }
        )
        NavTabItem(
            tab = AppNavTab.DOWNLOADS,
            selected = currentTab == AppNavTab.DOWNLOADS,
            selectedIcon = Icons.Filled.Download,
            unselectedIcon = Icons.Outlined.Download,
            badgeCount = downloadsCount,
            onClick = { onTabSelected(AppNavTab.DOWNLOADS) }
        )
        NavTabItem(
            tab = AppNavTab.PROFILE,
            selected = currentTab == AppNavTab.PROFILE,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            onClick = { onTabSelected(AppNavTab.PROFILE) }
        )
    }
}

@Composable
private fun NavTabItem(
    tab: AppNavTab,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("nav_tab_${tab.name.lowercase()}")
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = MovixaRed) {
                        Text(text = "$badgeCount", color = Color.White, fontSize = 10.sp)
                    }
                }
            ) {
                Icon(
                    imageVector = if (selected) selectedIcon else unselectedIcon,
                    contentDescription = tab.title,
                    tint = if (selected) MovixaRed else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = tab.title,
                tint = if (selected) MovixaRed else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = tab.title,
            color = if (selected) TextPrimary else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
