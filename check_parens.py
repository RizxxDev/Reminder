with open('app/src/main/java/com/example/ui/AddTaskScreen.kt', 'r') as f:
    text = f.read()

def check_balance(t):
    stack = []
    for i, c in enumerate(t):
        if c in '({[':
            stack.append((c, i))
        elif c in ')}]':
            if not stack:
                print(f"Unmatched closing {c} at {i}")
                return
            last_c, last_i = stack.pop()
            if (last_c == '(' and c != ')') or (last_c == '{' and c != '}') or (last_c == '[' and c != ']'):
                print(f"Mismatched {last_c} at {last_i} with {c} at {i}")
                return
    if stack:
        print(f"Unmatched opening {stack[-1][0]} at {stack[-1][1]}")
    else:
        print("All balanced!")

check_balance(text)
