package com.example.kerjapraktek.ui.arsip

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kerjapraktek.ViewModelFactory
import com.example.kerjapraktek.data.response.DataArsip
import com.example.kerjapraktek.ui.component.bar.BottomNavBar
import com.example.kerjapraktek.ui.requestDetail.DownloadStatus
import com.example.kerjapraktek.ui.theme.Primary

@Composable
fun ArsipScreen(
    navController: NavController,
    viewModel: ArsipViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val archives by viewModel.archives.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val context = LocalContext.current

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
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Archives",
                color = Primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (downloadStatus is DownloadStatus.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(archives) { archive ->
                    ArchiveCard(
                        archive = archive,
                        onDownloadProposal = { viewModel.downloadProposal(archive.kPRequest.id.toString()) },
                        onDownloadResponse = { viewModel.downloadResponseLetter(archive.responseLetterUrl, archive.kPRequest.company.toString()) },
                        isDownloading = downloadStatus is DownloadStatus.Loading
                    )
                }
            }
        }
    }
}

@Composable
fun ArchiveCard(
    archive: DataArsip,
    onDownloadProposal: () -> Unit,
    onDownloadResponse: () -> Unit,
    isDownloading: Boolean // New parameter to check if downloading
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = archive.kPRequest.group?.name ?: "Unknown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Company: ${archive.kPRequest.company}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Period: ${archive.kPRequest.startDate} to ${archive.kPRequest.endDate}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Status: ${archive.kPRequest.status}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDownloadProposal,
                    colors = ButtonDefaults.buttonColors(Primary),
                    modifier = Modifier.weight(1f),
                    enabled = !isDownloading // Disable when downloading
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Proposal")
                }

                if (archive.responseLetterUrl.isNotEmpty()) {
                    Button(
                        colors = ButtonDefaults.buttonColors(Primary),
                        onClick = onDownloadResponse,
                        modifier = Modifier.weight(1f),
                        enabled = !isDownloading // Disable when downloading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Response")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArsipScreenPreview() {
    val navController = rememberNavController()
    ArsipScreen(navController = navController)
}
