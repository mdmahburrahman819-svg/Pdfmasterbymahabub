package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.PdfImageElement

@Composable
fun ImageInspectorDialog(
    element: PdfImageElement,
    onDismiss: () -> Unit,
    onSave: (PdfImageElement) -> Unit,
    onPickNewImage: () -> Unit,
    onOpenUnsplashPicker: () -> Unit,
    onDelete: () -> Unit
) {
    var width by remember { mutableFloatStateOf(element.width) }
    var height by remember { mutableFloatStateOf(element.height) }
    var opacity by remember { mutableFloatStateOf(element.opacity) }
    var cornerRadius by remember { mutableFloatStateOf(element.cornerRadius) }
    var borderWidth by remember { mutableFloatStateOf(element.borderWidth) }
    var borderColor by remember { mutableStateOf(element.borderColor ?: 0xFF0F172AL) }

    val presetColors = listOf(
        0xFF0F172AL, // Dark Slate
        0xFFE11D48L, // Crimson Red
        0xFF2563EBL, // Royal Blue
        0xFF059669L, // Emerald
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
                    text = "Image Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_image_element_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete image element",
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
                // Replace Image options
                Text(text = "Replace Image", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickNewImage,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("replace_from_gallery_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Gallery")
                    }
                    OutlinedButton(
                        onClick = onOpenUnsplashPicker,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("replace_from_unsplash_btn")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Unsplash")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dimensions
                Text(text = "Size (${width.toInt()} x ${height.toInt()} pt)", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = width,
                    onValueChange = {
                        val ratio = if (width > 0) height / width else 1f
                        width = it
                        height = it * ratio
                    },
                    valueRange = 40f..400f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Opacity
                Text(text = "Opacity: ${(opacity * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.1f..1.0f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Corner Radius
                Text(text = "Corner Radius: ${cornerRadius.toInt()} pt", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = cornerRadius,
                    onValueChange = { cornerRadius = it },
                    valueRange = 0f..60f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Border
                Text(text = "Border Width: ${borderWidth.toInt()} pt", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = borderWidth,
                    onValueChange = { borderWidth = it },
                    valueRange = 0f..12f
                )

                if (borderWidth > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Border Color", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        presetColors.forEach { col ->
                            val isSelected = borderColor == col
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { borderColor = col }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        element.copy(
                            width = width,
                            height = height,
                            opacity = opacity,
                            cornerRadius = cornerRadius,
                            borderWidth = borderWidth,
                            borderColor = if (borderWidth > 0f) borderColor else null
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_image_settings_btn")
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
