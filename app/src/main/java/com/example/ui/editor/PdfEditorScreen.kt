package com.example.ui.editor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.PdfAnnotationStroke
import com.example.model.PdfImageElement
import com.example.model.PdfPageData
import com.example.model.PdfShapeElement
import com.example.model.PdfTextElement
import com.example.model.ShapeType
import com.example.model.StrokePoint
import com.example.model.TextAlignmentType
import com.example.viewmodel.EditorTool
import com.example.viewmodel.PdfViewModel
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfEditorScreen(
    viewModel: PdfViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentDoc by viewModel.currentDocument.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val selectedTextId by viewModel.selectedTextId.collectAsState()
    val selectedImageId by viewModel.selectedImageId.collectAsState()
    val selectedShapeId by viewModel.selectedShapeId.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    // Dialog & Sheet States
    var showTextInspector by remember { mutableStateOf<PdfTextElement?>(null) }
    var showImageInspector by remember { mutableStateOf<PdfImageElement?>(null) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showUnsplashPicker by remember { mutableStateOf(false) }
    var showPageManager by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showStampsMenu by remember { mutableStateOf(false) }
    var showAddImageMenu by remember { mutableStateOf(false) }

    // Photo picker for images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val inspectingImg = showImageInspector
            if (inspectingImg != null) {
                // Replace existing image
                viewModel.updateImageElement(inspectingImg.copy(uriString = uri.toString(), localFilePath = null, drawableResName = null))
                showImageInspector = null
                Toast.makeText(context, "Image replaced", Toast.LENGTH_SHORT).show()
            } else {
                // Add new image to page
                viewModel.addImageElement(uriString = uri.toString())
                Toast.makeText(context, "Image added to page", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    val doc = currentDoc
    if (doc == null || currentPageIndex !in doc.pages.indices) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No document loaded")
        }
        return
    }

    val page = doc.pages[currentPageIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .clickable { showRenameDialog = true }
                            .testTag("document_title_header")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(
                        onClick = { showPageManager = true },
                        modifier = Modifier.testTag("open_page_manager_btn")
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Manage Pages")
                    }
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_menu_btn")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export & Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            EditorBottomToolbar(
                activeTool = activeTool,
                onToolSelect = { tool ->
                    when (tool) {
                        EditorTool.SIGNATURE -> showSignatureDialog = true
                        EditorTool.ADD_IMAGE -> showAddImageMenu = true
                        EditorTool.ADD_SHAPE -> showStampsMenu = true
                        else -> viewModel.setTool(tool)
                    }
                },
                onAddStickyNote = {
                    viewModel.addTextElement(
                        text = "📌 Note: Add comment here",
                        x = page.width * 0.25f,
                        y = page.height * 0.25f,
                        fontSize = 15f,
                        color = 0xFFB45309L
                    )
                    Toast.makeText(context, "Sticky note added to page", Toast.LENGTH_SHORT).show()
                },
                onAddDateTime = {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val currentDateStr = dateFormat.format(java.util.Date())
                    viewModel.addTextElement(
                        text = currentDateStr,
                        x = page.width * 0.35f,
                        y = page.height * 0.15f,
                        fontSize = 15f
                    )
                    Toast.makeText(context, "Date placed on page", Toast.LENGTH_SHORT).show()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFE2E8F0)) // Canvas workspace neutral background
        ) {
            // Interactive Document Canvas
            InteractivePdfCanvas(
                page = page,
                activeTool = activeTool,
                selectedTextId = selectedTextId,
                selectedImageId = selectedImageId,
                selectedShapeId = selectedShapeId,
                onSelectText = { viewModel.selectText(it) },
                onSelectImage = { viewModel.selectImage(it) },
                onSelectShape = { viewModel.selectShape(it) },
                onClearSelection = { viewModel.clearSelection() },
                onUpdateText = { viewModel.updateTextElement(it) },
                onUpdateImage = { viewModel.updateImageElement(it) },
                onUpdateShape = { viewModel.updateShapeElement(it) },
                onAddText = { x, y ->
                    viewModel.addTextElement(
                        text = "New text",
                        x = x,
                        y = y
                    )
                    viewModel.setTool(EditorTool.SELECT)
                },
                onAddRedactionAndReplace = { x, y, w, h ->
                    viewModel.addRedactionAndReplacement(
                        x = x,
                        y = y,
                        width = w,
                        height = h,
                        replacementText = "Edited text"
                    )
                    viewModel.setTool(EditorTool.SELECT)
                    Toast.makeText(context, "Text erased & replaced with editable layer", Toast.LENGTH_SHORT).show()
                },
                onAddDrawingStroke = { viewModel.addDrawingStroke(it) },
                onEditTextDialog = { textElem -> showTextInspector = textElem },
                onEditImageDialog = { imgElem -> showImageInspector = imgElem }
            )

            // Contextual Floating Action Bar when element is selected
            if (selectedTextId != null || selectedImageId != null || selectedShapeId != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .testTag("floating_selection_bar"),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedTextId != null) {
                            val selText = page.textElements.firstOrNull { it.id == selectedTextId }
                            if (selText != null) {
                                Text(
                                    text = "Text Element",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(onClick = { showTextInspector = selText }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Text formatting")
                                }
                                IconButton(onClick = {
                                    viewModel.updateTextElement(selText.copy(isBold = !selText.isBold))
                                }) {
                                    Text("B", fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = {
                                    viewModel.updateTextElement(selText.copy(fontSize = selText.fontSize + 2f))
                                }) {
                                    Text("A+", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else if (selectedImageId != null) {
                            val selImg = page.imageElements.firstOrNull { it.id == selectedImageId }
                            if (selImg != null) {
                                Text(
                                    text = "Image Element",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(onClick = { showImageInspector = selImg }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Image Controls")
                                }
                                IconButton(onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Replace from Gallery")
                                }
                            }
                        } else if (selectedShapeId != null) {
                            Text(
                                text = "Stamp / Shape",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteSelectedElement() },
                            modifier = Modifier.testTag("floating_bar_delete_btn")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete element",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Deselect")
                        }
                    }
                }
            }

            // Quick instruction overlay when in special tool mode
            if (activeTool == EditorTool.REDACT_REPLACE) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Drag a box over text to erase & replace it",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else if (activeTool == EditorTool.ADD_TEXT) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Tap anywhere on page to place new text",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    // --- Dialogs & Bottom Sheets ---

    showTextInspector?.let { elem ->
        TextInspectorDialog(
            element = elem,
            onDismiss = { showTextInspector = null },
            onSave = { updated ->
                viewModel.updateTextElement(updated)
                showTextInspector = null
            },
            onDelete = {
                viewModel.deleteSelectedElement()
                showTextInspector = null
            }
        )
    }

    showImageInspector?.let { elem ->
        ImageInspectorDialog(
            element = elem,
            onDismiss = { showImageInspector = null },
            onSave = { updated ->
                viewModel.updateImageElement(updated)
                showImageInspector = null
            },
            onPickNewImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onOpenUnsplashPicker = {
                showUnsplashPicker = true
            },
            onDelete = {
                viewModel.deleteSelectedElement()
                showImageInspector = null
            }
        )
    }

    if (showSignatureDialog) {
        SignatureDialog(
            onDismiss = { showSignatureDialog = false },
            onSignatureReady = { sigPath ->
                viewModel.addImageElement(
                    localFilePath = sigPath,
                    x = 100f,
                    y = 350f,
                    width = 160f,
                    height = 80f
                )
                showSignatureDialog = false
                Toast.makeText(context, "Signature stamped on page", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showUnsplashPicker) {
        UnsplashPickerSheet(
            onDismiss = { showUnsplashPicker = false },
            onPhotoSelected = { photo ->
                val inspecting = showImageInspector
                if (inspecting != null) {
                    viewModel.updateImageElement(
                        inspecting.copy(
                            uriString = photo.imageUrl,
                            drawableResName = photo.drawableResName,
                            localFilePath = null
                        )
                    )
                    showImageInspector = null
                } else {
                    viewModel.addImageElement(
                        uriString = photo.imageUrl,
                        drawableResName = photo.drawableResName,
                        x = 80f,
                        y = 200f,
                        width = 180f,
                        height = 120f
                    )
                }
                showUnsplashPicker = false
                Toast.makeText(context, "Added photo: ${photo.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPageManager) {
        PageManagerSheet(
            pages = doc.pages,
            currentPageIndex = currentPageIndex,
            onSelectPage = {
                viewModel.setPage(it)
                showPageManager = false
            },
            onAddBlankPage = {
                viewModel.addBlankPage()
                showPageManager = false
            },
            onDuplicatePage = {
                viewModel.duplicatePage(it)
                showPageManager = false
            },
            onDeletePage = {
                viewModel.deletePage(it)
                showPageManager = false
            },
            onRotatePage = {
                viewModel.rotatePage(it)
            },
            onDismiss = { showPageManager = false }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            document = doc,
            onDismiss = { showExportDialog = false },
            onExportPdf = { onReady ->
                viewModel.exportDocument(context, onReady)
            }
        )
    }

    // Rename Document Dialog
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(doc.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Document Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.updateDocumentTitle(newTitle)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Image Choices Dialog
    if (showAddImageMenu) {
        AlertDialog(
            onDismissRequest = { showAddImageMenu = false },
            title = { Text("Add Image to PDF") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select source for the new image:")
                    OutlinedTextField(
                        value = "Device Gallery / Camera Roll",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddImageMenu = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )
                    OutlinedTextField(
                        value = "Unsplash Photo Gallery",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddImageMenu = false
                                showUnsplashPicker = true
                            }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddImageMenu = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Stamps & Shapes Dialog
    if (showStampsMenu) {
        AlertDialog(
            onDismissRequest = { showStampsMenu = false },
            title = { Text("Insert Stamp or Shape") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Official Stamps:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("APPROVED", "PAID", "DRAFT").forEach { stampName ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.addShapeElement(ShapeType.STAMP, stampText = stampName)
                                    showStampsMenu = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stampName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CONFIDENTIAL", "VOID").forEach { stampName ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.addShapeElement(ShapeType.STAMP, stampText = stampName)
                                    showStampsMenu = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stampName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Shapes:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addShapeElement(ShapeType.RECTANGLE)
                                showStampsMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Box")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.addShapeElement(ShapeType.CIRCLE)
                                showStampsMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Circle")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.addShapeElement(ShapeType.CHECKMARK)
                                showStampsMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addShapeElement(ShapeType.ARROW)
                                showStampsMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Arrow")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.addShapeElement(ShapeType.LINE)
                                showStampsMenu = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Line")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStampsMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InteractivePdfCanvas(
    page: PdfPageData,
    activeTool: EditorTool,
    selectedTextId: String?,
    selectedImageId: String?,
    selectedShapeId: String?,
    onSelectText: (String) -> Unit,
    onSelectImage: (String) -> Unit,
    onSelectShape: (String) -> Unit,
    onClearSelection: () -> Unit,
    onUpdateText: (PdfTextElement) -> Unit,
    onUpdateImage: (PdfImageElement) -> Unit,
    onUpdateShape: (PdfShapeElement) -> Unit,
    onAddText: (Float, Float) -> Unit,
    onAddRedactionAndReplace: (Float, Float, Float, Float) -> Unit,
    onAddDrawingStroke: (PdfAnnotationStroke) -> Unit,
    onEditTextDialog: (PdfTextElement) -> Unit,
    onEditImageDialog: (PdfImageElement) -> Unit
) {
    val context = LocalContext.current

    // Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1.0f, 3.5f)
        scale = newScale
        // Lock horizontal panning so document stays centered and does not drift left/right
        // Vertical panning allows scrolling through the page comfortably when zoomed
        val maxVerticalOffset = ((page.height * 1.5f) * (newScale - 1f)).coerceAtLeast(0f)
        val newOffsetY = if (newScale <= 1.05f) 0f else (offset.y + offsetChange.y).coerceIn(-maxVerticalOffset, maxVerticalOffset)
        offset = Offset(0f, newOffsetY)
    }

    // Redaction drag rectangle
    var redactStart by remember { mutableStateOf<Offset?>(null) }
    var redactEnd by remember { mutableStateOf<Offset?>(null) }

    // Drawing in-progress stroke
    var activeStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformState)
            .pointerInput(activeTool) {
                if (activeTool == EditorTool.SELECT) {
                    detectTapGestures(
                        onTap = { onClearSelection() }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val availableWidthPx = constraints.maxWidth.toFloat()
        // Compute ratio in pixel space (92% of available width to give comfortable margin)
        val pdfToDisplayRatio = (availableWidthPx * 0.92f) / page.width
        val displayWidthDp = with(density) { (page.width * pdfToDisplayRatio).toDp() }
        val displayHeightDp = with(density) { (page.height * pdfToDisplayRatio).toDp() }

        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .width(displayWidthDp)
                .height(displayHeightDp)
                .shadow(12.dp, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .pointerInput(activeTool) {
                    when (activeTool) {
                        EditorTool.ADD_TEXT -> {
                            detectTapGestures { tapOffset ->
                                val pdfX = tapOffset.x / pdfToDisplayRatio
                                val pdfY = tapOffset.y / pdfToDisplayRatio
                                onAddText(pdfX, pdfY)
                            }
                        }
                        EditorTool.REDACT_REPLACE -> {
                            detectDragGestures(
                                onDragStart = { start ->
                                    redactStart = start
                                    redactEnd = start
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    redactEnd = change.position
                                },
                                onDragEnd = {
                                    val start = redactStart
                                    val end = redactEnd
                                    if (start != null && end != null) {
                                        val minX = minOf(start.x, end.x) / pdfToDisplayRatio
                                        val minY = minOf(start.y, end.y) / pdfToDisplayRatio
                                        val width = kotlin.math.abs(end.x - start.x) / pdfToDisplayRatio
                                        val height = kotlin.math.abs(end.y - start.y) / pdfToDisplayRatio
                                        if (width > 10 && height > 8) {
                                            onAddRedactionAndReplace(minX, minY, width, height)
                                        }
                                    }
                                    redactStart = null
                                    redactEnd = null
                                },
                                onDragCancel = {
                                    redactStart = null
                                    redactEnd = null
                                }
                            )
                        }
                        EditorTool.DRAW_PEN, EditorTool.DRAW_HIGHLIGHTER -> {
                            detectDragGestures(
                                onDragStart = { start ->
                                    activeStrokePoints = listOf(start)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    activeStrokePoints = activeStrokePoints + change.position
                                },
                                onDragEnd = {
                                    if (activeStrokePoints.size > 1) {
                                        val strokePoints = activeStrokePoints.map {
                                            StrokePoint(it.x / pdfToDisplayRatio, it.y / pdfToDisplayRatio)
                                        }
                                        onAddDrawingStroke(
                                            PdfAnnotationStroke(
                                                points = strokePoints,
                                                color = if (activeTool == EditorTool.DRAW_HIGHLIGHTER) 0xAAFEF08AL else 0xFFE11D48L,
                                                strokeWidth = if (activeTool == EditorTool.DRAW_HIGHLIGHTER) 16f else 4f,
                                                isHighlighter = activeTool == EditorTool.DRAW_HIGHLIGHTER
                                            )
                                        )
                                    }
                                    activeStrokePoints = emptyList()
                                },
                                onDragCancel = {
                                    activeStrokePoints = emptyList()
                                }
                            )
                        }
                        else -> {}
                    }
                }
        ) {
            // 1. Base PDF Rendered Page Bitmap (or clean white background)
            if (page.baseImagePath != null && File(page.baseImagePath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(page.baseImagePath))
                        .crossfade(false)
                        .build(),
                    contentDescription = "PDF Base Page",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Redactions / Whiteouts (covering original printed text)
            page.redactions.forEach { red ->
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (red.x * pdfToDisplayRatio).roundToInt(),
                                (red.y * pdfToDisplayRatio).roundToInt()
                            )
                        }
                        .size(
                            with(density) { (red.width * pdfToDisplayRatio).toDp() },
                            with(density) { (red.height * pdfToDisplayRatio).toDp() }
                        )
                        .background(Color(red.color))
                )
            }

            // 3. Shapes & Stamps
            page.shapes.forEach { shape ->
                val isSelected = shape.id == selectedShapeId
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (shape.x * pdfToDisplayRatio).roundToInt(),
                                (shape.y * pdfToDisplayRatio).roundToInt()
                            )
                        }
                        .size(
                            with(density) { (shape.width * pdfToDisplayRatio).toDp() },
                            with(density) { (shape.height * pdfToDisplayRatio).toDp() }
                        )
                        .clickable { onSelectShape(shape.id) }
                ) {
                    if (shape.type == ShapeType.STAMP) {
                        RubberStampView(
                            text = shape.stampText ?: "APPROVED",
                            color = Color(shape.strokeColor)
                        )
                    } else {
                        ShapeItemView(shape = shape)
                    }

                    if (isSelected) {
                        SelectionBorderBox()
                    }
                }
            }

            // 4. Image Elements (Draggable & Selectable)
            page.imageElements.forEach { imgElem ->
                val isSelected = imgElem.id == selectedImageId
                InteractiveImageItem(
                    element = imgElem,
                    pdfToDisplayRatio = pdfToDisplayRatio,
                    isSelected = isSelected,
                    onSelect = { onSelectImage(imgElem.id) },
                    onDoubleTap = { onEditImageDialog(imgElem) },
                    onPositionChanged = { newX, newY ->
                        onUpdateImage(imgElem.copy(x = newX, y = newY))
                    },
                    onSizeChanged = { newW, newH ->
                        onUpdateImage(imgElem.copy(width = newW, height = newH))
                    }
                )
            }

            // 5. Text Elements (Draggable, Selectable, In-place editable)
            page.textElements.forEach { textElem ->
                val isSelected = textElem.id == selectedTextId
                InteractiveTextItem(
                    element = textElem,
                    pdfToDisplayRatio = pdfToDisplayRatio,
                    isSelected = isSelected,
                    onSelect = { onSelectText(textElem.id) },
                    onDoubleTap = { onEditTextDialog(textElem) },
                    onPositionChanged = { newX, newY ->
                        onUpdateText(textElem.copy(x = newX, y = newY))
                    },
                    onSizeChanged = { newW, newH ->
                        onUpdateText(textElem.copy(width = newW, height = newH))
                    }
                )
            }

            // 6. Freehand Drawings & Highlighters
            Canvas(modifier = Modifier.fillMaxSize()) {
                page.drawings.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(
                                stroke.points[0].x * pdfToDisplayRatio,
                                stroke.points[0].y * pdfToDisplayRatio
                            )
                            for (i in 1 until stroke.points.size) {
                                lineTo(
                                    stroke.points[i].x * pdfToDisplayRatio,
                                    stroke.points[i].y * pdfToDisplayRatio
                                )
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(stroke.color),
                            style = Stroke(
                                width = stroke.strokeWidth * (pdfToDisplayRatio / 1.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // In-progress drawing stroke
                if (activeStrokePoints.size > 1) {
                    val path = Path().apply {
                        moveTo(activeStrokePoints[0].x, activeStrokePoints[0].y)
                        for (i in 1 until activeStrokePoints.size) {
                            lineTo(activeStrokePoints[i].x, activeStrokePoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = if (activeTool == EditorTool.DRAW_HIGHLIGHTER) Color(0xAAFEF08A) else Color(0xFFE11D48),
                        style = Stroke(
                            width = if (activeTool == EditorTool.DRAW_HIGHLIGHTER) 16f else 4f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // In-progress Redaction selection box
                val rStart = redactStart
                val rEnd = redactEnd
                if (rStart != null && rEnd != null) {
                    val rect = Rect(
                        left = minOf(rStart.x, rEnd.x),
                        top = minOf(rStart.y, rEnd.y),
                        right = maxOf(rStart.x, rEnd.x),
                        bottom = maxOf(rStart.y, rEnd.y)
                    )
                    drawRect(
                        color = Color(0x66E11D48),
                        topLeft = rect.topLeft,
                        size = rect.size
                    )
                    drawRect(
                        color = Color(0xFFE11D48),
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveTextItem(
    element: PdfTextElement,
    pdfToDisplayRatio: Float,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDoubleTap: () -> Unit,
    onPositionChanged: (Float, Float) -> Unit,
    onSizeChanged: (Float, Float) -> Unit
) {
    val density = LocalDensity.current
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val displayX = (element.x * pdfToDisplayRatio) + dragOffsetX
    val displayY = (element.y * pdfToDisplayRatio) + dragOffsetY
    val displayWidth = with(density) { (element.width * pdfToDisplayRatio).toDp() }
    val displayHeight = with(density) { (element.height * pdfToDisplayRatio).toDp() }

    Box(
        modifier = Modifier
            .offset { IntOffset(displayX.roundToInt(), displayY.roundToInt()) }
            .width(displayWidth)
            .height(displayHeight)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = { onDoubleTap() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    },
                    onDragEnd = {
                        val newX = element.x + (dragOffsetX / pdfToDisplayRatio)
                        val newY = element.y + (dragOffsetY / pdfToDisplayRatio)
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onPositionChanged(newX.coerceAtLeast(0f), newY.coerceAtLeast(0f))
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    }
                )
            }
            .then(
                if (element.backgroundColor != null) {
                    Modifier.background(Color(element.backgroundColor))
                } else Modifier
            )
            .testTag("text_elem_${element.id}")
    ) {
        val fontFamily = when (element.fontFamily.lowercase()) {
            "serif" -> FontFamily.Serif
            "monospace" -> FontFamily.Monospace
            else -> FontFamily.Default
        }
        val textAlign = when (element.alignment) {
            TextAlignmentType.CENTER -> TextAlign.Center
            TextAlignmentType.RIGHT -> TextAlign.End
            TextAlignmentType.LEFT -> TextAlign.Start
        }

        Text(
            text = element.text,
            fontSize = (element.fontSize * (pdfToDisplayRatio / 1.35f)).sp,
            color = Color(element.color),
            fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = if (element.isUnderline) TextDecoration.Underline else TextDecoration.None,
            fontFamily = fontFamily,
            textAlign = textAlign,
            modifier = Modifier.fillMaxSize()
        )

        if (isSelected) {
            SelectionBorderBox()

            // Resize handle at bottom right corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .offset(4.dp, 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newW = element.width + (dragAmount.x / pdfToDisplayRatio)
                            val newH = element.height + (dragAmount.y / pdfToDisplayRatio)
                            onSizeChanged(newW.coerceAtLeast(40f), newH.coerceAtLeast(20f))
                        }
                    }
            )
        }
    }
}

@Composable
fun InteractiveImageItem(
    element: PdfImageElement,
    pdfToDisplayRatio: Float,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDoubleTap: () -> Unit,
    onPositionChanged: (Float, Float) -> Unit,
    onSizeChanged: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val displayX = (element.x * pdfToDisplayRatio) + dragOffsetX
    val displayY = (element.y * pdfToDisplayRatio) + dragOffsetY
    val displayWidth = with(density) { (element.width * pdfToDisplayRatio).toDp() }
    val displayHeight = with(density) { (element.height * pdfToDisplayRatio).toDp() }

    Box(
        modifier = Modifier
            .offset { IntOffset(displayX.roundToInt(), displayY.roundToInt()) }
            .width(displayWidth)
            .height(displayHeight)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = { onDoubleTap() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    },
                    onDragEnd = {
                        val newX = element.x + (dragOffsetX / pdfToDisplayRatio)
                        val newY = element.y + (dragOffsetY / pdfToDisplayRatio)
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onPositionChanged(newX.coerceAtLeast(0f), newY.coerceAtLeast(0f))
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    }
                )
            }
            .clip(RoundedCornerShape((element.cornerRadius * pdfToDisplayRatio).dp))
            .then(
                if (element.borderColor != null && element.borderWidth > 0f) {
                    Modifier.border(
                        width = (element.borderWidth * pdfToDisplayRatio).dp,
                        color = Color(element.borderColor),
                        shape = RoundedCornerShape((element.cornerRadius * pdfToDisplayRatio).dp)
                    )
                } else Modifier
            )
            .testTag("image_elem_${element.id}")
    ) {
        when {
            element.drawableResName != null -> {
                val resId = context.resources.getIdentifier(element.drawableResName, "drawable", context.packageName)
                if (resId != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Image element",
                        contentScale = ContentScale.Crop,
                        alpha = element.opacity,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            element.localFilePath != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(element.localFilePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image element",
                    contentScale = ContentScale.Crop,
                    alpha = element.opacity,
                    modifier = Modifier.fillMaxSize()
                )
            }
            element.uriString != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(element.uriString)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image element",
                    contentScale = ContentScale.Crop,
                    alpha = element.opacity,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )
            }
        }

        if (isSelected) {
            SelectionBorderBox()

            // Resize handle at bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .offset(4.dp, 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newW = element.width + (dragAmount.x / pdfToDisplayRatio)
                            val newH = element.height + (dragAmount.y / pdfToDisplayRatio)
                            onSizeChanged(newW.coerceAtLeast(30f), newH.coerceAtLeast(30f))
                        }
                    }
            )
        }
    }
}

@Composable
fun RubberStampView(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .rotate(-10f)
            .border(2.5.dp, color, RoundedCornerShape(8.dp))
            .padding(3.dp)
            .border(1.dp, color, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ShapeItemView(shape: PdfShapeElement) {
    when (shape.type) {
        ShapeType.LINE -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color(shape.strokeColor),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = shape.strokeWidth * density
                )
            }
        }
        ShapeType.ARROW -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = shape.strokeWidth * density
                drawLine(
                    color = Color(shape.strokeColor),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width - 8.dp.toPx(), size.height / 2f),
                    strokeWidth = strokePx
                )
                val arrowPath = Path().apply {
                    moveTo(size.width, size.height / 2f)
                    lineTo(size.width - 12.dp.toPx(), size.height / 2f - 7.dp.toPx())
                    lineTo(size.width - 12.dp.toPx(), size.height / 2f + 7.dp.toPx())
                    close()
                }
                drawPath(path = arrowPath, color = Color(shape.strokeColor))
            }
        }
        ShapeType.CIRCLE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(shape.strokeWidth.dp, Color(shape.strokeColor), CircleShape)
                    .then(
                        if (shape.fillColor != null) Modifier.background(Color(shape.fillColor), CircleShape) else Modifier
                    )
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(shape.strokeWidth.dp, Color(shape.strokeColor), RoundedCornerShape(4.dp))
                    .then(
                        if (shape.fillColor != null) Modifier.background(Color(shape.fillColor)) else Modifier
                    )
            )
        }
    }
}

@Composable
fun SelectionBorderBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
    )
}

@Composable
fun EditorBottomToolbar(
    activeTool: EditorTool,
    onToolSelect: (EditorTool) -> Unit,
    onAddStickyNote: () -> Unit = {},
    onAddDateTime: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Most critical tools first (Select, Text, Replace, Pen, Sign) visible immediately (~5 tools)
            ToolButton(
                icon = Icons.Default.NearMe,
                label = "Select",
                isSelected = activeTool == EditorTool.SELECT,
                onClick = { onToolSelect(EditorTool.SELECT) },
                testTag = "tool_select"
            )
            ToolButton(
                icon = Icons.Default.TextFields,
                label = "Text",
                isSelected = activeTool == EditorTool.ADD_TEXT,
                onClick = { onToolSelect(EditorTool.ADD_TEXT) },
                testTag = "tool_add_text"
            )
            ToolButton(
                icon = Icons.Default.AutoFixHigh,
                label = "Replace",
                isSelected = activeTool == EditorTool.REDACT_REPLACE,
                onClick = { onToolSelect(EditorTool.REDACT_REPLACE) },
                testTag = "tool_redact_replace"
            )
            ToolButton(
                icon = Icons.Default.Brush,
                label = "Pen",
                isSelected = activeTool == EditorTool.DRAW_PEN,
                onClick = { onToolSelect(EditorTool.DRAW_PEN) },
                testTag = "tool_draw_pen"
            )
            ToolButton(
                icon = Icons.Default.Draw,
                label = "Sign",
                isSelected = activeTool == EditorTool.SIGNATURE,
                onClick = { onToolSelect(EditorTool.SIGNATURE) },
                testTag = "tool_signature"
            )
            // Additional editing tools accessible via smooth horizontal scrolling
            ToolButton(
                icon = Icons.Default.Highlight,
                label = "Highlight",
                isSelected = activeTool == EditorTool.DRAW_HIGHLIGHTER,
                onClick = { onToolSelect(EditorTool.DRAW_HIGHLIGHTER) },
                testTag = "tool_draw_highlighter"
            )
            ToolButton(
                icon = Icons.Default.Image,
                label = "Image",
                isSelected = activeTool == EditorTool.ADD_IMAGE,
                onClick = { onToolSelect(EditorTool.ADD_IMAGE) },
                testTag = "tool_add_image"
            )
            ToolButton(
                icon = Icons.Default.Category,
                label = "Shapes",
                isSelected = activeTool == EditorTool.ADD_SHAPE,
                onClick = { onToolSelect(EditorTool.ADD_SHAPE) },
                testTag = "tool_stamps"
            )
            ToolButton(
                icon = Icons.Default.NoteAlt,
                label = "Sticky Note",
                isSelected = false,
                onClick = onAddStickyNote,
                testTag = "tool_sticky_note"
            )
            ToolButton(
                icon = Icons.Default.CalendarToday,
                label = "Date/Time",
                isSelected = false,
                onClick = onAddDateTime,
                testTag = "tool_date_time"
            )
        }
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
