package com.example.kerjapraktek.ui.addReply

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kerjapraktek.ViewModelFactory
import com.example.kerjapraktek.data.response.DataItem
import com.example.kerjapraktek.data.response.DataReply
import com.example.kerjapraktek.ui.reply.ReplyViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReplyScreen(
    navController: NavController,
    viewModel: ReplyViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    var selectedGroup by remember { mutableStateOf<DataItem?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }

    val replies by viewModel.replies.collectAsState()
    val requests by viewModel.requests.collectAsState()

    var isLoading by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFile = uri
    }

    val context = LocalContext.current


    LaunchedEffect(Unit) {
        viewModel.fetchReplyList()
        viewModel.fetchRequestList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Reply") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Group Selection Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Dropdown for group selection
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedGroup?.group?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Group") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            requests.forEach { request ->
                                DropdownMenuItem(
                                    text = { Text(request.group.name) },
                                    onClick = {
                                        selectedGroup = request
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Display selected group details
                    if (selectedGroup != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Group Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Company: ${selectedGroup?.company?: ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        // Add more group details here as needed
                    }
                }
            }

            // Response Letter Upload Card
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { launcher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedFile?.lastPathSegment ?: "Upload Response Letter")
                    }

                    if (selectedFile != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "File selected",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            fun Uri.getFilePathFromUri(context: Context): File? {
                return try {
                    val inputStream = context.contentResolver.openInputStream(this)
                    val tempFile = File.createTempFile("upload", ".pdf", context.cacheDir)
                    tempFile.deleteOnExit()
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                } catch (e: Exception) {
                    Log.e("FileUpload", "Error converting URI to File", e)
                    null
                }
            }

            // Submit Button
            Button(
                onClick = {

                    val posterPart = selectedFile?.let { uri ->
                        val file = uri.getFilePathFromUri(context)
                        val requestBody = file?.asRequestBody("application/pdf".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("reply", file?.name, requestBody!!)
                    }


                    if (selectedGroup != null && selectedFile != null) {
                        isLoading = true
                        viewModel.addReply(
                            selectedGroup!!.id,
                            posterPart!!,
                            onSuccess = {
                                isLoading = false
                                navController.navigate("reply") {
                                    popUpTo("reply") { inclusive = true }
                                }
                            },
                            onError = { e ->
                                isLoading = false
                                Log.e("AddEvent", "Error adding event", e)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedGroup != null && selectedFile != null
            ) {
                Text("Submit")
            }
        }
    }
}

    }
}
