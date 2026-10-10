package com.example.ui.settings

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.MintBadgeIcon
import com.example.ui.theme.MintSelectedPill
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealPrimaryDark

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    val context = LocalContext.current

    var appLockEnabled by remember { mutableStateOf(true) }
    var cloudBackupEnabled by remember { mutableStateOf(false) }
    var marketingEnabled by remember { mutableStateOf(true) }

    var showClearStorageDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Appearance Card (Segmented Button: System | Light | Dark)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_appearance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = TealPrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Segmented Toggle Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SegmentedThemeButton(
                            label = "System",
                            icon = Icons.Outlined.SettingsBrightness,
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeModeChanged(ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedThemeButton(
                            label = "Light",
                            icon = Icons.Outlined.LightMode,
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { onThemeModeChanged(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedThemeButton(
                            label = "Dark",
                            icon = Icons.Outlined.DarkMode,
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            onClick = { onThemeModeChanged(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. App Lock Card
        item {
            SettingsToggleCard(
                icon = Icons.Default.Fingerprint,
                title = "App lock",
                subtitle = "Require Face ID, fingerprint or your device PIN to open the app",
                isChecked = appLockEnabled,
                onCheckedChange = { appLockEnabled = it },
                testTag = "settings_app_lock"
            )
        }

        // 3. Security & privacy Card
        item {
            SettingsNavigationCard(
                icon = Icons.Default.Security,
                title = "Security & privacy",
                subtitle = "Private by default — nothing leaves your device",
                onClick = { showSecurityDialog = true },
                testTag = "settings_security_privacy"
            )
        }

        // 4. Back up to the cloud Card
        item {
            SettingsToggleCard(
                icon = Icons.Default.CloudUpload,
                title = "Back up to the cloud",
                subtitle = "Include documents in your device backup",
                isChecked = cloudBackupEnabled,
                onCheckedChange = { cloudBackupEnabled = it },
                testTag = "settings_cloud_backup"
            )
        }

        // 5. Storage Card (with Broom action icon)
        item {
            SettingsActionCard(
                icon = Icons.Default.FormatListBulleted,
                title = "Storage",
                subtitle = "Documents: 32.3 MB · Temporary: 585.4 MB",
                actionIcon = Icons.Default.CleaningServices,
                onActionClick = { showClearStorageDialog = true },
                testTag = "settings_storage"
            )
        }

        // 6. Back up documents Card (with Share ZIP action icon)
        item {
            SettingsActionCard(
                icon = Icons.Default.CloudUpload,
                title = "Back up documents",
                subtitle = "Share your whole library as one ZIP",
                actionIcon = Icons.Default.Share,
                onActionClick = {
                    Toast.makeText(context, "Preparing documents archive ZIP...", Toast.LENGTH_SHORT).show()
                },
                testTag = "settings_backup_zip"
            )
        }

        // 7. Marketing measurement Card
        item {
            SettingsToggleCard(
                icon = Icons.Default.TrendingUp,
                title = "Marketing measurement",
                subtitle = "Campaign and usage data are on",
                isChecked = marketingEnabled,
                onCheckedChange = { marketingEnabled = it },
                testTag = "settings_marketing"
            )
        }

        // 8. Share PDF Master
        item {
            SettingsNavigationCard(
                icon = Icons.Default.Share,
                title = "Share PDF Master",
                subtitle = "Send someone a link to the app",
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out PDF Master — Your secure on-device PDF studio!")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share PDF Master"))
                },
                testTag = "settings_share_app"
            )
        }

        // 9. Rate PDF Master
        item {
            SettingsNavigationCard(
                icon = Icons.Default.Star,
                title = "Rate PDF Master",
                subtitle = "A rating helps other people find the app",
                onClick = {
                    Toast.makeText(context, "Thank you for rating PDF Master 5 stars!", Toast.LENGTH_SHORT).show()
                },
                testTag = "settings_rate_app"
            )
        }

        // 10. Contact us
        item {
            SettingsNavigationCard(
                icon = Icons.Default.Email,
                title = "Contact us",
                subtitle = "Email our support team",
                onClick = { showContactDialog = true },
                testTag = "settings_contact_us"
            )
        }

        // 11. Version Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_version_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Slate900,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Version",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "1.1.0 (4)",
                        fontSize = 14.sp,
                        color = Slate500
                    )
                }
            }
        }
    }

    if (showClearStorageDialog) {
        AlertDialog(
            onDismissRequest = { showClearStorageDialog = false },
            title = { Text("Clean Temporary Storage") },
            text = { Text("Clear 585.4 MB of cached rendering data? Your original documents and edits will remain safe.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "Temporary cache cleared (585.4 MB freed)", Toast.LENGTH_SHORT).show()
                        showClearStorageDialog = false
                    }
                ) {
                    Text("Clean")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearStorageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = { Text("Security & Privacy") },
            text = { Text("PDF Master guarantees zero cloud tracking by default. All OCR, rendering, signatures, and file encryption occur in an isolated sandbox on your hardware.") },
            confirmButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact Support") },
            text = { Text("Our product engineering team is available 24/7 at support@pdfmaster.app.") },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SegmentedThemeButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MintSelectedPill else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = TealPrimaryDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TealPrimaryDark else Slate900
            )
        }
    }
}

@Composable
fun SettingsToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MintBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MintBadgeIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Slate500,
                    lineHeight = 16.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TealPrimaryDark
                )
            )
        }
    }
}

@Composable
fun SettingsNavigationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MintBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MintBadgeIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Slate500,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionIcon: ImageVector,
    onActionClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MintBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MintBadgeIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Slate500,
                    lineHeight = 16.sp
                )
            }

            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
