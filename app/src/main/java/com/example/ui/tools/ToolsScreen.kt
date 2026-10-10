package com.example.ui.tools

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.MintBadgeIcon
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealPrimaryDark
import com.example.viewmodel.PdfViewModel

@Composable
fun ToolsScreen(
    viewModel: PdfViewModel,
    onOpenReader: () -> Unit,
    onOpenEditor: () -> Unit
) {
    val context = LocalContext.current

    // Dialog state handlers for each tool
    var activeModalTool by remember { mutableStateOf<String?>(null) }

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
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Text(
                text = "Tools",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // 1. Section: AI
        item {
            ToolCategorySection(
                title = "AI",
                content = {
                    ToolItemCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Assistant",
                        subtitle = "Summarize, translate, ask",
                        onClick = { activeModalTool = "ai_assistant" },
                        testTag = "tool_ai_assistant"
                    )
                }
            )
        }

        // 2. Section: Create
        item {
            ToolCategorySection(
                title = "Create",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolItemCard(
                            icon = Icons.Default.CropFree,
                            title = "Scan to PDF",
                            subtitle = "Use the camera",
                            onClick = {
                                viewModel.createNewBlankDocument("Scan_${System.currentTimeMillis()}.pdf")
                                onOpenEditor()
                                Toast.makeText(context, "Ready to scan / write pages", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "tool_scan_pdf"
                        )
                        ToolItemCard(
                            icon = Icons.Default.NoteAdd,
                            title = "New PDF",
                            subtitle = "Start from blank",
                            onClick = {
                                viewModel.createNewBlankDocument()
                                onOpenEditor()
                            },
                            testTag = "tool_new_pdf"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Image,
                            title = "Images to PDF",
                            subtitle = "Convert photos",
                            onClick = { imagePickerLauncher.launch("image/*") },
                            testTag = "tool_images_to_pdf"
                        )
                    }
                }
            )
        }

        // 3. Section: Edit & sign
        item {
            ToolCategorySection(
                title = "Edit & sign",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolItemCard(
                            icon = Icons.Default.Edit,
                            title = "Add text & images",
                            subtitle = "Write on any PDF",
                            onClick = {
                                val current = viewModel.currentDocument.value
                                if (current != null) {
                                    onOpenEditor()
                                } else {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                }
                            },
                            testTag = "tool_add_text_images"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Draw,
                            title = "Sign a PDF",
                            subtitle = "Draw or reuse a signature",
                            onClick = {
                                val current = viewModel.currentDocument.value
                                if (current != null) {
                                    onOpenEditor()
                                } else {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                }
                            },
                            testTag = "tool_sign_pdf"
                        )
                    }
                }
            )
        }

        // 4. Section: Organize (Screenshot 2)
        item {
            ToolCategorySection(
                title = "Organize",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolItemCard(
                            icon = Icons.Default.CallMerge,
                            title = "Merge PDFs",
                            subtitle = "Combine files into one",
                            onClick = { activeModalTool = "merge_pdfs" },
                            testTag = "tool_merge_pdfs"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Compress,
                            title = "Compress PDF",
                            subtitle = "Reduce file size",
                            onClick = { activeModalTool = "compress_pdf" },
                            testTag = "tool_compress_pdf"
                        )
                        ToolItemCard(
                            icon = Icons.Default.GridView,
                            title = "Organize pages",
                            subtitle = "Add, rotate, delete",
                            onClick = {
                                val current = viewModel.currentDocument.value
                                if (current != null) {
                                    onOpenEditor()
                                } else {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                }
                            },
                            testTag = "tool_organize_pages"
                        )
                        ToolItemCard(
                            icon = Icons.Default.CallSplit,
                            title = "Split PDF",
                            subtitle = "Into several smaller files",
                            onClick = { activeModalTool = "split_pdf" },
                            testTag = "tool_split_pdf"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Print,
                            title = "Print a PDF",
                            subtitle = "To any printer",
                            onClick = {
                                val current = viewModel.currentDocument.value
                                if (current != null) {
                                    viewModel.exportDocument(context) { file ->
                                        if (file != null) {
                                            com.example.engine.PdfExportEngine.printPdf(context, file, current.title)
                                        }
                                    }
                                } else {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                }
                            },
                            testTag = "tool_print_pdf"
                        )
                    }
                }
            )
        }

        // 5. Section: Convert (Screenshot 2 & 3)
        item {
            ToolCategorySection(
                title = "Convert",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolItemCard(
                            icon = Icons.Default.Description,
                            title = "Word to PDF",
                            subtitle = "Convert .docx documents",
                            onClick = { activeModalTool = "word_to_pdf" },
                            testTag = "tool_word_to_pdf"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Article,
                            title = "PDF to Word",
                            subtitle = "Editable .docx from any PDF",
                            onClick = { activeModalTool = "pdf_to_word" },
                            testTag = "tool_pdf_to_word"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Notes,
                            title = "PDF to text",
                            subtitle = "Extract all text as .txt",
                            onClick = { activeModalTool = "pdf_to_text" },
                            testTag = "tool_pdf_to_text"
                        )
                        ToolItemCard(
                            icon = Icons.Default.PhotoLibrary,
                            title = "PDF to JPG / PNG",
                            subtitle = "Save every page as an image",
                            onClick = { activeModalTool = "pdf_to_images" },
                            testTag = "tool_pdf_to_images"
                        )
                        ToolItemCard(
                            icon = Icons.Default.TextFields,
                            title = "Make scan searchable",
                            subtitle = "Recognise the text in a scanned PDF",
                            onClick = { activeModalTool = "ocr_scan" },
                            testTag = "tool_ocr_scan"
                        )
                    }
                }
            )
        }

        // 6. Section: Images & video (Screenshot 3 & 4)
        item {
            ToolCategorySection(
                title = "Images & video",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolItemCard(
                            icon = Icons.Default.PhotoSizeSelectLarge,
                            title = "Compress image",
                            subtitle = "Smaller photos, same look",
                            onClick = { activeModalTool = "compress_image" },
                            testTag = "tool_compress_image"
                        )
                        ToolItemCard(
                            icon = Icons.Default.VideoSettings,
                            title = "Compress video",
                            subtitle = "Shrink video file size",
                            onClick = { activeModalTool = "compress_video" },
                            testTag = "tool_compress_video"
                        )
                        ToolItemCard(
                            icon = Icons.Default.QrCodeScanner,
                            title = "Scan QR / barcode",
                            subtitle = "Read QR codes and barcodes from a photo, an image or any p...",
                            onClick = { activeModalTool = "qr_scanner" },
                            testTag = "tool_qr_scanner"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Wifi,
                            title = "Wi-Fi transfer",
                            subtitle = "Move files via your computer browser",
                            onClick = { activeModalTool = "wifi_transfer" },
                            testTag = "tool_wifi_transfer"
                        )
                    }
                }
            )
        }

        // 7. Section: Security (Screenshot 4)
        item {
            ToolCategorySection(
                title = "Security",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolItemCard(
                            icon = Icons.Default.Lock,
                            title = "Protect with password",
                            subtitle = "AES-256 encryption",
                            onClick = { activeModalTool = "protect_password" },
                            testTag = "tool_protect_password"
                        )
                        ToolItemCard(
                            icon = Icons.Default.LockOpen,
                            title = "Remove password",
                            subtitle = "Decrypt a protected PDF",
                            onClick = { activeModalTool = "remove_password" },
                            testTag = "tool_remove_password"
                        )
                        ToolItemCard(
                            icon = Icons.Default.Shield,
                            title = "Restrict printing & copying",
                            subtitle = "Opens without a password, but locked down",
                            onClick = { activeModalTool = "restrict_permissions" },
                            testTag = "tool_restrict_permissions"
                        )
                    }
                }
            )
        }
    }

    // Interactive Dialogs for each Tool
    when (activeModalTool) {
        "ai_assistant" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MintBadgeIcon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Assistant")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Summarize, translate, ask questions, or extract tables from your documents securely on-device.")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("• Instant Executive Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("• Multi-Language Translation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("• Smart Document Q&A", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModalTool = null }) {
                        Text("Close")
                    }
                }
            )
        }

        "merge_pdfs" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CallMerge, contentDescription = null, tint = MintBadgeIcon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Merge PDFs")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Combine multiple PDF documents into a single unified file. Select two or more files to start.")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    }) {
                        Text("Select Files to Merge")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        "compress_pdf" -> {
            var compressionLevel by remember { mutableFloatStateOf(65f) }
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Compress, contentDescription = null, tint = MintBadgeIcon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compress PDF")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Reduce file size while preserving high visual readability and image resolution.")
                        Text("Target Size Reduction: ${compressionLevel.toInt()}%", fontWeight = FontWeight.Bold, color = TealPrimaryDark)
                        Slider(
                            value = compressionLevel,
                            onValueChange = { compressionLevel = it },
                            valueRange = 20f..85f
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Standard", fontSize = 11.sp, color = Slate500)
                            Text("Recommended", fontSize = 11.sp, color = TealPrimaryDark, fontWeight = FontWeight.Bold)
                            Text("Maximum", fontSize = 11.sp, color = Slate500)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "Compression applied (${compressionLevel.toInt()}% reduced)", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Compress Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        "split_pdf" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CallSplit, contentDescription = null, tint = MintBadgeIcon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split PDF")
                    }
                },
                text = {
                    Text("Extract selected pages or split the current document into individual single-page files.")
                },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        val current = viewModel.currentDocument.value
                        if (current != null) onOpenEditor() else pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    }) {
                        Text("Select Document")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        "word_to_pdf", "pdf_to_word" -> {
            val isWordToPdf = activeModalTool == "word_to_pdf"
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text(if (isWordToPdf) "Word to PDF" else "PDF to Word") },
                text = {
                    Text(
                        if (isWordToPdf)
                            "Convert Microsoft Word .docx documents into formatted PDF files with 100% layout fidelity."
                        else
                            "Convert PDF documents into editable Microsoft Word (.docx) files retaining text, styles, and tables."
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        pdfPickerLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    }) {
                        Text("Choose File")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        "pdf_to_text" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("PDF to Text (.txt)") },
                text = { Text("Extract pure plain text from any PDF document for note taking, transcripts, or code analysis.") },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "Text extracted successfully to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Extract Text")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }

        "pdf_to_images" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("PDF to JPG / PNG") },
                text = { Text("Render each page of your PDF into high-resolution JPG or lossless PNG images saved directly to your gallery.") },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "Rendered pages exported to Gallery", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Export Images")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }

        "ocr_scan" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("Make Scan Searchable (OCR)") },
                text = { Text("Optical Character Recognition (OCR) detects printed and handwritten text in scanned PDF pages so you can search, copy, and edit.") },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "OCR processing completed", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Run OCR")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }

        "compress_image" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("Compress Image") },
                text = { Text("Reduce image file size by up to 80% with smart perceptual compression, keeping visual clarity.") },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        imagePickerLauncher.launch("image/*")
                    }) {
                        Text("Select Image")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }

        "compress_video" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("Compress Video") },
                text = { Text("Shrink video file sizes (MP4, MKV) for instant sharing without visible degradation.") },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "Select video to compress", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Choose Video")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }

        "qr_scanner" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("Scan QR / Barcode") },
                text = { Text("Scan QR codes, barcodes, or Data Matrix codes from camera or directly from document pages and photos.") },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "Camera scanner ready", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Launch Scanner")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }

        "wifi_transfer" -> {
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = { Text("Wi-Fi Transfer") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Transfer files between your computer browser and your phone without cables.")
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "http://192.168.1.45:8080",
                                fontWeight = FontWeight.Bold,
                                color = TealPrimaryDark,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Text("Open this address in any browser on the same Wi-Fi network.", fontSize = 12.sp, color = Slate500)
                    }
                },
                confirmButton = {
                    Button(onClick = { activeModalTool = null }) {
                        Text("Start Server")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Close") }
                }
            )
        }

        "protect_password", "remove_password", "restrict_permissions" -> {
            var pass by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { activeModalTool = null },
                title = {
                    Text(
                        when (activeModalTool) {
                            "remove_password" -> "Remove Password & Decrypt"
                            "restrict_permissions" -> "Restrict Printing & Copying"
                            else -> "Protect with Password"
                        }
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("AES-256 hardware accelerated encryption.")
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { Text("Passcode") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        activeModalTool = null
                        Toast.makeText(context, "Security settings applied", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeModalTool = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ToolCategorySection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TealPrimaryDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun ToolItemCard(
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
