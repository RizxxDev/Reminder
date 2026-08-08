import re

with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'r') as f:
    content = f.read()

# I want to fix the `) {` issues.
# In my previous grep, it showed:
#   203	                                )
#   204	        ) {
content = content.replace(")\n        ) {", ") {")
content = content.replace(")\n        },", ") },")
content = content.replace("\n        }", "\n        }")

# Let's just fix all of them where there's an extra `)\n        )`
content = re.sub(r'\)[ \t]*\n[ \t]*\)[ \t]*\{', ') {', content)
content = re.sub(r'\)[ \t]*\n[ \t]*\)[ \t]*\,', ') ,', content)

with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'w') as f:
    f.write(content)
