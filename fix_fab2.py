with open('app/src/main/java/com/example/ui/components/WebpageAiFloatingActionButton.kt', 'r') as f:
    text = f.read()

target = """    var isExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")"""

if target not in text:
    target = """    var isExpanded by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")"""

replacement = """    var isExpanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val areAnimationsEnabled = androidx.compose.runtime.remember(context) {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")"""

text = text.replace(target, replacement)

with open('app/src/main/java/com/example/ui/components/WebpageAiFloatingActionButton.kt', 'w') as f:
    f.write(text)
