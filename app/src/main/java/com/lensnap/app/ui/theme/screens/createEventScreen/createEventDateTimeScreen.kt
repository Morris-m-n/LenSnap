package com.lensnap.app.ui.theme.screens.createEventScreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import java.util.Calendar

val PrimaryBlue = Color(0xFF0D6EFD)

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
                title = { Text("Event Details", fontSize = 35.sp, fontWeight = FontWeight.Thin) }
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
                if (eventDate.isEmpty()) {
                    Button(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),  // Increased height
                        shape = RoundedCornerShape(8.dp), // Less border radius
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Date")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),  // Increased height
                        shape = RoundedCornerShape(8.dp), // Less border radius
                        border = BorderStroke(1.dp, PrimaryBlue),  // Primary blue border
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(eventDate)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (eventTime.isEmpty()) {
                    Button(
                        onClick = { timePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),  // Increased height
                        shape = RoundedCornerShape(8.dp), // Less border radius
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Clock icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Time")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { timePickerDialog.show() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),  // Increased height
                        shape = RoundedCornerShape(8.dp), // Less border radius
                        border = BorderStroke(1.dp, PrimaryBlue),  // Primary blue border
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Clock icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(eventTime)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

//                eventImageBitmap?.let { bitmap ->
//                    Image(
//                        bitmap = bitmap.asImageBitmap(),
//                        contentDescription = "Event Image",
//                        modifier = Modifier
//                            .size(150.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                    )
//                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        ),
                        border = BorderStroke(1.dp, PrimaryBlue),  // Primary blue border
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),  // Increased height
                        shape = RoundedCornerShape(8.dp), // Less border radius
                    ) {
                        Text("Previous")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (eventDate.isNotEmpty() && eventTime.isNotEmpty()) {
                                onNext(eventDate, eventTime)
                            } else {
                                // Show error or feedback to user
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),  // Increased height
                        shape = RoundedCornerShape(8.dp), // Less border radius
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Forward Arrow")
                        }
                    }
                }
            }
        }
    )
}