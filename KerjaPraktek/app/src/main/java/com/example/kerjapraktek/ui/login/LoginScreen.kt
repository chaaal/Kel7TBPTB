package com.example.kerjapraktek.ui.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kerjapraktek.R


import com.example.kerjapraktek.ViewModelFactory
import com.example.kerjapraktek.data.pref.UserModel
import com.example.kerjapraktek.ui.theme.Background
import com.example.kerjapraktek.ui.theme.DarkOrange
import com.example.kerjapraktek.ui.theme.LightGray
import com.example.kerjapraktek.ui.theme.Primary
import com.example.kerjapraktek.ui.theme.Secondary
import com.example.kerjapraktek.ui.theme.White


@Composable
fun LoginScreen(context: Context, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginViewModel: LoginViewModel = viewModel(factory = ViewModelFactory.getInstance(context))
    var loginResult by remember { mutableStateOf<Boolean?>(null) }
    val loginState by loginViewModel.loginResult.observeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Background Pattern
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.7f),
                            Primary.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = White,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Aplikasi Pengelola",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkOrange
            )

            Text(
                text = "Kerja Praktek Mahasiswa",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkOrange
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Welcome Back Admin!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )

                    Text(
                        text = "Sign in to continue",
                        fontSize = 14.sp,
                        color = Secondary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                painterResource(id = R.drawable.ic_email),
                                contentDescription = "Email",
                                tint = Primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = LightGray,
                            focusedLabelColor = Primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Password",
                                tint = Primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = LightGray,
                            focusedLabelColor = Primary
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { loginViewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Primary)
                    ) {
                        Text(
                            text = "SIGN IN",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

        }

        LaunchedEffect(loginState) {
            loginState?.let { result ->
                result.onSuccess { response ->
                    loginViewModel.saveSession(
                        UserModel(
                            token = response.data.token,
                            isLogin = true
                        )
                    )
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }.onFailure { error ->
                    // Tampilkan pesan kesalahan
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}