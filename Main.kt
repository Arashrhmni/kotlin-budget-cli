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
        println("\n--- Menu ---")
        println("1. Add expense")
        println("2. Add income")
        println("3. View all transactions")
        println("4. Summary by category")
        println("5. Summary by payment method")
        println("6. Balance")
        println("7. Biggest expense")
        println("8. Smallest expense")
        println("9. Average expense")
        println("10. Delete transaction")
        println("11. Set budget limit")
        println("12. Check budget status")
        println("13. Filter transactions")
        println("14. Edit transaction")
        println("15. Monthly summary")
        println("16. Sort transactions")
        println("17. Set category budget")
        println("18. Check category budgets")
        println("19. Export transactions to CSV")
        println("20. Clear all transactions")
        println("21. Exit")

        when (promptChoice("Choose: ", 1..21)) {
            1 -> addTransaction(transactions, isExpense = true, budgetLimit = budgetLimit, categoryBudgets = categoryBudgets)
            2 -> addTransaction(transactions, isExpense = false, budgetLimit = budgetLimit, categoryBudgets = categoryBudgets)
            3 -> viewAll(transactions)
            4 -> summarizeByCategory(transactions)
            5 -> summarizeByPaymentMethod(transactions)
            6 -> showBalance(transactions)
            7 -> biggestExpense(transactions)
            8 -> smallestExpense(transactions)
            9 -> showAverageExpense(transactions)
            10 -> deleteTransaction(transactions, budgetLimit, categoryBudgets)
            11 -> budgetLimit = setBudgetLimit()
            12 -> checkBudgetStatus(transactions, budgetLimit)
            13 -> filterTransactions(transactions)
            14 -> editTransaction(transactions, budgetLimit, categoryBudgets)
            15 -> showMonthlySummary(transactions)
            16 -> sortTransactions(transactions)
            17 -> setCategoryBudget(categoryBudgets)
            18 -> checkCategoryBudgetStatus(transactions, categoryBudgets)
            19 -> exportTransactionsToCsv(transactions)
            20 -> clearAllTransactions(transactions)
            21 -> {
                saveTransactions(transactions)
                if (budgetLimit != null) {
                    saveBudgetLimit(budgetLimit)
                }
                saveCategoryBudgets(categoryBudgets)
                println("Bye! Your data was saved.")
                break
            }
        }
    }
}

fun getCategoryOptions(isExpense: Boolean): List<Category> {
    return if (isExpense) {
        listOf(Category.FOOD, Category.TRANSPORT, Category.RENT, Category.ENTERTAINMENT, Category.OTHER)
    } else {
        listOf(Category.SALARY, Category.FREELANCE, Category.OTHER)
    }
}

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

fun promptNonEmptyText(prompt: String): String {
    while (true) {
        print(prompt)
        val input = readln().trim()
        if (input.isNotEmpty()) return input
        println("This field cannot be empty.")
    }
}

fun promptPositiveDouble(prompt: String): Double {
    while (true) {
        print(prompt)
        val value = readln().trim().toDoubleOrNull()
        if (value != null && value > 0) return value
        println("Invalid amount. Please enter a positive number.")
    }
}

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

fun pickCategory(isExpense: Boolean): Category {
    val options = getCategoryOptions(isExpense)

    println("Pick a category:")
    options.forEachIndexed { index, category ->
        println("  ${index + 1}. ${category.name}")
    }

    val choice = promptChoice("Choose: ", 1..options.size)
    return options[choice - 1]
}

fun pickPaymentMethod(): PaymentMethod {
    val options = PaymentMethod.values().toList()

    println("Pick a payment method:")
    options.forEachIndexed { index, paymentMethod ->
        println("  ${index + 1}. ${paymentMethod.name}")
    }

    val choice = promptChoice("Choose: ", 1..options.size)
    return options[choice - 1]
}

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

fun viewAll(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }

    println("\nAll Transactions:")
    printTransactionList(transactions)
}

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

fun smallestExpense(transactions: List<Transaction>) {
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }

    val smallest = expenses.minBy { it.amount }
    println(
        "
Smallest expense: ${smallest.description} — €${"%.2f".format(smallest.amount)} " +
            "[${smallest.category.name}] [${smallest.paymentMethod.name}] on ${smallest.date}"
    )
}

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

fun setBudgetLimit(): Double {
    val limit = promptPositiveDouble("Enter monthly budget limit (€): ")
    saveBudgetLimit(limit)
    println("✅ Budget limit set to €${"%.2f".format(limit)}")
    return limit
}

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

fun showFilteredList(filtered: List<Transaction>, title: String) {
    if (filtered.isEmpty()) {
        println("No matching transactions found.")
        return
    }

    println("\n$title:")
    printTransactionList(filtered)
}

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

fun filterByDescription(transactions: List<Transaction>) {
    val query = promptNonEmptyText("Enter text to search for: ")
    val filtered = transactions.filter { it.description.contains(query, ignoreCase = true) }
    showFilteredList(filtered, "Search results for \"$query\"")
}

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

fun csvValue(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

fun loadTransactions(): List<Transaction> {
    val file = File(DATA_FILE_NAME)
    if (!file.exists()) return emptyList()

    return file.readLines().mapNotNull { parseTransaction(it) }
}

fun saveTransactions(transactions: List<Transaction>) {
    val file = File(DATA_FILE_NAME)
    file.printWriter().use { out ->
        transactions.forEach { transaction ->
            out.println(transactionToLine(transaction))
        }
    }
}

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

fun loadBudgetLimit(): Double? {
    val file = File(BUDGET_FILE_NAME)
    if (!file.exists()) return null
    return file.readText().trim().toDoubleOrNull()
}

fun saveBudgetLimit(limit: Double) {
    val file = File(BUDGET_FILE_NAME)
    file.writeText(limit.toString())
}

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

fun saveCategoryBudgets(categoryBudgets: Map<Category, Double>) {
    val file = File(CATEGORY_BUDGETS_FILE_NAME)
    file.printWriter().use { out ->
        categoryBudgets.toSortedMap(compareBy { it.name }).forEach { (category, amount) ->
            out.println("${category.name}|$amount")
        }
    }
}
