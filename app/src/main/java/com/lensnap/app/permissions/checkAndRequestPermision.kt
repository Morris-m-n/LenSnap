package com.lensnap.app.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun CheckAndRequestPermissions() {
    val context = LocalContext.current as ComponentActivity
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    if (!hasPermission) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permissions", "WRITE_EXTERNAL_STORAGE granted.")
            } else {
                Log.d("Permissions", "WRITE_EXTERNAL_STORAGE denied.")
            }
        }

        SideEffect {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
