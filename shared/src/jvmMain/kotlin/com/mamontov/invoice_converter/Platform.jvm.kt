package com.mamontov.invoice_converter

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.text.DecimalFormat
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream

private const val NO_INDEX: Int = -1
private const val NO_VALUE: Double = -1.0

class DesktopPlatform : Platform {
    override suspend fun convert(inputs: List<InputData>): List<ConvertedData> {
        val result = mutableListOf<ConvertedData>()
        inputs.forEach { inputData ->
            inputData.files.forEach { file ->
                val bytes = file.readAllBytesAsync()
                val inputStream = ByteArrayInputStream(bytes)
                val workbook = WorkbookFactory.create(inputStream)
                val workSheet = workbook.getSheetAt(0)

                var startRowNum = NO_INDEX
                var endRowNum = NO_INDEX

                var productsColumnIndex = NO_INDEX // наименование
                var countColumnIndex = NO_INDEX // количество
                val priceWithNdsColumnIndex = workSheet // цена с НДС
                    .toPriceWithNdsIndex(inputData.type)
                val priceWithoutNdsColumnIndex = workSheet // цена без НДС
                    .toPriceWithoutNdsIndex(inputData.type)

                val productCell = when (inputData.type) {
                    VendorType.Radiotech -> workSheet.findBy("товары (работы, услуги)")

                    else -> workSheet.findBy("товар", "наименование")
                }
                productCell
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

                        val priceWithNdsStr = priceWithNdsColumnIndex.toPriceString(productRow)
                        val priceWithoutNdsStr =
                            priceWithoutNdsColumnIndex.toPriceString(productRow)

                        if (productName != null && productsCount != null && (priceWithNdsStr != null || priceWithoutNdsStr != null)) {
                            val productsCountInt = productsCount.toInt()
                            val priceWithoutNds = priceWithoutNdsStr?.toDoubleOrNull()
                            val priceWithNds = priceWithNdsStr?.toDoubleOrNull()

                            val sumWithoutNds = priceWithoutNds
                                ?.let { productsCountInt * it }
                                ?: NO_VALUE
                            val sumWithNds = priceWithNds
                                ?.let { productsCountInt * it }
                                ?: NO_VALUE

                            result.add(
                                ConvertedData(
                                    productName = productName,
                                    productsCount = productsCountInt.toDouble(),
                                    priceWithNds = priceWithNds ?: NO_VALUE,
                                    priceWithoutNds = priceWithoutNds ?: NO_VALUE,
                                    sumWithNds = sumWithNds,
                                    sumWithoutNds = sumWithoutNds,
                                ).also {
                                    println("file ${file.name} ConvertedData=$it")
                                }
                            )
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

        workSheet.createRow(0).apply {
            createCell(0).setCellValue("Наименование поставщика")
            createCell(1).setCellValue("Количество")
            createCell(2).setCellValue("Цена без НДС")
            createCell(3).setCellValue("Цена с НДС")
            createCell(4).setCellValue("Сумма без НДС")
            createCell(5).setCellValue("Сумма с НДС")
        }

        data.forEachIndexed { index, convertedData ->
            workSheet.createRow(index + 1).apply {
                createCell(0).setCellValue(convertedData.productName)
                createCell(1).setCellValue(convertedData.productsCount)
                convertedData.priceWithoutNds
                    .takeIf { it != NO_VALUE }
                    ?.let { createCell(2).setCellValue(it) }
                convertedData.priceWithNds
                    .takeIf { it != NO_VALUE }
                    ?.let { createCell(3).setCellValue(it) }
                convertedData.sumWithoutNds
                    .takeIf { it != NO_VALUE }
                    ?.let { createCell(4).setCellValue(it) }
                convertedData.sumWithNds
                    .takeIf { it != NO_VALUE }
                    ?.let { createCell(5).setCellValue(it) }
            }
        }

        for (i in 0..5) {
            workSheet.autoSizeColumn(i)
        }

        val tempFile = createTempFile(
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

    private fun Sheet.toPriceWithNdsIndex(type: VendorType): Int {
        val cell = when (type) {
            VendorType.GetChips -> findBy("цена с ндс")

            VendorType.Radiotech -> findBy("цена")

            else -> null
        }

        return cell?.columnIndex ?: NO_INDEX
    }

    private fun Sheet.toPriceWithoutNdsIndex(type: VendorType): Int {
        val cell = when (type) {
            VendorType.Compel -> findBy("цена без ндс")

            VendorType.Platan -> findBy("цена")

//            VendorType.Prom -> findBy("цена без ндс")

            else -> null
        }

        return cell?.columnIndex ?: NO_INDEX
    }

    private fun Int.toPriceString(productRow: Row): String? {
        return takeIf { it >= 0 }
            ?.let { index ->
                productRow.getCell(index)
                    .takeIf { it.toString().isNotEmpty() }
                    ?.toString()
                    ?.replace(",", ".")
                    ?.replace("$", "")
                    ?.trim()
            }
    }
}

actual fun getPlatform(): Platform = DesktopPlatform()

// 1. Гетчипс - берем столбцы цен ТОЛЬКО С НДС +
// 2. Компэл - убрать столбец с долларами из счета - остается БЕЗ НДС +
// 3. Платан - берем столбцы ТОЛЬКО БЕЗ НДС +
// 4. Радиотех - добавить
// 5. Пром - проверить
