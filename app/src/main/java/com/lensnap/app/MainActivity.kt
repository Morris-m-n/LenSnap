package com.lensnap.app

import EventViewModel.EventViewModelFactory
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
import com.lensnap.app.data.UserViewModel.UserViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            CheckAndRequestPermissions()
            AppEntryPoint(auth)
        }
    }
}
@Composable
fun AppEntryPoint(auth: FirebaseAuth) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context)
    )
    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(context)
    )
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen()
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000L)
            showSplash = false
            Log.d("MainActivity", "Splash screen finished")
        }
    } else {
        val isLoggedIn = remember { mutableStateOf(userViewModel.getStoredUserId() != null) }
        Log.d("MainActivity", "User logged in: ${isLoggedIn.value}")

        // Add user data fetching
        LaunchedEffect(isLoggedIn.value) {
            if (isLoggedIn.value) {
                userViewModel.checkIfUserIsLoggedIn()
                Log.d("MainActivity", "Fetching user data")
            }
        }

        DisposableEffect(auth) {
            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                isLoggedIn.value = firebaseAuth.currentUser != null
                Log.d("MainActivity", "Auth state changed. User logged in: ${isLoggedIn.value}")
            }
            auth.addAuthStateListener(authStateListener)
            onDispose {
                auth.removeAuthStateListener(authStateListener)
            }
        }

        AppNavHost(
            navController = navController,
            startDestination = if (isLoggedIn.value) "dashboard" else "signIn"
        )
    }
}

//@Composable
//fun AppEntryPoint(auth: FirebaseAuth) {
//    val userViewModel: UserViewModel = viewModel(
//        factory = UserViewModelFactory(LocalContext.current)
//    )
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
//            startDestination = if (isLoggedIn.value) "dashboard" else "signIn"
//        )
//    }
//}
