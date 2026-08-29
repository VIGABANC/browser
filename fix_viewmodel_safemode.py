with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'r') as f:
    text = f.read()

import_target = "import com.example.data.security.AttestationManager"
import_replacement = "import com.example.data.security.AttestationManager\nimport com.example.data.security.SafeModeFilter"

if "import com.example.data.security.SafeModeFilter" not in text:
    text = text.replace(import_target, import_replacement)

init_target = "val attestationManager = com.example.data.security.AttestationManager(application.applicationContext)"
init_replacement = "val attestationManager = com.example.data.security.AttestationManager(application.applicationContext)\n    val safeModeFilter = SafeModeFilter()"

if "val safeModeFilter" not in text:
    text = text.replace(init_target, init_replacement)

request_dl_target = """    fun requestDownload(format: MediaFormat) {
        val media = _selectedMediaForGrabber.value ?: return
        val isSafe = _safeModeState.value.isSafeModeActive
        val attested = _safeModeState.value.hasUserAttested

        if (isSafe && !attested) {
            _isAttestationDialogOpen.value = true
            return
        }

        downloadManager.enqueueDownload(
            title = media.title ?: "Unknown Media",
            sourcePageUrl = media.pageUrl,
            downloadUrl = format.directUrl.ifBlank { media.url },
            format = format,
            isSafeModeAttested = attested
        )

        _isMediaGrabberOpen.value = false
    }"""

request_dl_replacement = """    fun requestDownload(format: MediaFormat) {
        val media = _selectedMediaForGrabber.value ?: return
        
        // 1. Safe mode check
        if (attestationManager.isSafeMode) {
            if (!safeModeFilter.isSafeSource(media.url, media.pageUrl)) {
                _isAttestationDialogOpen.value = true
                return
            }
        }

        val attested = !attestationManager.isSafeMode

        // 2. Storage/Network checks (omitted for brevity, done in worker)
        
        downloadManager.enqueueDownload(
            url = format.directUrl.ifBlank { media.url },
            outputPath = java.io.File(getApplication<android.app.Application>().getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "${media.title ?: "download"}.${format.container}").absolutePath,
            wifiOnly = true, // We could pull this from settings
            isAudioOnly = format.isAudioOnly
        )

        _isMediaGrabberOpen.value = false
    }"""

if "if (!safeModeFilter.isSafeSource" not in text:
    text = text.replace(request_dl_target, request_dl_replacement)

with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'w') as f:
    f.write(text)
