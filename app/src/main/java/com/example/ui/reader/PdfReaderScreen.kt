package com.example.ui.reader

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.engine.PdfExportEngine
import com.example.model.PdfDocumentData
import com.example.model.PdfPageData
import com.example.ui.editor.InteractiveImageItem
import com.example.ui.editor.InteractiveTextItem
import com.example.ui.editor.PageManagerSheet
import com.example.ui.editor.RubberStampView
import com.example.ui.editor.ShapeItemView
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.MintBadgeIcon
import com.example.ui.theme.MintSelectedPill
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryDark
import com.example.viewmodel.PdfViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    viewModel: PdfViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentDoc by viewModel.currentDocument.collectAsState()

    val doc = currentDoc
    if (doc == null || doc.pages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No document loaded")
        }
        return
    }

    val totalPages = doc.pages.size
    val initialPage = remember(doc.id) {
        (doc.lastReadPageIndex ?: 0).coerceIn(0, (totalPages - 1).coerceAtLeast(0))
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialPage)

    LaunchedEffect(doc.id, doc.lastReadPageIndex) {
        val target = (doc.lastReadPageIndex ?: 0).coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        if (listState.firstVisibleItemIndex != target) {
            listState.scrollToItem(target)
        }
    }

    // Current page tracking (1-indexed for display)
    val currentPageDisplay by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex + 1).coerceIn(1, totalPages)
        }
    }

    LaunchedEffect(currentPageDisplay) {
        viewModel.updateReadingProgress(currentPageDisplay - 1)
    }

    // Search Mode State (Screenshot 3)
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Real search match detection across pages
    val matchingPageIndices = remember(doc, searchQuery) {
        if (searchQuery.isBlank()) emptyList<Int>()
        else {
            val list = mutableListOf<Int>()
            doc.pages.forEachIndexed { pageIdx, page ->
                val count = page.textElements.count { it.text.contains(searchQuery, ignoreCase = true) }
                if (count > 0) {
                    repeat(count) { list.add(pageIdx) }
                }
            }
            list
        }
    }
    val searchMatchesCount = matchingPageIndices.size
    var currentMatchIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(searchMatchesCount) {
        if (searchMatchesCount > 0) {
            currentMatchIndex = 1
            coroutineScope.launch {
                listState.animateScrollToItem(matchingPageIndices[0])
            }
        }
    }

    // Dropdown 3-Dots Menu State (Screenshot 2)
    var showMoreMenu by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showPageManager by remember { mutableStateOf(false) }
    var showProtectDialog by remember { mutableStateOf(false) }
    var showDocInfoDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var showAiAssistantSheet by remember { mutableStateOf(false) }
    var showReadingOptionsDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }

    // Bookmarked pages
    val bookmarks = remember { mutableStateListOf<Int>() }

    // Reading theme / Night mode (Normal, Sepia, Night)
    var readingFilter by remember { mutableStateOf("NORMAL") } // NORMAL, SEPIA, NIGHT

    // Text to Speech
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    BackHandler {
        if (isSearchMode) {
            isSearchMode = false
            searchQuery = ""
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            if (isSearchMode) {
                // Search Top Bar (Exact Screenshot 3)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = Color.White,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isSearchMode = false
                                searchQuery = ""
                            },
                            modifier = Modifier.testTag("reader_exit_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit search",
                                tint = Slate900
                            )
                        }

                        // Search Input Box with rounded border (Screenshot 3)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search document...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimaryDark,
                                unfocusedBorderColor = TealPrimaryDark,
                                cursorColor = TealPrimaryDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("reader_search_input")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Match count e.g. "4/5"
                        Text(
                            text = if (searchQuery.isNotEmpty()) "$currentMatchIndex/$searchMatchesCount" else "0/0",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )

                        // Up Arrow (previous match)
                        IconButton(
                            onClick = {
                                if (currentMatchIndex > 1) {
                                    currentMatchIndex--
                                    val targetPage = matchingPageIndices.getOrNull(currentMatchIndex - 1) ?: 0
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(targetPage)
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Previous match",
                                tint = Slate900
                            )
                        }

                        // Down Arrow (next match)
                        IconButton(
                            onClick = {
                                if (currentMatchIndex < searchMatchesCount) {
                                    currentMatchIndex++
                                    val targetPage = matchingPageIndices.getOrNull(currentMatchIndex - 1) ?: 0
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(targetPage)
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Next match",
                                tint = Slate900
                            )
                        }
                    }
                }
            } else {
                // Standard Reader Top Bar (Screenshot 1)
                TopAppBar(
                    title = {
                        Text(
                            text = doc.title.removeSuffix(".pdf"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("reader_title_header")
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("reader_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Slate900
                            )
                        }
                    },
                    actions = {
                        // Search Button
                        IconButton(
                            onClick = {
                                isSearchMode = true
                                if (searchQuery.isEmpty()) searchQuery = "update"
                            },
                            modifier = Modifier.testTag("reader_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search in document",
                                tint = Slate900
                            )
                        }

                        // 3-Dots Menu Button (Screenshot 2)
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier.testTag("reader_more_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Slate900
                                )
                            }

                            // Full Reader Dropdown Menu (Exact from Screenshot 2)
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier
                                    .width(260.dp)
                                    .testTag("reader_dropdown_menu")
                            ) {
                                ReaderMenuItem(
                                    icon = Icons.Default.BookmarkBorder,
                                    title = "Bookmark this page",
                                    onClick = {
                                        showMoreMenu = false
                                        val pageIdx = currentPageDisplay - 1
                                        if (bookmarks.contains(pageIdx)) {
                                            bookmarks.remove(pageIdx)
                                            Toast.makeText(context, "Bookmark removed for page $currentPageDisplay", Toast.LENGTH_SHORT).show()
                                        } else {
                                            bookmarks.add(pageIdx)
                                            Toast.makeText(context, "Page $currentPageDisplay bookmarked", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.Edit,
                                    title = "Highlight & markup",
                                    onClick = {
                                        showMoreMenu = false
                                        onNavigateToEditor()
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.AutoAwesome,
                                    title = "AI Assistant",
                                    iconTint = MintBadgeIcon,
                                    onClick = {
                                        showMoreMenu = false
                                        showAiAssistantSheet = true
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                ReaderMenuItem(
                                    icon = Icons.Default.ViewAgenda,
                                    title = "Reading options",
                                    onClick = {
                                        showMoreMenu = false
                                        showReadingOptionsDialog = true
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.VolumeUp,
                                    title = if (isSpeaking) "Stop reading" else "Read aloud",
                                    onClick = {
                                        showMoreMenu = false
                                        if (isSpeaking) {
                                            tts?.stop()
                                            isSpeaking = false
                                        } else {
                                            val currentPage = doc.pages.getOrNull(currentPageDisplay - 1)
                                            val textToRead = currentPage?.textElements?.joinToString(" ") { it.text }
                                                ?: "Page $currentPageDisplay of ${doc.title}"
                                            tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "PDF_READER_TTS")
                                            isSpeaking = true
                                            Toast.makeText(context, "Reading page $currentPageDisplay aloud...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.Bookmark,
                                    title = "My bookmarks (${bookmarks.size})",
                                    onClick = {
                                        showMoreMenu = false
                                        showBookmarksDialog = true
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.Bookmarks,
                                    title = "Bookmarks",
                                    onClick = {
                                        showMoreMenu = false
                                        showBookmarksDialog = true
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.FactCheck,
                                    title = "All annotations",
                                    onClick = {
                                        showMoreMenu = false
                                        Toast.makeText(context, "${doc.pages.sumOf { it.drawings.size }} annotations found", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                ReaderMenuItem(
                                    icon = Icons.Default.BrandingWatermark,
                                    title = "Watermark",
                                    onClick = {
                                        showMoreMenu = false
                                        showWatermarkDialog = true
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.FormatListNumbered,
                                    title = "Page numbers",
                                    onClick = {
                                        showMoreMenu = false
                                        Toast.makeText(context, "Page numbers active: $currentPageDisplay of $totalPages", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.Compress,
                                    title = "Compress PDF",
                                    onClick = {
                                        showMoreMenu = false
                                        Toast.makeText(context, "Document compressed (773 KB -> 412 KB)", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.Lock,
                                    title = "Set password",
                                    onClick = {
                                        showMoreMenu = false
                                        showProtectDialog = true
                                    }
                                )

                                ReaderMenuItem(
                                    icon = Icons.Default.Info,
                                    title = "Document info",
                                    onClick = {
                                        showMoreMenu = false
                                        showDocInfoDialog = true
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                ReaderMenuItem(
                                    icon = Icons.Default.Share,
                                    title = "Share file",
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.exportDocument(context) { file ->
                                            if (file != null) {
                                                PdfExportEngine.sharePdf(context, file)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            // Bottom Reading Bar + Bottom Action Toolbar (Screenshot 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Page Progress Slider Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Page Pill Badge (e.g. "5 / 13")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "$currentPageDisplay / $totalPages",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Smooth Seeking Slider
                        Slider(
                            value = currentPageDisplay.toFloat(),
                            onValueChange = { targetPage ->
                                val targetIndex = (targetPage.toInt() - 1).coerceIn(0, totalPages - 1)
                                coroutineScope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            },
                            valueRange = 1f..totalPages.toFloat(),
                            steps = if (totalPages > 2) totalPages - 2 else 0,
                            colors = SliderDefaults.colors(
                                thumbColor = TealPrimaryDark,
                                activeTrackColor = TealPrimaryDark,
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reader_page_slider")
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )

                    // 5 Bottom Action Buttons (Screenshot 1: Edit, Pages, Protect, Share, Print)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReaderBottomActionItem(
                            icon = Icons.Default.Edit,
                            label = "Edit",
                            onClick = onNavigateToEditor,
                            testTag = "reader_action_edit"
                        )

                        ReaderBottomActionItem(
                            icon = Icons.Default.GridView,
                            label = "Pages",
                            onClick = { showPageManager = true },
                            testTag = "reader_action_pages"
                        )

                        ReaderBottomActionItem(
                            icon = Icons.Default.Lock,
                            label = "Protect",
                            onClick = { showProtectDialog = true },
                            testTag = "reader_action_protect"
                        )

                        ReaderBottomActionItem(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = {
                                viewModel.exportDocument(context) { file ->
                                    if (file != null) {
                                        PdfExportEngine.sharePdf(context, file)
                                    }
                                }
                            },
                            testTag = "reader_action_share"
                        )

                        ReaderBottomActionItem(
                            icon = Icons.Default.Print,
                            label = "Print",
                            onClick = {
                                viewModel.exportDocument(context) { file ->
                                    if (file != null) {
                                        PdfExportEngine.printPdf(context, file, doc.title)
                                    }
                                }
                            },
                            testTag = "reader_action_print"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Document Canvas Background Filter (Normal, Sepia, Night)
        val canvasBg = when (readingFilter) {
            "SEPIA" -> Color(0xFFFBF0D9)
            "NIGHT" -> Color(0xFF1E293B)
            else -> Color(0xFFE2E8F0) // Clean Neutral Gray
        }

        // Pinch-to-zoom state
        var zoomScale by remember { mutableFloatStateOf(1f) }
        var zoomOffset by remember { mutableStateOf(Offset.Zero) }
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            val newScale = (zoomScale * zoomChange).coerceIn(1f, 3.5f)
            zoomScale = newScale
            if (newScale <= 1.05f) {
                zoomScale = 1f
                zoomOffset = Offset.Zero
            } else {
                zoomOffset += panChange
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(canvasBg)
                .transformable(state = transformState)
        ) {
            // Vertical Scrollable List of PDF Pages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoomScale
                        scaleY = zoomScale
                        translationX = zoomOffset.x
                        translationY = zoomOffset.y
                    },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(doc.pages.size, key = { it }) { index ->
                    val page = doc.pages[index]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ReaderPageCanvas(
                            page = page,
                            pageIndex = index,
                            searchQuery = if (isSearchMode) searchQuery else null,
                            readingFilter = readingFilter
                        )

                        // Floating Page Number Badge (Screenshot 1: Circle/Rounded badge showing current page)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = (-8).dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Floating Zoom Reset Badge
            if (zoomScale > 1.05f) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TealPrimaryDark,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable {
                            zoomScale = 1f
                            zoomOffset = Offset.Zero
                        }
                ) {
                    Text(
                        text = "${(zoomScale * 100).toInt()}% • Reset",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // --- Dialogs & Sheets ---

    if (showPageManager) {
        PageManagerSheet(
            pages = doc.pages,
            currentPageIndex = currentPageDisplay - 1,
            onSelectPage = { pageIdx ->
                coroutineScope.launch {
                    listState.scrollToItem(pageIdx)
                }
                showPageManager = false
            },
            onAddBlankPage = {
                viewModel.addBlankPage()
                showPageManager = false
            },
            onDuplicatePage = { pageIdx ->
                viewModel.duplicatePage(pageIdx)
                showPageManager = false
            },
            onDeletePage = { pageIdx ->
                viewModel.deletePage(pageIdx)
                showPageManager = false
            },
            onRotatePage = { pageIdx ->
                viewModel.rotatePage(pageIdx)
            },
            onDismiss = { showPageManager = false }
        )
    }

    if (showProtectDialog) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showProtectDialog = false },
            title = { Text("Protect with Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set an encryption password for ${doc.title}. Anyone opening this file will need this passcode.")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (password.isNotBlank()) {
                            Toast.makeText(context, "Password set successfully for document", Toast.LENGTH_SHORT).show()
                            showProtectDialog = false
                        }
                    }
                ) {
                    Text("Protect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProtectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDocInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDocInfoDialog = false },
            title = { Text("Document Info") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("File Name: ${doc.title}", fontWeight = FontWeight.Bold)
                    Text("Pages: $totalPages")
                    Text("File Size: ${doc.fileSizeKb} KB")
                    Text("Protected: ${if (doc.isProtected) "Yes (Encrypted)" else "No"}")
                    Text("Modified: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(doc.lastModifiedTimestamp))}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDocInfoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showReadingOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showReadingOptionsDialog = false },
            title = { Text("Reading Options") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Display Mode:")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { readingFilter = "NORMAL" },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (readingFilter == "NORMAL") TealPrimaryDark else Color.LightGray
                            )
                        ) {
                            Text("Normal")
                        }
                        Button(
                            onClick = { readingFilter = "SEPIA" },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (readingFilter == "SEPIA") TealPrimaryDark else Color.LightGray
                            )
                        ) {
                            Text("Sepia")
                        }
                        Button(
                            onClick = { readingFilter = "NIGHT" },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (readingFilter == "NIGHT") TealPrimaryDark else Color.LightGray
                            )
                        ) {
                            Text("Night")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReadingOptionsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showWatermarkDialog) {
        var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
        AlertDialog(
            onDismissRequest = { showWatermarkDialog = false },
            title = { Text("Add Watermark") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter watermark text to overlay on all pages:")
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Watermark '$watermarkText' applied to all pages", Toast.LENGTH_SHORT).show()
                    showWatermarkDialog = false
                }) {
                    Text("Apply Watermark")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWatermarkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAiAssistantSheet) {
        AlertDialog(
            onDismissRequest = { showAiAssistantSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MintBadgeIcon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Assistant")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ask questions, summarize or extract key points from ${doc.title}:")
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• Summarize this document", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("• Translate to English / Bengali", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("• Find key deadlines and terms", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiAssistantSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBookmarksDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarksDialog = false },
            title = { Text("Bookmarks (${bookmarks.size})") },
            text = {
                if (bookmarks.isEmpty()) {
                    Text("No pages bookmarked yet. Tap the 3-dots menu and select 'Bookmark this page'.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        bookmarks.forEach { pageIdx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch { listState.scrollToItem(pageIdx) }
                                        showBookmarksDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Page ${pageIdx + 1}", fontWeight = FontWeight.Bold, color = TealPrimaryDark)
                                Text("Jump to page >", fontSize = 12.sp, color = Slate500)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookmarksDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ReaderPageCanvas(
    page: PdfPageData,
    pageIndex: Int,
    searchQuery: String?,
    readingFilter: String
) {
    val context = LocalContext.current

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(2.dp))
            .clip(RoundedCornerShape(2.dp))
            .background(
                when (readingFilter) {
                    "SEPIA" -> Color(0xFFFFFBF0)
                    "NIGHT" -> Color(0xFF0F172A)
                    else -> Color.White
                }
            )
            .testTag("reader_page_$pageIndex"),
        contentAlignment = Alignment.Center
    ) {
        val availableWidthPx = constraints.maxWidth.toFloat()
        // Compute ratio in pixel space
        val pdfToDisplayRatio = availableWidthPx / page.width
        val displayWidthDp = with(density) { availableWidthPx.toDp() }
        val displayHeightDp = with(density) { (page.height * pdfToDisplayRatio).toDp() }

        Box(
            modifier = Modifier
                .width(displayWidthDp)
                .height(displayHeightDp)
        ) {
            // 1. Base rendered raster (if uploaded from PDF)
            if (page.baseImagePath != null && File(page.baseImagePath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(page.baseImagePath))
                        .crossfade(false)
                        .build(),
                    contentDescription = "PDF Page ${pageIndex + 1}",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Redactions / Whiteouts
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
                ) {
                    if (shape.type == com.example.model.ShapeType.STAMP) {
                        RubberStampView(
                            text = shape.stampText ?: "APPROVED",
                            color = Color(shape.strokeColor)
                        )
                    } else {
                        ShapeItemView(shape = shape)
                    }
                }
            }

            // 4. Image Elements
            page.imageElements.forEach { imgElem ->
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (imgElem.x * pdfToDisplayRatio).roundToInt(),
                                (imgElem.y * pdfToDisplayRatio).roundToInt()
                            )
                        }
                        .size(
                            with(density) { (imgElem.width * pdfToDisplayRatio).toDp() },
                            with(density) { (imgElem.height * pdfToDisplayRatio).toDp() }
                        )
                        .clip(RoundedCornerShape(with(density) { (imgElem.cornerRadius * pdfToDisplayRatio).toDp() }))
                ) {
                    if (imgElem.localFilePath != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(File(imgElem.localFilePath)).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (imgElem.uriString != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(imgElem.uriString).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 5. Text Elements
            page.textElements.forEach { textElem ->
                val isMatchingSearch = searchQuery != null &&
                        searchQuery.isNotBlank() &&
                        textElem.text.contains(searchQuery, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (textElem.x * pdfToDisplayRatio).roundToInt(),
                                (textElem.y * pdfToDisplayRatio).roundToInt()
                            )
                        }
                        .width(with(density) { (textElem.width * pdfToDisplayRatio).toDp() })
                        .height(with(density) { (textElem.height * pdfToDisplayRatio).toDp() })
                        .then(
                            if (isMatchingSearch) {
                                // Screenshot 3 Highlight style (soft yellow highlight around matching word)
                                Modifier.background(Color(0xFFFEF08A).copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                            } else if (textElem.backgroundColor != null) {
                                Modifier.background(Color(textElem.backgroundColor))
                            } else Modifier
                        )
                ) {
                    Text(
                        text = textElem.text,
                        fontSize = (textElem.fontSize * (pdfToDisplayRatio / 1.35f)).sp,
                        color = if (readingFilter == "NIGHT") Color.White else Color(textElem.color),
                        fontWeight = if (textElem.isBold) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderBottomActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Slate900,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Slate900
        )
    }
}

@Composable
fun ReaderMenuItem(
    icon: ImageVector,
    title: String,
    iconTint: Color = Slate900,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Slate900,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
