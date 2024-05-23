package com.mamontov.invoice_converter.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mamontov.invoice_converter.common.ui.theme.AppTheme
import com.mamontov.invoice_converter.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import moe.tlaster.kfilepicker.FilePicker
import moe.tlaster.kfilepicker.PlatformFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    AppTheme(
        isDarkTheme = false,
    ) {
        val scope = rememberCoroutineScope()
        var pickedFiles by remember { mutableStateOf(listOf<PlatformFile>()) }
        var savedResult by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            Column {
                Row {
//                    Image(
//                        painterResource(MR.images.ic_folder),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .padding(start = 16.dp)
//                            .size(42.dp)
//                            .clickable {
//                                pickFiles(scope) { files ->
//                                    pickedFiles = files
//                                }
//                            },
//                        colorFilter = ColorFilter.tint(AppTheme.colors.onSurface)
//                    )

                    Button(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = { pickFilesToConvert(scope) { pickedFiles = it } }
                    ) {
                        Text(text = "Select files to convert")
                    }

                    Button(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = {
                            convertFiles(scope, pickedFiles) {
                                savedResult = it
                            }
                        }
                    ) {
                        Text(text = "Convert selected files")
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(pickedFiles) { file ->
                        PickedFileItem(file)
                    }
                }

                if (savedResult != null) {
                    AlertDialog(
                        onDismissRequest = {
                            savedResult = null
                        },
                        modifier = Modifier
                            .size(400.dp),
                        properties = DialogProperties(),
                    ) {
                        Column(
                            modifier = Modifier.background(AppTheme.colors.background),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = savedResult!!)
                            Button(
                                modifier = Modifier.padding(start = 16.dp),
                                onClick = {
                                    savedResult = null
                                }
                            ) {
                                Text(text = "OK")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickedFileItem(
    file: PlatformFile,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(text = "File name = ${file.name}")
        Text(text = "File path = ${file.path}")
    }
}

private fun pickFilesToConvert(scope: CoroutineScope, onFilesPicked: (List<PlatformFile>) -> Unit) {
    scope.launch {
        val files = FilePicker.pickFiles(
            allowedExtensions = listOf("xls", "xlsx"),
            allowMultiple = true
        )
        files.forEach {
            println("name=${it.name} ; path = ${it.path}")
        }
        onFilesPicked(files)
    }
}

private fun convertFiles(
    scope: CoroutineScope,
    files: List<PlatformFile>,
    onResult: (String) -> Unit
) {
    scope.launch {
        val data = getPlatform().convert(files)
        getPlatform().writeData(data)
            .onSuccess {
                onResult("Success result, saved file on $it")
            }
            .onFailure {
                onResult("Error occurred ${it.message ?: "Something went wrong"}")
            }
    }
}
