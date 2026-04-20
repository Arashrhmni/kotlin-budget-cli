# 💶 Kotlin Budget Tracker

A command-line budget tracking tool written in Kotlin. Built as a learning project while getting started with Kotlin — now with simple file saving/loading and automatic dates for each transaction.

## Features

- Add expenses and income with a description, amount, category, and automatic date
- Categories enforced via `enum class` for safer input
- `sealed interface` models both `Expense` and `Income` as transaction types
- View all transactions with type labels and dates
- Summarize spending and income broken down by category
- See your current balance (total income minus total expenses)
- Find your biggest single expense
- Automatically save transactions to `transactions.txt`
- Automatically load saved transactions when the program starts

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
- Every time you add a transaction, the file is updated automatically
- When you restart the app, previous transactions are loaded back in

## Example session

```text
💶 Kotlin Budget Tracker
Loaded 0 transaction(s).

--- Menu ---
1. Add expense
2. Add income
3. View all transactions
4. Summary by category
5. Balance
6. Biggest expense
7. Exit
Choose: 1
Description: Groceries
Amount (€): 42.50
Pick a category:
  1. FOOD
  2. TRANSPORT
  3. RENT
  4. ENTERTAINMENT
  5. OTHER
Choose: 1
✅ Expense added: Groceries — €42.50 [FOOD] on 2026-04-20
```

## What I practiced

- `data class` for structured data
- `enum class` for locked, type-safe category options
- `sealed interface` to model a closed set of transaction types (`Expense`, `Income`)
- `LocalDate` for automatic transaction dates
- Basic file handling with `File`, `readLines()`, and `printWriter()`
- `when` expressions on sealed types
- `filterIsInstance<T>()` to filter a mixed list by type
- `mutableListOf` and list operations
- Lambda functions (`forEach`, `forEachIndexed`, `groupBy`, `maxBy`, `sumOf`)
- Input handling with `readln()` and `toDoubleOrNull()`
- Function decomposition
