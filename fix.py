with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'r') as f:
    content = f.read()

# Revert the sed command effect:
content = content.replace("            )\n        }", "            )")
content = content.replace(".padding(padding) {", ".padding(padding)\n        ) {")

with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'w') as f:
    f.write(content)
