
package com.lensnap.app

import EventViewModel.EventViewModelFactory
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.lensnap.app.navigation.AppNavHost
import com.lensnap.app.permissions.CheckAndRequestPermissions
import com.lensnap.app.data.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.lensnap.app.ui.theme.screens.splashScreen.SplashScreen
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.lensnap.app.data.DailyUpdateRepository
import com.lensnap.app.data.DailyUpdatesViewModel
import com.lensnap.app.data.DailyUpdatesViewModelFactory
import com.lensnap.app.data.PostRepository
import com.lensnap.app.data.SearchRepository
import com.lensnap.app.data.SearchViewModel
import com.lensnap.app.data.UserViewModel.UserViewModelFactory

//ORIGINAL
//class MainActivity : ComponentActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
//    private var selectedImageUri by mutableStateOf<Uri?>(null)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        auth = FirebaseAuth.getInstance()
//
//        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            uri?.let { selectedUri ->
//                Log.d("MainActivity", "Image selected: $selectedUri")
//                selectedImageUri = selectedUri
//            }
//        }
//        setContent {
//            CheckAndRequestPermissions()
//            AppEntryPoint(
//                auth = auth,
//                imagePickerLauncher = imagePickerLauncher,
//                selectedImageUri = selectedImageUri
//            )
//        }
//    }
//}
//@Composable
//fun AppEntryPoint(
//    auth: FirebaseAuth,
//    imagePickerLauncher: ActivityResultLauncher<String>,
//    selectedImageUri: Uri?
//) {
//    val context = LocalContext.current
//    val userViewModel: UserViewModel = viewModel(
//        factory = UserViewModelFactory(context)
//    )
//    val eventViewModel: EventViewModel = viewModel(
//        factory = EventViewModelFactory(context)
//    )
//    val dailyUpdateRepository = DailyUpdateRepository(userViewModel)
//    val dailyUpdatesViewModel: DailyUpdatesViewModel = viewModel(
//        factory = DailyUpdatesViewModelFactory(dailyUpdateRepository, userViewModel)
//    )
//
//    val navController = rememberNavController()
//    var showSplash by remember { mutableStateOf(true) }
//
//    if (showSplash) {
//        SplashScreen()
//        LaunchedEffect(Unit) {
//            kotlinx.coroutines.delay(3000L)
//            showSplash = false
//            Log.d("MainActivity", "Splash screen finished")
//        }
//    } else {
//        val isLoggedIn = remember { mutableStateOf(userViewModel.getStoredUserId() != null) }
//        Log.d("MainActivity", "User logged in: ${isLoggedIn.value}")
//
//        // Add user data fetching
//        LaunchedEffect(isLoggedIn.value) {
//            if (isLoggedIn.value) {
//                userViewModel.checkIfUserIsLoggedIn()
//                Log.d("MainActivity", "Fetching user data")
//            }
//        }
//
//        DisposableEffect(auth) {
//            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//                isLoggedIn.value = firebaseAuth.currentUser != null
//                Log.d("MainActivity", "Auth state changed. User logged in: ${isLoggedIn.value}")
//            }
//            auth.addAuthStateListener(authStateListener)
//            onDispose {
//                auth.removeAuthStateListener(authStateListener)
//            }
//        }
//
//        AppNavHost(
//            navController = navController,
//            startDestination = if (isLoggedIn.value) "dashboard" else "signIn",
//            userViewModel = userViewModel,
//            eventViewModel = eventViewModel,
//            dailyUpdateRepository = DailyUpdateRepository(userViewModel),
//            postRepository = PostRepository(),
//            imagePickerLauncher = imagePickerLauncher,
//            selectedImageUri = null,
//            dailyUpdatesViewModel = dailyUpdatesViewModel,
//        )
//    }
//}

//class MainActivity : ComponentActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
//    private var selectedImageUri by mutableStateOf<Uri?>(null)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        auth = FirebaseAuth.getInstance()
//
//        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            uri?.let { selectedUri ->
//                Log.d("MainActivity", "Image selected: $selectedUri")
//                selectedImageUri = selectedUri
//            }
//        }
//
//        setContent {
//            CheckAndRequestPermissions()
//            AppEntryPoint(
//                auth = auth,
//                imagePickerLauncher = imagePickerLauncher,
//                selectedImageUri = selectedImageUri
//            )
//        }
//    }
//}
//
//@Composable
//fun AppEntryPoint(
//    auth: FirebaseAuth,
//    imagePickerLauncher: ActivityResultLauncher<String>,
//    selectedImageUri: Uri?
//) {
//    val context = LocalContext.current
//    val userViewModel: UserViewModel = viewModel(
//        factory = UserViewModelFactory(context)
//    )
//    val eventViewModel: EventViewModel = viewModel(
//        factory = EventViewModelFactory(context)
//    )
//    val dailyUpdateRepository = DailyUpdateRepository(userViewModel)
//    val dailyUpdatesViewModel: DailyUpdatesViewModel = viewModel(
//        factory = DailyUpdatesViewModelFactory(dailyUpdateRepository, userViewModel)
//    )
//
//    val navController = rememberNavController()
//    var showSplash by remember { mutableStateOf(true) }
//    var isLoggedIn by remember { mutableStateOf(false) }
//
//    // Splash Screen Logic
//    if (showSplash) {
//        SplashScreen()
//        LaunchedEffect(Unit) {
//            kotlinx.coroutines.delay(3000L) // Wait for splash duration
//            val currentUser = auth.currentUser
//            val storedUserId = userViewModel.getStoredUserId()
//            isLoggedIn = currentUser != null && storedUserId != null
//            Log.d("AppEntryPoint", "Splash finished. isLoggedIn: $isLoggedIn, userId: $storedUserId")
//            showSplash = false
//        }
//    } else {
//        // Listen to Auth State Changes
//        DisposableEffect(auth) {
//            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//                val currentUser = firebaseAuth.currentUser
//                val storedUserId = userViewModel.getStoredUserId()
//                isLoggedIn = currentUser != null && storedUserId != null
//                Log.d("AppEntryPoint", "Auth state changed. isLoggedIn: $isLoggedIn")
//            }
//            auth.addAuthStateListener(authStateListener)
//            onDispose {
//                auth.removeAuthStateListener(authStateListener)
//            }
//        }
//
//        // Navigation Logic
//        AppNavHost(
//            navController = navController,
//            startDestination = if (isLoggedIn) "dashboard" else "signIn",
//            userViewModel = userViewModel,
//            eventViewModel = eventViewModel,
//            dailyUpdateRepository = dailyUpdateRepository,
//            postRepository = PostRepository(),
//            imagePickerLauncher = imagePickerLauncher,
//            selectedImageUri = selectedImageUri,
//            dailyUpdatesViewModel = dailyUpdatesViewModel,
//        )
//    }
//}

//WORKING VERSION
//class MainActivity : ComponentActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
//    private var selectedImageUri by mutableStateOf<Uri?>(null)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        auth = FirebaseAuth.getInstance()
//
//
//        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            uri?.let { selectedUri ->
//                Log.d("MainActivity", "Image selected: $selectedUri")
//                selectedImageUri = selectedUri
//            }
//        }
//
//        setContent {
//            CheckAndRequestPermissions()
//            AppEntryPoint(
//                auth = auth,
//                imagePickerLauncher = imagePickerLauncher,
//                selectedImageUri = selectedImageUri
//            )
//        }
//    }
//}
//
//@Composable
//fun AppEntryPoint(
//    auth: FirebaseAuth,
//    imagePickerLauncher: ActivityResultLauncher<String>,
//    selectedImageUri: Uri?
//) {
//    val context = LocalContext.current
//    val userViewModel: UserViewModel = viewModel(
//        factory = UserViewModelFactory(context)
//    )
//    val eventViewModel: EventViewModel = viewModel(
//        factory = EventViewModelFactory(context)
//    )
//    val dailyUpdateRepository = DailyUpdateRepository(userViewModel)
//    val dailyUpdatesViewModel: DailyUpdatesViewModel = viewModel(
//        factory = DailyUpdatesViewModelFactory(dailyUpdateRepository, userViewModel)
//    )
//
//    val navController = rememberNavController()
//    var showSplash by remember { mutableStateOf(true) }
//    var isLoggedIn by remember { mutableStateOf(false) }
//
//    // Splash Screen Logic
//    if (showSplash) {
//        SplashScreen()
//        LaunchedEffect(Unit) {
//            auth.signOut() // Sign out the user to reset the authentication state
//            kotlinx.coroutines.delay(3000L) // Wait for splash duration
//            val currentUser = auth.currentUser
//            isLoggedIn = currentUser != null
//            Log.d("AppEntryPoint", "Splash finished. isLoggedIn: $isLoggedIn, currentUser: $currentUser")
//            showSplash = false
//        }
//    } else {
//        // Listen to Auth State Changes
//        DisposableEffect(auth) {
//            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//                val currentUser = firebaseAuth.currentUser
//                isLoggedIn = currentUser != null
//                Log.d("AppEntryPoint", "Auth state changed. isLoggedIn: $isLoggedIn, currentUser: $currentUser")
//            }
//            auth.addAuthStateListener(authStateListener)
//            onDispose {
//                auth.removeAuthStateListener(authStateListener)
//            }
//        }
//
//        // Navigation Logic
//        Log.d("AppEntryPoint", "Navigating to: ${if (isLoggedIn) "dashboard" else "signIn"}")
//        AppNavHost(
//            navController = navController,
//            startDestination = if (isLoggedIn) "dashboard" else "signIn",
//            userViewModel = userViewModel,
//            eventViewModel = eventViewModel,
//            dailyUpdateRepository = dailyUpdateRepository,
//            postRepository = PostRepository(),
//            imagePickerLauncher = imagePickerLauncher,
//            selectedImageUri = selectedImageUri,
//            dailyUpdatesViewModel = dailyUpdatesViewModel,
//        )
//    }
//}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private lateinit var sharedPreferences: SharedPreferences
    private val APP_VERSION_KEY = "app_version"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                Log.d("MainActivity", "Image selected: $selectedUri")
                selectedImageUri = selectedUri
            }
        }

        checkForAppUpdateOrFreshInstall()

        setContent {
            CheckAndRequestPermissions()
            AppEntryPoint(
                auth = auth,
                imagePickerLauncher = imagePickerLauncher,
                selectedImageUri = selectedImageUri
            )
        }
    }

    private fun checkForAppUpdateOrFreshInstall() {
        val currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode
        val storedVersionCode = sharedPreferences.getInt(APP_VERSION_KEY, -1)

        if (currentVersionCode != storedVersionCode) {
            // Fresh install or app update detected
            auth.signOut() // Clear the user session
            sharedPreferences.edit().putInt(APP_VERSION_KEY, currentVersionCode).apply()
        }
    }
}

@Composable
fun AppEntryPoint(
    auth: FirebaseAuth,
    imagePickerLauncher: ActivityResultLauncher<String>,
    selectedImageUri: Uri?
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context)
    )
    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(context)
    )
    val dailyUpdateRepository = DailyUpdateRepository(userViewModel)
    val dailyUpdatesViewModel: DailyUpdatesViewModel = viewModel(
        factory = DailyUpdatesViewModelFactory(dailyUpdateRepository, userViewModel)
    )

    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }

    // Splash Screen Logic
    if (showSplash) {
        SplashScreen()
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000L) // Wait for splash duration
            val currentUser = auth.currentUser
            isLoggedIn = currentUser != null
            Log.d("AppEntryPoint", "Splash finished. isLoggedIn: $isLoggedIn, currentUser: $currentUser")
            showSplash = false
        }
    } else {
        // Listen to Auth State Changes
        DisposableEffect(auth) {
            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val currentUser = firebaseAuth.currentUser
                isLoggedIn = currentUser != null
                Log.d("AppEntryPoint", "Auth state changed. isLoggedIn: $isLoggedIn, currentUser: $currentUser")
            }
            auth.addAuthStateListener(authStateListener)
            onDispose {
                auth.removeAuthStateListener(authStateListener)
            }
        }

        // Navigation Logic
        Log.d("AppEntryPoint", "Navigating to: ${if (isLoggedIn) "dashboard" else "signIn"}")
        AppNavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "dashboard" else "signIn",
            userViewModel = userViewModel,
            eventViewModel = eventViewModel,
            dailyUpdateRepository = dailyUpdateRepository,
            postRepository = PostRepository(),
            imagePickerLauncher = imagePickerLauncher,
            selectedImageUri = selectedImageUri,
            dailyUpdatesViewModel = dailyUpdatesViewModel,
            searchViewModel = SearchViewModel(repository = SearchRepository()),
        )
    }
}
