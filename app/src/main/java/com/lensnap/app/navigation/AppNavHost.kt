package com.lensnap.app.navigation

import SignUpScreen
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.lensnap.app.data.PostRepository
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.UserRegistration
import com.lensnap.app.ui.screens.eventDetails.EventDetailsScreen
import com.lensnap.app.ui.theme.screens.MainEventScreen
import com.lensnap.app.ui.theme.screens.addCaptionScreen.AddCaptionScreen
import com.lensnap.app.ui.theme.screens.createEventScreen.*
import com.lensnap.app.ui.theme.screens.dashboard.DashboardScreen
import com.lensnap.app.ui.theme.screens.joinEventScreen.JoinEventScreen
import com.lensnap.app.ui.theme.screens.profileScreen.EditProfileScreen
import com.lensnap.app.ui.theme.screens.profileScreen.ProfileScreen
import com.lensnap.app.ui.theme.screens.signInScreen.SignInScreen
import com.lensnap.app.ui.theme.screens.successScreen.EventSuccessScreen
import com.lensnap.app.ui.theme.screens.userProfileScreen.UserProfileScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "signIn"
) {
    val userViewModel: UserViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("signIn") {
            SignInScreen(
                viewModel = userViewModel,
                onSignInSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signUp")
                }
            )
        }
        composable("signUp") {
            SignUpScreen(
                viewModel = userViewModel,
                onSignUpSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate("signIn")
                }
            )
        }
        composable("dashboard") {
            val user = userViewModel.getCurrentUser()
            val userProfileImageUrl = user?.profilePhotoUrl
            val currentUserId = user?.id ?: ""
            val currentUsername = user?.username ?: ""

            // Assuming you have an instance of PostRepository
            val postRepository = PostRepository()

            DashboardScreen(
                eventViewModel = eventViewModel,
                userProfileImageUrl = userProfileImageUrl,
                currentUserId = currentUserId,
                currentUsername = currentUsername,
                onNavigateToProfile = { navController.navigate("profile") },
                onStartEvent = { navController.navigate("create_event_details") },
                onJoinEvent = { navController.navigate("join event") },
                onSearch = { /* Implement search logic */ },
                onEventClick = { event ->
                    val eventJson = Uri.encode(Gson().toJson(event))
                    navController.navigate("eventDetails/$eventJson")
                },
                navController = navController,
                userViewModel = userViewModel,
                postRepository = postRepository // Passing the postRepository instance
            )
        }
        composable("create_event_details") {
            CreateEventDetailsScreen(
                onNext = { eventName, eventDescription, eventLocation, eventImageUri, eventImageBitmap ->
                    eventViewModel.updateEventName(eventName)
                    eventViewModel.updateEventDescription(eventDescription)
                    eventViewModel.updateEventLocation(eventLocation)
                    eventViewModel.updateEventImageUri(eventImageUri)
                    eventViewModel.updateEventImageBitmap(eventImageBitmap)
                    navController.navigate("create_event_date_time")
                }
            )
        }
        composable("create_event_date_time") {
            CreateEventDateTimeScreen(
                onPrevious = { navController.popBackStack() },
                onNext = { eventDate, eventTime ->
                    eventViewModel.updateEventDate(eventDate)
                    eventViewModel.updateEventTime(eventTime)
                    navController.navigate("create_event_confirmation")
                },
                eventName = eventViewModel.eventName.value ?: "",
                eventDescription = eventViewModel.eventDescription.value ?: "",
                eventLocation = eventViewModel.eventLocation.value ?: "",
                eventImageUri = eventViewModel.eventImageUri.value,
                eventImageBitmap = eventViewModel.eventImageBitmap.value
            )
        }
        composable("create_event_confirmation") {
            CreateEventConfirmationScreen(
                onPrevious = { navController.popBackStack() },
                onConfirm = { event, capturedImages, galleryImages ->
                    eventViewModel.createEvent(event, capturedImages, galleryImages, onSuccess = { pairingCode, qrCodeUrl ->
                        val encodedEventName = Uri.encode(event.name)
                        val encodedQrCodeUrl = Uri.encode(qrCodeUrl)
                        val route = "eventSuccess/$encodedEventName/$pairingCode/$encodedQrCodeUrl"
                        navController.navigate(route)
                    }, onError = { error ->
                        Log.e("AppNavHost", "Error creating event: $error")
                    })
                },
                eventName = eventViewModel.eventName.value ?: "",
                eventDescription = eventViewModel.eventDescription.value ?: "",
                eventLocation = eventViewModel.eventLocation.value ?: "",
                eventDate = eventViewModel.eventDate.value ?: "",
                eventTime = eventViewModel.eventTime.value ?: "",
                eventImageUri = eventViewModel.eventImageUri.value,
                eventImageBitmap = eventViewModel.eventImageBitmap.value
            )
        }
        composable("eventSuccess/{eventName}/{pairingCode}/{qrCodeUrl}") { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName")
            val pairingCode = backStackEntry.arguments?.getString("pairingCode")
            val qrCodeUrl = backStackEntry.arguments?.getString("qrCodeUrl")

            EventSuccessScreen(
                eventName = eventName ?: "",
                qrCodeUrl = qrCodeUrl ?: "",
                pairingCode = pairingCode ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable("join event") {
            JoinEventScreen(
                navController = navController,
                onJoinSuccess = { eventCode ->
                    navController.navigate("photoCapture/$eventCode")
                }
            )
        }
        composable("photoCapture/{eventCode}") { backStackEntry ->
            val eventCode = backStackEntry.arguments?.getString("eventCode")
            MainEventScreen(
                eventCode = eventCode,
                navController = navController,
                viewModel = eventViewModel
            )
        }
        composable("profile") {
            val user: UserRegistration? = userViewModel.getCurrentUser()
            user?.let {
                ProfileScreen(
                    user = it,
                    onEdit = { updatedUser ->
                        navController.navigate("editProfile")
                    },
                    onDelete = { userViewModel.deleteUser() },
                    onAddOrChangeProfilePhoto = { /* Handle adding or changing profile photo */ },
                    onCreatePost = { mediaUri ->
                        val encodedUri = Uri.encode(mediaUri.toString())
                        navController.navigate("add_caption_screen/$encodedUri")
                    },
                    onSignOut = {
                        userViewModel.signOut()
                        navController.navigate("signin") // Navigate to login screen after sign out
                    }
                )
            }
        }
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(
                userViewModel = userViewModel,
                userId = userId,
                navController = navController
            )
        }
        composable("editProfile") {
            val user: UserRegistration? = userViewModel.getCurrentUser()
            user?.let {
                EditProfileScreen(
                    user = it,
                    viewModel = userViewModel,
                    onSave = { updatedUser ->
                        userViewModel.updateUser(updatedUser)
                        navController.navigateUp()
                    },
                    onCancel = { navController.navigateUp() }
                )
            }
        }
        composable("eventDetails/{eventJson}") { backStackEntry ->
            val eventJson = backStackEntry.arguments?.getString("eventJson")
            val event = Gson().fromJson(eventJson, Event::class.java)
            EventDetailsScreen(event = event, navController = navController)
        }
        composable("add_caption_screen/{mediaUri}") { backStackEntry ->
            val mediaUri = backStackEntry.arguments?.getString("mediaUri")?.let { Uri.parse(it) }
            mediaUri?.let {
                AddCaptionScreen(
                    mediaUri = it,
                    onSubmit = { caption ->
                        userViewModel.submitPost(it, caption)
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}
