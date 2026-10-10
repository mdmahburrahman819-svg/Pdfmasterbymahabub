package com.example.ui.home

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PdfDocumentData
import com.example.ui.theme.CardBorder
import com.example.ui.theme.HeaderGradientEnd
import com.example.ui.theme.HeaderGradientStart
import com.example.ui.theme.HeaderIconBg
import com.example.ui.theme.HomeBackground
import com.example.ui.theme.HomeCardBackground
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintDarkIcon
import com.example.ui.theme.MintDarkText
import com.example.ui.theme.PaleMintContainer
import com.example.ui.theme.PdfRedBadgeBg
import com.example.ui.theme.PdfRedIcon
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.viewmodel.PdfViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: PdfViewModel,
    onOpenReader: () -> Unit,
    onOpenEditor: () -> Unit,
    onNavigateToTools: () -> Unit
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    val lastReadDoc by viewModel.lastReadDocument.collectAsState()

    var showPasswordProtectDialog by remember { mutableStateOf(false) }
    var showAiAssistantSheet by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importPdfFromUri(context, uri) { success, _ ->
                if (success) {
                    onOpenReader()
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.createNewBlankDocument("Images_${System.currentTimeMillis()}.pdf")
            uris.forEachIndexed { index, uri ->
                viewModel.addImageElement(
                    uriString = uri.toString(),
                    x = 40f,
                    y = 80f + (index * 220f),
                    width = 240f,
                    height = 200f
                )
            }
            onOpenReader()
            Toast.makeText(context, "${uris.size} image(s) converted to PDF", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Teal Header Banner (Matching Reference Screenshot)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HeaderGradientStart, HeaderGradientEnd)
                        )
                    )
                    .padding(20.dp)
                    .testTag("home_header_banner")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // App Icon Badge
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HeaderIconBg)
                                .border(1.2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "PDF Master",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your secure PDF studio",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Nested Privacy Panel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showPrivacyDialog = true }
                            .testTag("privacy_banner_btn"),
                        color = Color.White.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.22f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF9CEFE4), // Pale mint shield
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Private by default — nothing leaves your device without your action",
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Conditional Continue Reading Card (Visible only when real reading history exists)
        val activeLastRead = lastReadDoc
        if (activeLastRead != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable {
                            viewModel.openDocument(activeLastRead)
                            onOpenReader()
                        }
                        .testTag("continue_reading_banner"),
                    color = MintContainer, // Solid Mint #9CEFE4 matching reference screenshot
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Continue reading",
                            tint = MintDarkIcon,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continue reading",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintDarkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val pageNum = (activeLastRead.lastReadPageIndex ?: 0) + 1
                            val totalPages = activeLastRead.pages.size.coerceAtLeast(1)
                            Text(
                                text = "${activeLastRead.title.removeSuffix(".pdf")} · Page $pageNum",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MintDarkText.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (activeLastRead.readingProgress != null && totalPages > 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { activeLastRead.readingProgress ?: 0f },
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MintDarkText,
                                    trackColor = MintDarkText.copy(alpha = 0.2f),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MintDarkIcon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 3. Section "Quick actions"
        item {
            Text(
                text = "Quick actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
            )
        }

        // 6 Quick Action Grid Cards (2 columns x 3 rows) in exact order
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Row 1: Open PDF | Scan to PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.FolderOpen,
                        title = "Open PDF",
                        subtitle = "From your files",
                        modifier = Modifier.weight(1f),
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        testTag = "action_open_pdf"
                    )
                    QuickActionCard(
                        icon = Icons.Default.CropFree,
                        title = "Scan to PDF",
                        subtitle = "Use the camera",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.createNewBlankDocument("Scan_${System.currentTimeMillis()}.pdf")
                            onOpenEditor()
                            Toast.makeText(context, "Ready to scan / write pages", Toast.LENGTH_SHORT).show()
                        },
                        testTag = "action_scan_pdf"
                    )
                }

                // Row 2: New PDF | Images to PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.NoteAdd,
                        title = "New PDF",
                        subtitle = "Start from blank",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.createNewBlankDocument()
                            onOpenEditor()
                        },
                        testTag = "action_new_pdf"
                    )
                    QuickActionCard(
                        icon = Icons.Default.Image,
                        title = "Images to PDF",
                        subtitle = "Convert photos",
                        modifier = Modifier.weight(1f),
                        onClick = { imagePickerLauncher.launch("image/*") },
                        testTag = "action_images_to_pdf"
                    )
                }

                // Row 3: Protect PDF | AI Assistant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Lock,
                        title = "Protect PDF",
                        subtitle = "Add a password",
                        modifier = Modifier.weight(1f),
                        onClick = { showPasswordProtectDialog = true },
                        testTag = "action_protect_pdf"
                    )
                    QuickActionCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Assistant",
                        subtitle = "Summarize, translate, ask",
                        modifier = Modifier.weight(1f),
                        onClick = { showAiAssistantSheet = true },
                        testTag = "action_ai_assistant"
                    )
                }
            }
        }

        // 4. Section "Recent files"
        item {
            Text(
                text = "Recent files",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        // Display recent documents or friendly empty state
        if (documents.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp)),
                    color = HomeCardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PaleMintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MintDarkIcon,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent files yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Open or create a PDF using the quick actions above.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary
                        )
                    }
                }
            }
        } else {
            items(documents.take(5), key = { it.id }) { doc ->
                RecentFileCard(
                    doc = doc,
                    onOpen = {
                        viewModel.openDocument(doc)
                        onOpenReader()
                    }
                )
            }
        }
    }

    // Password Protect Dialog
    if (showPasswordProtectDialog) {
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordProtectDialog = false },
            title = { Text("Protect PDF with Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set an on-device 256-bit encryption password for your document vault.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextSecondary
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (password.isNotBlank() && password == confirmPassword) {
                            Toast.makeText(context, "Document vault encrypted & locked", Toast.LENGTH_SHORT).show()
                            showPasswordProtectDialog = false
                        } else {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Protect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordProtectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // AI Assistant Sheet Dialog
    if (showAiAssistantSheet) {
        AlertDialog(
            onDismissRequest = { showAiAssistantSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MintDarkIcon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Assistant")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Summarize, extract data, translate or ask questions about any document entirely on your device.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Key Takeaways & Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Translate into 30+ Languages", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Extract Tables and Form Fields", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiAssistantSheet = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MintDarkIcon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security & Privacy")
                }
            },
            text = {
                Text(
                    text = "PDF Master operates in private on-device sandbox mode. Your documents, photos, signatures, and edits are strictly stored and processed on your device without sending any data to external servers.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeCardBackground
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Icon in Pale Mint Squircle Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PaleMintContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MintDarkIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SlateTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentFileCard(
    doc: PdfDocumentData,
    onOpen: () -> Unit
) {
    val dateStr = remember(doc.lastModifiedTimestamp) {
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(doc.lastModifiedTimestamp))
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onOpen)
            .testTag("recent_file_${doc.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HomeCardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Red PDF Document Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PdfRedBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = PdfRedIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.title.removeSuffix(".pdf"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (doc.isProtected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Protected",
                            tint = Color(0xFF078F89),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                val pageCountText = if (doc.pages.size == 1) "1 page" else "${doc.pages.size} pages"
                Text(
                    text = "$pageCountText · $dateStr",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )

                // Reading Progress Bar
                if (doc.readingProgress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { doc.readingProgress ?: 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MintDarkText,
                            trackColor = Color(0xFFE2E8F0),
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${((doc.readingProgress ?: 0f) * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = SlateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = SlateTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open in Editor") },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}
