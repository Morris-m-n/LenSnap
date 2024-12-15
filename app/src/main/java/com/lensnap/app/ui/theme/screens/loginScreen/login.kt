package com.lensnap.app.ui.theme.screens.signInScreen

import LoadingTextAnimation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import com.lensnap.app.data.UserViewModel

@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    viewModel: UserViewModel = viewModel(),
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit // Added navigation to sign-up screen
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.observeAsState(false)

    val aliceBlueColor = Color(0xFFF0F8FF)
    val bootstrapPrimaryColor = Color(0xFF007BFF)
    val slightlyDarkerAliceBlue = Color(0xFFECF2FF) // Slightly darker than Alice Blue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(aliceBlueColor),
        contentAlignment = Alignment.Center // Center the box
    ) {
        if (isLoading) {
            LoadingTextAnimation()
        } else {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .graphicsLayer {
                        shadowElevation = 8.dp.toPx()
                        shape = RoundedCornerShape(12.dp)
                        clip = true
                    }
                    .background(
                        color = slightlyDarkerAliceBlue,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, bootstrapPrimaryColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 24.sp,
                        color = bootstrapPrimaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Sign In",
                        fontSize = 20.sp,
                        color = bootstrapPrimaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = bootstrapPrimaryColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent),
                        textStyle = TextStyle(color = Color.Black), // User input text color
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedLabelColor = bootstrapPrimaryColor,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = bootstrapPrimaryColor,
                            focusedBorderColor = bootstrapPrimaryColor,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = bootstrapPrimaryColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent),
                        textStyle = TextStyle(color = Color.Black), // User input text color
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedLabelColor = bootstrapPrimaryColor,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = bootstrapPrimaryColor,
                            focusedBorderColor = bootstrapPrimaryColor,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.signIn(email, password) { success, error ->
                                if (success) {
                                    onSignInSuccess()
                                } else {
                                    errorMessage = error
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .background(bootstrapPrimaryColor),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bootstrapPrimaryColor
                        ),
                        shape = RoundedCornerShape(8.dp) // Lesser border radius
                    ) {
                        Text("Sign In", color = Color.White)
                    }
                    errorMessage?.let {
                        Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ClickableText(
                        text = AnnotatedString("Don't have an account? Sign Up"),
                        onClick = { onNavigateToSignUp() },
                        style = TextStyle(
                            color = bootstrapPrimaryColor, // Set text color to Bootstrap Primary Blue
                            textDecoration = TextDecoration.Underline,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
