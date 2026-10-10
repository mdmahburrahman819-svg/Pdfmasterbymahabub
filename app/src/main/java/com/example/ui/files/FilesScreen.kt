package com.example.ui.files

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PdfDocumentData
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.MintBadgeIcon
import com.example.ui.theme.MintSelectedPill
import com.example.ui.theme.PdfRedBadgeBg
import com.example.ui.theme.PdfRedIcon
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealPrimaryDark
import com.example.viewmodel.PdfViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortField {
    NAME, DATE, SIZE, TYPE
}

@Composable
fun FilesScreen(
    viewModel: PdfViewModel,
    onOpenReader: () -> Unit,
    onOpenEditor: () -> Unit
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var selectedFilterChip by remember { mutableStateOf("All") }
    val filterChips = listOf("All", "PDF", "Documents", "Media")

    var isGridView by remember { mutableStateOf(false) }
    var sortField by remember { mutableStateOf(SortField.DATE) }
    var isAscending by remember { mutableStateOf(true) }

    var showSortMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

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

    // Filter & Sort documents
    val processedDocs = remember(documents, searchQuery, selectedFilterChip, sortField, isAscending) {
        val filtered = documents.filter { doc ->
            val matchesSearch = doc.title.contains(searchQuery, ignoreCase = true)
            val matchesChip = when (selectedFilterChip) {
                "PDF" -> doc.title.endsWith(".pdf", ignoreCase = true)
                "Documents" -> doc.title.contains("Agreement", true) || doc.title.contains("Resume", true) || doc.title.contains("Invoice", true)
                "Media" -> doc.title.contains("Images", true) || doc.title.contains("Photo", true)
                else -> true
            }
            matchesSearch && matchesChip
        }

        when (sortField) {
            SortField.NAME -> if (isAscending) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
            SortField.DATE -> if (isAscending) filtered.sortedBy { it.lastModifiedTimestamp } else filtered.sortedByDescending { it.lastModifiedTimestamp }
            SortField.SIZE -> if (isAscending) filtered.sortedBy { it.fileSizeKb } else filtered.sortedByDescending { it.fileSizeKb }
            SortField.TYPE -> if (isAscending) filtered.sortedBy { it.id } else filtered.sortedByDescending { it.id }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Row (Screenshot 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Files",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.weight(1f)
            )

            // Sort icon
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier.testTag("files_sort_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Sort and view options",
                        tint = Slate900
                    )
                }

                // Dropdown Menu matching Screenshot 4
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(if (isGridView) "List view" else "Grid view")
                            }
                        },
                        onClick = {
                            isGridView = !isGridView
                            showSortMenu = false
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sortField == SortField.NAME) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(22.dp))
                                }
                                Text("Name")
                            }
                        },
                        onClick = {
                            sortField = SortField.NAME
                            showSortMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sortField == SortField.DATE) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(22.dp))
                                }
                                Text("Date")
                            }
                        },
                        onClick = {
                            sortField = SortField.DATE
                            showSortMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sortField == SortField.SIZE) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(22.dp))
                                }
                                Text("Size")
                            }
                        },
                        onClick = {
                            sortField = SortField.SIZE
                            showSortMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sortField == SortField.TYPE) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(22.dp))
                                }
                                Text("Type")
                            }
                        },
                        onClick = {
                            sortField = SortField.TYPE
                            showSortMenu = false
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isAscending) "Ascending" else "Descending")
                            }
                        },
                        onClick = {
                            isAscending = !isAscending
                            showSortMenu = false
                        }
                    )
                }
            }

            // Search icon
            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search files",
                    tint = Slate900
                )
            }

            // More options 3 dots
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Slate900
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Select all") },
                        onClick = {
                            showMoreMenu = false
                            Toast.makeText(context, "All ${processedDocs.size} files selected", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import document") },
                        onClick = {
                            showMoreMenu = false
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                    )
                }
            }

            // Plus icon
            Box {
                IconButton(onClick = { showAddMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new file",
                        tint = Slate900
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open PDF") },
                        onClick = {
                            showAddMenu = false
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Create Blank PDF") },
                        onClick = {
                            showAddMenu = false
                            viewModel.createNewBlankDocument()
                            onOpenEditor()
                        }
                    )
                }
            }
        }

        // Search Bar (if toggled)
        if (isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search files...") },
                trailingIcon = {
                    IconButton(onClick = {
                        searchQuery = ""
                        isSearchActive = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Filter chips row (Screenshot 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterChips.forEach { chipName ->
                val isSelected = selectedFilterChip == chipName
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterChip = chipName },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TealPrimaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = chipName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TealPrimaryDark else Slate900
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintSelectedPill,
                        selectedLabelColor = TealPrimaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Files List / Grid View
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(processedDocs, key = { it.id }) { doc ->
                    FileGridCard(
                        doc = doc,
                        onOpen = {
                            viewModel.openDocument(doc)
                            onOpenReader()
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(processedDocs, key = { it.id }) { doc ->
                    com.example.ui.home.RecentFileCard(
                        doc = doc,
                        onOpen = {
                            viewModel.openDocument(doc)
                            onOpenReader()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FileGridCard(
    doc: PdfDocumentData,
    onOpen: () -> Unit
) {
    val dateStr = remember(doc.lastModifiedTimestamp) {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(doc.lastModifiedTimestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpen)
            .testTag("file_grid_${doc.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PdfRedBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = PdfRedIcon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (doc.isProtected) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Protected",
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = doc.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${doc.fileSizeKb} KB · $dateStr",
                fontSize = 11.sp,
                color = Slate500,
                maxLines = 1
            )
        }
    }
}
