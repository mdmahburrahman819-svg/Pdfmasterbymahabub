package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PdfTextElement
import com.example.model.TextAlignmentType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextInspectorDialog(
    element: PdfTextElement,
    onDismiss: () -> Unit,
    onSave: (PdfTextElement) -> Unit,
    onDelete: () -> Unit
) {
    var textContent by remember { mutableStateOf(element.text) }
    var fontSize by remember { mutableFloatStateOf(element.fontSize) }
    var selectedColor by remember { mutableStateOf(element.color) }
    var isBold by remember { mutableStateOf(element.isBold) }
    var isItalic by remember { mutableStateOf(element.isItalic) }
    var isUnderline by remember { mutableStateOf(element.isUnderline) }
    var alignment by remember { mutableStateOf(element.alignment) }
    var fontFamily by remember { mutableStateOf(element.fontFamily) }
    var backgroundColor by remember { mutableStateOf(element.backgroundColor) }

    val presetColors = listOf(
        0xFF0F172AL, // Dark Slate
        0xFFE11D48L, // Crimson Red
        0xFF2563EBL, // Royal Blue
        0xFF059669L, // Emerald
        0xFFD97706L, // Amber
        0xFF7C3AEDL, // Violet
        0xFFFFFFFFL  // White
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Format Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_text_element_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete text element",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Text input
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    label = { Text("Text Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_text_input"),
                    minLines = 2,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Font size slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Font Size: ${fontSize.toInt()} sp", style = MaterialTheme.typography.bodyMedium)
                    Row {
                        TextButton(onClick = { fontSize = (fontSize - 2).coerceAtLeast(8f) }) {
                            Text("A-", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { fontSize = (fontSize + 2).coerceAtMost(72f) }) {
                            Text("A+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 8f..64f,
                    steps = 27
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Style toggles
                Text(text = "Style & Alignment", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isBold = !isBold },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isBold) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                    }
                    IconButton(
                        onClick = { isItalic = !isItalic },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isItalic) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                    }
                    IconButton(
                        onClick = { isUnderline = !isUnderline },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isUnderline) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { alignment = TextAlignmentType.LEFT },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (alignment == TextAlignmentType.LEFT) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatAlignLeft, contentDescription = "Align Left")
                    }
                    IconButton(
                        onClick = { alignment = TextAlignmentType.CENTER },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (alignment == TextAlignmentType.CENTER) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatAlignCenter, contentDescription = "Align Center")
                    }
                    IconButton(
                        onClick = { alignment = TextAlignmentType.RIGHT },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (alignment == TextAlignmentType.RIGHT) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatAlignRight, contentDescription = "Align Right")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Font Family
                Text(text = "Font Family", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("sans-serif" to "Modern", "serif" to "Editorial", "monospace" to "Code").forEach { (family, label) ->
                        FilterChip(
                            selected = fontFamily.equals(family, ignoreCase = true),
                            onClick = { fontFamily = family },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Text Color
                Text(text = "Text Color", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { colorValue ->
                        val isSelected = selectedColor == colorValue
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorValue }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Background / Highlight Box
                Text(text = "Background Highlight", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = backgroundColor == null,
                        onClick = { backgroundColor = null },
                        label = { Text("None") }
                    )
                    FilterChip(
                        selected = backgroundColor == 0xFFFFFFFFL,
                        onClick = { backgroundColor = 0xFFFFFFFFL },
                        label = { Text("Whiteout") }
                    )
                    FilterChip(
                        selected = backgroundColor == 0xFFFEF08AL,
                        onClick = { backgroundColor = 0xFFFEF08AL },
                        label = { Text("Yellow") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        element.copy(
                            text = textContent,
                            fontSize = fontSize,
                            color = selectedColor,
                            isBold = isBold,
                            isItalic = isItalic,
                            isUnderline = isUnderline,
                            alignment = alignment,
                            fontFamily = fontFamily,
                            backgroundColor = backgroundColor,
                            width = (textContent.length * fontSize * 0.65f).coerceIn(element.width, 500f)
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_text_formatting_btn")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
