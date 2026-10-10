package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PDF Master", appName)
  }

  @Test
  fun `verify sample templates creation`() {
    val templates = com.example.model.TemplateProvider.createSampleTemplates()
    org.junit.Assert.assertTrue(templates.isNotEmpty())
    val agreement = templates[0]
    org.junit.Assert.assertTrue(agreement.pages[0].textElements.isNotEmpty())
  }

  @Test
  fun `verify reading history persistence and restoration`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    // Clear prefs for fresh test run
    app.getSharedPreferences("pdf_master_reading_history", Context.MODE_PRIVATE).edit().clear().commit()

    val viewModel1 = com.example.viewmodel.PdfViewModel(app)
    // On fresh install, no reading history should exist
    org.junit.Assert.assertNull("Initial lastReadDocument must be null", viewModel1.lastReadDocument.value)

    val doc = viewModel1.documents.value.first()
    viewModel1.openDocument(doc)
    viewModel1.updateReadingProgress(pageIndex = 2)

    val lastRead = viewModel1.lastReadDocument.value
    org.junit.Assert.assertNotNull("lastReadDocument should now be present", lastRead)
    assertEquals(doc.id, lastRead?.id)
    assertEquals(2, lastRead?.lastReadPageIndex)

    // Simulate app restart with a new ViewModel instance
    val viewModel2 = com.example.viewmodel.PdfViewModel(app)
    val restoredLastRead = viewModel2.lastReadDocument.value
    org.junit.Assert.assertNotNull("Restored lastReadDocument must persist across instances", restoredLastRead)
    assertEquals(doc.id, restoredLastRead?.id)
    assertEquals(2, restoredLastRead?.lastReadPageIndex)

    // Verify openDocument restores to saved page index
    viewModel2.openDocument(restoredLastRead!!)
    assertEquals(2, viewModel2.currentPageIndex.value)
  }
}
