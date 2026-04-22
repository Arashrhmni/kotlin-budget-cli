# 💶 Kotlin Budget Tracker

A command-line budget tracking tool written in Kotlin. Built as a learning project while getting started with Kotlin — now with simple file saving/loading, automatic dates, transaction deletion, a budget limit feature, filtering, and editing.

## Features

- Add expenses and income with a description, amount, category, and automatic date
- Edit a transaction by number
- Delete transactions by number
- Filter transactions by type, category, or description text
- Set a monthly budget limit and check how much you have left
- Show a warning when your expenses go over the budget
- Categories enforced via `enum class` for safer input
- `sealed interface` models both `Expense` and `Income` as transaction types
- View all transactions with type labels and dates
- Summarize spending and income broken down by category
- See your current balance (total income minus total expenses)
- Find your biggest single expense
- Automatically save transactions to `transactions.txt`
- Automatically load saved transactions when the program starts
- Save the budget limit to `budget.txt`

## Getting Started

### Prerequisites

- [Kotlin](https://kotlinlang.org/docs/command-line.html) installed (`kotlinc`)
- Java runtime (JRE 8+)

## Run

### Compile

```bash
kotlinc Main.kt -include-runtime -d budget.jar
```

### Execute

```bash
java -jar budget.jar
```

## How saving works

- The app creates a file called `transactions.txt`
- Every time you add, edit, or delete a transaction, the file is updated automatically
- The app also saves your budget limit in `budget.txt`
- When you restart the app, both transactions and the budget limit are loaded back in

## Example session

```text
💶 Kotlin Budget Tracker
Loaded 2 transaction(s).
Current budget limit: €300.00

--- Menu ---
1. Add expense
2. Add income
3. View all transactions
4. Summary by category
5. Balance
6. Biggest expense
7. Delete transaction
8. Set budget limit
9. Check budget status
10. Filter transactions
11. Edit transaction
12. Exit
Choose: 10
```

## What I practiced

- `data class` for structured data
- `enum class` for locked, type-safe category options
- `sealed interface` to model a closed set of transaction types (`Expense`, `Income`)
- `LocalDate` for automatic transaction dates
- Basic file handling with `File`, `readLines()`, `printWriter()`, and `writeText()`
- `when` expressions on sealed types
- `filterIsInstance<T>()` to filter a mixed list by type
- `contains(..., ignoreCase = true)` for simple search
- `mutableListOf` and list operations
- Lambda functions (`forEach`, `forEachIndexed`, `groupBy`, `maxBy`, `sumOf`)
- Input handling with `readln()` and number parsing
- Function decomposition
