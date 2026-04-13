# 💶 Kotlin Budget Tracker

A command-line budget tracking tool written in Kotlin. Built as a learning project while getting started with Kotlin — covers core language features like data classes, functions, lambdas, and user input handling.

## Features

- Add expenses with a description, amount, and category
- View all recorded expenses with a running total
- Summarize spending broken down by category
- Find your biggest single expense

## Getting Started

### Prerequisites

- [Kotlin](https://kotlinlang.org/docs/command-line.html) installed (`kotlinc`)
- Java runtime (JRE 8+)

### Run

**Compile:**
```bash
kotlinc src/Main.kt -include-runtime -jar budget.jar
```

**Execute:**
```bash
java -jar budget.jar
```

### Example session

```
💶 Kotlin Budget Tracker

--- Menu ---
1. Add expense
2. View all expenses
3. Summary by category
4. Biggest expense
5. Exit
Choose: 1
Description: Groceries
Amount (€): 42.50
Category (food/transport/rent/entertainment/other): food
✅ Added: Groceries — €42.50 [food]
```

## What I practiced

- `data class` for structured data
- `mutableListOf` and list operations
- Lambda functions (`forEach`, `groupBy`, `maxBy`, `sumOf`)
- Input handling with `readln()` and `toDoubleOrNull()`
- Function decomposition and `when` expressions