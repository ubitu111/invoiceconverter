package com.mamontov.invoice_converter

import moe.tlaster.kfilepicker.PlatformFile

interface Platform {
    suspend fun convert(inputs: List<InputData>): List<ConvertedData>
    suspend fun writeData(data: List<ConvertedData>): Result<String>
}

data class ConvertedData(
    val productName: String,
    val productsCount: Double,
    val priceWithNds: Double,
    val priceWithoutNds: Double,
    val sumWithNds: Double,
    val sumWithoutNds: Double,
)

data class InputData(
    val files: List<PlatformFile>,
    val type: VendorType,
) {
    companion object {
        val EMPTY = InputData(
            files = listOf(),
            type = VendorType.Empty,
        )
    }
}

enum class VendorType(val stringName: String) {
    GetChips("Гетчипс"),
    Compel("Компэл"),
    Platan("Платан"),
    Radiotech("Радиотех"),
    Prom("Пром"),
    Empty(""),
}

expect fun getPlatform(): Platform
