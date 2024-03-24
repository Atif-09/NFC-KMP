package nfcRead

import PlatformContext
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.SharedFlow

expect class NfcManager(platformContext: PlatformContext) {

    val flow: SharedFlow<String>

    @Composable
    fun RegisterForNFC()
}