import java.io.File
import java.time.LocalDate

const val DATA_FILE_NAME = "transactions.txt"
const val BUDGET_FILE_NAME = "budget.txt"
const val CATEGORY_BUDGETS_FILE_NAME = "category_budgets.txt"

// Enum class: locked set of valid categories
// no more typos like "fod" or "Food"
enum class Category {
    FOOD, TRANSPORT, RENT, ENTERTAINMENT, SALARY, FREELANCE, OTHER
}

// Sealed interface: defines the contract for all transaction types
sealed interface Transaction {
    val description: String
    val amount: Double
    val category: Category
    val date: LocalDate
}

// Expense and Income both implement Transaction
data class Expense(
    override val description: String,
    override val amount: Double,
    override val category: Category,
    override val date: LocalDate
) : Transaction

data class Income(
    override val description: String,
    override val amount: Double,
    override val category: Category,
    override val date: LocalDate
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
        println("5. Balance")
        println("6. Biggest expense")
        println("7. Delete transaction")
        println("8. Set budget limit")
        println("9. Check budget status")
        println("10. Filter transactions")
        println("11. Edit transaction")
        println("12. Monthly summary")
        println("13. Sort transactions")
        println("14. Set category budget")
        println("15. Check category budgets")
        println("16. Exit")

        when (promptChoice("Choose: ", 1..16)) {
            1 -> addTransaction(transactions, isExpense = true, budgetLimit = budgetLimit, categoryBudgets = categoryBudgets)
            2 -> addTransaction(transactions, isExpense = false, budgetLimit = budgetLimit, categoryBudgets = categoryBudgets)
            3 -> viewAll(transactions)
            4 -> summarizeByCategory(transactions)
            5 -> showBalance(transactions)
            6 -> biggestExpense(transactions)
            7 -> deleteTransaction(transactions, budgetLimit, categoryBudgets)
            8 -> budgetLimit = setBudgetLimit()
            9 -> checkBudgetStatus(transactions, budgetLimit)
            10 -> filterTransactions(transactions)
            11 -> editTransaction(transactions, budgetLimit, categoryBudgets)
            12 -> showMonthlySummary(transactions)
            13 -> sortTransactions(transactions)
            14 -> setCategoryBudget(categoryBudgets)
            15 -> checkCategoryBudgetStatus(transactions, categoryBudgets)
            16 -> {
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

fun pickCategory(isExpense: Boolean): Category {
    val options = getCategoryOptions(isExpense)

    println("Pick a category:")
    options.forEachIndexed { index, category ->
        println("  ${index + 1}. ${category.name}")
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
    val date = LocalDate.now()

    if (isExpense) {
        transactions.add(Expense(description, amount, category, date))
    } else {
        transactions.add(Income(description, amount, category, date))
    }

    saveTransactions(transactions)
    println("✅ $type added: $description — €${"%.2f".format(amount)} [${category.name}] on $date")

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
            "  ${index + 1}. [$label] [${transaction.category.name}] ${transaction.description}: " +
                "€${"%.2f".format(transaction.amount)} (${transaction.date})"
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
            "[${biggest.category.name}] on ${biggest.date}"
    )
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
    val removed = transactions.removeAt(index - 1)
    saveTransactions(transactions)

    val type = when (removed) {
        is Expense -> "Expense"
        is Income -> "Income"
    }

    println("Removed $type: ${removed.description} — €${"%.2f".format(removed.amount)}")

    if (budgetLimit != null) {
        checkBudgetStatus(transactions, budgetLimit)
    }

    if (removed is Expense) {
        showCategoryBudgetStatusForCategory(transactions, removed.category, categoryBudgets)
    }
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

    when (promptChoice("Choose: ", 1..4)) {
        1 -> showFilteredList(transactions.filterIsInstance<Expense>(), "Expenses only")
        2 -> showFilteredList(transactions.filterIsInstance<Income>(), "Income only")
        3 -> filterByCategory(transactions)
        4 -> filterByDescription(transactions)
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

    val updatedTransaction = if (isExpense) {
        Expense(newDescription, newAmount, newCategory, oldTransaction.date)
    } else {
        Income(newDescription, newAmount, newCategory, oldTransaction.date)
    }

    transactions[index - 1] = updatedTransaction
    saveTransactions(transactions)

    println("✅ Transaction updated.")
    println(
        "Old: ${oldTransaction.description} — €${"%.2f".format(oldTransaction.amount)} " +
            "[${oldTransaction.category.name}] (${oldTransaction.date})"
    )
    println(
        "New: ${updatedTransaction.description} — €${"%.2f".format(updatedTransaction.amount)} " +
            "[${updatedTransaction.category.name}] (${updatedTransaction.date})"
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

    val currentDate = LocalDate.now()
    val monthTransactions = transactions.filter {
        it.date.year == currentDate.year && it.date.month == currentDate.month
    }

    if (monthTransactions.isEmpty()) {
        println("No transactions found for ${currentDate.month.name} ${currentDate.year}.")
        return
    }

    val monthlyIncome = monthTransactions.filterIsInstance<Income>().sumOf { it.amount }
    val monthlyExpenses = monthTransactions.filterIsInstance<Expense>().sumOf { it.amount }
    val monthlyBalance = monthlyIncome - monthlyExpenses

    println("\nMonthly summary for ${currentDate.month.name} ${currentDate.year}:")
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
        transaction.date.toString()
    ).joinToString("|")
}

fun parseTransaction(line: String): Transaction? {
    val parts = line.split("|", limit = 5)
    if (parts.size != 5) return null

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

    return when (type) {
        "EXPENSE" -> Expense(description, amount, category, date)
        "INCOME" -> Income(description, amount, category, date)
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
