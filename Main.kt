// Enum class: locked set of valid categories — no more typos like "fod" or "Food"
enum class Category {
    FOOD, TRANSPORT, RENT, ENTERTAINMENT, SALARY, FREELANCE, OTHER
}

// Sealed interface: defines the contract for all transaction types
sealed interface Transaction {
    val description: String
    val amount: Double
}

// Expense and Income both implement Transaction
data class Expense(
    override val description: String,
    override val amount: Double,
    val category: Category
) : Transaction

data class Income(
    override val description: String,
    override val amount: Double,
    val category: Category
) : Transaction

fun main() {
    val transactions = mutableListOf<Transaction>()

    println("💶 Kotlin Budget Tracker")

    while (true) {
        println("\n--- Menu ---")
        println("1. Add expense")
        println("2. Add income")
        println("3. View all transactions")
        println("4. Summary by category")
        println("5. Balance")
        println("6. Biggest expense")
        println("7. Exit")
        print("Choose: ")

        when (readln().trim()) {
            "1" -> addTransaction(transactions, isExpense = true)
            "2" -> addTransaction(transactions, isExpense = false)
            "3" -> viewAll(transactions)
            "4" -> summarizeByCategory(transactions)
            "5" -> showBalance(transactions)
            "6" -> biggestExpense(transactions)
            "7" -> {
                println("Bye!")
                break
            }
            else -> println("Invalid option. Please choose 1–7.")
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

fun addTransaction(transactions: MutableList<Transaction>, isExpense: Boolean) {
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

    if (isExpense) {
        transactions.add(Expense(desc, amount, category))
    } else {
        transactions.add(Income(desc, amount, category))
    }

    println("✅ $type added: $desc — €${"%.2f".format(amount)} [${category.name}]")
}

fun viewAll(transactions: List<Transaction>) {
    if (transactions.isEmpty()) {
        println("No transactions recorded yet.")
        return
    }
    println("\nAll Transactions:")
    transactions.forEachIndexed { index, t ->
        // 'when' on a sealed interface — compiler knows all possible types
        val label = when (t) {
            is Expense -> "EXPENSE"
            is Income  -> "INCOME "
        }
        val category = when (t) {
            is Expense -> t.category.name
            is Income  -> t.category.name
        }
        println("  ${index + 1}. [$label] [${category}] ${t.description}: €${"%.2f".format(t.amount)}")
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
    println("\nBiggest expense: ${biggest.description} — €${"%.2f".format(biggest.amount)} [${biggest.category.name}]")
}
