package com.lensnap.app.ui.theme.screens.joinEventScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.DocumentSnapshot
import android.util.Log
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingCodeScreen(onJoinSuccess: (String) -> Unit, navController: NavController) {
    var pairingCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Pairing Code") }
            )
        },
        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                TextField(
                    value = pairingCode,
                    onValueChange = { pairingCode = it },
                    label = { Text("Pairing Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (pairingCode.isNotBlank()) {
                        Log.d("PairingCodeScreen", "Pairing code entered: $pairingCode")
                        verifyPairingCode(pairingCode, onSuccess = { eventDocument: DocumentSnapshot ->
                            val eventId = eventDocument.id
                            Log.d("PairingCodeScreen", "Pairing code verified successfully with event ID: $eventId")
                            onJoinSuccess(eventId)
                            navController.navigate("photoCapture/$eventId")
                        }, onError = { error: String ->
                            errorMessage = error
                            Log.e("PairingCodeScreen", "Pairing code verification failed: $error")
                        })
                    } else {
                        errorMessage = "Pairing code cannot be blank"
                    }
                }) {
                    Text("Join Event")
                }
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red)
                }
            }
        }
    )
}
