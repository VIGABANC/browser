import re

with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'r') as f:
    text = f.read()

# Add MemoryWatchdog instantiation
import_target = "import android.app.Application"
import_replacement = "import android.app.Application\nimport kotlinx.coroutines.delay"
text = text.replace(import_target, import_replacement)

init_target = """    init {
        // Seed default bookmarks and history in Room DB if empty"""
init_replacement = """    private val memoryWatchdog = MemoryWatchdog(application.applicationContext)

    init {
        // Memory Watchdog loop
        viewModelScope.launch {
            while (kotlinx.coroutines.isActive) {
                memoryWatchdog.checkMemoryPressure()
                delay(30_000)
            }
        }
        
        // Seed default bookmarks and history in Room DB if empty"""
text = text.replace(init_target, init_replacement)

with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'w') as f:
    f.write(text)
