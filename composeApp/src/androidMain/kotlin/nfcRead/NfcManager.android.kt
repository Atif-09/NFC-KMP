package nfcRead

import PlatformContext
import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

actual class NfcManager actual constructor(private val platformContext: PlatformContext) :
    NfcAdapter.ReaderCallback {
    private var mNfcAdapter: NfcAdapter? = null
    private val _tagData = MutableSharedFlow<String>()

    val scope = CoroutineScope(SupervisorJob())

    @Composable
    actual fun RegisterForNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(platformContext.context);

        if (mNfcAdapter != null) {

            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 500)

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag after reading
            mNfcAdapter!!.enableReaderMode(
                LocalContext.current as Activity,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        val mNdef = Ndef.get(tag)
        val mNdefMessage: NdefMessage = mNdef.cachedNdefMessage
        val record = mNdefMessage.records
        val ndefRecordsCount = record.size

        if (ndefRecordsCount > 0) {
            for (i in 0 until ndefRecordsCount) {
                val payload = String(record[i].payload, Charsets.UTF_8)
                scope.launch {
                    _tagData.emit(payload)
                }
            }
        }
    }

    actual val flow: SharedFlow<String>
        get() = _tagData
}