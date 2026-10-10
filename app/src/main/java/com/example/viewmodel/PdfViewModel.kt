package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.PdfExportEngine
import com.example.engine.PdfRendererHelper
import com.example.model.PdfAnnotationStroke
import com.example.model.PdfDocumentData
import com.example.model.PdfImageElement
import com.example.model.PdfPageData
import com.example.model.PdfRedaction
import com.example.model.PdfShapeElement
import com.example.model.PdfTextElement
import com.example.model.ShapeType
import com.example.model.TemplateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class EditorTool {
    SELECT,
    ADD_TEXT,
    REDACT_REPLACE,
    DRAW_PEN,
    DRAW_HIGHLIGHTER,
    ADD_IMAGE,
    ADD_SHAPE,
    SIGNATURE
}

sealed interface EditorUiEvent {
    data class ShowMessage(val message: String) : EditorUiEvent
    data class ExportSuccess(val file: File) : EditorUiEvent
    data class ExportError(val error: String) : EditorUiEvent
}

class PdfViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("pdf_master_reading_history", Context.MODE_PRIVATE)

    private val _documents = MutableStateFlow<List<PdfDocumentData>>(emptyList())
    val documents: StateFlow<List<PdfDocumentData>> = _documents.asStateFlow()

    private val _lastReadDocId = MutableStateFlow<String?>(null)
    val lastReadDocId: StateFlow<String?> = _lastReadDocId.asStateFlow()

    private val _lastReadDocument = MutableStateFlow<PdfDocumentData?>(null)
    val lastReadDocument: StateFlow<PdfDocumentData?> = _lastReadDocument.asStateFlow()

    private val _currentDocument = MutableStateFlow<PdfDocumentData?>(null)
    val currentDocument: StateFlow<PdfDocumentData?> = _currentDocument.asStateFlow()

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    private val _activeTool = MutableStateFlow(EditorTool.SELECT)
    val activeTool: StateFlow<EditorTool> = _activeTool.asStateFlow()

    private val _selectedTextId = MutableStateFlow<String?>(null)
    val selectedTextId: StateFlow<String?> = _selectedTextId.asStateFlow()

    private val _selectedImageId = MutableStateFlow<String?>(null)
    val selectedImageId: StateFlow<String?> = _selectedImageId.asStateFlow()

    private val _selectedShapeId = MutableStateFlow<String?>(null)
    val selectedShapeId: StateFlow<String?> = _selectedShapeId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<PdfDocumentData>()
    private val redoStack = mutableListOf<PdfDocumentData>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    init {
        // Load initial templates into documents list
        val templates = TemplateProvider.createSampleTemplates()

        // Restore persisted reading history if available
        val savedDocId = prefs.getString("last_read_doc_id", null)
        val savedPageIndex = prefs.getInt("last_read_page_index", -1)
        val savedProgress = if (prefs.contains("last_read_progress")) prefs.getFloat("last_read_progress", 0f) else null

        if (savedDocId != null && savedPageIndex >= 0) {
            val matchingDoc = templates.firstOrNull { it.id == savedDocId }
            if (matchingDoc != null) {
                val restored = matchingDoc.copy(
                    lastReadPageIndex = savedPageIndex,
                    readingProgress = savedProgress
                )
                _documents.value = templates.map { if (it.id == savedDocId) restored else it }
                _lastReadDocId.value = savedDocId
                _lastReadDocument.value = restored
            } else {
                _documents.value = templates
            }
        } else {
            _documents.value = templates
        }
    }

    fun openDocument(doc: PdfDocumentData, targetPage: Int? = null) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        val pageToOpen = targetPage ?: doc.lastReadPageIndex ?: 0
        _currentPageIndex.value = pageToOpen.coerceIn(0, (doc.pages.size - 1).coerceAtLeast(0))
        _activeTool.value = EditorTool.SELECT
        clearSelection()
        val updated = doc.copy(
            lastModifiedTimestamp = System.currentTimeMillis(),
            lastReadPageIndex = pageToOpen
        )
        _currentDocument.value = updated
        _documents.update { list ->
            listOf(updated) + list.filter { it.id != doc.id }
        }
    }

    fun updateReadingProgress(pageIndex: Int) {
        val doc = _currentDocument.value ?: return
        val totalPages = doc.pages.size
        if (totalPages <= 0) return
        val progress = if (totalPages > 1) {
            (pageIndex.toFloat() / (totalPages - 1).toFloat()).coerceIn(0f, 1f)
        } else 1f
        val updated = doc.copy(
            readingProgress = progress,
            lastReadPageIndex = pageIndex,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
        _currentDocument.value = updated
        syncDocumentInList(updated)
        _lastReadDocument.value = updated
        _lastReadDocId.value = doc.id

        // Persist reading history across app restarts
        prefs.edit()
            .putString("last_read_doc_id", doc.id)
            .putString("last_read_doc_title", doc.title)
            .putInt("last_read_page_index", pageIndex)
            .putInt("last_read_total_pages", totalPages)
            .putFloat("last_read_progress", progress)
            .putLong("last_read_timestamp", System.currentTimeMillis())
            .apply()
        _lastReadDocId.value = doc.id
    }

    fun createNewBlankDocument(title: String = "New Blank Document.pdf") {
        val blank = TemplateProvider.createBlankDocument(title)
        _documents.update { listOf(blank) + it }
        openDocument(blank)
    }

    fun importPdfFromUri(context: Context, uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = PdfRendererHelper.importPdfFromUri(context, uri)
            _isLoading.value = false
            result.onSuccess { doc ->
                _documents.update { listOf(doc) + it }
                openDocument(doc)
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Failed to import PDF")
            }
        }
    }

    fun setPage(index: Int) {
        val maxIndex = (_currentDocument.value?.pages?.size ?: 1) - 1
        if (index in 0..maxIndex) {
            clearSelection()
            _currentPageIndex.value = index
        }
    }

    fun setTool(tool: EditorTool) {
        _activeTool.value = tool
        if (tool != EditorTool.SELECT) {
            clearSelection()
        }
    }

    fun selectText(id: String?) {
        _selectedTextId.value = id
        _selectedImageId.value = null
        _selectedShapeId.value = null
        _activeTool.value = EditorTool.SELECT
    }

    fun selectImage(id: String?) {
        _selectedImageId.value = id
        _selectedTextId.value = null
        _selectedShapeId.value = null
        _activeTool.value = EditorTool.SELECT
    }

    fun selectShape(id: String?) {
        _selectedShapeId.value = id
        _selectedTextId.value = null
        _selectedImageId.value = null
        _activeTool.value = EditorTool.SELECT
    }

    fun clearSelection() {
        _selectedTextId.value = null
        _selectedImageId.value = null
        _selectedShapeId.value = null
    }

    private fun pushUndoState(doc: PdfDocumentData) {
        undoStack.add(doc)
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        val doc = _currentDocument.value ?: return
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(doc)
            _currentDocument.value = previous
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            clearSelection()
        }
    }

    fun redo() {
        val doc = _currentDocument.value ?: return
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(doc)
            _currentDocument.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            clearSelection()
        }
    }

    // --- Page Level Modifications ---

    fun updateCurrentPage(updateBlock: (PdfPageData) -> PdfPageData) {
        val doc = _currentDocument.value ?: return
        val pageIdx = _currentPageIndex.value
        if (pageIdx !in doc.pages.indices) return

        pushUndoState(doc)

        val updatedPages = doc.pages.mapIndexed { idx, page ->
            if (idx == pageIdx) updateBlock(page) else page
        }
        val updatedDoc = doc.copy(
            lastModifiedTimestamp = System.currentTimeMillis(),
            pages = updatedPages
        )
        _currentDocument.value = updatedDoc
        syncDocumentInList(updatedDoc)
    }

    // --- Text Elements ---

    fun addTextElement(text: String, x: Float, y: Float, fontSize: Float = 16f, color: Long = 0xFF0F172AL) {
        val newElem = PdfTextElement(
            text = text,
            x = x,
            y = y,
            width = (text.length * fontSize * 0.65f).coerceIn(120f, 380f),
            height = (fontSize * 1.8f).coerceAtLeast(32f),
            fontSize = fontSize,
            color = color
        )
        updateCurrentPage { page ->
            page.copy(textElements = page.textElements + newElem)
        }
        selectText(newElem.id)
    }

    fun updateTextElement(element: PdfTextElement) {
        updateCurrentPage { page ->
            val updated = page.textElements.map { if (it.id == element.id) element else it }
            page.copy(textElements = updated)
        }
    }

    // --- Image Elements ---

    fun addImageElement(
        uriString: String? = null,
        localFilePath: String? = null,
        drawableResName: String? = null,
        x: Float = 60f,
        y: Float = 200f,
        width: Float = 160f,
        height: Float = 160f
    ) {
        val newImg = PdfImageElement(
            uriString = uriString,
            localFilePath = localFilePath,
            drawableResName = drawableResName,
            x = x,
            y = y,
            width = width,
            height = height
        )
        updateCurrentPage { page ->
            page.copy(imageElements = page.imageElements + newImg)
        }
        selectImage(newImg.id)
    }

    fun updateImageElement(element: PdfImageElement) {
        updateCurrentPage { page ->
            val updated = page.imageElements.map { if (it.id == element.id) element else it }
            page.copy(imageElements = updated)
        }
    }

    // --- Redaction & Replace ---

    fun addRedactionAndReplacement(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        replacementText: String? = null
    ) {
        val redaction = PdfRedaction(
            x = x,
            y = y,
            width = width,
            height = height
        )
        val textElem = if (!replacementText.isNullOrBlank()) {
            PdfTextElement(
                text = replacementText,
                x = x,
                y = y,
                width = width,
                height = height,
                fontSize = (height * 0.7f).coerceIn(10f, 28f)
            )
        } else null

        updateCurrentPage { page ->
            page.copy(
                redactions = page.redactions + redaction,
                textElements = if (textElem != null) page.textElements + textElem else page.textElements
            )
        }

        if (textElem != null) {
            selectText(textElem.id)
        }
    }

    // --- Annotations & Drawings ---

    fun addDrawingStroke(stroke: PdfAnnotationStroke) {
        updateCurrentPage { page ->
            page.copy(drawings = page.drawings + stroke)
        }
    }

    // --- Shapes & Stamps ---

    fun addShapeElement(type: ShapeType, stampText: String? = null, x: Float = 100f, y: Float = 200f) {
        val shape = PdfShapeElement(
            type = type,
            x = x,
            y = y,
            width = if (type == ShapeType.STAMP) 150f else 120f,
            height = if (type == ShapeType.STAMP) 60f else 80f,
            strokeColor = if (type == ShapeType.STAMP) 0xFF059669L else 0xFFE11D48L,
            stampText = stampText
        )
        updateCurrentPage { page ->
            page.copy(shapes = page.shapes + shape)
        }
        selectShape(shape.id)
    }

    fun updateShapeElement(element: PdfShapeElement) {
        updateCurrentPage { page ->
            val updated = page.shapes.map { if (it.id == element.id) element else it }
            page.copy(shapes = updated)
        }
    }

    // --- Delete Selected ---

    fun deleteSelectedElement() {
        val tId = _selectedTextId.value
        val iId = _selectedImageId.value
        val sId = _selectedShapeId.value

        if (tId != null) {
            updateCurrentPage { page ->
                page.copy(textElements = page.textElements.filter { it.id != tId })
            }
            _selectedTextId.value = null
        } else if (iId != null) {
            updateCurrentPage { page ->
                page.copy(imageElements = page.imageElements.filter { it.id != iId })
            }
            _selectedImageId.value = null
        } else if (sId != null) {
            updateCurrentPage { page ->
                page.copy(shapes = page.shapes.filter { it.id != sId })
            }
            _selectedShapeId.value = null
        }
    }

    // --- Page Management ---

    fun addBlankPage() {
        val doc = _currentDocument.value ?: return
        pushUndoState(doc)
        val newPage = PdfPageData(pageIndex = doc.pages.size)
        val updated = doc.copy(pages = doc.pages + newPage)
        _currentDocument.value = updated
        syncDocumentInList(updated)
        _currentPageIndex.value = updated.pages.lastIndex
    }

    fun duplicatePage(pageIndex: Int) {
        val doc = _currentDocument.value ?: return
        if (pageIndex !in doc.pages.indices) return
        pushUndoState(doc)
        val source = doc.pages[pageIndex]
        val duplicate = source.copy(
            pageIndex = doc.pages.size,
            textElements = source.textElements.map { it.copy(id = UUID.randomUUID().toString()) },
            imageElements = source.imageElements.map { it.copy(id = UUID.randomUUID().toString()) },
            shapes = source.shapes.map { it.copy(id = UUID.randomUUID().toString()) },
            drawings = source.drawings.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        val newPages = doc.pages.toMutableList().apply { add(pageIndex + 1, duplicate) }
        val reindexed = newPages.mapIndexed { idx, p -> p.copy(pageIndex = idx) }
        val updated = doc.copy(pages = reindexed)
        _currentDocument.value = updated
        syncDocumentInList(updated)
        _currentPageIndex.value = pageIndex + 1
    }

    fun deletePage(pageIndex: Int) {
        val doc = _currentDocument.value ?: return
        if (doc.pages.size <= 1) return // Keep at least one page
        pushUndoState(doc)
        val newPages = doc.pages.filterIndexed { idx, _ -> idx != pageIndex }
            .mapIndexed { idx, p -> p.copy(pageIndex = idx) }
        val updated = doc.copy(pages = newPages)
        _currentDocument.value = updated
        syncDocumentInList(updated)
        _currentPageIndex.value = pageIndex.coerceAtMost(updated.pages.lastIndex)
    }

    fun rotatePage(pageIndex: Int) {
        updateCurrentPage { page ->
            val nextRot = (page.rotation + 90) % 360
            page.copy(rotation = nextRot)
        }
    }

    fun updateDocumentTitle(newTitle: String) {
        val doc = _currentDocument.value ?: return
        val updated = doc.copy(title = newTitle)
        _currentDocument.value = updated
        syncDocumentInList(updated)
    }

    private fun syncDocumentInList(doc: PdfDocumentData) {
        _documents.update { list ->
            val exists = list.any { it.id == doc.id }
            if (exists) {
                list.map { if (it.id == doc.id) doc else it }
            } else {
                listOf(doc) + list
            }
        }
    }

    fun exportDocument(context: Context, onResult: (File?) -> Unit) {
        val doc = _currentDocument.value ?: run {
            onResult(null)
            return
        }
        viewModelScope.launch {
            _isExporting.value = true
            val result = PdfExportEngine.exportPdf(context, doc)
            _isExporting.value = false
            result.onSuccess { file ->
                onResult(file)
            }.onFailure {
                onResult(null)
            }
        }
    }
}
