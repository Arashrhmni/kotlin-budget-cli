// import means we bring code from the Java/Kotlin library into this file.
// File is used later for reading, writing, and deleting text files.
import java.io.File
// LocalDate stores dates without time, for example 2026-05-18.
import java.time.LocalDate
// Month lets us convert a number like 5 into MAY for monthly summaries.
import java.time.Month
// Locale.US is used so CSV amounts use a dot as decimal separator, for example 24.50.
import java.util.Locale

// const val is a fixed value known before the program runs.
// val means the value cannot be reassigned. const val is even stricter: it is fixed at compile time.
// These file names do not change, so const val is a good fit.
const val DATA_FILE_NAME = "transactions.txt"
const val BUDGET_FILE_NAME = "budget.txt"
const val CATEGORY_BUDGETS_FILE_NAME = "category_budgets.txt"
const val CSV_EXPORT_FILE_NAME = "transactions_export.csv"

// enum class = a fixed list of allowed values.
// Enum values are usually written in uppercase, like FOOD or RENT.
// Category is locked to these values, so we avoid typos like "fod" or "Food".
enum class Category {
    FOOD, TRANSPORT, RENT, ENTERTAINMENT, SALARY, FREELANCE, OTHER
}

// Another enum class. Every payment method must be one of these fixed options.
enum class PaymentMethod {
    CASH, CARD, PAYPAL, BANK_TRANSFER, OTHER
}

// sealed interface = a contract that is limited to this codebase/module.
// Interface means every class that uses Transaction must provide these properties.
// Sealed means the possible transaction types are controlled here: Expense and Income.
sealed interface Transaction {
    val description: String
    val amount: Double
    val category: Category
    val date: LocalDate
    val paymentMethod: PaymentMethod
}

// data class = a class mainly used to hold data.
// Expense and Income both implement Transaction.
// override means these properties come from the Transaction interface.
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

// fun creates a function. main() is the special function where a Kotlin program starts.
// Function shape: fun functionName(input: Type): OutputType { ... }
fun main() {
    // val cannot be reassigned, but the MutableList inside it can still be changed.
    // toMutableList() makes the loaded list editable with add(), removeAt(), and clear().
    val transactions = loadTransactions().toMutableList()
    // var can be reassigned. We use var because the user can set or remove the budget limit.
    // Double? means this value can be a number or null. null = no budget limit set.
    var budgetLimit = loadBudgetLimit()
    // A MutableMap stores key-value pairs. Here: Category -> budget amount.
    val categoryBudgets = loadCategoryBudgets().toMutableMap()

    println("💶 Kotlin Budget Tracker")
    // $ and ${...} inside a string insert variables or calculations into text.
    // Here ${transactions.size} prints how many items are in the list.
    println("Loaded ${transactions.size} transaction(s).")
    // if/else works like in many languages. != means "not equal".
    // null means "nothing/no value". Here null means no budget limit is set.
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

    // while (true) keeps the app running until the user chooses Exit.
    while (true) {
        printMainMenu()

        // when is like switch-case: it chooses what to do based on the result.
        // 1..7 is a range, meaning valid choices from 1 to 7, including both numbers.
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
                // break stops the while loop and ends the app.
                break
            }
        }
    }
}

// This function has no return type written, so it returns Unit.
// Unit means it performs an action but does not return a useful value.
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
            // return exits this submenu function and goes back to the main menu.
            3 -> return
        }
    }
}

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

// Boolean means true or false. Here true = expense, false = income.
// List<Category> means the function returns a read-only list of Category values.
fun getCategoryOptions(isExpense: Boolean): List<Category> {
    return if (isExpense) {
        listOf(Category.FOOD, Category.TRANSPORT, Category.RENT, Category.ENTERTAINMENT, Category.OTHER)
    } else {
        listOf(Category.SALARY, Category.FREELANCE, Category.OTHER)
    }
}

// IntRange is a range of integers like 1..7.
// This function keeps asking until the user enters a valid number.
fun promptChoice(prompt: String, validRange: IntRange): Int {
    while (true) {
        print(prompt)
        // readln() gets terminal input as text.
        // trim() removes extra spaces.
        // toIntOrNull() converts text to Int or returns null if the input is invalid.
        val choice = readln().trim().toIntOrNull()
        // && means AND: both sides must be true.
        // "choice in validRange" checks if the number is inside the allowed range.
        if (choice != null && choice in validRange) {
            return choice
        }
        println("Invalid choice. Please enter a number from ${validRange.first} to ${validRange.last}.")
    }
}

// This function returns Boolean: true for yes, false for no.
fun promptYesNo(prompt: String): Boolean {
    while (true) {
        print(prompt)
        // lowercase() lets us accept Y, y, YES, yes, etc.
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

// Double is a decimal number type, used here for money amounts.
fun promptPositiveDouble(prompt: String): Double {
    while (true) {
        print(prompt)
        // toDoubleOrNull() avoids crashes when the user types something that is not a number.
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

// defaultDate is used when the user presses Enter without typing a date.
fun promptDate(prompt: String, defaultDate: LocalDate): LocalDate {
    while (true) {
        print(prompt)
        val input = readln().trim()
        // If the input is empty, return the default value.
        if (input.isEmpty()) return defaultDate

        try {
            // LocalDate.parse() converts text like 2026-05-18 into a LocalDate object.
            return LocalDate.parse(input)
        } catch (_: Exception) {
            println("Invalid date. Please use the format YYYY-MM-DD, for example 2026-05-18.")
        }
    }
}

fun promptYear(prompt: String): Int {
    while (true) {
        print(prompt)
        // Again we use toIntOrNull() so invalid text does not crash the program.
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
    // forEachIndexed is a loop that gives both the index and the value.
    options.forEachIndexed { index, category ->
        // index starts at 0, so index + 1 makes the menu start at 1.
        println("  ${index + 1}. ${category.name}")
    }

    // options.size is the number of items in the list.
    val choice = promptChoice("Choose: ", 1..options.size)
    // Lists use 0-based indexes, so choice 1 means index 0.
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
    // forEachIndexed is a lambda function. It gives us both the index and the current item.
    // index starts at 0, so we print index + 1 to show user-friendly numbers.
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
        // add() inserts a new item into the mutable list.
        transactions.add(Expense(description, amount, category, date, paymentMethod))
    } else {
        transactions.add(Income(description, amount, category, date, paymentMethod))
    }

    saveTransactions(transactions)
    println(
        // $type and $description are string templates.
        // ${"%.2f".format(amount)} formats the amount with 2 decimal places.
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
    // This loop prints each transaction together with its number in the list.
    transactions.forEachIndexed { index, transaction ->
        // Because Transaction is sealed, when can safely check Expense vs Income.
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
    // filterIsInstance<Expense>() keeps only Expense objects from the mixed Transaction list.
    // filterIsInstance<Expense>() keeps only Expense objects from the mixed Transaction list.
    val expenses = transactions.filterIsInstance<Expense>()
    if (expenses.isEmpty()) {
        println("  No expenses recorded yet.")
    } else {
        expenses
            // groupBy creates groups. Here all transactions with the same category are grouped together.
            .groupBy { it.category }
            .forEach { (category, items) ->
                // sumOf adds all amounts in the group.
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
            // groupBy can also group by payment method.
            .groupBy { it.paymentMethod }
            .entries
            // sortedBy sorts the grouped results alphabetically by enum name.
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

    // maxBy finds the item with the highest amount.
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

    // minBy finds the item with the lowest amount.
    val smallest = expenses.minBy { it.amount }
    println(
        "\nSmallest expense: ${smallest.description} — €${"%.2f".format(smallest.amount)} " +
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
    // Average = total divided by number of items.
    val averageExpense = totalExpenses / expenses.size

    println("\nAverage expense:")
    println("  Number of expenses: ${expenses.size}")
    println("  Total expenses:     €${"%.2f".format(totalExpenses)}")
    println("  Average expense:    €${"%.2f".format(averageExpense)}")
}

fun showTransactionCountSummary(transactions: List<Transaction>) {
    val expenses = transactions.filterIsInstance<Expense>()
    val incomes = transactions.filterIsInstance<Income>()

    println("\nTransaction count summary:")
    println("  Expenses: ${expenses.size}")
    println("  Income:   ${incomes.size}")
    println("  Total:    ${transactions.size}")
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

    // removeAt() removes one item from a MutableList by index.
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

    // clear() removes all items from the MutableList.
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

fun checkBudgetStatus(transactions: List<Transaction>, budgetLimit: Double?) {
    if (budgetLimit == null) {
        println("No budget limit set yet.")
        return
    }

    val totalExpenses = transactions.filterIsInstance<Expense>().sumOf { it.amount }
    // Simple calculation: remaining money = limit - spent.
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
    // filter keeps only items where the lambda returns true.
    // contains(..., ignoreCase = true) searches text without caring about uppercase/lowercase.
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

    // This lambda keeps only transactions from the selected year and month.
    val monthTransactions = transactions.filter {
        // == compares values. && means both conditions must be true.
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
        // sortedByDescending sorts from highest/newest to lowest/oldest.
        1 -> transactions.sortedByDescending { it.date }
        // sortedBy sorts from lowest/oldest to highest/newest.
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

    // This adds a new key-value pair or updates the old value for this category.
    categoryBudgets[selectedCategory] = limit
    saveCategoryBudgets(categoryBudgets)

    println("✅ Category budget set: ${selectedCategory.name} = €${"%.2f".format(limit)}")
}

fun removeCategoryBudget(categoryBudgets: MutableMap<Category, Double>) {
    if (categoryBudgets.isEmpty()) {
        println("No category budgets set yet.")
        return
    }

    val categoriesWithBudgets = categoryBudgets.keys.sortedBy { it.name }

    println("Choose a category budget to remove:")
    categoriesWithBudgets.forEachIndexed { index, category ->
        // ?: is the Elvis operator. If the value on the left is null, Kotlin uses the value on the right.
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

    // remove() deletes one key-value pair from the MutableMap.
    categoryBudgets.remove(selectedCategory)
    saveCategoryBudgets(categoryBudgets)
    println("✅ Category budget removed: ${selectedCategory.name}")
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
    // ?: is the Elvis operator. If there is no budget for this category, return immediately.
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
    // use { ... } closes the file automatically after writing.
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

            // joinToString(",") joins the values with commas.
            // csvValue(it) safely wraps each value in quotes.
            out.println(row.joinToString(",") { csvValue(it) })
        }
    }

    println("✅ Transactions exported to $CSV_EXPORT_FILE_NAME")
}

fun csvValue(value: String): String {
    // In CSV, quotes inside values must be doubled.
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

fun loadTransactions(): List<Transaction> {
    val file = File(DATA_FILE_NAME)
    // ! means NOT. If the file does not exist, return an empty list.
    if (!file.exists()) return emptyList()

    // mapNotNull converts each line to a Transaction and skips invalid lines that return null.
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

    // The | character is used as a separator in the save file, so we replace it in descriptions.
    val safeDescription = transaction.description.replace("|", "/")
    return listOf(
        type,
        safeDescription,
        transaction.amount.toString(),
        transaction.category.name,
        transaction.date.toString(),
        transaction.paymentMethod.name
    ).joinToString("|") // joinToString makes one save line separated by |
}

fun parseTransaction(line: String): Transaction? {
    // split breaks the saved line back into parts. limit = 6 keeps the description safer.
    val parts = line.split("|", limit = 6)
    if (parts.size != 5 && parts.size != 6) return null

    val type = parts[0]
    val description = parts[1]
    // ?: return null means: if conversion fails, this saved line is invalid, so skip it.
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
        } catch (_: IllegalArgumentException) { // catch handles invalid enum text safely.
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
    // writeText replaces the whole file content with this value.
    file.writeText(limit.toString())
}

fun deleteBudgetLimit() {
    val file = File(BUDGET_FILE_NAME)
    if (file.exists()) {
        // delete() removes the file from disk.
        file.delete()
    }
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
    }.toMap() // Converts pairs like Category to amount into a Map.
}

fun saveCategoryBudgets(categoryBudgets: Map<Category, Double>) {
    val file = File(CATEGORY_BUDGETS_FILE_NAME)
    file.printWriter().use { out ->
        categoryBudgets.toSortedMap(compareBy { it.name }).forEach { (category, amount) ->
            out.println("${category.name}|$amount")
        }
    }
}
