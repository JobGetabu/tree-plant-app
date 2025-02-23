package com.mobiletreeplantingapp.ui.screen.navigation.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.data.model.SavedTree

@Composable
fun EditTreeDialog(
    tree: SavedTree,
    onDismiss: () -> Unit,
    onConfirm: (SavedTree) -> Unit
) {
    var species by remember { mutableStateOf(tree.species) }
    var notes by remember { mutableStateOf(tree.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tree") },
        text = {
            Column {
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(tree.copy(
                        species = species,
                        notes = notes
                    ))
                    onDismiss()
                },
                enabled = species.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 