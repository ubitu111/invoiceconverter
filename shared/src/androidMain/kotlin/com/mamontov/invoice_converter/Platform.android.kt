package com.mamontov.invoice_converter

class AndroidPlatform : Platform {
    override suspend fun convert(inputs: List<InputData>): List<ConvertedData> {
        TODO("Not yet implemented")
    }

    override suspend fun writeData(data: List<ConvertedData>): Result<String> {
        TODO("Not yet implemented")
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()
