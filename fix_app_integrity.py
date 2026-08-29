with open('app/src/main/java/com/example/AegisApplication.kt', 'r') as f:
    text = f.read()

target = """        // 4. Initialize Room database on IO thread
        GlobalScope.launch(Dispatchers.IO) {
            AegisDatabase.getDatabase(this@AegisApplication)
        }"""

replacement = """        // 4. Initialize Room database on IO thread
        GlobalScope.launch(Dispatchers.IO) {
            AegisDatabase.getDatabase(this@AegisApplication)
        }
        
        // 5. Verify Attestation Log Integrity
        GlobalScope.launch(Dispatchers.IO) {
            val am = com.example.data.security.AttestationManager(this@AegisApplication)
            if (!am.verifyLogIntegrity()) {
                // Revert to safe mode if log is tampered
                am.revertToSafeMode()
            }
        }"""

if "verifyLogIntegrity" not in text:
    text = text.replace(target, replacement)

with open('app/src/main/java/com/example/AegisApplication.kt', 'w') as f:
    f.write(text)
