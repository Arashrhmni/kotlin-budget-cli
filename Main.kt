import java.io.File
import java.time.LocalDate
import java.time.Month
import java.util.Locale

const val DATA_FILE_NAME = "transactions.txt"
const val BUDGET_FILE_NAME = "budget.txt"
const val CATEGORY_BUDGETS_FILE_NAME = "category_budgets.txt"
const val CSV_EXPORT_FILE_NAME = "transactions_export.csv"

// Enum class: locked set of valid categories
// no more typos like "fod" or "Food"
enum class Category {
    FOOD, TRANSPORT, RENT, ENTERTAINMENT, SALARY, FREELANCE, OTHER
}

// Enum class: locked set of valid payment methods
enum class PaymentMethod {
    CASH, CARD, PAYPAL, BANK_TRANSFER, OTHER
}

// Sealed interface: defines the contract for all transaction types
sealed interface Transaction {
    val description: String
    val amount: Double
    val category: Category
    val date: LocalDate
    val paymentMethod: PaymentMethod
}

// Expense and Income both implement Transaction
data class Expense(
    override val description: String,
    override val amount: Double,
    override val category: Category,
    override val date: LocalDate,
    override val paymentMethod: PaymentMethod
) : Transaction

data class Income(
    override val description: String,
    override val amount: Double,
    override val category: Category,
    override val date: LocalDate,
    override val paymentMethod: PaymentMethod
) : Transaction

// Starts the app, loads saved data, and keeps showing the main menu until the user exits.
fun main() {
    val transactions = loadTransactions().toMutableList()
    var budgetLimit = loadBudgetLimit()
    val categoryBudgets = loadCategoryBudgets().toMutableMap()

    println("💶 Kotlin Budget Tracker")
    println("Loaded ${transactions.size} transaction(s).")
    if (budgetLimit != null) {
        println("Current budget limit: €${"%.2f".format(budgetLimit)}")
    } else {
        println("No budget limit set yet.")
    }

    if (categoryBudgets.isNotEmpty()) {
        println("Loaded ${categoryBudgets.size} category budget(s).")
    } else {
        println("No category budgets set yet.")
    }

    while (true) {
        printMainMenu()

        when (promptChoice("Choose: ", 1..7)) {
            1 -> showAddTransactionMenu(transactions, budgetLimit, categoryBudgets)
            2 -> showViewTransactionsMenu(transactions)
            3 -> showReportsMenu(transactions)
            4 -> budgetLimit = showBudgetMenu(transactions, budgetLimit, categoryBudgets)
            5 -> exportTransactionsToCsv(transactions)
            6 -> showDeleteMenu(transactions, budgetLimit, categoryBudgets)
            7 -> {
                saveTransactions(transactions)
                if (budgetLimit != null) {
                    saveBudgetLimit(budgetLimit)
                } else {
                    deleteBudgetLimit()
                }
                saveCategoryBudgets(categoryBudgets)
                println("Bye! Your data was saved.")
                break
            }
        }
    }
}

// Prints the first menu that sends the user to smaller submenus.
fun printMainMenu() {
    println("\n--- Main Menu ---")
    println("1. Add transaction")
    println("2. View and search transactions")
    println("3. Reports and summaries")
    println("4. Manage budgets")
    println("5. Export data")
    println("6. Delete or clear data")
    println("7. Exit")
}

// Shows the submenu for adding either an expense or an income.
fun showAddTransactionMenu(
    transactions: MutableList<Transaction>,
    budgetLimit: Double?,
    categoryBudgets: Map<Category, Double>
) {
    while (true) {
        println("\n--- Add Transaction ---")
        println("1. Add expense")
        println("2. Add income")
        println("3. Back to main menu")

        when (promptChoice("Choose: ", 1..3)) {
            1 -> addTransaction(transactions, isExpense = true, budgetLimit = budgetLimit, categoryBudgets = categoryBudgets)
            2 -> addTransaction(transactions, isExpense = false, budgetLimit = budgetLimit, categoryBudgets = categoryBudgets)
            3 -> return
        }
    }
}

// Shows the submenu for viewing, filtering, and sorting transactions.
fun showViewTransactionsMenu(transactions: List<Transaction>) {
    while (true) {
        println("\n--- View and Search Transactions ---")
        println("1. View all transactions")
        println("2. Filter transactions")
        println("3. Sort transactions")
        println("4. Back to main menu")

        when (promptChoice("Choose: ", 1..4)) {
            1 -> viewAll(transactions)
            2 -> filterTransactions(transactions)
            3 -> sortTransactions(transactions)
            4 -> return
        }
    }
}

// Shows the submenu for reports like balance, summaries, and biggest/smallest expenses.
fun showReportsMenu(transactions: List<Transaction>) {
    while (true) {
        println("\n--- Reports and Summaries ---")
        println("1. Summary by category")
        println("2. Summary by payment method")
        println("3. Balance")
        println("4. Biggest expense")
        println("5. Smallest expense")
        println("6. Average expense")
        println("7. Transaction count summary")
        println("8. Monthly summary")
        println("9. Back to main menu")

        when (promptChoice("Choose: ", 1..9)) {
            1 -> summarizeByCategory(transactions)
            2 -> summarizeByPaymentMethod(transactions)
            3 -> showBalance(transactions)
            4 -> biggestExpense(transactions)
            5 -> smallestExpense(transactions)
            6 -> showAverageExpense(transactions)
            7 -> showTransactionCountSummary(transactions)
            8 -> showMonthlySummary(transactions)
            9 -> return
        }
    }
}

// Shows the submenu for setting, removing, and checking budget limits.
fun showBudgetMenu(
    transactions: List<Transaction>,
    budgetLimit: Double?,
    categoryBudgets: MutableMap<Category, Double>
): Double? {
    var updatedBudgetLimit = budgetLimit

    while (true) {
        println("\n--- Manage Budgets ---")
        println("1. Set budget limit")
        println("2. Remove budget limit")
        println("3. Check budget status")
        println("4. Set category budget")
        println("5. Remove category budget")
        println("6. Check category budgets")
        println("7. Back to main menu")

        when (promptChoice("Choose: ", 1..7)) {
            1 -> updatedBudgetLimit = setBudgetLimit()
            2 -> updatedBudgetLimit = removeBudgetLimit(updatedBudgetLimit)
            3 -> checkBudgetStatus(transactions, updatedBudgetLimit)
            4 -> setCategoryBudget(categoryBudgets)
            5 -> removeCategoryBudget(categoryBudgets)
            6 -> checkCategoryBudgetStatus(transactions, categoryBudgets)
            7 -> return updatedBudgetLimit
        }
    }
}

// Shows the submenu for deleting one transaction or clearing all transactions.
fun showDeleteMenu(
    transactions: MutableList<Transaction>,
    budgetLimit: Double?,
    categoryBudgets: Map<Category, Double>
) {
    while (true) {
        println("\n--- Delete or Clear Data ---")
        println("1. Delete one transaction")
        println("2. Clear all transactions")
        println("3. Back to main menu")

        when (promptChoice("Choose: ", 1..3)) {
            1 -> deleteTransaction(transactions, budgetLimit, categoryBudgets)
            2 -> clearAllTransactions(transactions)
            3 -> return
        }
    }
}

// Returns different category choices for expenses and income.
fun getCategoryOptions(isExpense: Boolean): List<Category> {
    return if (isExpense) {
        listOf(Category.FOOD, Category.TRANSPORT, Category.RENT, Category.ENTERTAINMENT, Category.OTHER)
    } else {
        listOf(Category.SALARY, Category.FREELANCE, Category.OTHER)
    }
}

// Reads a menu number and keeps asking until the number is valid.
fun promptChoice(prompt: String, validRange: IntRange): Int {
    while (true) {
        print(prompt)
        val choice = readln().trim().toIntOrNull()
        if (choice != null && choice in validRange) {
            return choice
        }
        println("Invalid choice. Please enter a number from ${validRange.first} to ${validRange.last}.")
    }
}

// Reads a yes/no answer and returns true for yes, false for no.
fun promptYesNo(prompt: String): Boolean {
    while (true) {
        print(prompt)
        when (readln().trim().lowercase()) {
            "y", "yes" -> return true
            "n", "no" -> return false
            else -> println("Invalid answer. Please enter y or n.")
        }
    }
}

// Reads text input and does not allow an empty answer.
fun promptNonEmptyText(prompt: String): String {
    while (true) {
        print(prompt)
        val input = readln().trim()
        if (input.isNotEmpty()) return input
        println("This field cannot be empty.")
    }
}

// Reads a positive number, used for amounts and budget limits.
fun promptPositiveDouble(prompt: String): Double {
    while (true) {
        print(prompt)
        val value = readln().trim().toDoubleOrNull()
        if (value != null && value > 0) return value
        println("Invalid amount. Please enter a positive number.")
    }
}

// Reads a new positive number, or keeps the old value if the user presses Enter.
fun promptOptionalPositiveDouble(prompt: String, currentValue: Double): Double {
    while (true) {
        print(prompt)
        val input = readln().trim()
        if (input.isEmpty()) return currentValue

        val value = input.toDoubleOrNull()
        if (value != null && value > 0) return value
        println("Invalid amount. Please enter a positive number or press Enter to keep the current value.")
    }
}

// Reads a date in YYYY-MM-DD format, or uses the default date when Enter is pressed.
fun promptDate(prompt: String, defaultDate: LocalDate): LocalDate {
    while (true) {
        print(prompt)
        val input = readln().trim()
        if (input.isEmpty()) return defaultDate

        try {
            return LocalDate.parse(input)
        } catch (_: Exception) {
            println("Invalid date. Please use the format YYYY-MM-DD, for example 2026-05-18.")
        }
    }
}

// Reads a year for monthly summaries and keeps it inside a simple safe range.
fun promptYear(prompt: String): Int {
    while (true) {
        print(prompt)
        val year = readln().trim().toIntOrNull()
        if (year != null && year in 1900..2100) {
            return year
        }
        println("Invalid year. Please enter a year between 1900 and 2100.")
    }
}

// Lets the user choose a category from the allowed enum values.
fun pickCategory(isExpense: Boolean): Category {
    val options = getCategoryOptions(isExpense)

    println("Pick a category:")
    options.forEachIndexed { index, category ->
        println("  ${index + 1}. ${category.name}")
    }

    val choice = promptChoice("Choose: ", 1..options.size)
    return options[choice - 1]
}

// Lets the user choose a payment method from the allowed enum values.
fun pickPaymentMethod(): PaymentMethod {
    val options = PaymentMethod.values().toList()

    println("Pick a payment method:")
    options.forEachIndexed { index, paymentMethod ->
        println("  ${index + 1}. ${paymentMethod.name}")
    }

    val choice = promptChoice("Choose: ", 1..options.size)
    return options[choice - 1]
}

// Lets the user change the category while editing, or press Enter to keep the old one.
fun editCategory(currentCategory: Category, isExpense: Boolean): Category {
    val options = getCategoryOptions(isExpense)

    println("Current category: ${currentCategory.name}")
    println("Choose a new category or press Enter to keep it:")
    options.forEachIndexed { index, category ->
        println("  ${index + 1}. ${category.name}")
    }

    while (true) {
        print("Choose: ")
        val input = readln().trim()
        if (input.isEmpty()) return currentCategory

        val choice = input.toIntOrNull()
        if (choice != null && choice in 1..options.size) {
            return options[choice - 1]
        }

        println("Invalid choice. Enter a number from 1 to ${options.size}, or press Enter to keep the current category.")
    }
}

// Lets the user change the payment method while editing, or press Enter to keep the old one.
fun editPaymentMethod(currentPaymentMethod: PaymentMethod): PaymentMethod {
    val options = PaymentMethod.values().toList()

    println("Current payment method: ${currentPaymentMethod.name}")
    println("Choose a new payment method or press Enter to keep it:")
    options.forEachIndexed { index, paymentMethod ->
        println("  ${index + 1}. ${paymentMethod.name}")
    }

    while (true) {
        print("Choose: ")
        val input = readln().trim()
        if (input.isEmpty()) return currentPaymentMethod

        val choice = input.toIntOrNull()
        if (choice != null && choice in 1..options.size) {
            return options[choice - 1]
        }

        println("Invalid choice. Enter a number from 1 to ${options.size}, or press Enter to keep the current payment method.")
    }
}

// Creates a new expense or income, adds it to the list, and saves it to the file.
fun addTransaction(
    transactions: MutableList<Transaction>,
    isExpense: Boolean,
    budgetLimit: Double?,
    categoryBudgets: Map<Category, Double>
) {
    val type = if (isExpense) "Expense" else "Income"
    val description = promptNonEmptyText("Description: ")
    val amount = promptPositiveDouble("Amount (€): ")
    val category = pickCategory(isExpense)
    val date = promptDate("Date (YYYY-MM-DD) or press Enter for today: ", LocalDate.now())
    val paymentMethod = pickPaymentMethod()

    if (isExpense) {
        transactions.add(Expense(description, amount, category, date, paymentMethod))
    } else {
        transactions.add(Income(description, amount, category, date, paymentMethod))
    }

    saveTransactions(transactions)
    println(
        "✅ $type added: $description — €${"%.2f".format(amount)} " +
            "[${category.name}] [${paymentMethod.name}] on $date"
    )

    if (isExpense && budgetLimit != null) {
        checkBudgetStatus(transactions, budgetLimit)
    }

    if (isExpense) {
        showCategoryBudgetStatusForCategory(transactions, category, categoryBudgets)
    }
}

// Shows all saved transactions, or a message if the list is empty.
fun viewAll(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    println("\nAll Transactions:")
    printTransactionList(transactions)
}

// Prints a numbered transaction list in one shared format.
fun printTransactionList(transactions: List<Transaction>) {
    transactions.forEachIndexed { index, transaction ->
        val label = when (transaction) {
            is Expense -> "EXPENSE"
            is Income -> "INCOME "
        }
        println(
            "  ${index + 1}. [$label] [${transaction.category.name}] [${transaction.paymentMethod.name}] " +
                "${transaction.description}: €${"%.2f".format(transaction.amount)} (${transaction.date})"
        )
    }
}

// Groups expenses and income by category and prints the totals.
fun summarizeByCategory(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    println("\nExpenses by Category:")
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("  No expenses recorded yet.")
    } else {
        expenses
            .groupBy { it.category }
            .forEach { (category, items) ->
                val total = items.sumOf { it.amount }
                println("  ${category.name}: €${"%.2f".format(total)} (${items.size} item(s))")
            }
    }

    println("\nIncome by Category:")
    val incomes = transactions.filterIsInstance<Income>()
    if (incomes.isEmpty()) {
        println("  No income recorded yet.")
    } else {
        incomes
            .groupBy { it.category }
            .forEach { (category, items) ->
                val total = items.sumOf { it.amount }
                println("  ${category.name}: €${"%.2f".format(total)} (${items.size} item(s))")
            }
    }
}

// Groups expenses and income by payment method and prints the totals.
fun summarizeByPaymentMethod(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    println("\nExpenses by Payment Method:")
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("  No expenses recorded yet.")
    } else {
        expenses
            .groupBy { it.paymentMethod }
            .entries
            .sortedBy { it.key.name }
            .forEach { (paymentMethod, items) ->
                val total = items.sumOf { it.amount }
                println("  ${paymentMethod.name}: €${"%.2f".format(total)} (${items.size} item(s))")
            }
    }

    println("\nIncome by Payment Method:")
    val incomes = transactions.filterIsInstance<Income>()
    if (incomes.isEmpty()) {
        println("  No income recorded yet.")
    } else {
        incomes
            .groupBy { it.paymentMethod }
            .entries
            .sortedBy { it.key.name }
            .forEach { (paymentMethod, items) ->
                val total = items.sumOf { it.amount }
                println("  ${paymentMethod.name}: €${"%.2f".format(total)} (${items.size} item(s))")
            }
    }
}

// Calculates total income minus total expenses.
fun showBalance(transactions: List<Transaction>) {
    val totalIncome = transactions.filterIsInstance<Income>().sumOf { it.amount }
    val totalExpenses = transactions.filterIsInstance<Expense>().sumOf { it.amount }
    val balance = totalIncome - totalExpenses

    println("\n─────────────────────────")
    println("  Total Income:   €${"%.2f".format(totalIncome)}")
    println("  Total Expenses: €${"%.2f".format(totalExpenses)}")
    println("  Balance:        €${"%.2f".format(balance)}")
    if (balance < 0) println("  ⚠️  You're spending more than you earn!")
    println("─────────────────────────")
}

// Finds and prints the expense with the highest amount.
fun biggestExpense(transactions: List<Transaction>) {
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }

    val biggest = expenses.maxBy { it.amount }
    println(
        "\nBiggest expense: ${biggest.description} — €${"%.2f".format(biggest.amount)} " +
            "[${biggest.category.name}] [${biggest.paymentMethod.name}] on ${biggest.date}"
    )
}

// Finds and prints the expense with the lowest amount.
fun smallestExpense(transactions: List<Transaction>) {
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }

    val smallest = expenses.minBy { it.amount }
    println(
        "\nSmallest expense: ${smallest.description} — €${"%.2f".format(smallest.amount)} " +
            "[${smallest.category.name}] [${smallest.paymentMethod.name}] on ${smallest.date}"
    )
}

// Calculates the average amount of all expenses.
fun showAverageExpense(transactions: List<Transaction>) {
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }

    val totalExpenses = expenses.sumOf { it.amount }
    val averageExpense = totalExpenses / expenses.size

    println("\nAverage expense:")
    println("  Number of expenses: ${expenses.size}")
    println("  Total expenses:     €${"%.2f".format(totalExpenses)}")
    println("  Average expense:    €${"%.2f".format(averageExpense)}")
}

// Counts how many expenses, income items, and total transactions exist.
fun showTransactionCountSummary(transactions: List<Transaction>) {
    val expenses = transactions.filterIsInstance<Expense>()
    val incomes = transactions.filterIsInstance<Income>()

    println("\nTransaction count summary:")
    println("  Expenses: ${expenses.size}")
    println("  Income:   ${incomes.size}")
    println("  Total:    ${transactions.size}")
}

// Deletes one selected transaction after the user confirms it.
fun deleteTransaction(
    transactions: MutableList<Transaction>,
    budgetLimit: Double?,
    categoryBudgets: Map<Category, Double>
) {
    if (transactions.isEmpty()) {
        println("No transactions to delete.")
        return
    }

    viewAll(transactions)
    val index = promptChoice("Enter transaction number to delete: ", 1..transactions.size)
    val selectedTransaction = transactions[index - 1]

    val type = when (selectedTransaction) {
        is Expense -> "Expense"
        is Income -> "Income"
    }

    println(
        "Selected $type: ${selectedTransaction.description} — €${"%.2f".format(selectedTransaction.amount)} " +
            "[${selectedTransaction.category.name}] [${selectedTransaction.paymentMethod.name}] on ${selectedTransaction.date}"
    )

    val shouldDelete = promptYesNo("Are you sure you want to delete this transaction? (y/n): ")
    if (!shouldDelete) {
        println("Delete cancelled.")
        return
    }

    val removed = transactions.removeAt(index - 1)
    saveTransactions(transactions)

    println("Removed $type: ${removed.description} — €${"%.2f".format(removed.amount)}")

    if (budgetLimit != null) {
        checkBudgetStatus(transactions, budgetLimit)
    }

    if (removed is Expense) {
        showCategoryBudgetStatusForCategory(transactions, removed.category, categoryBudgets)
    }
}

// Clears all transactions only after the user types DELETE.
fun clearAllTransactions(transactions: MutableList<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions to clear.")
        return
    }

    println("\nClear all transactions")
    println("This will delete all ${transactions.size} saved transaction(s).")
    println("Your budget limit and category budgets will stay saved.")

    print("Type DELETE to confirm: ")
    val confirmation = readln().trim()
    if (confirmation != "DELETE") {
        println("Clear all cancelled.")
        return
    }

    transactions.clear()
    saveTransactions(transactions)
    println("✅ All transactions were cleared.")
}

// Saves a new monthly budget limit.
fun setBudgetLimit(): Double {
    val limit = promptPositiveDouble("Enter monthly budget limit (€): ")
    saveBudgetLimit(limit)
    println("✅ Budget limit set to €${"%.2f".format(limit)}")
    return limit
}

// Removes the monthly budget limit and deletes its saved file.
fun removeBudgetLimit(currentBudgetLimit: Double?): Double? {
    if (currentBudgetLimit == null) {
        println("No budget limit is set yet.")
        return null
    }

    println("Current budget limit: €${"%.2f".format(currentBudgetLimit)}")
    val shouldRemove = promptYesNo("Are you sure you want to remove the budget limit? (y/n): ")
    if (!shouldRemove) {
        println("Remove budget limit cancelled.")
        return currentBudgetLimit
    }

    deleteBudgetLimit()
    println("✅ Budget limit removed.")
    return null
}

// Compares total expenses with the monthly budget limit.
fun checkBudgetStatus(transactions: List<Transaction>, budgetLimit: Double?) {
    if (budgetLimit == null) {
        println("No budget limit set yet.")
        return
    }

    val totalExpenses = transactions.filterIsInstance<Expense>().sumOf { it.amount }
    val remaining = budgetLimit - totalExpenses

    println("\nBudget status:")
    println("  Budget limit:   €${"%.2f".format(budgetLimit)}")
    println("  Total expenses: €${"%.2f".format(totalExpenses)}")

    if (remaining < 0) {
        println("  ⚠️ You are over budget by €${"%.2f".format(-remaining)}")
    } else {
        println("  Remaining:      €${"%.2f".format(remaining)}")
    }
}

// Shows filter choices and sends the user to the selected filter function.
fun filterTransactions(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    println("\nFilter Transactions")
    println("1. View only expenses")
    println("2. View only income")
    println("3. View by category")
    println("4. Search by description")
    println("5. View by payment method")

    when (promptChoice("Choose: ", 1..5)) {
        1 -> showFilteredList(transactions.filterIsInstance<Expense>(), "Expenses only")
        2 -> showFilteredList(transactions.filterIsInstance<Income>(), "Income only")
        3 -> filterByCategory(transactions)
        4 -> filterByDescription(transactions)
        5 -> filterByPaymentMethod(transactions)
    }
}

// Prints filtered results or a message if nothing matched.
fun showFilteredList(filtered: List<Transaction>, title: String) {
    if (filtered.isEmpty()) {
        println("No matching transactions found.")
        return
    }

    println("\n$title:")
    printTransactionList(filtered)
}

// Shows only transactions from one selected category.
fun filterByCategory(transactions: List<Transaction>) {
    println("Pick a category to filter by:")
    val allCategories = Category.values().toList()
    allCategories.forEachIndexed { index, category ->
        println("  ${index + 1}. ${category.name}")
    }

    val choice = promptChoice("Choose: ", 1..allCategories.size)
    val selectedCategory = allCategories[choice - 1]
    val filtered = transactions.filter { it.category == selectedCategory }
    showFilteredList(filtered, "Transactions in ${selectedCategory.name}")
}

// Searches transaction descriptions using simple case-insensitive text matching.
fun filterByDescription(transactions: List<Transaction>) {
    val query = promptNonEmptyText("Enter text to search for: ")
    val filtered = transactions.filter { it.description.contains(query, ignoreCase = true) }
    showFilteredList(filtered, "Search results for \"$query\"")
}

// Shows only transactions with one selected payment method.
fun filterByPaymentMethod(transactions: List<Transaction>) {
    println("Pick a payment method to filter by:")
    val allPaymentMethods = PaymentMethod.values().toList()
    allPaymentMethods.forEachIndexed { index, paymentMethod ->
        println("  ${index + 1}. ${paymentMethod.name}")
    }

    val choice = promptChoice("Choose: ", 1..allPaymentMethods.size)
    val selectedPaymentMethod = allPaymentMethods[choice - 1]
    val filtered = transactions.filter { it.paymentMethod == selectedPaymentMethod }
    showFilteredList(filtered, "Transactions paid with ${selectedPaymentMethod.name}")
}

// Updates one selected transaction and saves the changed list.
fun editTransaction(
    transactions: MutableList<Transaction>,
    budgetLimit: Double?,
    categoryBudgets: Map<Category, Double>
) {
    if (transactions.isEmpty()) {
        println("No transactions to edit.")
        return
    }

    viewAll(transactions)
    val index = promptChoice("Enter transaction number to edit: ", 1..transactions.size)
    val oldTransaction = transactions[index - 1]
    val isExpense = oldTransaction is Expense

    println("Editing transaction $index.")
    println("Press Enter to keep the current value.")

    print("New description [${oldTransaction.description}]: ")
    val descriptionInput = readln().trim()
    val newDescription = if (descriptionInput.isEmpty()) oldTransaction.description else descriptionInput

    val newAmount = promptOptionalPositiveDouble(
        "New amount [${"%.2f".format(oldTransaction.amount)}]: ",
        oldTransaction.amount
    )
    val newCategory = editCategory(oldTransaction.category, isExpense)
    val newDate = promptDate("New date [${oldTransaction.date}] or press Enter to keep it: ", oldTransaction.date)
    val newPaymentMethod = editPaymentMethod(oldTransaction.paymentMethod)

    val updatedTransaction = if (isExpense) {
        Expense(newDescription, newAmount, newCategory, newDate, newPaymentMethod)
    } else {
        Income(newDescription, newAmount, newCategory, newDate, newPaymentMethod)
    }

    transactions[index - 1] = updatedTransaction
    saveTransactions(transactions)

    println("✅ Transaction updated.")
    println(
        "Old: ${oldTransaction.description} — €${"%.2f".format(oldTransaction.amount)} " +
            "[${oldTransaction.category.name}] [${oldTransaction.paymentMethod.name}] (${oldTransaction.date})"
    )
    println(
        "New: ${updatedTransaction.description} — €${"%.2f".format(updatedTransaction.amount)} " +
            "[${updatedTransaction.category.name}] [${updatedTransaction.paymentMethod.name}] (${updatedTransaction.date})"
    )

    if (budgetLimit != null) {
        checkBudgetStatus(transactions, budgetLimit)
    }

    if (oldTransaction is Expense) {
        showCategoryBudgetStatusForCategory(transactions, oldTransaction.category, categoryBudgets)
    }
    if (updatedTransaction is Expense && updatedTransaction.category != oldTransaction.category) {
        showCategoryBudgetStatusForCategory(transactions, updatedTransaction.category, categoryBudgets)
    }
}

// Shows income, expenses, balance, and count for a selected year and month.
fun showMonthlySummary(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    val year = promptYear("Enter year, for example 2026: ")
    val monthNumber = promptChoice("Enter month (1-12): ", 1..12)
    val selectedMonth = Month.of(monthNumber)

    val monthTransactions = transactions.filter {
        it.date.year == year && it.date.month == selectedMonth
    }

    if (monthTransactions.isEmpty()) {
        println("No transactions found for ${selectedMonth.name} $year.")
        return
    }

    val monthlyIncome = monthTransactions.filterIsInstance<Income>().sumOf { it.amount }
    val monthlyExpenses = monthTransactions.filterIsInstance<Expense>().sumOf { it.amount }
    val monthlyBalance = monthlyIncome - monthlyExpenses

    println("\nMonthly summary for ${selectedMonth.name} $year:")
    println("  Transactions:   ${monthTransactions.size}")
    println("  Income:         €${"%.2f".format(monthlyIncome)}")
    println("  Expenses:       €${"%.2f".format(monthlyExpenses)}")
    println("  Balance:        €${"%.2f".format(monthlyBalance)}")
}

// Sorts transactions by date or amount and prints the sorted result.
fun sortTransactions(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    println("\nSort Transactions")
    println("1. Newest first")
    println("2. Oldest first")
    println("3. Highest amount first")
    println("4. Lowest amount first")

    val sorted = when (promptChoice("Choose: ", 1..4)) {
        1 -> transactions.sortedByDescending { it.date }
        2 -> transactions.sortedBy { it.date }
        3 -> transactions.sortedByDescending { it.amount }
        4 -> transactions.sortedBy { it.amount }
        else -> transactions
    }

    println("\nSorted Transactions:")
    printTransactionList(sorted)
}

// Adds or updates a budget limit for one expense category.
fun setCategoryBudget(categoryBudgets: MutableMap<Category, Double>) {
    val expenseCategories = getCategoryOptions(isExpense = true)

    println("Choose an expense category for the budget:")
    expenseCategories.forEachIndexed { index, category ->
        println("  ${index + 1}. ${category.name}")
    }

    val choice = promptChoice("Choose: ", 1..expenseCategories.size)
    val selectedCategory = expenseCategories[choice - 1]
    val limit = promptPositiveDouble("Enter budget for ${selectedCategory.name} (€): ")

    categoryBudgets[selectedCategory] = limit
    saveCategoryBudgets(categoryBudgets)

    println("✅ Category budget set: ${selectedCategory.name} = €${"%.2f".format(limit)}")
}

// Removes one saved category budget after confirmation.
fun removeCategoryBudget(categoryBudgets: MutableMap<Category, Double>) {
    if (categoryBudgets.isEmpty()) {
        println("No category budgets set yet.")
        return
    }

    val categoriesWithBudgets = categoryBudgets.keys.sortedBy { it.name }

    println("Choose a category budget to remove:")
    categoriesWithBudgets.forEachIndexed { index, category ->
        val limit = categoryBudgets[category] ?: 0.0
        println("  ${index + 1}. ${category.name} — €${"%.2f".format(limit)}")
    }

    val choice = promptChoice("Choose: ", 1..categoriesWithBudgets.size)
    val selectedCategory = categoriesWithBudgets[choice - 1]
    val selectedLimit = categoryBudgets[selectedCategory] ?: 0.0

    println("Selected category budget: ${selectedCategory.name} — €${"%.2f".format(selectedLimit)}")
    val shouldRemove = promptYesNo("Are you sure you want to remove this category budget? (y/n): ")
    if (!shouldRemove) {
        println("Remove category budget cancelled.")
        return
    }

    categoryBudgets.remove(selectedCategory)
    saveCategoryBudgets(categoryBudgets)
    println("✅ Category budget removed: ${selectedCategory.name}")
}

// Checks all saved category budgets and shows how much is left or over.
fun checkCategoryBudgetStatus(transactions: List<Transaction>, categoryBudgets: Map<Category, Double>) {
    if (categoryBudgets.isEmpty()) {
        println("No category budgets set yet.")
        return
    }

    println("\nCategory budget status:")
    categoryBudgets.toSortedMap(compareBy { it.name }).forEach { (category, limit) ->
        val totalExpenses = transactions
            .filterIsInstance<Expense>()
            .filter { it.category == category }
            .sumOf { it.amount }

        val remaining = limit - totalExpenses
        println("  ${category.name}")
        println("    Limit:   €${"%.2f".format(limit)}")
        println("    Spent:   €${"%.2f".format(totalExpenses)}")
        if (remaining < 0) {
            println("    ⚠️ Over by €${"%.2f".format(-remaining)}")
        } else {
            println("    Left:    €${"%.2f".format(remaining)}")
        }
    }
}

// Shows the budget status for one category after adding, editing, or deleting an expense.
fun showCategoryBudgetStatusForCategory(
    transactions: List<Transaction>,
    category: Category,
    categoryBudgets: Map<Category, Double>
) {
    val limit = categoryBudgets[category] ?: return
    val totalExpenses = transactions
        .filterIsInstance<Expense>()
        .filter { it.category == category }
        .sumOf { it.amount }

    val remaining = limit - totalExpenses
    println("\n${category.name} budget status:")
    println("  Limit: €${"%.2f".format(limit)}")
    println("  Spent: €${"%.2f".format(totalExpenses)}")
    if (remaining < 0) {
        println("  ⚠️ You are over the ${category.name} budget by €${"%.2f".format(-remaining)}")
    } else {
        println("  Left:  €${"%.2f".format(remaining)}")
    }
}


// Exports all transactions to a CSV file that can be opened in spreadsheet apps.
fun exportTransactionsToCsv(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions to export.")
        return
    }

    val file = File(CSV_EXPORT_FILE_NAME)
    file.printWriter().use { out ->
        out.println("Type,Description,Amount,Category,Date,PaymentMethod")

        transactions.forEach { transaction ->
            val type = when (transaction) {
                is Expense -> "EXPENSE"
                is Income -> "INCOME"
            }

            val row = listOf(
                type,
                transaction.description,
                "%.2f".format(Locale.US, transaction.amount),
                transaction.category.name,
                transaction.date.toString(),
                transaction.paymentMethod.name
            )

            out.println(row.joinToString(",") { csvValue(it) })
        }
    }

    println("✅ Transactions exported to $CSV_EXPORT_FILE_NAME")
}

// Escapes one CSV value by wrapping it in quotes and doubling inner quotes.
fun csvValue(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

// Loads saved transactions from transactions.txt when the app starts.
fun loadTransactions(): List<Transaction> {
    val file = File(DATA_FILE_NAME)
    if (!file.exists()) return emptyList()

    return file.readLines().mapNotNull { parseTransaction(it) }
}

// Saves all transactions to transactions.txt.
fun saveTransactions(transactions: List<Transaction>) {
    val file = File(DATA_FILE_NAME)
    file.printWriter().use { out ->
        transactions.forEach { transaction ->
            out.println(transactionToLine(transaction))
        }
    }
}

// Converts one transaction into one text line for saving.
fun transactionToLine(transaction: Transaction): String {
    val type = when (transaction) {
        is Expense -> "EXPENSE"
        is Income -> "INCOME"
    }

    val safeDescription = transaction.description.replace("|", "/")
    return listOf(
        type,
        safeDescription,
        transaction.amount.toString(),
        transaction.category.name,
        transaction.date.toString(),
        transaction.paymentMethod.name
    ).joinToString("|")
}

// Converts one saved text line back into an Expense or Income object.
fun parseTransaction(line: String): Transaction? {
    val parts = line.split("|", limit = 6)
    if (parts.size != 5 && parts.size != 6) return null

    val type = parts[0]
    val description = parts[1]
    val amount = parts[2].toDoubleOrNull() ?: return null
    val category = try {
        Category.valueOf(parts[3])
    } catch (_: IllegalArgumentException) {
        return null
    }
    val date = try {
        LocalDate.parse(parts[4])
    } catch (_: Exception) {
        return null
    }
    val paymentMethod = if (parts.size == 6) {
        try {
            PaymentMethod.valueOf(parts[5])
        } catch (_: IllegalArgumentException) {
            PaymentMethod.OTHER
        }
    } else {
        PaymentMethod.OTHER
    }

    return when (type) {
        "EXPENSE" -> Expense(description, amount, category, date, paymentMethod)
        "INCOME" -> Income(description, amount, category, date, paymentMethod)
        else -> null
    }
}

// Loads the saved budget limit, or returns null if no budget is set.
fun loadBudgetLimit(): Double? {
    val file = File(BUDGET_FILE_NAME)
    if (!file.exists()) return null
    return file.readText().trim().toDoubleOrNull()
}

// Saves the budget limit to budget.txt.
fun saveBudgetLimit(limit: Double) {
    val file = File(BUDGET_FILE_NAME)
    file.writeText(limit.toString())
}

// Deletes budget.txt when the user removes the budget limit.
fun deleteBudgetLimit() {
    val file = File(BUDGET_FILE_NAME)
    if (file.exists()) {
        file.delete()
    }
}

// Loads saved category budgets from category_budgets.txt.
fun loadCategoryBudgets(): Map<Category, Double> {
    val file = File(CATEGORY_BUDGETS_FILE_NAME)
    if (!file.exists()) return emptyMap()

    return file.readLines().mapNotNull { line ->
        val parts = line.split("|", limit = 2)
        if (parts.size != 2) return@mapNotNull null

        val category = try {
            Category.valueOf(parts[0])
        } catch (_: IllegalArgumentException) {
            return@mapNotNull null
        }

        val amount = parts[1].toDoubleOrNull() ?: return@mapNotNull null
        if (amount <= 0) return@mapNotNull null

        category to amount
    }.toMap()
}

// Saves all category budgets to category_budgets.txt.
fun saveCategoryBudgets(categoryBudgets: Map<Category, Double>) {
    val file = File(CATEGORY_BUDGETS_FILE_NAME)
    file.printWriter().use { out ->
        categoryBudgets.toSortedMap(compareBy { it.name }).forEach { (category, amount) ->
            out.println("${category.name}|$amount")
        }
    }
}
