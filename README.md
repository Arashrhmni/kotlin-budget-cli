# 💶 Kotlin Budget Tracker

A command-line budget tracking tool written in Kotlin. Built as a learning project while getting started with Kotlin — now with simple file saving/loading, optional custom dates, payment methods, deletion, editing, filtering, sorting, selected monthly summaries, average expense calculation, CSV export, a total budget limit, category budgets, and stronger input validation.

## Features

- Add expenses and income with a description, amount, category, date, and payment method
- Press Enter to use today's date, or enter a custom date in `YYYY-MM-DD` format
- Pick a payment method from `CASH`, `CARD`, `PAYPAL`, `BANK_TRANSFER`, or `OTHER`
- Edit a transaction by number, including the date and payment method
- Delete transactions by number, with confirmation before the transaction is removed
- Filter transactions by type, category, or description text
- Sort transactions by date or amount
- Show a monthly summary for a selected year and month
- Show the average expense amount
- Export all transactions to a CSV file that can be opened in Excel
- Set a total monthly budget limit and check how much you have left
- Set category budgets for expense categories like `FOOD` or `TRANSPORT`
- Show category budget status and warnings when you go over a category budget
- Better input validation for menu choices, amounts, dates, categories, and payment methods
- Categories enforced via `enum class` for safer input
- Payment methods enforced via `enum class` for safer input
- `sealed interface` models both `Expense` and `Income` as transaction types
- View all transactions with type labels, categories, payment methods, and dates
- Summarize spending and income broken down by category
- See your current balance (total income minus total expenses)
- Find your biggest single expense
- Automatically save transactions to `transactions.txt`
- Automatically load saved transactions when the program starts
- Save the total budget limit to `budget.txt`
- Save category budgets to `category_budgets.txt`
- Export transactions to `transactions_export.csv`

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
- Older saved transactions without a payment method are still loaded, and their payment method is set to `OTHER`

## How CSV export works

- The app can export all saved transactions to `transactions_export.csv`
- The CSV file contains these columns: `Type`, `Description`, `Amount`, `Category`, `Date`, and `PaymentMethod`
- The exported file can be opened with spreadsheet tools like Excel, Google Sheets, or LibreOffice Calc

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
17. Export transactions to CSV
18. Exit
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
Pick a payment method:
  1. CASH
  2. CARD
  3. PAYPAL
  4. BANK_TRANSFER
  5. OTHER
Choose: 2
✅ Expense added: Groceries — €24.50 [FOOD] [CARD] on 2026-05-18
```

## Example delete confirmation

```text
Choose: 8
Enter transaction number to delete: 2
Selected Expense: Coffee — €3.50 [FOOD] [CARD] on 2026-05-18
Are you sure you want to delete this transaction? (y/n): n
Delete cancelled.
```

## Example monthly summary

```text
Choose: 13
Enter year, for example 2026: 2026
Enter month (1-12): 5

Monthly summary for MAY 2026:
  Transactions:   4
  Income:         €1200.00
  Expenses:       €340.00
  Balance:        €860.00
```

## Example average expense

```text
Choose: 7

Average expense:
  Number of expenses: 3
  Total expenses:     €90.00
  Average expense:    €30.00
```

## Example CSV export

```text
Choose: 17
✅ Transactions exported to transactions_export.csv
```

Example CSV content:

```csv
Type,Description,Amount,Category,Date,PaymentMethod
"EXPENSE","Groceries","24.50","FOOD","2026-05-18","CARD"
"INCOME","Salary","1200.00","SALARY","2026-05-01","BANK_TRANSFER"
```

## What I practiced

- `data class` for structured data
- `enum class` for locked, type-safe category options
- `enum class` for locked, type-safe payment method options
- `sealed interface` to model a closed set of transaction types (`Expense`, `Income`)
- `LocalDate` for transaction dates
- `LocalDate.parse()` for custom date input
- `Month.of()` for selected monthly summaries
- Basic file handling with `File`, `readLines()`, `printWriter()`, and `writeText()`
- CSV export with simple row creation and value escaping
- Backward-compatible file loading for older transaction lines
- `when` expressions on sealed types
- `filterIsInstance<T>()` to filter a mixed list by type
- `contains(..., ignoreCase = true)` for simple search
- Sorting with `sortedBy()` and `sortedByDescending()`
- Date-based filtering for monthly summaries
- Simple average calculation
- `mutableListOf` and list operations
- Lambda functions (`forEach`, `forEachIndexed`, `groupBy`, `maxBy`, `sumOf`)
- Input handling with `readln()` and number parsing
- Simple yes/no confirmation handling before deleting data
- Reusable helper functions for validation
- Function decomposition
