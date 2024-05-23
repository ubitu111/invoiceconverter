import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mamontov.invoice_converter.root.RootScreen
import moe.tlaster.kfilepicker.FilePicker

fun main() {
    application {
        val state = rememberWindowState(
            size = DpSize(1200.dp, 600.dp)
        )
        Window(
            onCloseRequest = { exitApplication() },
            state = state,
            title = "Invoice Converter"
        ) {
            FilePicker.init(window)
            RootScreen()
        }
    }
}
