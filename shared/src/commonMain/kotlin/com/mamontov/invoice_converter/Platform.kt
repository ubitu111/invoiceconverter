package com.mamontov.invoice_converter

import moe.tlaster.kfilepicker.PlatformFile

interface Platform {
    suspend fun convert(files: List<PlatformFile>): List<ConvertedData>
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

expect fun getPlatform(): Platform
