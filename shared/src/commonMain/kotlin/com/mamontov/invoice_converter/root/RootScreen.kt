package com.mamontov.invoice_converter.root

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mamontov.invoice_converter.InputData
import com.mamontov.invoice_converter.MR
import com.mamontov.invoice_converter.VendorType
import com.mamontov.invoice_converter.common.ui.theme.AppTheme
import com.mamontov.invoice_converter.getPlatform
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import moe.tlaster.kfilepicker.FilePicker
import moe.tlaster.kfilepicker.PlatformFile

@Composable
fun RootScreen() {
    AppTheme(
        isDarkTheme = false,
    ) {
        val scope = rememberCoroutineScope()

        var getChipsFiles by remember { mutableStateOf(InputData.EMPTY) }
        var compelFiles by remember { mutableStateOf(InputData.EMPTY) }
        var platanFiles by remember { mutableStateOf(InputData.EMPTY) }
        var radiotechFiles by remember { mutableStateOf(InputData.EMPTY) }
        var promFiles by remember { mutableStateOf(InputData.EMPTY) }

        var savedResult by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            Column {
                Row {
                    PickItem(VendorType.GetChips) {
                        pickFilesToConvert(scope) {
                            getChipsFiles = InputData(files = it, type = VendorType.GetChips)
                        }
                    }

                    PickItem(VendorType.Compel) {
                        pickFilesToConvert(scope) {
                            compelFiles = InputData(files = it, type = VendorType.Compel)
                        }
                    }

                    PickItem(VendorType.Platan) {
                        pickFilesToConvert(scope) {
                            platanFiles = InputData(files = it, type = VendorType.Platan)
                        }
                    }

                    PickItem(VendorType.Radiotech) {
                        pickFilesToConvert(scope) {
                            radiotechFiles = InputData(files = it, type = VendorType.Radiotech)
                        }
                    }

                    PickItem(VendorType.Prom) {
                        pickFilesToConvert(scope) {
                            promFiles = InputData(files = it, type = VendorType.Prom)
                        }
                    }
                }

                Button(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                    onClick = {
                        getChipsFiles = InputData.EMPTY
                        compelFiles = InputData.EMPTY
                        platanFiles = InputData.EMPTY
                        radiotechFiles = InputData.EMPTY
                        promFiles = InputData.EMPTY
                    }
                ) {
                    Text(text = "Очистить список файлов")
                }

                Button(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                    onClick = {
                        convertFiles(
                            getChipsFiles,
                            compelFiles,
                            platanFiles,
                            radiotechFiles,
                            promFiles,
                            scope = scope,
                            onResult = { savedResult = it },
                        )
                    }
                ) {
                    Text(text = "Обработать выбранные")
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    pickedFilesBlock(getChipsFiles)
                    pickedFilesBlock(compelFiles)
                    pickedFilesBlock(platanFiles)
                    pickedFilesBlock(radiotechFiles)
                    pickedFilesBlock(promFiles)
                }

                if (savedResult != null) {
                    ShowDialog(savedResult!!) {
                        savedResult = null
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDialog(
    text: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .width(400.dp)
            .height(200.dp),
        properties = DialogProperties(),
    ) {
        Column(
            modifier = Modifier.background(AppTheme.colors.background).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SelectionContainer {
                Text(text = text, textAlign = TextAlign.Center)
            }
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = { onDismiss() }
            ) {
                Text(text = "OK")
            }
        }
    }
}

@Composable
private fun PickItem(
    vendorType: VendorType,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 16.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(MR.images.ic_folder),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp),
            colorFilter = ColorFilter.tint(AppTheme.colors.onSurface)
        )
        Text(text = vendorType.stringName)
    }
}

private fun LazyListScope.pickedFilesBlock(
    inputData: InputData,
) {
    if (inputData.files.isNotEmpty()) {
        item {
            Text(
                inputData.type.stringName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        items(inputData.files) { file ->
            PickedFileItem(file)
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
            .padding(bottom = 8.dp)
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
    vararg inputs: InputData,
    scope: CoroutineScope,
    onResult: (String) -> Unit
) {
    if (inputs.toList().any { it != InputData.EMPTY }) {
        scope.launch {
            val data = getPlatform().convert(inputs.toList())
            getPlatform().writeData(data)
                .onSuccess {
                    onResult("Сгенерированный файл лежит в папке Загрузки, имя: $it")
                }
                .onFailure {
                    onResult("Произошла ошибка ${it.message}")
                }
        }
    } else {
        onResult("Нужно выбрать файлы для обработки")
    }
}
