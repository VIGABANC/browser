with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'r') as f:
    text = f.read()

import_target = "import java.util.UUID"
import_replacement = "import java.util.UUID\nimport java.security.MessageDigest"

if "import java.security.MessageDigest" not in text:
    text = text.replace(import_target, import_replacement)

# Look for the deduplication in onMediaDetectedFromJs
dedup_target = """                    detected.add(
                        DetectedMedia(
                            id = java.util.UUID.randomUUID().toString(),"""

dedup_replacement = """                    val hashId = hashUrl(url)
                    detected.add(
                        DetectedMedia(
                            id = hashId,"""

if "val hashId = hashUrl(url)" not in text:
    text = text.replace(dedup_target, dedup_replacement)

end_of_js_target = """                }
            }
            updateActiveTab { it.copy(detectedMedia = (it.detectedMedia + detected).distinctBy { m -> m.url }.takeLast(20)) }"""

end_of_js_replacement = """                }
            }
            updateActiveTab { it.copy(detectedMedia = (it.detectedMedia + detected).distinctBy { m -> m.id }.takeLast(20)) }"""

if "distinctBy { m -> m.id }" not in text:
    text = text.replace(end_of_js_target, end_of_js_replacement)

# Add hashUrl method if not exists
hash_method = """    private fun hashUrl(url: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray())
            .take(8)
            .joinToString("") { "%02x".format(it) }
    }"""

if "private fun hashUrl" not in text:
    text = text + "\n" + hash_method + "\n"

with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'w') as f:
    f.write(text)
