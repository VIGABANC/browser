with open('app/src/main/java/com/example/ui/pages/ShieldDashboardPage.kt', 'r') as f:
    text = f.read()

text = text.replace("            androidx.compose.foundation.text.selection.SelectionContainer {\n", "")
text = text.replace("            }\n            Spacer(modifier = Modifier.height(20.dp))", "            Spacer(modifier = Modifier.height(20.dp))")

# Now apply carefully
target = """            Spacer(modifier = Modifier.height(16.dp))
            // Metrics Grid
            Text("Impact & Économies Réelles", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AegisCyanPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row("""

replacement = """            Spacer(modifier = Modifier.height(16.dp))
            // Metrics Grid
            Text("Impact & Économies Réelles", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AegisCyanPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.text.selection.SelectionContainer {
                Row("""

text = text.replace(target, replacement)

target2 = """                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Advanced Shield Controls"""

replacement2 = """                }
            }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Advanced Shield Controls"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/pages/ShieldDashboardPage.kt', 'w') as f:
    f.write(text)
