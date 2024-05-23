package com.mamontov.invoice_converter

import moe.tlaster.kfilepicker.PlatformFile

class AndroidPlatform : Platform {
    override suspend fun convert(files: List<PlatformFile>): List<ConvertedData> {
        TODO("Not yet implemented")
    }

    override suspend fun writeData(data: List<ConvertedData>): Result<String> {
        TODO("Not yet implemented")
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()
