package com.mamontov.invoice_converter

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.io.path.Path
import kotlin.io.path.outputStream

class DesktopPlatform : Platform {
    override suspend fun convert(inputs: List<InputData>): List<ConvertedData> {
        val result = mutableListOf<ConvertedData>()
        inputs.forEach { inputData ->
            inputData.files.forEach { file ->
                val bytes = file.readAllBytesAsync()
                val inputStream = ByteArrayInputStream(bytes)
                val workbook = WorkbookFactory.create(inputStream)
                val workSheet = workbook.getSheetAt(0)

                var startRowNum = -1
                var endRowNum = -1

                var productsColumnIndex = -1 // наименование
                var countColumnIndex = -1 // количество
                var priceWithNdsColumnIndex = -1 // цена с НДС
                var priceWithoutNdsColumnIndex = -1 // цена без НДС

                workSheet.findBy("товар", "наименование")
                    ?.also { cell ->
                        println("file ${file.name} Cell = $cell contain Товар columnIndex = ${cell.columnIndex} rowIndex = ${cell.rowIndex}")
                        startRowNum = cell.rowIndex + 1
                        productsColumnIndex = cell.columnIndex
                    }
                println("Find start row index = $startRowNum")
                println("Find products column index = $productsColumnIndex")

                workSheet.findBy("итого")
                    ?.also { cell ->
                        println("file ${file.name} Cell = $cell contain итого columnIndex = ${cell.columnIndex} rowIndex = ${cell.rowIndex}")
                        endRowNum = cell.rowIndex - 1
                    }
                    ?: run {
                        endRowNum = workSheet.lastRowNum
                    }
                println("Find end row index = $endRowNum")

                workSheet.findBy("кол-во", "количест")
                    ?.also { cell ->
                        countColumnIndex = cell.columnIndex
                    }

                val priceWithoutNdsCell = workSheet.findBy("цена без ндс")
                    ?.also { priceWithoutNdsColumnIndex = it.columnIndex }
                println("priceWithoutNdsCell = $priceWithoutNdsCell")

                val priceWithNdsCell = workSheet.findBy("цена с ндс")
                    ?.also { priceWithNdsColumnIndex = it.columnIndex }
                println("priceWithNdsCell = $priceWithNdsCell")

                // не найдено ячеек с ценами с ндс или без, ищем в разных ячейках
                if (priceWithNdsCell == null && priceWithoutNdsCell == null) {
                    val (priceColumnIndex, priceRowIndex) = workSheet.findBy("цена")
                        ?.let { it.columnIndex to it.rowIndex }
                        ?: return@forEach // todo добавить маркер, что файл не был обработан

                    val priceCellLower =
                        workSheet.getRow(priceRowIndex + 1).getCell(priceColumnIndex)
                    if (priceCellLower.toString().lowercase() == "с ндс") {
                        priceWithNdsColumnIndex = priceColumnIndex
                    } else if (priceCellLower.toString().lowercase() == "без ндс") {
                        priceWithoutNdsColumnIndex = priceColumnIndex
                    } else {
                        val priceCellRight =
                            workSheet.getRow(priceRowIndex).getCell(priceColumnIndex + 1)
                        if (priceCellRight.toString().lowercase() == "с ндс") {
                            priceWithNdsColumnIndex = priceColumnIndex
                        } else if (priceCellRight.toString().lowercase() == "без ндс") {
                            priceWithoutNdsColumnIndex = priceColumnIndex
                        }
                    }
                }


                for (i in startRowNum..endRowNum) {
                    val productRow = workSheet.getRow(i)

                    try {
                        val productName = productRow.getCell(productsColumnIndex)
                            .takeIf { it.toString().isNotEmpty() }
                            .toExactString()
                            ?.lines()
                            ?.joinToString("")

                        val productsCount = productRow.getCell(countColumnIndex)
                            .takeIf { it.toString().isNotEmpty() }
                            .toExactString()

                        var priceWithNds = priceWithNdsColumnIndex
                            .takeIf { it >= 0 }
                            ?.let { index ->
                                productRow.getCell(index)
                                    .takeIf { it.toString().isNotEmpty() }
                                    ?.toString()
                                    ?.replace(",", ".")
                                    ?.replace("$", "")
                                    ?.trim()
                            }

                        var priceWithoutNds = priceWithoutNdsColumnIndex
                            .takeIf { it >= 0 }
                            ?.let { index ->
                                productRow.getCell(index)
                                    .takeIf { it.toString().isNotEmpty() }
                                    ?.toString()
                                    ?.replace(",", ".")
                                    ?.replace("$", "")
                                    ?.trim()
                            }

                        if (priceWithNds == null) {
                            priceWithNds = priceWithoutNds?.toDoubleOrNull()
                                ?.let { it * 1.2 }
                                ?.toBigDecimal()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toString()
                        }

                        if (priceWithoutNds == null) {
                            priceWithoutNds = priceWithNds?.toDoubleOrNull()
                                ?.div(1.2)
                                ?.toBigDecimal()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toString()
                        }

                        if (productName != null && productsCount != null && priceWithNds != null && priceWithoutNds != null) {
                            val productsCountInt = productsCount.toInt()
                            val sumWithoutNds = productsCountInt * priceWithoutNds.toDouble()
                            val sumWithNds = (sumWithoutNds * 1.2).toBigDecimal()
                                .setScale(2, RoundingMode.HALF_UP)
                                .toDouble()

                            result.add(
                                ConvertedData(
                                    productName = productName,
                                    productsCount = productsCountInt.toDouble(),
                                    priceWithNds = priceWithNds.toDouble(),
                                    priceWithoutNds = priceWithoutNds.toDouble(),
                                    sumWithNds = sumWithNds,
                                    sumWithoutNds = sumWithoutNds,
                                )
                            )
                            println("file ${file.name} productName=$productName productsCount=${productsCountInt.toDouble()} priceWithNds=$priceWithNds priceWithoutNds=$priceWithoutNds summWithNds=$sumWithNds summWithoutNds=$sumWithoutNds")
                        } else {
                            // todo добавить маркер, что файл не был обработан
                        }
                    } catch (ex: Exception) {
                        println(ex)
                    }
                }
            }
        }

        return result
    }

    override suspend fun writeData(data: List<ConvertedData>): Result<String> {
        val workbook = XSSFWorkbook()
        val workSheet = workbook.createSheet()

        val titleRow = workSheet.createRow(0)

        titleRow.createCell(0).setCellValue("Наименование поставщика")
        titleRow.createCell(1).setCellValue("Количество")
        titleRow.createCell(2).setCellValue("Цена без НДС")
        titleRow.createCell(3).setCellValue("Цена с НДС")
        titleRow.createCell(4).setCellValue("Сумма без НДС")
        titleRow.createCell(5).setCellValue("Сумма с НДС")

        data.forEachIndexed { index, convertedData ->
            val row = workSheet.createRow(index + 1)
            row.createCell(0).setCellValue(convertedData.productName)
            row.createCell(1).setCellValue(convertedData.productsCount)
            row.createCell(2).setCellValue(convertedData.priceWithoutNds)
            row.createCell(3).setCellValue(convertedData.priceWithNds)
            row.createCell(4).setCellValue(convertedData.sumWithoutNds)
            row.createCell(5).setCellValue(convertedData.sumWithNds)
        }

        val tempFile = kotlin.io.path.createTempFile(
            directory = Path(System.getProperty("user.home") + "/Downloads/"),
            prefix = "generated_output_",
            suffix = ".xlsx",
        )
        workbook.write(tempFile.outputStream())
        workbook.close()

        return Result.success("${tempFile.fileName}")
    }

    private fun Sheet.findBy(vararg values: String): Cell? {
        forEach { row ->
            row.forEach { cell ->
                values.forEach { value ->
                    val contains = cell.toString().lowercase()
                        .lines()
                        .joinToString("")
                        .replace("c", "с")
                        .contains(value)
                    if (contains) {
                        return cell
                    }
                }
            }
        }
        return null
    }

    private fun Cell?.toExactString(): String? {
        return when (this?.cellType) {
            CellType.NUMERIC -> {
                val format = DecimalFormat("#.####")
                format.format(numericCellValue).trim()
            }

            CellType.STRING ->
                stringCellValue.trim()

            CellType.FORMULA ->
                cellFormula.trim()

            else -> null
        }
    }
}

actual fun getPlatform(): Platform = DesktopPlatform()
