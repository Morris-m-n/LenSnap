package com.lensnap.app.ui.theme.screens.searchScreen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import com.lensnap.app.data.SearchViewModel
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.EventSearchResult
import com.lensnap.app.models.UserSearchResult
import com.lensnap.app.ui.theme.screens.userProfileScreen.FollowButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
    userViewModel: UserViewModel
) {
    val query = remember { mutableStateOf("") }
    val userSearchResults by viewModel.userSearchResults.collectAsState()
    val eventSearchResults by viewModel.eventSearchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query.value,
                onValueChange = { query.value = it },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(end = 8.dp),
                maxLines = 1,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(fontSize = 14.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    containerColor = Color(0xFFF0F0F0),
                    cursorColor = Color(0xFF0D6EFD),
                    focusedLabelColor = Color(0xFF0D6EFD)
                ),
                placeholder = { Text(text = "Search...", fontSize = 14.sp) }
            )

            Button(
                onClick = {
                    Log.d("SearchScreen", "Search button clicked with query: ${query.value}")
                    viewModel.performUserSearch(query.value)
                    viewModel.performEventSearch(query.value)
                },
                enabled = query.value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D6EFD)
                ),
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(userSearchResults.chunked(2)) { pair ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { result ->
                        UserSearchResultItem(
                            result = result,
                            navController = navController,
                            userViewModel = userViewModel
                        )
                    }
                }
            }
            items(eventSearchResults) { result ->
                EventSearchResultItem(
                    result = result,
                    navController = navController
                )
            }
        }
    }
}

//@Composable
//fun UserSearchResultItem(
//    result: UserSearchResult,
//    navController: NavController
//) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
//        modifier = Modifier
//            .fillMaxWidth(0.4f)
//            .padding(8.dp)
//            .clickable {
//                navController.navigate("profile/${result.userId}")
//            }
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxSize()
//        ) {
//            Image(
//                painter = rememberImagePainter(result.profilePhotoUrl),
//                contentDescription = "Profile Image",
//                modifier = Modifier
//                    .size(150.dp)
//                    .clip(RoundedCornerShape(12.dp)),
//                contentScale = ContentScale.Crop
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = result.username,
//                style = MaterialTheme.typography.bodyLarge,
//                textAlign = TextAlign.Center
//            )
//            Text(
//                text = result.email,
//                style = MaterialTheme.typography.bodySmall,
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Button(
//                onClick = { /* Implement follow functionality here */ },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF0D6EFD)
//                )
//            ) {
//                Text("Follow", color = Color.White)
//            }
//        }
//    }
//}

@Composable
fun UserSearchResultItem(
    result: UserSearchResult,
    navController: NavController,
    userViewModel: UserViewModel // Add userViewModel parameter
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .padding(8.dp)
            .clickable {
                navController.navigate("profile/${result.userId}")
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Image(
                painter = rememberImagePainter(result.profilePhotoUrl),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.username,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = result.email,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Call the FollowButton composable
            FollowButton(userViewModel = userViewModel, targetUserId = result.userId)
        }
    }
}

//@Composable
//fun EventSearchResultItem(result: EventSearchResult) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .clickable { /* You can add an onClick action here if needed */ },
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//        ) {
//            Image(
//                painter = rememberImagePainter(data = result.imageUrl),
//                contentDescription = "Event Image",
//                modifier = Modifier
//                    .fillMaxSize()
//                    .clip(RoundedCornerShape(12.dp)),
//                contentScale = ContentScale.Crop
//            )
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
//                            startY = 200f
//                        )
//                    )
//                    .padding(8.dp)
//            ) {
//                Column(modifier = Modifier.align(Alignment.BottomStart)) {
//                    Text(
//                        text = result.name,
//                        color = Color.White,
//                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                        textAlign = TextAlign.Left
//                    )
//                    Text(
//                        text = result.location,
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall,
//                        textAlign = TextAlign.Left
//                    )
//                    Text(
//                        text = result.date,
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall,
//                        textAlign = TextAlign.Left
//                    )
//                    Text(
//                        text = result.description,
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall,
//                        textAlign = TextAlign.Left
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
fun EventSearchResultItem(result: EventSearchResult, navController: NavController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val eventJson = Uri.encode(Gson().toJson(result))
                navController.navigate("eventDetails/$eventJson")
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = result.imageUrl),
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 200f
                        )
                    )
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = result.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Left
                    )
                    Text(
                        text = result.location,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Left
                    )
                    Text(
                        text = result.date,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Left
                    )
                    Text(
                        text = result.description,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Left
                    )
                }
            }
        }
    }
}
