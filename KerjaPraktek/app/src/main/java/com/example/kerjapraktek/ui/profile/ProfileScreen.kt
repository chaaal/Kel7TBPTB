package com.example.kerjapraktek.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kerjapraktek.ui.component.bar.BottomNavBar
import com.example.kerjapraktek.R
import com.example.kerjapraktek.ViewModelFactory
import com.example.kerjapraktek.ui.info.InfoViewModel
import com.example.kerjapraktek.ui.theme.Primary

@Composable
fun ProfileScreen(navController: NavController,
                  viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(
                            LocalContext.current))

) {

    val userData by viewModel.user.collectAsState()



    viewModel.fetchUser()




    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }
    var jabatan by remember { mutableStateOf("") }
    var newPhoneNumber by remember { mutableStateOf(noHp) }

    // Password states
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val passwordChangeResult by viewModel.passwordChangeResult.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }


    var showPhoneDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }



    // Effect untuk menangani hasil perubahan password
    LaunchedEffect(passwordChangeResult) {
        when (passwordChangeResult) {
            true -> {
                showPasswordDialog = false
                showSuccessDialog = true
                viewModel.resetPasswordChangeResult()
            }
            false -> {
                // Tampilkan pesan error jika diperlukan
                passwordError = "Failed to change password. Please try again."
                viewModel.resetPasswordChangeResult()
            }
            null -> { /* Do nothing */ }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Dialog tidak bisa ditutup dengan tap di luar */ },
            title = { Text("Success") },
            text = { Text("Password successfully changed. Please login again.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }




    LaunchedEffect(userData) {
        name = userData?.data?.name ?: ""
        email = userData?.data?.email ?: ""
        noHp = userData?.data?.noHp ?: ""
        jabatan = userData?.data?.jabatan ?: ""
    }







    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.file),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileInfoCard(
                title = "Name",
                value = name,
                icon = Icons.Default.Person
            )

            ProfileInfoCard(
                title = "Position",
                value = jabatan,
                icon = Icons.Default.Work
            )

            PhoneNumberCard(
                phoneNumber = noHp,
                onEditClick = { showPhoneDialog = true }
            )

            PasswordCard(
                onChangePasswordClick = { showPasswordDialog = true }
            )

            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth().height(48.dp),

                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),

            colors = ButtonDefaults.buttonColors(
                    Primary
                )
            ) {
                Text("Logout")
            }
        }

        // Phone Number Dialog
        if (showPhoneDialog) {
            AlertDialog(
                onDismissRequest = { showPhoneDialog = false },
                title = { Text("Edit Phone Number") },
                text = {
                    OutlinedTextField(
                        value = newPhoneNumber,
                        onValueChange = { newPhoneNumber = it },
                        label = { Text("New Phone Number") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updatePhone(newPhoneNumber)
                            if (newPhoneNumber.isNotEmpty()) {
                                noHp = newPhoneNumber
                            }
                            viewModel.updatePhone(newPhoneNumber)
                            showPhoneDialog = false
                            newPhoneNumber = ""
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPhoneDialog = false
                            newPhoneNumber = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Password Change Dialog
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPasswordDialog = false
                    passwordError = null
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                },
                title = { Text("Change Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                passwordError = null
                            },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                passwordError = null
                            },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        if (passwordError != null) {
                            Text(
                                text = passwordError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when {
                                currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                                    passwordError = "All fields are required"
                                }
                                newPassword != confirmPassword -> {
                                    passwordError = "New passwords do not match"
                                }
                                newPassword.length < 6 -> {
                                    passwordError = "Password must be at least 6 characters"
                                }
                                else -> {
                                    viewModel.changePassword(currentPassword, newPassword)
                                }
                            }
                        }
                    ) {
                        Text("Change Password")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPasswordDialog = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            passwordError = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary
            )
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun PhoneNumberCard(phoneNumber: String, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Primary
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "Phone Number",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            TextButton(onClick = onEditClick) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun PasswordCard(onChangePasswordClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Primary
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "••••••••",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            TextButton(onClick = onChangePasswordClick) {
                Text("Change")
            }
        }
    }
}