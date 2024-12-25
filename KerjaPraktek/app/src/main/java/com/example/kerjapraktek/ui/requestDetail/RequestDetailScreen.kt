package com.example.kerjapraktek.ui.requestDetail

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kerjapraktek.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    navController: NavController,
    requestId: String,
    viewModel: RequestDetailViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(requestId) {
        viewModel.getRequest(requestId)
    }

    val request by viewModel.request.collectAsState()

    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Group Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = request?.request?.group?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = request?.request?.company ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Members Section
            Text(
                text = "Members",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            request?.request?.group?.members?.forEach { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nim: ${member.nim}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Phone: ${member.phoneNumber}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Email: ${member.email}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Date Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = request?.request?.startDate ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Column {
                            Text(
                                text = "End Date",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = request?.request?.endDate ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Show Reason if Rejected
            if (request?.request?.status == "Rejected") {
                Text(
                    text = "Reason for Rejection:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = request?.request?.reason ?: "No reason provided",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Download Proposal Button
            Button(
                onClick = { viewModel.downloadProposal(requestId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = downloadStatus !is DownloadStatus.Loading
            ) {
                when (downloadStatus) {
                    is DownloadStatus.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Downloading...")
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Proposal")
                    }
                }
            }

            // Show status message
            LaunchedEffect(downloadStatus) {
                when (downloadStatus) {
                    is DownloadStatus.Success -> {
                        Toast.makeText(
                            context,
                            "Download started. Check notification for progress.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is DownloadStatus.Error -> {
                        if ((downloadStatus as DownloadStatus.Error).message.contains("URIs")) {
                            // Ignore file URI error since download is actually successful
                            return@LaunchedEffect
                        }
                        Toast.makeText(
                            context,
                            (downloadStatus as DownloadStatus.Error).message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }



            // Action Buttons
            if (request?.request?.status == "Pending") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reject")
                    }

                    Button(
                        onClick = { showAcceptDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Accept")
                    }
                }
            }
        }

        // Reject Dialog
        if (showRejectDialog) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Reject Request") },
                text = {
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason for rejection") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.rejectRequest(requestId, rejectReason)
                            showRejectDialog = false
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        enabled = rejectReason.isNotBlank()
                    ) {
                        Text("Confirm Rejection")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Accept Dialog
        if (showAcceptDialog) {
            AlertDialog(
                onDismissRequest = { showAcceptDialog = false },
                title = { Text("Accept Request") },
                text = { Text("Are you sure you want to accept this KP request?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.acceptRequest(requestId)
                            showAcceptDialog = false
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAcceptDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestDetailScreenPreview() {
    RequestDetailScreen(navController = NavController(LocalContext.current), requestId = "1")
}
