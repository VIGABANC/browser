with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'r') as f:
    text = f.read()

text = text.replace("while (kotlinx.coroutines.isActive)", "while (kotlinx.coroutines.currentCoroutineContext().isActive)")
text = text.replace("import kotlinx.coroutines.delay", "import kotlinx.coroutines.delay\nimport kotlinx.coroutines.isActive")

with open('app/src/main/java/com/example/viewmodel/BrowserViewModel.kt', 'w') as f:
    f.write(text)
