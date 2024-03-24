import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nfcRead.NfcManager
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var searchTag by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val nfcManager = NfcManager(getPlatformContext())
        var tagData by remember { mutableStateOf("") }

        scope.launch {

            nfcManager.flow.collectLatest {
                tagData = it
            }
        }
        Box(Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tagData.isNotEmpty()) {

                    Text("NFC Tag Data:  ${tagData.substring(3,20)}")
                }
                Button(onClick = {
                    searchTag = !searchTag
                }, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                    Text("Search Tag")
                }
            }

            if (searchTag) {
                nfcManager.RegisterForNFC()
            }

        }
    }
}