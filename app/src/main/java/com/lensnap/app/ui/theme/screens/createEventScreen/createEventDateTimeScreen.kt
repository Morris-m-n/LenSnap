package com.lensnap.app.ui.theme.screens.createEventScreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDateTimeScreen(
    onPrevious: () -> Unit,
    onNext: (String, String) -> Unit,
    eventName: String,
    eventDescription: String,
    eventLocation: String,
    eventImageUri: Uri?,
    eventImageBitmap: Bitmap?
) {
    var eventDate by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            eventDate = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            eventTime = "$hour:$minute"
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Date & Time") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { datePickerDialog.show() }) {
                    Text(text = if (eventDate.isEmpty()) "Select Date" else eventDate)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { timePickerDialog.show() }) {
                    Text(text = if (eventTime.isEmpty()) "Select Time" else eventTime)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onPrevious) {
                        Text("Previous")
                    }
                    Button(onClick = { onNext(eventDate, eventTime) }) {
                        Text("Next")
                    }
                }
            }
        }
    )
}
