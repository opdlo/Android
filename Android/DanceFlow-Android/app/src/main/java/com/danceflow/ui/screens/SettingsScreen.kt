package com.danceflow.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danceflow.app.data.RetrofitInstance
import com.danceflow.app.data.SessionManager
import com.danceflow.app.data.dto.ChangePasswordRequest
import com.danceflow.app.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMsg by remember { mutableStateOf("") }
    var bgMsg by remember { mutableStateOf("") }

    val bgPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val input = context.contentResolver.openInputStream(it)
                    val bytes = input?.readBytes() ?: return@launch
                    input.close()
                    val part = MultipartBody.Part.createFormData("file", "bg_${System.currentTimeMillis()}.jpg",
                        bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                    val resp = RetrofitInstance.getProfileApi().uploadBackground(part)
                    bgMsg = if (resp.isSuccessful) "Background updated" else "Upload failed"
                } catch (e: Exception) { bgMsg = "Error: ${e.message}" }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Change Password", fontSize = 16.sp)
            OutlinedTextField(value = oldPassword, onValueChange = { oldPassword = it; passwordMsg = "" },
                label = { Text("Old password") }, visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; passwordMsg = "" },
                label = { Text("New password") }, visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; passwordMsg = "" },
                label = { Text("Confirm password") }, visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            if (passwordMsg.isNotEmpty()) {
                Text(passwordMsg, fontSize = 13.sp, color = if (passwordMsg.startsWith("Error") || passwordMsg.startsWith("Wrong")) MaterialTheme.colorScheme.error else SecondaryColor)
            }
            Button(onClick = {
                if (newPassword.length < 6) { passwordMsg = "Password too short"; return@Button }
                if (newPassword != confirmPassword) { passwordMsg = "Passwords don't match"; return@Button }
                scope.launch {
                    try {
                        val resp = RetrofitInstance.getAuthApi().changePassword(ChangePasswordRequest(oldPassword, newPassword))
                        passwordMsg = if (resp.isSuccessful) "Password changed" else "Wrong password"
                    } catch (e: Exception) { passwordMsg = "Error: ${e.message}" }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Update password") }

            Divider()
            Text("Background Image", fontSize = 16.sp)
            OutlinedButton(onClick = { bgPickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Choose image")
            }
            if (bgMsg.isNotEmpty()) Text(bgMsg, fontSize = 13.sp, color = TextSecondary)

            Divider()
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log out")
            }
        }
    }
}
