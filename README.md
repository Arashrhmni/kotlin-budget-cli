# 💶 Kotlin Budget Tracker

A command-line budget tracking tool written in Kotlin. Built as a learning project while getting started with Kotlin — now with simple file saving/loading, optional custom dates, deletion, editing, filtering, sorting, selected monthly summaries, average expense calculation, a total budget limit, category budgets, and stronger input validation.

## Features

- Add expenses and income with a description, amount, category, and date
- Press Enter to use today's date, or enter a custom date in `YYYY-MM-DD` format
- Edit a transaction by number, including the date
- Delete transactions by number
- Filter transactions by type, category, or description text
- Sort transactions by date or amount
- Show a monthly summary for any selected year and month
- Show the average expense amount
- Set a total monthly budget limit and check how much you have left
- Set category budgets for expense categories like `FOOD` or `TRANSPORT`
- Show category budget status and warnings when you go over a category budget
- Better input validation for menu choices, amounts, and categories
- Categories enforced via `enum class` for safer input
- `sealed interface` models both `Expense` and `Income` as transaction types
- View all transactions with type labels and dates
- Summarize spending and income broken down by category
- See your current balance (total income minus total expenses)
- Find your biggest single expense
- Calculate your average expense
- Automatically save transactions to `transactions.txt`
- Automatically load saved transactions when the program starts
- Save the total budget limit to `budget.txt`
- Save category budgets to `category_budgets.txt`

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

- The app creates a file called `transactions.txt` for all transactions
- The app saves the total budget limit in `budget.txt`
- The app saves category budgets in `category_budgets.txt`
- Every time you add, edit, delete, or update a budget, the files are updated automatically
- When you restart the app, all saved data is loaded back in

## Example session

```text
💶 Kotlin Budget Tracker
Loaded 3 transaction(s).
Current budget limit: €500.00
Loaded 2 category budget(s).

--- Menu ---
1. Add expense
2. Add income
3. View all transactions
4. Summary by category
5. Balance
6. Biggest expense
7. Average expense
8. Delete transaction
9. Set budget limit
10. Check budget status
11. Filter transactions
12. Edit transaction
13. Monthly summary
14. Sort transactions
15. Set category budget
16. Check category budgets
17. Exit
Choose: 1
Description: Groceries
Amount (€): 24.50
Pick a category:
  1. FOOD
  2. TRANSPORT
  3. RENT
  4. ENTERTAINMENT
  5. OTHER
Choose: 1
Date (YYYY-MM-DD) or press Enter for today: 2026-05-18
✅ Expense added: Groceries — €24.50 [FOOD] on 2026-05-18

--- Menu ---
13. Monthly summary
Choose: 13

Monthly Summary
Enter year, for example 2026: 2026
Enter month (1-12): 5

Monthly summary for MAY 2026:
  Transactions:   1
  Income:         €0.00
  Expenses:       €24.50
  Balance:        €-24.50

--- Menu ---
7. Average expense
Choose: 7

Average expense:
  Number of expenses: 1
  Total expenses:     €24.50
  Average expense:   €24.50
```

## What I practiced

- `data class` for structured data
- `enum class` for locked, type-safe category options
- `sealed interface` to model a closed set of transaction types (`Expense`, `Income`)
- `LocalDate` for transaction dates
- `LocalDate.parse()` for custom date input
- Basic file handling with `File`, `readLines()`, `printWriter()`, and `writeText()`
- `when` expressions on sealed types
- `filterIsInstance<T>()` to filter a mixed list by type
- `contains(..., ignoreCase = true)` for simple search
- Sorting with `sortedBy()` and `sortedByDescending()`
- Date-based filtering for selected monthly summaries
- Average calculation using `sumOf()` and list `size`
- `mutableListOf` and list operations
- Lambda functions (`forEach`, `forEachIndexed`, `groupBy`, `maxBy`, `sumOf`)
- Input handling with `readln()` and number parsing
- Reusable helper functions for validation
- Function decomposition
