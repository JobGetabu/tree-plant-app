package com.mobiletreeplantingapp.ui.screen.navigation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiletreeplantingapp.data.model.SavedTree

@Composable
fun SavedTreeCard(
    tree: SavedTree,
    onEdit: (SavedTree) -> Unit,
    onDelete: (SavedTree) -> Unit,
    onStartPlanting: (SavedTree) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tree.species,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = { onEdit(tree) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { onDelete(tree) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
            Text(
                text = tree.notes,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { onStartPlanting(tree) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Continue Planting")
            }
        }
    }
} 