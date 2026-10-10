package com.example.model

import java.util.UUID

enum class TextAlignmentType {
    LEFT, CENTER, RIGHT
}

enum class ShapeType {
    RECTANGLE,
    CIRCLE,
    LINE,
    ARROW,
    CHECKMARK,
    STAMP
}

data class PdfTextElement(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val x: Float, // in PDF points (0..595 for A4 width)
    val y: Float, // in PDF points (0..842 for A4 height)
    val width: Float = 160f,
    val height: Float = 40f,
    val fontSize: Float = 16f,
    val color: Long = 0xFF0F172AL, // Slate900
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val alignment: TextAlignmentType = TextAlignmentType.LEFT,
    val fontFamily: String = "sans-serif", // "sans-serif", "serif", "monospace"
    val backgroundColor: Long? = null,
    val rotation: Float = 0f
)

data class PdfImageElement(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String? = null,
    val localFilePath: String? = null,
    val drawableResName: String? = null,
    val x: Float,
    val y: Float,
    val width: Float = 140f,
    val height: Float = 140f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val cornerRadius: Float = 0f,
    val borderColor: Long? = null,
    val borderWidth: Float = 0f
)

data class PdfRedaction(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Long = 0xFFFFFFFFL // Default whiteout to cover existing text in uploaded PDF
)

data class StrokePoint(
    val x: Float,
    val y: Float
)

data class PdfAnnotationStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<StrokePoint> = emptyList(),
    val color: Long = 0xFFE11D48L,
    val strokeWidth: Float = 4f,
    val isHighlighter: Boolean = false
)

data class PdfShapeElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType = ShapeType.RECTANGLE,
    val x: Float,
    val y: Float,
    val width: Float = 120f,
    val height: Float = 60f,
    val strokeColor: Long = 0xFF0F172AL,
    val fillColor: Long? = null,
    val strokeWidth: Float = 2f,
    val stampText: String? = null // For STAMP type: "APPROVED", "PAID", "DRAFT", "CONFIDENTIAL"
)

data class PdfPageData(
    val pageIndex: Int,
    val width: Float = 595f, // Standard A4 width in pt
    val height: Float = 842f, // Standard A4 height in pt
    val rotation: Int = 0, // 0, 90, 180, 270
    val baseImagePath: String? = null, // Path to rendered raster from uploaded PDF
    val textElements: List<PdfTextElement> = emptyList(),
    val imageElements: List<PdfImageElement> = emptyList(),
    val redactions: List<PdfRedaction> = emptyList(),
    val drawings: List<PdfAnnotationStroke> = emptyList(),
    val shapes: List<PdfShapeElement> = emptyList()
)

data class PdfDocumentData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourceUri: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val isProtected: Boolean = false,
    val fileSizeKb: Int = 773,
    val readingProgress: Float? = null, // e.g. 0.50f for 50%, null for no bar
    val lastReadPageIndex: Int? = null, // 0-indexed last read page
    val pages: List<PdfPageData> = listOf(PdfPageData(pageIndex = 0))
)
