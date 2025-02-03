package com.lensnap.app.ui.theme.screens.joinEventScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.compareTo
import kotlin.text.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingCodeScreen(onJoinSuccess: (String) -> Unit, navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val fieldSpacing = 5.dp
    val fieldWidth = (screenWidth - (fieldSpacing * 8)) / 6 // Adjusted for spacing and centering
    val fieldHeight = fieldWidth * 1.0f // Adjusted height for uniform look

    var errorMessage by remember { mutableStateOf("") }
    val pairingCode = remember { mutableStateListOf(*Array(8) { "" }) }
    val focusRequesters = remember { List(8) { FocusRequester() } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f) // Limit the height of the composable
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Enable scrolling to handle keyboard overlap
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Title
            Text(
                "Enter Pairing Code",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Custom vertical grid layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row with 5 cells
                Row(
                    horizontalArrangement = Arrangement.spacedBy(fieldSpacing),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    repeat(5) { index ->
                        OutlinedTextField(
                            value = pairingCode[index],
                            onValueChange = { newChar ->
                                if (newChar.length <= 1) {
                                    pairingCode[index] = newChar
                                }
                                if (newChar.isNotEmpty() && index < 7) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            },
                            modifier = Modifier
                                .size(fieldWidth, fieldHeight)
                                .focusRequester(focusRequesters[index]),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFF0d6efd),
                                unfocusedBorderColor = Color(0xFF0d6efd),
                                cursorColor = Color(0xFF0d6efd)
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                imeAction = if (index < 4) ImeAction.Next else ImeAction.Done
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom row with 3 cells
                Row(
                    horizontalArrangement = Arrangement.spacedBy(fieldSpacing),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    repeat(3) { index ->
                        OutlinedTextField(
                            value = pairingCode[index + 5],
                            onValueChange = { newChar ->
                                if (newChar.length <= 1) {
                                    pairingCode[index + 5] = newChar
                                }
                                if (newChar.isNotEmpty() && index + 5 < 7) {
                                    focusRequesters[index + 6].requestFocus()
                                }
                            },
                            modifier = Modifier
                                .size(fieldWidth, fieldHeight)
                                .focusRequester(focusRequesters[index + 5]),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFF0d6efd),
                                unfocusedBorderColor = Color(0xFF0d6efd),
                                cursorColor = Color(0xFF0d6efd)
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                imeAction = if (index + 5 < 7) ImeAction.Next else ImeAction.Done
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom

            // Join Event button
            Button(
                onClick = {
                    val enteredCode = pairingCode.joinToString("")
                    if (enteredCode.length == 8) {
                        verifyPairingCode(
                            enteredCode,
                            onSuccess = { eventDocument ->
                                val eventId = eventDocument.id
                                onJoinSuccess(eventId)
                                navController.navigate("photoCapture/$eventId")
                            },
                            onError = { error -> errorMessage = error }
                        )
                    } else {
                        errorMessage = "Please enter a valid 8-character pairing code"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0d6efd)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Join Event",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeApi::class)
//@Composable
//fun PairingCodeScreen(onJoinSuccess: (String) -> Unit, navController: NavController) {
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val fieldSpacing = 8.dp
//    val fieldWidth = (screenWidth - (fieldSpacing * 6)) / 6 // Adjusted for spacing and centering
//    val fieldHeight = fieldWidth * 1.0f // Adjusted height to reduce size
//
//    var errorMessage by remember { mutableStateOf("") }
//    val pairingCode = remember { mutableStateListOf(*Array(8) { "" }) }
//    val focusRequesters = remember { List(8) { FocusRequester() } }
//
//    Box(
//        contentAlignment = Alignment.Center
//    ) {
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Title
//            Text("Enter Pairing Code", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
//
//            // Custom vertical grid layout
//            Column(
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            ) {
//                // Top row with 5 cells
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(fieldSpacing),
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                ) {
//                    repeat(5) { index ->
//                        OutlinedTextField(
//                            value = pairingCode[index],
//                            onValueChange = { newChar ->
//                                if (newChar.length <= 1) {
//                                    pairingCode[index] = newChar
//                                }
//                                if (newChar.isNotEmpty() && index < 7) {
//                                    focusRequesters[index + 1].requestFocus()
//                                }
//                            },
//                            modifier = Modifier
//                                .size(fieldWidth, fieldHeight)
//                                .focusRequester(focusRequesters[index]),
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedBorderColor = Color(0xFF0d6efd),
//                                unfocusedBorderColor = Color(0xFF0d6efd),
//                                cursorColor = Color(0xFF0d6efd)
//                            ),
//                            textStyle = MaterialTheme.typography.bodyLarge.copy(
//                                color = Color.Black,
//                                textAlign = TextAlign.Center
//                            ),
//                            singleLine = true,
//                            keyboardOptions = KeyboardOptions.Default.copy(
//                                keyboardType = KeyboardType.Text,
//                                imeAction = if (index < 4) ImeAction.Next else ImeAction.Done
//                            )
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Bottom row with 3 cells, centered under top row
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(fieldSpacing),
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                ) {
//                    repeat(3) { index ->
//                        OutlinedTextField(
//                            value = pairingCode[index + 5],
//                            onValueChange = { newChar ->
//                                if (newChar.length <= 1) {
//                                    pairingCode[index + 5] = newChar
//                                }
//                                if (newChar.isNotEmpty() && index + 5 < 7) {
//                                    focusRequesters[index + 6].requestFocus()
//                                }
//                            },
//                            modifier = Modifier
//                                .size(fieldWidth, fieldHeight)
//                                .focusRequester(focusRequesters[index + 5]),
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedBorderColor = Color(0xFF0d6efd),
//                                unfocusedBorderColor = Color(0xFF0d6efd)
//                            ),
//                            textStyle = MaterialTheme.typography.bodyLarge.copy(
//                                color = Color.Black,
//                                textAlign = TextAlign.Center
//                            ),
//                            singleLine = true,
//                            keyboardOptions = KeyboardOptions.Default.copy(
//                                keyboardType = KeyboardType.Text,
//                                imeAction = if (index + 5 < 7) ImeAction.Next else ImeAction.Done
//                            )
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                // Error message
//                if (errorMessage.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = errorMessage,
//                        color = Color.Red,
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
//                    )
//                }
//            }
//        }
//
//        // Join Event button at the bottom
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 32.dp)
//        ) {
//            Button(
//                onClick = {
//                    val enteredCode = pairingCode.joinToString("")
//                    if (enteredCode.length == 8) {
//                        verifyPairingCode(
//                            enteredCode,
//                            onSuccess = { eventDocument ->
//                                val eventId = eventDocument.id
//                                onJoinSuccess(eventId)
//                                navController.navigate("photoCapture/$eventId")
//                            },
//                            onError = { error -> errorMessage = error }
//                        )
//                    } else {
//                        errorMessage = "Please enter a valid 8-character pairing code"
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth(0.7f)
//                    .height(48.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0d6efd)),
//                shape = RoundedCornerShape(8.dp), // Less border radius
//
//            ) {
//                Text(
//                    text = "Join Event",
//                    color = Color.White,
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//        }
//    }
//}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeApi::class)
//@Composable
//fun PairingCodeScreen(onJoinSuccess: (String) -> Unit, navController: NavController) {
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val fieldSpacing = 8.dp
//    val fieldWidth = (screenWidth - (fieldSpacing * 12)) / 5 // For 5 fields and 4 spacers
//    val fieldHeight = fieldWidth * 1.2f // Adjust height based on width
//
//    var errorMessage by remember { mutableStateOf("") }
//    val pairingCode = remember { mutableStateListOf(*Array(8) { "" }) }
//    val focusRequesters = remember { List(8) { FocusRequester() } }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Title
//        Text(
//            text = "Enter Pairing Code",
//            style = MaterialTheme.typography.titleLarge,
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//                .padding(bottom = 16.dp)
//        )
//
//        // Custom vertical grid layout
//        Column(
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally),
//        ) {
//            // Top row with 5 cells
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(fieldSpacing),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                repeat(5) { index ->
//                    OutlinedTextField(
//                        value = pairingCode[index],
//                        onValueChange = { newChar ->
//                            if (newChar.length <= 1) {
//                                pairingCode[index] = newChar
//                            }
//                            if (newChar.isNotEmpty() && index < 7) {
//                                focusRequesters[index + 1].requestFocus()
//                            }
//                        },
//                        modifier = Modifier
//                            .size(fieldWidth, fieldHeight)
//                            .focusRequester(focusRequesters[index]),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            focusedBorderColor = Color(0xFF0d6efd),
//                            unfocusedBorderColor = Color(0xFF0d6efd)
//                        ),
//                        textStyle = MaterialTheme.typography.bodyLarge.copy(
//                            color = Color.Black,
//                            textAlign = TextAlign.Center
//                        ),
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Text,
//                            imeAction = if (index < 4) ImeAction.Next else ImeAction.Done
//                        )
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Bottom row with 3 cells, centered under top row
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(fieldSpacing),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Spacer(modifier = Modifier.weight(1f)) // Spacer to center-align bottom row
//                repeat(3) { index ->
//                    OutlinedTextField(
//                        value = pairingCode[index + 5],
//                        onValueChange = { newChar ->
//                            if (newChar.length <= 1) {
//                                pairingCode[index + 5] = newChar
//                            }
//                            if (newChar.isNotEmpty() && index + 5 < 7) {
//                                focusRequesters[index + 6].requestFocus()
//                            }
//                        },
//                        modifier = Modifier
//                            .size(fieldWidth, fieldHeight)
//                            .focusRequester(focusRequesters[index + 5]),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            focusedBorderColor = Color(0xFF0d6efd),
//                            unfocusedBorderColor = Color(0xFF0d6efd)
//                        ),
//                        textStyle = MaterialTheme.typography.bodyLarge.copy(
//                            color = Color.Black,
//                            textAlign = TextAlign.Center
//                        ),
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Text,
//                            imeAction = if (index + 5 < 7) ImeAction.Next else ImeAction.Done
//                        )
//                    )
//                }
//                Spacer(modifier = Modifier.weight(1f)) // Spacer to balance alignment
//            }
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Join Event button
//        Button(
//            onClick = {
//                val enteredCode = pairingCode.joinToString("")
//                if (enteredCode.length == 8) {
//                    verifyPairingCode(
//                        enteredCode,
//                        onSuccess = { eventDocument ->
//                            val eventId = eventDocument.id
//                            onJoinSuccess(eventId)
//                            navController.navigate("photoCapture/$eventId")
//                        },
//                        onError = { error -> errorMessage = error }
//                    )
//                } else {
//                    errorMessage = "Please enter a valid 8-character pairing code"
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth(0.7f)
//                .height(48.dp)
//                .clip(RoundedCornerShape(12.dp)),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0d6efd))
//        ) {
//            Text(
//                text = "Join Event",
//                color = Color.White,
//                style = MaterialTheme.typography.bodyLarge
//            )
//        }
//
//        // Error message
//        if (errorMessage.isNotEmpty()) {
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = errorMessage,
//                color = Color.Red,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//    }
//}

//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import com.google.firebase.firestore.DocumentSnapshot
//import android.util.Log
//import androidx.navigation.NavController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PairingCodeScreen(onJoinSuccess: (String) -> Unit, navController: NavController) {
//    var pairingCode by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Enter Pairing Code") }
//            )
//        },
//        content = { paddingValues ->
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(16.dp)
//            ) {
//                TextField(
//                    value = pairingCode,
//                    onValueChange = { pairingCode = it },
//                    label = { Text("Pairing Code") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = {
//                    if (pairingCode.isNotBlank()) {
//                        Log.d("PairingCodeScreen", "Pairing code entered: $pairingCode")
//                        verifyPairingCode(pairingCode, onSuccess = { eventDocument: DocumentSnapshot ->
//                            val eventId = eventDocument.id
//                            Log.d("PairingCodeScreen", "Pairing code verified successfully with event ID: $eventId")
//                            onJoinSuccess(eventId)
//                            navController.navigate("photoCapture/$eventId")
//                        }, onError = { error: String ->
//                            errorMessage = error
//                            Log.e("PairingCodeScreen", "Pairing code verification failed: $error")
//                        })
//                    } else {
//                        errorMessage = "Pairing code cannot be blank"
//                    }
//                }) {
//                    Text("Join Event")
//                }
//                if (errorMessage.isNotEmpty()) {
//                    Text(errorMessage, color = Color.Red)
//                }
//            }
//        }
//    )
//}
