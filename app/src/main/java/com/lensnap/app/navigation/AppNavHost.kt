package com.lensnap.app.navigation

import IndividualChatScreen
import SignUpScreen
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import com.google.gson.Gson
import com.lensnap.app.data.PostRepository
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.data.ChatInviteViewModel
//import com.lensnap.app.data.FirebaseSignaling
import com.lensnap.app.models.UserRegistration
import com.lensnap.app.ui.theme.screens.createEventScreen.CreateEventConfirmationScreen
import com.lensnap.app.ui.theme.screens.createEventScreen.CreateEventDateTimeScreen
import com.lensnap.app.ui.theme.screens.createEventScreen.CreateEventDetailsScreen
import com.lensnap.app.ui.theme.screens.dashboard.DashboardScreen
import com.lensnap.app.ui.theme.screens.signInScreen.SignInScreen
import com.lensnap.app.ui.theme.screens.successScreen.EventSuccessScreen
import com.lensnap.app.ui.screens.eventDetails.EventDetailsScreen
import com.lensnap.app.ui.theme.screens.MainEventScreen
import com.lensnap.app.ui.theme.screens.addCaptionScreen.AddCaptionScreen
import com.lensnap.app.ui.theme.screens.callScreen.CallScreen
import com.lensnap.app.ui.theme.screens.chatScreen.ChatsScreen
import com.lensnap.app.ui.theme.screens.incomingCallScreen.IncomingCallScreen
import com.lensnap.app.ui.theme.screens.joinEventScreen.JoinEventScreen
import com.lensnap.app.ui.theme.screens.profileScreen.EditProfileScreen
import com.lensnap.app.ui.theme.screens.profileScreen.ProfileScreen
import com.lensnap.app.ui.theme.screens.userProfileScreen.UserProfileScreen
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lensnap.app.data.DailyUpdateRepository
import com.lensnap.app.data.DailyUpdatesViewModel
import com.lensnap.app.data.SearchViewModel
import com.lensnap.app.ui.theme.screens.PostUpdateScreen.PostUpdateScreen
import com.lensnap.app.ui.theme.screens.eventUpdateScreen.EventUpdateScreen
import com.lensnap.app.ui.theme.screens.searchScreen.SearchScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = "signIn",
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    dailyUpdatesViewModel: DailyUpdatesViewModel,
    searchViewModel: SearchViewModel,
    dailyUpdateRepository: DailyUpdateRepository,
    postRepository: PostRepository,
    imagePickerLauncher: ActivityResultLauncher<String>,
    selectedImageUri: Uri?,
//    makeCall: () -> Unit,
//    answerCall: () -> Unit,
    navController: NavHostController,
) {
    val chatInviteViewModel = ChatInviteViewModel()
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
                    navController.navigate("signIn") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate("signIn")
                },
            )
        }
        composable("dashboard") {
            // Fetch the stored user ID
            val userId = userViewModel.getStoredUserId()

            if (userId != null) {
                // Fetch the current user data
                LaunchedEffect(userId) {
                    userViewModel.fetchCurrentUser(userId)
                }

                // Observe the LiveData and get the current user
                val currentUser by userViewModel.currentUser.observeAsState()

                val userProfileImageUrl = currentUser?.profilePhotoUrl
                val currentUsername = currentUser?.username ?: ""

                if (currentUser == null) {
                    Log.e("DashboardRoute", "User is null. Unable to fetch user details.")
                } else {
                    Log.d("DashboardRoute", "Fetched user data: ID = ${currentUser?.id}, Username = ${currentUser?.username}")
                }

                // Assuming you have an instance of PostRepository
                val postRepository = PostRepository()

                // Pass the fetched userId and other details to DashboardScreen
                DashboardScreen(
                    eventViewModel = eventViewModel,
                    userProfileImageUrl = userProfileImageUrl,
                    userId = userId, // Use userId here
                    currentUsername = currentUsername,
                    onNavigateToProfile = { navController.navigate("profile") },
                    onStartEvent = { navController.navigate("create_event_details") },
                    onJoinEvent = { navController.navigate("join_event") },
                    onSearch = { navController.navigate("search")},
                    onEventClick = { event ->
                        val eventJson = Uri.encode(Gson().toJson(event))
                        navController.navigate("eventDetails/$eventJson")
                    },
                    onHome = { navController.navigate("dashboard") }, // Added the onHome navigation
                    navController = navController,
                    userViewModel = userViewModel,
                    postRepository = postRepository, // Passing the postRepository instance
                    dailyUpdateRepository = dailyUpdateRepository // Passing the dailyUpdateRepository instance
                )
            } else {
                Log.e("DashboardRoute", "Stored user ID is null")
            }
        }
        composable("chats") {
            Log.d("Navigation", "Navigating to ChatsScreen.")
            val user = userViewModel.getCurrentUser()
            val currentUserId = user?.id ?: ""
            ChatsScreen(
                navController = navController,
                userViewModel = userViewModel,
                chatInviteViewModel = chatInviteViewModel,
                currentUserId = currentUserId
            )
        }
        //ORIGINAL
//        composable(
//            route = "individual_chat/{chatId}/{receiverId}?eventImageUrl={eventImageUrl}",
//            arguments = listOf(
//                navArgument("chatId") { type = NavType.StringType },
//                navArgument("receiverId") { type = NavType.StringType },
//                navArgument("eventImageUrl") { type = NavType.StringType; nullable = true }
//            )
//        ) { backStackEntry ->
//            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
//            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: return@composable
//            val eventImageUrl = backStackEntry.arguments?.getString("eventImageUrl")
//
//            IndividualChatScreen(
//                chatId = chatId,
//                userViewModel = userViewModel,
//                receiverId = receiverId,
//                imagePickerLauncher = imagePickerLauncher,
//                selectedImageUri = selectedImageUri,
//                navController = navController,
//                eventImageUrl = eventImageUrl
//            )
//        }

        composable(
            route = "individual_chat/{chatId}/{receiverId}?event={event}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("event") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: return@composable
            val eventJson = backStackEntry.arguments?.getString("event")

            val event: Event? = eventJson?.let { Gson().fromJson(it, Event::class.java) }

            IndividualChatScreen(
                chatId = chatId,
                userViewModel = userViewModel,
                receiverId = receiverId,
                imagePickerLauncher = imagePickerLauncher,
                selectedImageUri = selectedImageUri,
                navController = navController,
                event = event // Pass the event object
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
                eventImageBitmap = eventViewModel.eventImageBitmap.value,
                userViewModel = userViewModel // Pass the UserViewModel instance
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
        composable("join_event") {
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
                Log.d("Navigation", "Navigating to ProfileScreen for user: ${it.id}")  // Log when ProfileScreen is navigated to

                ProfileScreen(
                    navController = navController, // Pass NavController here
                    user = it,
                    onEdit = { updatedUser ->
                        Log.d("Navigation", "Navigating to Edit Profile screen") // Log when navigating to Edit Profile
                        navController.navigate("editProfile")
                    },
                    onDelete = {
                        Log.d("UserAction", "User delete action triggered for user: ${it.id}")  // Log delete action
                        userViewModel.deleteUser()
                    },
                    onAddOrChangeProfilePhoto = {
                        Log.d("UserAction", "Add or Change Profile Photo action triggered for user: ${it.id}")
                        // Handle adding or changing profile photo
                    },
                    onCreatePost = { mediaUri ->
                        val encodedUri = Uri.encode(mediaUri.toString())
                        Log.d("Navigation", "Navigating to Add Caption screen with mediaUri: $encodedUri")  // Log navigation with encoded URI
                        navController.navigate("add_caption_screen/$encodedUri")
                    },
                    onSignOut = {
                        Log.d("UserAction", "Sign out action triggered for user: ${it.id}")  // Log sign out action
                        userViewModel.signOut()
                        navController.navigate("signIn") // Navigate to signIn screen after sign out
                    },
                    userViewModel = userViewModel, // Pass the UserViewModel
                    eventViewModel = eventViewModel, // Pass the EventViewModel
                    dailyUpdatesViewModel = dailyUpdatesViewModel, // Pass the DailyUpdatesViewModel
                    postRepository = postRepository
                )
            }
        }

        composable("event_update_screen/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventUpdateScreen(
                eventId = eventId,
                eventViewModel = eventViewModel,
                navController = navController
            )
        }

        composable("post_update_screen/{userId}/{postId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostUpdateScreen(
                postId = postId,
                navController = navController,
                postRepository = postRepository,  // Make sure you pass this properly
                userId = userId
            )
        }

        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(
                userViewModel = userViewModel,
                userId = userId,
                navController = navController,
                eventViewModel = eventViewModel
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
                val isLoading by userViewModel.loading
                AddCaptionScreen(
                    mediaUri = it,
                    onSubmit = { caption ->
                        userViewModel.submitPost(it, caption) {
                            navController.navigateUp() // Navigate back only after submission is complete
                        }
                    },
                    navController = navController,
                    isLoading = isLoading
                )
            }
        }

        composable("search"){
            SearchScreen(
                viewModel = searchViewModel,
                navController = navController,
                userViewModel = userViewModel
                )
        }

        composable("callScreen/{receiverId}/{callType}") { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val callType = backStackEntry.arguments?.getString("callType") ?: "voice"

            // Ensure signaling is properly initialized
//            val signaling = remember { FirebaseSignaling() }

            CallScreen(
                receiverId = receiverId,
                userViewModel = userViewModel, // Pass the UserViewModel
                isVideoCall = callType == "video",
                navController = navController, // Pass the navController initialized in MainActivity
//                signaling = signaling, // Pass the signaling object
                onHangUpClick = { navController.popBackStack() }
            )
        }
        composable("incomingCallScreen/{receiverId}/{callerName}/{callerProfileUrl}/{isVideoCall}") { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val callerName = backStackEntry.arguments?.getString("callerName") ?: ""
            val callerProfileUrl = backStackEntry.arguments?.getString("callerProfileUrl") ?: ""
            val isVideoCall = backStackEntry.arguments?.getString("isVideoCall")?.toBoolean() ?: false

            IncomingCallScreen(
                receiverId = receiverId,
                callerName = callerName,
                callerProfileUrl = callerProfileUrl,
                isVideoCall = isVideoCall,
                navController = navController,
                onAcceptCall = {
                    // Handle accept call
                    navController.navigate("callScreen/$receiverId/${if (isVideoCall) "video" else "voice"}")
                },
                onRejectCall = {
                    // Handle reject call
                    navController.popBackStack()
                }
            )
        }
    }
}