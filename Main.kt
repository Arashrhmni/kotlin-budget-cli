import java.io.File
import java.time.LocalDate

const val DATA_FILE_NAME = "transactions.txt"
const val BUDGET_FILE_NAME = "budget.txt"

// Enum class: locked set of valid categories — no more typos like "fod" or "Food"
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

    println("💶 Kotlin Budget Tracker")
    println("Loaded ${transactions.size} transaction(s).")
    if (budgetLimit != null) {
        println("Current budget limit: €${"%.2f".format(budgetLimit)}")
    } else {
        println("No budget limit set yet.")
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
        println("10. Exit")
        print("Choose: ")

        when (readln().trim()) {
            "1" -> addTransaction(transactions, isExpense = true, budgetLimit = budgetLimit)
            "2" -> addTransaction(transactions, isExpense = false, budgetLimit = budgetLimit)
            "3" -> viewAll(transactions)
            "4" -> summarizeByCategory(transactions)
            "5" -> showBalance(transactions)
            "6" -> biggestExpense(transactions)
            "7" -> deleteTransaction(transactions, budgetLimit)
            "8" -> budgetLimit = setBudgetLimit()
            "9" -> checkBudgetStatus(transactions, budgetLimit)
            "10" -> {
                saveTransactions(transactions)
                if (budgetLimit != null) {
                    saveBudgetLimit(budgetLimit)
                }
                println("Bye! Your transactions were saved to $DATA_FILE_NAME")
                break
            }
            else -> println("Invalid option. Please choose 1–10.")
        }
    }
}

fun pickCategory(isExpense: Boolean): Category {
    val options = if (isExpense) {
        listOf(Category.FOOD, Category.TRANSPORT, Category.RENT, Category.ENTERTAINMENT, Category.OTHER)
    } else {
        listOf(Category.SALARY, Category.FREELANCE, Category.OTHER)
    }

    println("Pick a category:")
    options.forEachIndexed { index, cat -> println("  ${index + 1}. ${cat.name}") }
    print("Choose: ")

    val choice = readln().toIntOrNull()
    return if (choice != null && choice in 1..options.size) {
        options[choice - 1]
    } else {
        println("Invalid choice, defaulting to OTHER.")
        Category.OTHER
    }
}

fun addTransaction(transactions: MutableList<Transaction>, isExpense: Boolean, budgetLimit: Double?) {
    val type = if (isExpense) "Expense" else "Income"

    print("Description: ")
    val desc = readln().trim()
    if (desc.isEmpty()) {
        println("Description cannot be empty.")
        return
    }

    print("Amount (€): ")
    val amount = readln().toDoubleOrNull()
    if (amount == null || amount <= 0) {
        println("Invalid amount. Please enter a positive number.")
        return
    }

    val category = pickCategory(isExpense)
    val date = LocalDate.now()

    if (isExpense) {
        transactions.add(Expense(desc, amount, category, date))
    } else {
        transactions.add(Income(desc, amount, category, date))
    }

    saveTransactions(transactions)
    println("✅ $type added: $desc — €${"%.2f".format(amount)} [${category.name}] on $date")

    if (isExpense && budgetLimit != null) {
        checkBudgetStatus(transactions, budgetLimit)
    }
}

fun viewAll(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }
    println("\nAll Transactions:")
    transactions.forEachIndexed { index, t ->
        val label = when (t) {
            is Expense -> "EXPENSE"
            is Income -> "INCOME "
        }
        println("  ${index + 1}. [$label] [${t.category.name}] ${t.description}: €${"%.2f".format(t.amount)} (${t.date})")
    }
}

fun summarizeByCategory(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }
    println("\nExpenses by Category:")
    transactions
        .filterIsInstance<Expense>()
        .groupBy { it.category }
        .forEach { (category, items) ->
            val total = items.sumOf { it.amount }
            println("  ${category.name}: €${"%.2f".format(total)} (${items.size} item(s))")
        }

    println("\nIncome by Category:")
    transactions
        .filterIsInstance<Income>()
        .groupBy { it.category }
        .forEach { (category, items) ->
            val total = items.sumOf { it.amount }
            println("  ${category.name}: €${"%.2f".format(total)} (${items.size} item(s))")
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
    println("\nBiggest expense: ${biggest.description} — €${"%.2f".format(biggest.amount)} [${biggest.category.name}] on ${biggest.date}")
}

fun deleteTransaction(transactions: MutableList<Transaction>, budgetLimit: Double?) {
    if (transactions.isEmpty()) {
        println("No transactions to delete.")
        return
    }

    viewAll(transactions)
    print("Enter transaction number to delete: ")
    val index = readln().toIntOrNull()

    if (index == null || index !in 1..transactions.size) {
        println("Invalid number.")
        return
    }

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
}

fun setBudgetLimit(): Double? {
    print("Enter monthly budget limit (€): ")
    val limit = readln().toDoubleOrNull()

    if (limit == null || limit <= 0) {
        println("Invalid budget amount. Please enter a positive number.")
        return null
    }

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

fun loadTransactions(): List<Transaction> {
    val file = File(DATA_FILE_NAME)
    if (!file.exists()) return emptyList()

    return file.readLines()
        .mapNotNull { parseTransaction(it) }
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
