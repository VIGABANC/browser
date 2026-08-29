with open('app/src/main/java/com/example/ui/components/WebpageAiFloatingActionButton.kt', 'r') as f:
    text = f.read()

target = """@Composable
fun WebpageAiFloatingActionButton(
    pageTitle: String,
    hasDetectedMedia: Boolean,
    onTriggerAiTask: (AiTaskType, String) -> Unit,
    onOpenMediaGrabber: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")"""

replacement = """import android.provider.Settings
import androidx.compose.ui.platform.LocalContext

@Composable
fun WebpageAiFloatingActionButton(
    pageTitle: String,
    hasDetectedMedia: Boolean,
    onTriggerAiTask: (AiTaskType, String) -> Unit,
    onOpenMediaGrabber: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val areAnimationsEnabled = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")"""

text = text.replace(target, replacement)

target_scale = """.scale(if (!isExpanded) pulseScale else 1f)"""
replacement_scale = """.scale(if (!isExpanded && areAnimationsEnabled) pulseScale else 1f)"""
text = text.replace(target_scale, replacement_scale)

with open('app/src/main/java/com/example/ui/components/WebpageAiFloatingActionButton.kt', 'w') as f:
    f.write(text)
