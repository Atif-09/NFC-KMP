package nfcRead

import PlatformContext
import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import platform.CoreNFC.NFCNDEFMessage
import platform.CoreNFC.NFCNDEFPayload
import platform.CoreNFC.NFCNDEFReaderSession
import platform.CoreNFC.NFCNDEFReaderSessionDelegateProtocol
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.darwin.NSObject
import platform.posix.memcpy

actual class NfcManager actual constructor(platformContext: PlatformContext) : NSObject(),
    NFCNDEFReaderSessionDelegateProtocol {

    private val scope = CoroutineScope(SupervisorJob())

    private val _tagData = MutableSharedFlow<String>()

    @Composable
    actual fun RegisterForNFC() {
        if (NFCNDEFReaderSession.readingAvailable()) {
            val session = NFCNDEFReaderSession(this, null, false)
            session.alertMessage = "Hold your iPhone near the item to learn more about it."
            session.beginSession()
        }
    }

    actual val flow: SharedFlow<String>
        get() = _tagData

    override fun readerSession(session: NFCNDEFReaderSession, didDetectNDEFs: List<*>) {
        val message = didDetectNDEFs.firstOrNull() as? NFCNDEFMessage
        val records = message?.records as List<NFCNDEFPayload>

        records.forEach {
            val payloadMessage = it.payload.toByteArray().decodeToString().substringAfter("en")
            scope.launch {
                _tagData.emit(payloadMessage)
            }
        }
        session.invalidateSession()
    }

    override fun readerSessionDidBecomeActive(session: NFCNDEFReaderSession) {
    }

    override fun readerSession(session: NFCNDEFReaderSession, didInvalidateWithError: NSError) {
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val data = this
    val d = memScoped { data }
    return ByteArray(d.length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), d.bytes, d.length)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toData(): NSData = memScoped {
    NSData.create(
        bytes = allocArrayOf(this@toData),
        length = this@toData.size.toULong()
    )
}
