package com.lensnap.app.ui.theme.screens.signUpScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopicsSelectionScreen(
    onComplete: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    val topics = listOf("Entertainment", "Sports", "Technology", "Health", "Travel")
    val selectedTopics = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Step 4: Topics of Interest", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        topics.forEach { topic ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (selectedTopics.contains(topic)) {
                            selectedTopics.remove(topic)
                        } else {
                            selectedTopics.add(topic)
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedTopics.contains(topic),
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(topic)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = { onComplete(selectedTopics) }) {
                Text("Complete")
            }
        }
    }
}
