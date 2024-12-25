package com.example.kerjapraktek.ui.replyDetail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kerjapraktek.ViewModelFactory
import com.example.kerjapraktek.ui.reply.ReplyViewModel
import com.example.kerjapraktek.ui.requestDetail.DownloadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyDetailScreen(
    navController: NavController,
    replyId: String,
    viewModel: ReplyViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val replyDetail by viewModel.reply.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(replyId) {
        viewModel.getReplyById(replyId)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reply Detail") },
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
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Group Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Group Name",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = replyDetail?.kPRequest?.group?.name ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Company",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = replyDetail?.kPRequest?.company ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Response Letter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Response Letter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Show a loading indicator while downloading
                    if (downloadStatus is DownloadStatus.Loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            // Start the download
                            replyDetail?.responseLetterUrl?.let { url ->
                                viewModel.downloadResponseLetter(url, replyDetail?.kPRequest?.company.toString())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = downloadStatus !is DownloadStatus.Loading // Disable the button when downloading
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Response Letter")
                    }
                }
            }
        }
    }
}
