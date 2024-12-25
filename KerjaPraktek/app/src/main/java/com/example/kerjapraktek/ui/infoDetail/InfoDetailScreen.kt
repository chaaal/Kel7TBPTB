package com.example.kerjapraktek.ui.infoDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kerjapraktek.ViewModelFactory
import com.example.kerjapraktek.data.response.DataInfo
import androidx.navigation.NavController
import com.example.kerjapraktek.ui.info.InfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDetailScreen(
    navController: NavController,
    infoId: String,
    viewModel: InfoViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {


    val info by viewModel.info.collectAsState()

    LaunchedEffect(infoId) {
        viewModel.getInfoById(infoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcement Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        info?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                // Additional info like creation date could be added here
            }
        }


    }
}