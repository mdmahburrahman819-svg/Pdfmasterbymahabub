package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MintBadgeIcon
import com.example.ui.theme.MintSelectedPill
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

enum class MainTab(val title: String) {
    HOME("Home"),
    FILES("Files"),
    TOOLS("Tools"),
    SETTINGS("Settings")
}

@Composable
fun PdfMasterBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    iconSelected = Icons.Filled.Home,
                    iconUnselected = Icons.Outlined.Home,
                    isSelected = selectedTab == MainTab.HOME,
                    onClick = { onTabSelected(MainTab.HOME) },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_home"
                )
                BottomNavItem(
                    label = "Files",
                    iconSelected = Icons.Filled.Folder,
                    iconUnselected = Icons.Outlined.Folder,
                    isSelected = selectedTab == MainTab.FILES,
                    onClick = { onTabSelected(MainTab.FILES) },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_files"
                )
                BottomNavItem(
                    label = "Tools",
                    iconSelected = Icons.Filled.Widgets,
                    iconUnselected = Icons.Outlined.Widgets,
                    isSelected = selectedTab == MainTab.TOOLS,
                    onClick = { onTabSelected(MainTab.TOOLS) },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_tools"
                )
                BottomNavItem(
                    label = "Settings",
                    iconSelected = Icons.Filled.Settings,
                    iconUnselected = Icons.Outlined.Settings,
                    isSelected = selectedTab == MainTab.SETTINGS,
                    onClick = { onTabSelected(MainTab.SETTINGS) },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_settings"
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) MintSelectedPill else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) iconSelected else iconUnselected,
                contentDescription = label,
                tint = if (isSelected) MintBadgeIcon else Slate500,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Slate900 else Slate500,
            maxLines = 1,
            softWrap = false
        )
    }
}
