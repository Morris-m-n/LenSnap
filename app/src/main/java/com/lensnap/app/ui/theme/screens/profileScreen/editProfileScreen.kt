package com.lensnap.app.ui.theme.screens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import com.lensnap.app.R
import com.lensnap.app.data.UserViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.lensnap.app.models.UserRegistration

private val AliceBlue = Color(0xFFF0F8FF)

@Composable
fun EditProfileScreen(
    user: UserRegistration,
    viewModel: UserViewModel,
    onSave: (UserRegistration) -> Unit,
    onCancel: () -> Unit
) {
    val usernameState = remember { mutableStateOf(user.username) }
    val emailState = remember { mutableStateOf(user.email) }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profilePhotoUri = it }
    }

    Scaffold(
//        topBar = { CustomTopAppBar() },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AliceBlue)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (profilePhotoUri != null) {
                        Image(
                            painter = rememberImagePainter(profilePhotoUri),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.placeholder),
                            contentDescription = "Add Profile Photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = usernameState.value,
                        onValueChange = { usernameState.value = it },
                        label = { Text("Username") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = emailState.value,
                        onValueChange = { emailState.value = it },
                        label = { Text("Email") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        showDialog = true // Show confirmation dialog
                    }) {
                        Text("Save")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { onCancel() }) {
                        Text("Cancel")
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Confirm Update") },
                        text = { Text("Are you sure you want to update your profile information?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                if (profilePhotoUri != null) {
                                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, profilePhotoUri)
                                    viewModel.uploadProfilePhoto(bitmap, onSuccess = { url ->
                                        val updatedUser = user.copy(
                                            username = usernameState.value,
                                            email = emailState.value,
                                            profilePhotoUrl = url // Use URL from Firebase
                                        )
                                        onSave(updatedUser)
                                    }, onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    })
                                } else {
                                    val updatedUser = user.copy(
                                        username = usernameState.value,
                                        email = emailState.value
                                    )
                                    onSave(updatedUser)
                                }
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    )
}
