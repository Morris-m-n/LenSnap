import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.R
import kotlinx.coroutines.launch

//@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun SignUpScreen(
//    viewModel: UserViewModel = viewModel(),
//    onSignUpSuccess: () -> Unit,
//    onNavigateToSignIn: () -> Unit // Added navigation to sign-in screen
//) {
//    var username by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    // Image Picker
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        profilePhotoUri = uri
//    }
//
//    val isLoading by viewModel.isLoading.observeAsState(false)
//
//    val aliceBlueColor = Color(0xFFF0F8FF)
//    val bootstrapPrimaryColor = Color(0xFF007BFF)
//    val slightlyDarkerAliceBlue = Color(0xFFECF2FF) // Slightly darker than Alice Blue
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(aliceBlueColor),
//        contentAlignment = Alignment.Center // Center the box
//    ) {
//        if (isLoading) {
//            LoadingTextAnimation()
//        } else {
//            Box(
//                modifier = Modifier
//                    .padding(16.dp)
//                    .graphicsLayer {
//                        shadowElevation = 8.dp.toPx()
//                        shape = RoundedCornerShape(12.dp)
//                        clip = true
//                    }
//                    .background(
//                        color = slightlyDarkerAliceBlue,
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    .border(1.dp, bootstrapPrimaryColor, RoundedCornerShape(12.dp)),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = "Welcome",
//                        fontSize = 24.sp,
//                        color = bootstrapPrimaryColor,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                    Text(
//                        text = "Sign Up",
//                        fontSize = 20.sp,
//                        color = bootstrapPrimaryColor,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//                    OutlinedTextField(
//                        value = username,
//                        onValueChange = { username = it },
//                        label = { Text("Username", color = bootstrapPrimaryColor) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.Transparent),
//                        textStyle = TextStyle(color = Color.Black), // User input text color
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            focusedLabelColor = bootstrapPrimaryColor,
//                            unfocusedLabelColor = Color.Gray,
//                            cursorColor = bootstrapPrimaryColor,
//                            focusedBorderColor = bootstrapPrimaryColor,
//                            unfocusedBorderColor = Color.Gray
//                        )
//                    )
//                    OutlinedTextField(
//                        value = email,
//                        onValueChange = { email = it },
//                        label = { Text("Email", color = bootstrapPrimaryColor) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.Transparent),
//                        textStyle = TextStyle(color = Color.Black), // User input text color
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            focusedLabelColor = bootstrapPrimaryColor,
//                            unfocusedLabelColor = Color.Gray,
//                            cursorColor = bootstrapPrimaryColor,
//                            focusedBorderColor = bootstrapPrimaryColor,
//                            unfocusedBorderColor = Color.Gray
//                        )
//                    )
//                    OutlinedTextField(
//                        value = password,
//                        onValueChange = { password = it },
//                        label = { Text("Password", color = bootstrapPrimaryColor) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.Transparent),
//                        textStyle = TextStyle(color = Color.Black), // User input text color
//                        visualTransformation = PasswordVisualTransformation(),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            focusedLabelColor = bootstrapPrimaryColor,
//                            unfocusedLabelColor = Color.Gray,
//                            cursorColor = bootstrapPrimaryColor,
//                            focusedBorderColor = bootstrapPrimaryColor,
//                            unfocusedBorderColor = Color.Gray
//                        )
//                    )
//                    OutlinedTextField(
//                        value = confirmPassword,
//                        onValueChange = { confirmPassword = it },
//                        label = { Text("Confirm Password", color = bootstrapPrimaryColor) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.Transparent),
//                        textStyle = TextStyle(color = Color.Black), // User input text color
//                        visualTransformation = PasswordVisualTransformation(),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            focusedLabelColor = bootstrapPrimaryColor,
//                            unfocusedLabelColor = Color.Gray,
//                            cursorColor = bootstrapPrimaryColor,
//                            focusedBorderColor = bootstrapPrimaryColor,
//                            unfocusedBorderColor = Color.Gray
//                        )
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Profile Photo
//                    profilePhotoUri?.let { uri ->
//                        Image(
//                            painter = rememberImagePainter(uri),
//                            contentDescription = null,
//                            modifier = Modifier.size(100.dp)
//                        )
//                    }
//                    Button(
//                        onClick = { launcher.launch("image/*") },
//                        modifier = Modifier
//                            .padding(top = 16.dp)
//                            .background(bootstrapPrimaryColor),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = bootstrapPrimaryColor
//                        ),
//                        shape = RoundedCornerShape(8.dp) // Lesser border radius
//                    ) {
//                        Text(text = if (profilePhotoUri == null) "Select Profile Photo" else "Change Profile Photo", color = Color.White)
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = {
//                            if (password == confirmPassword) {
//                                viewModel.signUp(email, password, username, profilePhotoUri) { success, error ->
//                                    if (success) {
//                                        onSignUpSuccess()
//                                    } else {
//                                        errorMessage = error
//                                    }
//                                }
//                            } else {
//                                errorMessage = "Passwords do not match"
//                            }
//                        },
//                        modifier = Modifier
//                            .padding(top = 16.dp)
//                            .background(bootstrapPrimaryColor),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = bootstrapPrimaryColor
//                        ),
//                        shape = RoundedCornerShape(8.dp) // Lesser border radius
//                    ) {
//                        Text("Sign Up", color = Color.White)
//                    }
//                    errorMessage?.let {
//                        Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//                    ClickableText(
//                        text = AnnotatedString("Already have an account? Sign In"),
//                        onClick = { onNavigateToSignIn() },
//                        style = TextStyle(
//                            color = bootstrapPrimaryColor, // Set text color to Bootstrap Primary Blue
//                            textDecoration = TextDecoration.Underline,
//                            textAlign = TextAlign.Center
//                        )
//                    )
//                }
//            }
//        }
//    }
//}

//WORKING VERSION
//@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun SignUpScreen(
//    viewModel: UserViewModel = viewModel(),
//    onSignUpSuccess: () -> Unit,
//    onNavigateToSignIn: () -> Unit // Added navigation to sign-in screen
//) {
//    var username by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) } // State for password visibility
//    var confirmPasswordVisible by remember { mutableStateOf(false) } // State for confirm password visibility
//    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    // Image Picker
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        profilePhotoUri = uri
//    }
//
//    val isLoading by viewModel.isLoading.observeAsState(false)
//
//    val aliceBlueColor = Color(0xFFF0F8FF)
//    val bootstrapPrimaryColor = Color(0xFF0d6efd)
//    val textFieldColor = Color(0xFF424242) // Dark Gray for text fields
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(aliceBlueColor),
//        contentAlignment = Alignment.Center // Center the box
//    ) {
//        if (isLoading) {
//            LoadingTextAnimation()
//        } else {
//            Box(
//                modifier = Modifier
//                    .padding(16.dp)
//                    .graphicsLayer {
//                        shadowElevation = 8.dp.toPx()
//                        shape = RoundedCornerShape(12.dp)
//                        clip = true
//                    }
//                    .background(
//                        color = aliceBlueColor,
//                        shape = RoundedCornerShape(12.dp)
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                LazyColumn(
//                    modifier = Modifier.padding(16.dp),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    item {
//                        Text(
//                            text = "Welcome",
//                            fontSize = 24.sp,
//                            color = textFieldColor,
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                    }
//                    item {
//                        Text(
//                            text = "Sign Up",
//                            fontSize = 20.sp,
//                            color = textFieldColor,
//                            modifier = Modifier.padding(bottom = 16.dp)
//                        )
//                    }
//                    item {
//                        OutlinedTextField(
//                            value = username,
//                            onValueChange = { username = it },
//                            label = { Text("Username", color = textFieldColor) },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color.Transparent),
//                            textStyle = TextStyle(color = Color.Black), // User input text color
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedLabelColor = bootstrapPrimaryColor,
//                                unfocusedLabelColor = Color.Gray,
//                                cursorColor = bootstrapPrimaryColor,
//                                focusedBorderColor = bootstrapPrimaryColor,
//                                unfocusedBorderColor = Color.Gray
//                            )
//                        )
//                    }
//                    item {
//                        OutlinedTextField(
//                            value = email,
//                            onValueChange = { email = it },
//                            label = { Text("Email", color = textFieldColor) },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color.Transparent),
//                            textStyle = TextStyle(color = Color.Black), // User input text color
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedLabelColor = bootstrapPrimaryColor,
//                                unfocusedLabelColor = Color.Gray,
//                                cursorColor = bootstrapPrimaryColor,
//                                focusedBorderColor = bootstrapPrimaryColor,
//                                unfocusedBorderColor = Color.Gray
//                            )
//                        )
//                    }
//                    item {
//                        OutlinedTextField(
//                            value = password,
//                            onValueChange = { password = it },
//                            label = { Text("Password", color = textFieldColor) },
//                            trailingIcon = {
//                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                                    Icon(
//                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                                        contentDescription = "Toggle Password Visibility"
//                                    )
//                                }
//                            },
//                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color.Transparent),
//                            textStyle = TextStyle(color = Color.Black), // User input text color
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedLabelColor = bootstrapPrimaryColor,
//                                unfocusedLabelColor = Color.Gray,
//                                cursorColor = bootstrapPrimaryColor,
//                                focusedBorderColor = bootstrapPrimaryColor,
//                                unfocusedBorderColor = Color.Gray
//                            )
//                        )
//                    }
//                    item {
//                        OutlinedTextField(
//                            value = confirmPassword,
//                            onValueChange = { confirmPassword = it },
//                            label = { Text("Confirm Password", color = textFieldColor) },
//                            trailingIcon = {
//                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
//                                    Icon(
//                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                                        contentDescription = "Toggle Confirm Password Visibility"
//                                    )
//                                }
//                            },
//                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color.Transparent),
//                            textStyle = TextStyle(color = Color.Black), // User input text color
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedLabelColor = bootstrapPrimaryColor,
//                                unfocusedLabelColor = Color.Gray,
//                                cursorColor = bootstrapPrimaryColor,
//                                focusedBorderColor = bootstrapPrimaryColor,
//                                unfocusedBorderColor = Color.Gray
//                            )
//                        )
//                    }
//                    item {
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // Profile Photo
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .size(100.dp)
//                                .background(Color.LightGray, RoundedCornerShape(8.dp)) // Box shaped with rounded edges
//                                .clickable { launcher.launch("image/*") },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            profilePhotoUri?.let { uri ->
//                                Image(
//                                    painter = rememberImagePainter(uri),
//                                    contentDescription = null,
//                                    modifier = Modifier
//                                        .size(100.dp)
//                                        .clip(RoundedCornerShape(8.dp)) // Box shaped with rounded edges
//                                )
//                            } ?: Icon(
//                                imageVector = Icons.Default.CameraAlt,
//                                contentDescription = "Add Profile Photo",
//                                tint = Color.DarkGray
//                            )
//                        }
//                    }
//
//                    item {
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                    item {
//                        Button(
//                            onClick = {
//                                if (password == confirmPassword) {
//                                    viewModel.signUp(email, password, username, profilePhotoUri) { success, error ->
//                                        if (success) {
//                                            onSignUpSuccess()
//                                        } else {
//                                            errorMessage = error
//                                        }
//                                    }
//                                } else {
//                                    errorMessage = "Passwords do not match"
//                                }
//                            },
//                            modifier = Modifier
//                                .padding(top = 16.dp)
//                                .clip(RoundedCornerShape(8.dp)), // Less border radius
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = bootstrapPrimaryColor
//                            )
//                        ) {
//                            Text("Sign Up", color = Color.White)
//                        }
//                    }
//                    item {
//                        errorMessage?.let {
//                            Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
//                        }
//                    }
//
//                    item {
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                    item {
//                        ClickableText(
//                            text = AnnotatedString("Already have an account? Sign In"),
//                            onClick = { onNavigateToSignIn() },
//                            style = TextStyle(
//                                color = bootstrapPrimaryColor, // Set text color to Bootstrap Primary Blue
//                                textDecoration = TextDecoration.Underline,
//                                textAlign = TextAlign.Center
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

//WORKING PAGED VERSION
@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun SignUpScreen(
    viewModel: UserViewModel = viewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Image Picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePhotoUri = uri
    }

    val isLoading by viewModel.isLoading.observeAsState(false)
    val aliceBlueColor = Color(0xFFF0F8FF)
    val bootstrapPrimaryColor = Color(0xFF0d6efd)
    val textFieldColor = Color(0xFF424242)

    val pagerState = rememberPagerState() // Pager state for horizontal scrolling
    val coroutineScope = rememberCoroutineScope() // Declare a coroutine scope

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(aliceBlueColor),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Step ${pagerState.currentPage + 1} of 3",
            color = bootstrapPrimaryColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        if (isLoading) {
            LoadingTextAnimation()
        } else {
            HorizontalPager(
                count = 3, // Three pages
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress indicator at the top
//                    Text(
//                        text = "Step ${page + 1} of 3",
//                        color = bootstrapPrimaryColor,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.align(Alignment.Start)
//                    )
                    if (page == 0) {
                        // First page: App logo, username, email
//                        Icon(
//                            painter = painterResource(id = R.drawable.join),
//                            contentDescription = "Join Event",
//                            tint = bootstrapPrimaryColor,
//                            modifier = Modifier.size(100.dp)
//                        )
                        Image(
                            painter = painterResource(id = R.drawable.join), // Replace with your app logo resource
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Sign Up",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Thin,
//                            color = textFieldColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username", color = textFieldColor) },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.Transparent)
                                .padding(bottom = 16.dp),
                            textStyle = TextStyle(color = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = bootstrapPrimaryColor,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = bootstrapPrimaryColor,
                                focusedBorderColor = bootstrapPrimaryColor,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", color = textFieldColor) },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.Transparent)
                                .padding(bottom = 16.dp),
                            textStyle = TextStyle(color = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = bootstrapPrimaryColor,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = bootstrapPrimaryColor,
                                focusedBorderColor = bootstrapPrimaryColor,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        ClickableText(
                            text = AnnotatedString("Already have an account? Sign In"),
                            onClick = { onNavigateToSignIn() },
                            style = TextStyle(
                                color = bootstrapPrimaryColor, // Set text color to Bootstrap Primary Blue
                                textDecoration = TextDecoration.Underline,
                                textAlign = TextAlign.Center
                            )
                        )
                    } else if (page == 1) {
                        // Second page: password fields
                        Text(
                            text = "Secure Your Account",
                            fontSize = 20.sp,
                            color = textFieldColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = textFieldColor) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.Password,
                                        contentDescription = "Toggle Password Visibility",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF0d6efd)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(bottom = 16.dp),
                            textStyle = TextStyle(color = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = bootstrapPrimaryColor,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = bootstrapPrimaryColor,
                                focusedBorderColor = bootstrapPrimaryColor,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password", color = textFieldColor) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.Password,
                                        contentDescription = "Toggle Confirm Password Visibility",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF0d6efd)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(bottom = 16.dp),
                            textStyle = TextStyle(color = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedLabelColor = bootstrapPrimaryColor,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = bootstrapPrimaryColor,
                                focusedBorderColor = bootstrapPrimaryColor,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    } else if (page == 2) {
                        // Third page: profile image
                        Text(
                            text = "Add Your Profile Photo",
                            fontSize = 20.sp,
                            color = textFieldColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            profilePhotoUri?.let { uri ->
                                Image(
                                    painter = rememberImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(8.dp)), // Box shaped with rounded edges
                                    contentScale = ContentScale.Crop
                                )
                            } ?: Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Profile Photo",
                                tint = Color.DarkGray
                            )
                        }
                        Text(
                            text = "This can be changed later",
                            color = textFieldColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = {
                                if (password == confirmPassword) {
                                    viewModel.signUp(email, password, username, profilePhotoUri) { success, error ->
                                        if (success) {
                                            onSignUpSuccess()
                                        } else {
                                            errorMessage = error
                                        }
                                    }
                                } else {
                                    errorMessage = "Passwords do not match"
                                }
                            },
                            modifier = Modifier
                                .padding(top = 16.dp),
//                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = bootstrapPrimaryColor
                            ),
                            shape = RoundedCornerShape(8.dp) // Less border radius
                        ) {
                            Text("Sign Up", color = Color.White)
                        }
                        errorMessage?.let {
                            Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
                // Next button at the bottom right
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (page < 2) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            },
                            modifier = Modifier
                                .padding(16.dp),
//                                .clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = bootstrapPrimaryColor
                            ),
                            shape = RoundedCornerShape(8.dp), // Less border radius
                        ) {
                            Text("Next", color = Color.White)
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next"
                            )
                        }
                    }
                }
            }
        }
    }
}
