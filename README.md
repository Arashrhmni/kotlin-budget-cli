# 💶 Kotlin Budget Tracker

A command-line budget tracking tool written in Kotlin. Built as a learning project while getting started with Kotlin. The app can save and load transactions, use custom dates, track payment methods, filter and sort data, show summaries, manage budgets, export to CSV, and safely delete data.

The menu is now grouped into smaller sections, so the app is easier to use than one long menu with many options.

## Features

- Add expenses and income with a description, amount, category, date, and payment method
- Press Enter to use today's date, or enter a custom date in `YYYY-MM-DD` format
- Pick a payment method from `CASH`, `CARD`, `PAYPAL`, `BANK_TRANSFER`, or `OTHER`
- View all transactions with type labels, categories, payment methods, and dates
- Edit a transaction by number, including the date and payment method
- Delete one transaction with confirmation before it is removed
- Clear all transactions with a strong `DELETE` confirmation
- Filter transactions by type, category, description text, or payment method
- Sort transactions by date or amount
- Summarize spending and income by category
- Summarize spending and income by payment method
- Show the current balance
- Find the biggest single expense
- Find the smallest single expense
- Show the average expense amount
- Show how many expense, income, and total transactions are saved
- Show a monthly summary for a selected year and month
- Set, check, and remove a total monthly budget limit
- Set, check, and remove category budgets
- Export all transactions to a CSV file that can be opened in Excel
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

## Main menu

```text
💶 Kotlin Budget Tracker
Loaded 3 transaction(s).
Current budget limit: €500.00
Loaded 2 category budget(s).

--- Main Menu ---
1. Add transaction
2. View and search transactions
3. Reports and summaries
4. Manage budgets
5. Export data
6. Delete or clear data
7. Exit
Choose:
```

## Add transaction menu

```text
--- Add Transaction ---
1. Add expense
2. Add income
3. Back to main menu
```

Example:

```text
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

## View and search transactions menu

```text
--- View and Search Transactions ---
1. View all transactions
2. Filter transactions
3. Sort transactions
4. Back to main menu
```

Example filter by payment method:

```text
--- View and Search Transactions ---
Choose: 2

Filter Transactions
1. View only expenses
2. View only income
3. View by category
4. Search by description
5. View by payment method
Choose: 5
Pick a payment method to filter by:
  1. CASH
  2. CARD
  3. PAYPAL
  4. BANK_TRANSFER
  5. OTHER
Choose: 2

Transactions paid with CARD:
  1. [EXPENSE] [FOOD] [CARD] Groceries: €24.50 (2026-05-18)
```

## Reports and summaries menu

```text
--- Reports and Summaries ---
1. Summary by category
2. Summary by payment method
3. Balance
4. Biggest expense
5. Smallest expense
6. Average expense
7. Transaction count summary
8. Monthly summary
9. Back to main menu
```

Example transaction count summary:

```text
Transaction count summary:
  Expenses: 3
  Income:   1
  Total:    4
```

Example monthly summary:

```text
Enter year, for example 2026: 2026
Enter month (1-12): 5

Monthly summary for MAY 2026:
  Transactions:   4
  Income:         €1200.00
  Expenses:       €340.00
  Balance:        €860.00
```

## Manage budgets menu

```text
--- Manage Budgets ---
1. Set budget limit
2. Remove budget limit
3. Check budget status
4. Set category budget
5. Remove category budget
6. Check category budgets
7. Back to main menu
```

Example remove budget limit:

```text
Current budget limit: €500.00
Are you sure you want to remove the budget limit? (y/n): y
✅ Budget limit removed.
```

Example remove category budget:

```text
Choose a category budget to remove:
  1. FOOD — €200.00
  2. TRANSPORT — €80.00
Choose: 1
Selected category budget: FOOD — €200.00
Are you sure you want to remove this category budget? (y/n): y
✅ Category budget removed: FOOD
```

## Export data

Choosing `5. Export data` from the main menu creates this file:

```text
transactions_export.csv
```

The CSV file contains these columns:

```csv
Type,Description,Amount,Category,Date,PaymentMethod
"EXPENSE","Groceries","24.50","FOOD","2026-05-18","CARD"
"INCOME","Salary","1200.00","SALARY","2026-05-01","BANK_TRANSFER"
```

## Delete or clear data menu

```text
--- Delete or Clear Data ---
1. Delete one transaction
2. Clear all transactions
3. Back to main menu
```

Example delete confirmation:

```text
Enter transaction number to delete: 2
Selected Expense: Coffee — €3.50 [FOOD] [CARD] on 2026-05-18
Are you sure you want to delete this transaction? (y/n): n
Delete cancelled.
```

Example clear all transactions:

```text
Clear all transactions
This will delete all 4 saved transaction(s).
Your budget limit and category budgets will stay saved.
Type DELETE to confirm: DELETE
✅ All transactions were cleared.
```

## How saving works

- The app creates a file called `transactions.txt` for all transactions
- The app saves the total budget limit in `budget.txt`
- If the budget limit is removed, `budget.txt` is deleted
- The app saves category budgets in `category_budgets.txt`
- If a category budget is removed, `category_budgets.txt` is updated
- Every time you add, edit, delete, or update a budget, the files are updated automatically
- When you restart the app, all saved data is loaded back in
- Clearing all transactions only empties `transactions.txt`; the budget files stay saved
- Older saved transactions without a payment method are still loaded, and their payment method is set to `OTHER`

## What I practiced

- `data class` for structured data
- `enum class` for locked, type-safe category and payment method options
- `sealed interface` to model a closed set of transaction types (`Expense`, `Income`)
- `LocalDate` and `LocalDate.parse()` for transaction dates
- `Month.of()` for selected monthly summaries
- Basic file handling with `File`, `readLines()`, `printWriter()`, `writeText()`, and `delete()`
- CSV export with simple row creation and value escaping
- Clearing a mutable list with `clear()`
- Removing an item from a mutable map with `remove()`
- Filtering a list by enum values such as category and payment method
- Grouping transactions with `groupBy`
- Backward-compatible file loading for older transaction lines
- `when` expressions on sealed types
- `filterIsInstance<T>()` to filter a mixed list by type
- `contains(..., ignoreCase = true)` for simple search
- Sorting with `sortedBy()` and `sortedByDescending()`
- Finding biggest and smallest values with `maxBy` and `minBy`
- Simple average calculation
- Counting list items with `.size`
- Input handling with `readln()` and number parsing
- Simple yes/no confirmation handling before deleting data or removing settings
- Reusable helper functions for validation
- Function decomposition
- Nested menus for a cleaner command-line app structure
