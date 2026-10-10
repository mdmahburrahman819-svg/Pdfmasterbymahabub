package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.PdfAnnotationStroke
import com.example.model.PdfDocumentData
import com.example.model.PdfImageElement
import com.example.model.PdfPageData
import com.example.model.PdfRedaction
import com.example.model.PdfShapeElement
import com.example.model.PdfTextElement
import com.example.model.ShapeType
import com.example.model.TextAlignmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object PdfExportEngine {

    suspend fun exportPdf(context: Context, doc: PdfDocumentData): Result<File> =
        withContext(Dispatchers.IO) {
            try {
                val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
                val sanitizedTitle = doc.title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val finalFileName = if (sanitizedTitle.endsWith(".pdf", ignoreCase = true)) {
                    sanitizedTitle
                } else {
                    "$sanitizedTitle.pdf"
                }
                val outputFile = File(exportDir, "${System.currentTimeMillis()}_$finalFileName")

                val pdfDocument = PdfDocument()

                doc.pages.forEachIndexed { index, pageData ->
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        pageData.width.toInt().coerceAtLeast(100),
                        pageData.height.toInt().coerceAtLeast(100),
                        index + 1
                    ).create()

                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    renderPageContent(context, canvas, pageData)

                    pdfDocument.finishPage(page)
                }

                FileOutputStream(outputFile).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()

                Result.success(outputFile)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    private fun renderPageContent(context: Context, canvas: Canvas, page: PdfPageData) {
        // 1. Draw base page raster (if uploaded from PDF) or clear white
        if (page.baseImagePath != null && File(page.baseImagePath).exists()) {
            val baseBitmap = BitmapFactory.decodeFile(page.baseImagePath)
            if (baseBitmap != null) {
                val destRect = RectF(0f, 0f, page.width, page.height)
                canvas.drawBitmap(baseBitmap, null, destRect, null)
                baseBitmap.recycle()
            }
        } else {
            // White clean page canvas
            val bgPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, page.width, page.height, bgPaint)
        }

        // 2. Draw redactions (whiteouts to erase underlying printed text)
        page.redactions.forEach { red ->
            val redPaint = Paint().apply {
                color = red.color.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(red.x, red.y, red.x + red.width, red.y + red.height, redPaint)
        }

        // 3. Draw shapes & stamps
        page.shapes.forEach { shape ->
            drawShape(canvas, shape)
        }

        // 4. Draw image elements
        page.imageElements.forEach { imgElem ->
            drawImageElement(context, canvas, imgElem)
        }

        // 5. Draw text elements
        page.textElements.forEach { textElem ->
            drawTextElement(canvas, textElem)
        }

        // 6. Draw annotations & freehand drawings
        page.drawings.forEach { stroke ->
            drawAnnotationStroke(canvas, stroke)
        }
    }

    private fun drawTextElement(canvas: Canvas, elem: PdfTextElement) {
        canvas.save()
        if (elem.rotation != 0f) {
            canvas.rotate(elem.rotation, elem.x + elem.width / 2f, elem.y + elem.height / 2f)
        }

        // Background highlight / box
        if (elem.backgroundColor != null) {
            val bgPaint = Paint().apply {
                color = elem.backgroundColor.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(elem.x, elem.y, elem.x + elem.width, elem.y + elem.height, bgPaint)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = elem.color.toInt()
            textSize = elem.fontSize
            isUnderlineText = elem.isUnderline

            val styleFlags = when {
                elem.isBold && elem.isItalic -> Typeface.BOLD_ITALIC
                elem.isBold -> Typeface.BOLD
                elem.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }

            typeface = when (elem.fontFamily.lowercase()) {
                "serif" -> Typeface.create(Typeface.SERIF, styleFlags)
                "monospace" -> Typeface.create(Typeface.MONOSPACE, styleFlags)
                else -> Typeface.create(Typeface.SANS_SERIF, styleFlags)
            }
        }

        val lines = elem.text.split("\n")
        val fontMetrics = textPaint.fontMetrics
        val lineHeight = fontMetrics.bottom - fontMetrics.top + 4f
        var currentY = elem.y - fontMetrics.top

        for (line in lines) {
            val textWidth = textPaint.measureText(line)
            val currentX = when (elem.alignment) {
                TextAlignmentType.CENTER -> elem.x + (elem.width - textWidth) / 2f
                TextAlignmentType.RIGHT -> elem.x + elem.width - textWidth
                TextAlignmentType.LEFT -> elem.x
            }
            canvas.drawText(line, currentX, currentY, textPaint)
            currentY += lineHeight
        }

        canvas.restore()
    }

    private fun drawImageElement(context: Context, canvas: Canvas, elem: PdfImageElement) {
        val bitmap: Bitmap? = try {
            when {
                elem.localFilePath != null -> BitmapFactory.decodeFile(elem.localFilePath)
                elem.uriString != null -> {
                    val uri = Uri.parse(elem.uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                elem.drawableResName != null -> {
                    val resId = context.resources.getIdentifier(elem.drawableResName, "drawable", context.packageName)
                    if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }

        if (bitmap != null) {
            canvas.save()
            if (elem.rotation != 0f) {
                canvas.rotate(elem.rotation, elem.x + elem.width / 2f, elem.y + elem.height / 2f)
            }

            val destRect = RectF(elem.x, elem.y, elem.x + elem.width, elem.y + elem.height)

            if (elem.cornerRadius > 0f) {
                val clipPath = Path().apply {
                    addRoundRect(destRect, elem.cornerRadius, elem.cornerRadius, Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                alpha = (elem.opacity * 255).toInt().coerceIn(0, 255)
            }
            canvas.drawBitmap(bitmap, null, destRect, paint)

            if (elem.borderColor != null && elem.borderWidth > 0f) {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = elem.borderColor.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = elem.borderWidth
                }
                if (elem.cornerRadius > 0f) {
                    canvas.drawRoundRect(destRect, elem.cornerRadius, elem.cornerRadius, borderPaint)
                } else {
                    canvas.drawRect(destRect, borderPaint)
                }
            }

            canvas.restore()
            bitmap.recycle()
        }
    }

    private fun drawShape(canvas: Canvas, shape: PdfShapeElement) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = shape.strokeColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = shape.strokeWidth
        }

        val fillPaint = if (shape.fillColor != null) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = shape.fillColor.toInt()
                style = Paint.Style.FILL
            }
        } else null

        val rect = RectF(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height)

        when (shape.type) {
            ShapeType.RECTANGLE -> {
                fillPaint?.let { canvas.drawRect(rect, it) }
                canvas.drawRect(rect, strokePaint)
            }
            ShapeType.CIRCLE -> {
                fillPaint?.let { canvas.drawOval(rect, it) }
                canvas.drawOval(rect, strokePaint)
            }
            ShapeType.LINE -> {
                canvas.drawLine(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height, strokePaint)
            }
            ShapeType.ARROW -> {
                canvas.drawLine(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height, strokePaint)
                // Draw arrowhead
                val arrowHeadPaint = Paint(strokePaint).apply { style = Paint.Style.FILL }
                canvas.drawCircle(shape.x + shape.width, shape.y + shape.height, shape.strokeWidth * 2f, arrowHeadPaint)
            }
            ShapeType.CHECKMARK -> {
                val path = Path().apply {
                    moveTo(shape.x + shape.width * 0.15f, shape.y + shape.height * 0.5f)
                    lineTo(shape.x + shape.width * 0.45f, shape.y + shape.height * 0.85f)
                    lineTo(shape.x + shape.width * 0.9f, shape.y + shape.height * 0.15f)
                }
                strokePaint.strokeCap = Paint.Cap.ROUND
                strokePaint.strokeJoin = Paint.Join.ROUND
                canvas.drawPath(path, strokePaint)
            }
            ShapeType.STAMP -> {
                canvas.save()
                canvas.rotate(-10f, rect.centerX(), rect.centerY())

                // Double border for stamp look
                val outerStampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = shape.strokeColor.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 3.5f
                }
                canvas.drawRoundRect(rect, 8f, 8f, outerStampPaint)

                val innerRect = RectF(rect.left + 5f, rect.top + 5f, rect.right - 5f, rect.bottom - 5f)
                val innerStampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = shape.strokeColor.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
                canvas.drawRoundRect(innerRect, 6f, 6f, innerStampPaint)

                // Stamp text
                val stampText = shape.stampText ?: "APPROVED"
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = shape.strokeColor.toInt()
                    textSize = 20f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val metrics = textPaint.fontMetrics
                val textY = rect.centerY() - (metrics.top + metrics.bottom) / 2f
                canvas.drawText(stampText, rect.centerX(), textY, textPaint)

                canvas.restore()
            }
        }
    }

    private fun drawAnnotationStroke(canvas: Canvas, stroke: PdfAnnotationStroke) {
        if (stroke.points.size < 2) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toInt()
            style = Paint.Style.STROKE
            strokeWidth = stroke.strokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            if (stroke.isHighlighter) {
                alpha = 110 // Semitransparent highlighter
            }
        }

        val path = Path().apply {
            moveTo(stroke.points[0].x, stroke.points[0].y)
            for (i in 1 until stroke.points.size) {
                lineTo(stroke.points[i].x, stroke.points[i].y)
            }
        }

        canvas.drawPath(path, paint)
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share PDF Document")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun printPdf(context: Context, pdfFile: File, documentName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder(documentName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (destination == null) {
                    callback?.onWriteFailed("Destination descriptor is null")
                    return
                }

                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(documentName, adapter, PrintAttributes.Builder().build())
    }

    suspend fun savePdfToDownloads(context: Context, sourceFile: File, displayName: String): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val cleanName = if (displayName.endsWith(".pdf", ignoreCase = true)) displayName else "$displayName.pdf"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, cleanName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            FileInputStream(sourceFile).use { input ->
                                input.copyTo(out)
                            }
                        }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        context.contentResolver.update(uri, values, null, null)
                        return@withContext uri
                    }
                }
                // Fallback: return file provider URI
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", sourceFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
