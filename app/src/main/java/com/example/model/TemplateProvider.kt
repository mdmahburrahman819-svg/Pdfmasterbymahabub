package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TemplateProvider {

    fun createSampleTemplates(): List<PdfDocumentData> {
        val todayStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())

        // 1. Business Service Agreement
        val agreement = PdfDocumentData(
            id = "template_agreement",
            title = "Consulting Agreement.pdf",
            pages = listOf(
                PdfPageData(
                    pageIndex = 0,
                    width = 595f,
                    height = 842f,
                    textElements = listOf(
                        PdfTextElement(
                            text = "INDEPENDENT CONSULTING AGREEMENT",
                            x = 40f,
                            y = 48f,
                            width = 515f,
                            height = 30f,
                            fontSize = 20f,
                            isBold = true,
                            color = 0xFF0F172AL,
                            alignment = TextAlignmentType.CENTER
                        ),
                        PdfTextElement(
                            text = "Effective Date: $todayStr",
                            x = 40f,
                            y = 86f,
                            width = 250f,
                            height = 20f,
                            fontSize = 11f,
                            isItalic = true,
                            color = 0xFF475569L
                        ),
                        PdfTextElement(
                            text = "Reference No: CA-2026-8941",
                            x = 350f,
                            y = 86f,
                            width = 200f,
                            height = 20f,
                            fontSize = 11f,
                            color = 0xFF475569L,
                            alignment = TextAlignmentType.RIGHT
                        ),
                        PdfTextElement(
                            text = "1. PARTIES & ENGAGEMENT",
                            x = 40f,
                            y = 125f,
                            width = 515f,
                            height = 22f,
                            fontSize = 13f,
                            isBold = true,
                            color = 0xFFE11D48L
                        ),
                        PdfTextElement(
                            text = "This Consulting Services Agreement is entered into between Vertex Digital Solutions LLC (\"Company\") and Apex Design Studio (\"Consultant\"). Consultant agrees to perform digital product architecture and user experience services according to agreed milestones.",
                            x = 40f,
                            y = 152f,
                            width = 515f,
                            height = 48f,
                            fontSize = 10.5f,
                            color = 0xFF1E293BL
                        ),
                        PdfTextElement(
                            text = "2. SCOPE OF SERVICES & DELIVERABLES",
                            x = 40f,
                            y = 212f,
                            width = 515f,
                            height = 22f,
                            fontSize = 13f,
                            isBold = true,
                            color = 0xFFE11D48L
                        ),
                        PdfTextElement(
                            text = "• Phase 1: High-fidelity interactive design system and component architecture.\n• Phase 2: Full functional implementation of modern document rendering engine.\n• Phase 3: Comprehensive testing, security review, and multi-format export pipeline.",
                            x = 40f,
                            y = 238f,
                            width = 515f,
                            height = 55f,
                            fontSize = 10.5f,
                            color = 0xFF1E293BL
                        ),
                        PdfTextElement(
                            text = "3. COMPENSATION & PAYMENT SCHEDULE",
                            x = 40f,
                            y = 305f,
                            width = 515f,
                            height = 22f,
                            fontSize = 13f,
                            isBold = true,
                            color = 0xFFE11D48L
                        ),
                        PdfTextElement(
                            text = "Company agrees to pay Consultant a fixed sum of $8,500.00 USD upon delivery of final milestones. Payments are net-15 following digital invoice submission.",
                            x = 40f,
                            y = 330f,
                            width = 515f,
                            height = 36f,
                            fontSize = 10.5f,
                            color = 0xFF1E293BL
                        ),
                        PdfTextElement(
                            text = "4. SIGNATURES & ACKNOWLEDGEMENT",
                            x = 40f,
                            y = 385f,
                            width = 515f,
                            height = 22f,
                            fontSize = 13f,
                            isBold = true,
                            color = 0xFFE11D48L
                        ),
                        PdfTextElement(
                            text = "Company Representative:\nSarah Jenkins, VP of Engineering",
                            x = 40f,
                            y = 420f,
                            width = 240f,
                            height = 40f,
                            fontSize = 11f,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "Consultant Representative:\nAlex Mercer, Lead Architect",
                            x = 310f,
                            y = 420f,
                            width = 240f,
                            height = 40f,
                            fontSize = 11f,
                            color = 0xFF0F172AL
                        )
                    ),
                    imageElements = listOf(
                        PdfImageElement(
                            drawableResName = "pdf_icon_fg_1791399220170",
                            x = 40f,
                            y = 475f,
                            width = 70f,
                            height = 70f,
                            cornerRadius = 12f
                        )
                    ),
                    shapes = listOf(
                        PdfShapeElement(
                            type = ShapeType.STAMP,
                            x = 340f,
                            y = 470f,
                            width = 150f,
                            height = 65f,
                            strokeColor = 0xFF059669L,
                            stampText = "APPROVED"
                        )
                    )
                )
            )
        )

        // 2. Creative Commercial Invoice
        val invoice = PdfDocumentData(
            id = "template_invoice",
            title = "Commercial Invoice #1042.pdf",
            pages = listOf(
                PdfPageData(
                    pageIndex = 0,
                    width = 595f,
                    height = 842f,
                    textElements = listOf(
                        PdfTextElement(
                            text = "INVOICE",
                            x = 40f,
                            y = 40f,
                            width = 250f,
                            height = 36f,
                            fontSize = 28f,
                            isBold = true,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "Invoice #: INV-2026-1042\nDate: $todayStr\nDue Date: Net 30 Days",
                            x = 350f,
                            y = 42f,
                            width = 205f,
                            height = 50f,
                            fontSize = 11f,
                            alignment = TextAlignmentType.RIGHT,
                            color = 0xFF475569L
                        ),
                        PdfTextElement(
                            text = "BILLED TO:\nAcme Corporation Inc.\n100 Enterprise Boulevard, Suite 400\nSan Francisco, CA 94107",
                            x = 40f,
                            y = 110f,
                            width = 250f,
                            height = 65f,
                            fontSize = 11f,
                            color = 0xFF1E293BL
                        ),
                        PdfTextElement(
                            text = "ISSUED BY:\nStudio Nordic Digital\n742 Evergreen Terrace\nSeattle, WA 98101",
                            x = 350f,
                            y = 110f,
                            width = 205f,
                            height = 65f,
                            fontSize = 11f,
                            alignment = TextAlignmentType.RIGHT,
                            color = 0xFF1E293BL
                        ),
                        PdfTextElement(
                            text = "Description                                                        Qty     Rate         Amount",
                            x = 40f,
                            y = 195f,
                            width = 515f,
                            height = 20f,
                            fontSize = 11f,
                            isBold = true,
                            color = 0xFFE11D48L
                        ),
                        PdfTextElement(
                            text = "1. Mobile App UX/UI Concept Design                      1      $3,200.00    $3,200.00\n2. High-Performance PDF Engine Architecture        1      $2,800.00    $2,800.00\n3. Cloud Typography & Asset Integration                 1      $1,500.00    $1,500.00",
                            x = 40f,
                            y = 225f,
                            width = 515f,
                            height = 65f,
                            fontSize = 11f,
                            fontFamily = "monospace",
                            color = 0xFF1E293BL
                        ),
                        PdfTextElement(
                            text = "SUBTOTAL:    $7,500.00\nTAX (8.5%):     $637.50\nTOTAL DUE:   $8,137.50",
                            x = 330f,
                            y = 310f,
                            width = 225f,
                            height = 60f,
                            fontSize = 12f,
                            isBold = true,
                            fontFamily = "monospace",
                            alignment = TextAlignmentType.RIGHT,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "Thank you for your business! Please submit payment via wire transfer or electronic ACH.",
                            x = 40f,
                            y = 400f,
                            width = 515f,
                            height = 25f,
                            fontSize = 10f,
                            isItalic = true,
                            color = 0xFF64748BL,
                            alignment = TextAlignmentType.CENTER
                        )
                    ),
                    imageElements = listOf(
                        PdfImageElement(
                            drawableResName = "splash_hero_1791399236200",
                            x = 40f,
                            y = 445f,
                            width = 180f,
                            height = 100f,
                            cornerRadius = 8f
                        )
                    ),
                    shapes = listOf(
                        PdfShapeElement(
                            type = ShapeType.STAMP,
                            x = 350f,
                            y = 460f,
                            width = 140f,
                            height = 60f,
                            strokeColor = 0xFF059669L,
                            stampText = "PAID"
                        )
                    )
                )
            )
        )

        // 3. Modern Executive Resume
        val resume = PdfDocumentData(
            id = "template_resume",
            title = "Executive Resume.pdf",
            pages = listOf(
                PdfPageData(
                    pageIndex = 0,
                    width = 595f,
                    height = 842f,
                    textElements = listOf(
                        PdfTextElement(
                            text = "JORDAN K. VANCE",
                            x = 160f,
                            y = 45f,
                            width = 395f,
                            height = 32f,
                            fontSize = 24f,
                            isBold = true,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "Principal Software Architect • Android & Cloud Systems",
                            x = 160f,
                            y = 80f,
                            width = 395f,
                            height = 20f,
                            fontSize = 12f,
                            isItalic = true,
                            color = 0xFFE11D48L
                        ),
                        PdfTextElement(
                            text = "Email: jordan.vance@example.com | Phone: +1 (555) 329-8472 | San Francisco, CA",
                            x = 160f,
                            y = 105f,
                            width = 395f,
                            height = 20f,
                            fontSize = 9.5f,
                            color = 0xFF64748BL
                        ),
                        PdfTextElement(
                            text = "PROFESSIONAL SUMMARY",
                            x = 40f,
                            y = 150f,
                            width = 515f,
                            height = 20f,
                            fontSize = 12f,
                            isBold = true,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "Dynamic engineering leader with 10+ years specializing in high-throughput mobile applications, vector graphics pipelines, and resilient distributed cloud infrastructure. Demonstrated track record delivering apps with 20M+ active users.",
                            x = 40f,
                            y = 175f,
                            width = 515f,
                            height = 45f,
                            fontSize = 10.5f,
                            color = 0xFF334155L
                        ),
                        PdfTextElement(
                            text = "WORK EXPERIENCE",
                            x = 40f,
                            y = 230f,
                            width = 515f,
                            height = 20f,
                            fontSize = 12f,
                            isBold = true,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "Senior Staff Android Engineer — Stellar Systems (2022 - Present)\n• Architected real-time vector document rendering engine with sub-16ms frame times.\n• Led cross-functional team of 14 mobile developers shipping bi-weekly release cycles.\n\nLead Mobile Developer — Pulse Technologies (2018 - 2022)\n• Built offline-first synchronized data layer supporting 5M+ daily synchronizations.",
                            x = 40f,
                            y = 255f,
                            width = 515f,
                            height = 100f,
                            fontSize = 10.5f,
                            color = 0xFF334155L
                        ),
                        PdfTextElement(
                            text = "EDUCATION & CERTIFICATIONS",
                            x = 40f,
                            y = 370f,
                            width = 515f,
                            height = 20f,
                            fontSize = 12f,
                            isBold = true,
                            color = 0xFF0F172AL
                        ),
                        PdfTextElement(
                            text = "B.S. in Computer Science — University of California, Berkeley (Honors)\nCertified Google Cloud Architect & Kotlin Specialist",
                            x = 40f,
                            y = 395f,
                            width = 515f,
                            height = 35f,
                            fontSize = 10.5f,
                            color = 0xFF334155L
                        )
                    ),
                    imageElements = listOf(
                        PdfImageElement(
                            drawableResName = "pdf_icon_fg_1791399220170",
                            x = 40f,
                            y = 40f,
                            width = 100f,
                            height = 100f,
                            cornerRadius = 50f // Circular profile avatar
                        )
                    )
                )
            )
        )

        // 0. Documents from user's latest screenshot ( আলিম ২০২৬ আরবি ১ম পত্র প্রশ্ন (2), Untitled, আসসালামু আলাইকুম। কেমন আছেন )
        val alimDoc = PdfDocumentData(
            id = "doc_alim_arabic",
            title = "আলিম ২০২৬ আরবি ১ম পত্র প্রশ্ন (2).pdf",
            fileSizeKb = 1240,
            readingProgress = null,
            lastReadPageIndex = null,
            pages = List(4) { idx ->
                PdfPageData(
                    pageIndex = idx,
                    textElements = listOf(
                        PdfTextElement(
                            text = "আলিম পরীক্ষা ২০২৬ - আরবি ১ম পত্র",
                            x = 50f,
                            y = 60f,
                            width = 495f,
                            height = 30f,
                            fontSize = 20f,
                            isBold = true,
                            alignment = TextAlignmentType.CENTER
                        ),
                        PdfTextElement(
                            text = "পৃষ্ঠা ${idx + 1} / ৪ · সর্বমোট নম্বর: ১০০ · সময়: ৩ ঘণ্টা",
                            x = 50f,
                            y = 100f,
                            width = 495f,
                            height = 24f,
                            fontSize = 12f,
                            color = 0xFF475569L,
                            alignment = TextAlignmentType.CENTER
                        )
                    )
                )
            }
        )

        val untitledDoc = PdfDocumentData(
            id = "doc_untitled_page",
            title = "Untitled.pdf",
            fileSizeKb = 345,
            readingProgress = null,
            pages = listOf(
                PdfPageData(
                    pageIndex = 0,
                    textElements = listOf(
                        PdfTextElement(
                            text = "Untitled Document Notes",
                            x = 50f,
                            y = 60f,
                            width = 400f,
                            height = 30f,
                            fontSize = 18f
                        )
                    )
                )
            )
        )

        val assalamuDoc = PdfDocumentData(
            id = "doc_assalamu_pdf",
            title = "আসসালামু আলাইকুম। কেমন আছেন.pdf",
            fileSizeKb = 890,
            readingProgress = null,
            lastReadPageIndex = null,
            pages = List(3) { idx ->
                PdfPageData(
                    pageIndex = idx,
                    textElements = listOf(
                        PdfTextElement(
                            text = "আসসালামু আলাইকুম ওয়ারাহমাতুল্লাহ",
                            x = 50f,
                            y = 60f,
                            width = 495f,
                            height = 30f,
                            fontSize = 20f,
                            isBold = true,
                            alignment = TextAlignmentType.CENTER
                        ),
                        PdfTextElement(
                            text = "পৃষ্ঠা ${idx + 1} / ৩ · পিডিএফ মাস্টার সিকিউর ভিউয়ার",
                            x = 50f,
                            y = 100f,
                            width = 495f,
                            height = 24f,
                            fontSize = 12f,
                            color = 0xFF0D9488L,
                            alignment = TextAlignmentType.CENTER
                        )
                    )
                )
            }
        )

        // 1. Protected Scan Document
        val scanDoc = PdfDocumentData(
            id = "doc_scan_pdf",
            title = "Scan.pdf",
            isProtected = true,
            fileSizeKb = 773,
            pages = listOf(
                PdfPageData(
                    pageIndex = 0,
                    width = 595f,
                    height = 842f,
                    textElements = listOf(
                        PdfTextElement(
                            text = "OFFICIAL SCANNED RECORD",
                            x = 50f,
                            y = 60f,
                            width = 495f,
                            height = 32f,
                            fontSize = 22f,
                            isBold = true,
                            color = 0xFF004D40L,
                            alignment = TextAlignmentType.CENTER
                        ),
                        PdfTextElement(
                            text = "Document ID: SCAN-2026-0810-SECURE\nClassification: Confidential & Protected",
                            x = 50f,
                            y = 110f,
                            width = 495f,
                            height = 40f,
                            fontSize = 12f,
                            color = 0xFF0D9488L
                        ),
                        PdfTextElement(
                            text = "This document has been verified and encrypted by PDF Master on-device security vault. No unauthenticated modifications are allowed without passcode authorization.",
                            x = 50f,
                            y = 165f,
                            width = 495f,
                            height = 50f,
                            fontSize = 11f,
                            color = 0xFF334155L
                        )
                    ),
                    shapes = listOf(
                        PdfShapeElement(
                            type = ShapeType.STAMP,
                            x = 350f,
                            y = 230f,
                            width = 150f,
                            height = 65f,
                            strokeColor = 0xFF00796BL,
                            stampText = "VERIFIED"
                        )
                    )
                )
            )
        )

        return listOf(alimDoc, untitledDoc, assalamuDoc, scanDoc, agreement, invoice, resume)
    }

    fun createBlankDocument(title: String = "Untitled Document.pdf"): PdfDocumentData {
        return PdfDocumentData(
            title = title,
            pages = listOf(
                PdfPageData(
                    pageIndex = 0,
                    width = 595f,
                    height = 842f,
                    textElements = listOf(
                        PdfTextElement(
                            text = "Tap here to start editing text",
                            x = 60f,
                            y = 80f,
                            width = 300f,
                            height = 30f,
                            fontSize = 18f,
                            color = 0xFF0F172AL
                        )
                    )
                )
            )
        )
    }
}
