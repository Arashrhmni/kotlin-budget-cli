data class Expense(val description: String, val amount: Double, val category: String)

fun main() {
    val expenses = mutableListOf<Expense>()

    println("💶 Kotlin Budget Tracker")

    while (true) {
        println("\n--- Menu ---")
        println("1. Add expense")
        println("2. View all expenses")
        println("3. Summary by category")
        println("4. Biggest expense")
        println("5. Exit")
        print("Choose: ")

        when (readln().trim()) {
            "1" -> addExpense(expenses)
            "2" -> viewAll(expenses)
            "3" -> summarizeByCategory(expenses)
            "4" -> biggestExpense(expenses)
            "5" -> {
                println("Bye!")
                break
            }
            else -> println("Invalid option. Please choose 1–5.")
        }
    }
}

fun addExpense(expenses: MutableList<Expense>) {
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

    print("Category (food/transport/rent/entertainment/other): ")
    val category = readln().trim().lowercase().ifEmpty { "other" }

    expenses.add(Expense(desc, amount, category))
    println("✅ Added: $desc — €${"%.2f".format(amount)} [$category]")
}

fun viewAll(expenses: List<Expense>) {
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }
    println("\nAll Expenses:")
    expenses.forEachIndexed { index, it ->
        println("  ${index + 1}. [${it.category}] ${it.description}: €${"%.2f".format(it.amount)}")
    }
    println("─────────────────────────")
    println("  Total: €${"%.2f".format(expenses.sumOf { it.amount })}")
}

fun summarizeByCategory(expenses: List<Expense>) {
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }
    println("\nSummary by Category:")
    val grouped = expenses.groupBy { it.category }
    grouped.forEach { (category, items) ->
        val total = items.sumOf { it.amount }
        println("  $category: €${"%.2f".format(total)} (${items.size} expense(s))")
    }
}

fun biggestExpense(expenses: List<Expense>) {
    if (expenses.isEmpty()) {
        println("No expenses recorded yet.")
        return
    }
    val biggest = expenses.maxBy { it.amount }
    println("\nBiggest expense: ${biggest.description} — €${"%.2f".format(biggest.amount)} [${biggest.category}]")
}
