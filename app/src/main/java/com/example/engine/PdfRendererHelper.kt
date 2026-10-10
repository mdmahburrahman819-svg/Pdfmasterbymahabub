package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.model.PdfDocumentData
import com.example.model.PdfPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PdfRendererHelper {

    suspend fun importPdfFromUri(context: Context, uri: Uri): Result<PdfDocumentData> =
        withContext(Dispatchers.IO) {
            try {
                val fileName = getFileNameFromUri(context, uri) ?: "Imported_Document.pdf"
                val cacheDir = File(context.cacheDir, "imported_pdfs").apply { mkdirs() }
                val targetPdfFile = File(cacheDir, "${UUID.randomUUID()}_$fileName")

                // Copy stream to cache
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetPdfFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext Result.failure(Exception("Cannot open document input stream"))

                val pfd = ParcelFileDescriptor.open(targetPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val pageCount = renderer.pageCount

                val pages = mutableListOf<PdfPageData>()
                val pageImageDir = File(context.filesDir, "pdf_pages").apply { mkdirs() }

                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    val originalWidth = page.width.toFloat()
                    val originalHeight = page.height.toFloat()

                    // High resolution rendering (2.0x scale) strictly preserving true aspect ratio
                    val scaleFactor = 2.0f
                    val renderWidth = (originalWidth * scaleFactor).toInt().coerceAtLeast(1)
                    val renderHeight = (originalHeight * scaleFactor).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    // White background before rendering
                    bitmap.eraseColor(android.graphics.Color.WHITE)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    // Save bitmap to internal storage
                    val pageImageFile = File(pageImageDir, "page_${UUID.randomUUID()}.png")
                    FileOutputStream(pageImageFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                    }
                    bitmap.recycle()

                    pages.add(
                        PdfPageData(
                            pageIndex = i,
                            width = originalWidth,
                            height = originalHeight,
                            baseImagePath = pageImageFile.absolutePath
                        )
                    )
                }

                renderer.close()
                pfd.close()

                val doc = PdfDocumentData(
                    title = fileName,
                    sourceUri = uri.toString(),
                    pages = if (pages.isNotEmpty()) pages else listOf(PdfPageData(pageIndex = 0))
                )
                Result.success(doc)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }
}
