with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'r') as f:
    lines = f.readlines()

# Clean up the bottom lines (from Save button to the end)
# We want the Button to be inside the inner Column.
# So the end of the file should just be:
#             } // button
#         } // inner column
#     } // outer column
# } // scaffold
# } // AddTaskScreen

# Find where Button ends.
text = "".join(lines)
import re

text = re.sub(r'                Text\("Simpan Tugas".*\n.*\n.*\n.*\n.*', '                Text("Simpan Tugas", style = MaterialTheme.typography.labelLarge)\n            }\n        }\n    }\n}\n', text)

with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'w') as f:
    f.write(text)
