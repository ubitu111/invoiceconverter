package com.mamontov.invoice_converter

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override fun openFolder() {

    }
}

actual fun getPlatform(): Platform = IOSPlatform()
