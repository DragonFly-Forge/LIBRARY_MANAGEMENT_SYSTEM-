import java.util.Scanner

data class Book(
    val bookId: String,
    val title: String,
    val author: String,
    val publicationYear: Int,
    var availableCopies: Int
)

data class IssueRecord(
    val bookId: String,
    val studentId: String,
    var status: String = "ISSUED"
)

class LibraryManagementSystem {
    private val books = mutableMapOf<String, Book>()
    private val issueRecords = mutableListOf<IssueRecord>()

    fun addBook(id: String, title: String, author: String, year: Int, copies: Int): Boolean {
        val trimmedId = id.trim()
        val trimmedTitle = title.trim()
        val trimmedAuthor = author.trim()

        if (trimmedId.isEmpty() || trimmedTitle.isEmpty() || trimmedAuthor.isEmpty()) {
            println("Error: Fields cannot be blank.")
            return false
        }
        if (books.containsKey(trimmedId)) {
            println("Error: A book with ID '$trimmedId' already exists.")
            return false
        }
        if (year <= 0 || year > 2026) {
            println("Error: Invalid publication year.")
            return false
        }
        if (copies < 0) {
            println("Error: Number of copies cannot be negative.")
            return false
        }

        books[trimmedId] = Book(trimmedId, trimmedTitle, trimmedAuthor, year, copies)
        println("Book '$trimmedTitle' (ID: $trimmedId) added successfully.")
        return true
    }

    fun displayAllBooks() {
        if (books.isEmpty()) {
            println("No books currently registered in the catalogue.")
            return
        }

        println("\n" + "=".repeat(85))
        println(String.format("%-10s | %-30s | %-20s | %-6s | %-6s", "Book ID", "Title", "Author", "Year", "Copies"))
        println("-".repeat(85))
        for (b in books.values) {
            val truncatedTitle = if (b.title.length > 28) b.title.take(25) + "..." else b.title
            val truncatedAuthor = if (b.author.length > 18) b.author.take(15) + "..." else b.author
            println(String.format("%-10s | %-30s | %-20s | %-6d | %-6d", b.bookId, truncatedTitle, truncatedAuthor, b.publicationYear, b.availableCopies))
        }
        println("=".repeat(85))
    }

    fun searchBook(query: String) {
        val q = query.trim().lowercase()
        val results = books.values.filter {
            it.bookId.lowercase() == q || it.title.lowercase().contains(q)
        }

        if (results.isEmpty()) {
            println("No book found matching query: \"$query\"")
            return
        }

        println("\nFound ${results.size} matching book(s):")
        for (b in results) {
            val availability = if (b.availableCopies > 0) "AVAILABLE (${b.availableCopies} in stock)" else "OUT OF STOCK"
            println("----------------------------------------")
            println("ID:           ${b.bookId}")
            println("Title:        ${b.title}")
            println("Author:       ${b.author}")
            println("Year:         ${b.publicationYear}")
            println("Availability: $availability")
        }
        println("----------------------------------------")
    }

    fun issueBook(bookId: String, studentId: String): Boolean {
        val bId = bookId.trim()
        val sId = studentId.trim()

        val book = books[bId]
        if (book == null) {
            println("Error: Book with ID '$bId' does not exist.")
            return false
        }
        if (book.availableCopies <= 0) {
            println("Error: Book '${book.title}' is currently out of stock.")
            return false
        }

        val alreadyIssued = issueRecords.any { it.bookId == bId && it.studentId == sId && it.status == "ISSUED" }
        if (alreadyIssued) {
            println("Error: Student '$sId' already has an active issue record for this book.")
            return false
        }

        book.availableCopies -= 1
        issueRecords.add(IssueRecord(bId, sId, "ISSUED"))
        println("Success: Book '${book.title}' issued to student '$sId'.")
        return true
    }

    fun returnBook(bookId: String, studentId: String): Boolean {
        val bId = bookId.trim()
        val sId = studentId.trim()

        val record = issueRecords.find { it.bookId == bId && it.studentId == sId && it.status == "ISSUED" }
        if (record == null) {
            println("Error: No outstanding issue record found for student '$sId' and book ID '$bId'.")
            return false
        }

        record.status = "RETURNED"
        books[bId]?.let { it.availableCopies += 1 }
        println("Success: Book ID '$bId' returned by student '$sId'.")
        return true
    }

    fun displayIssuedBooks() {
        val activeIssues = issueRecords.filter { it.status == "ISSUED" }
        if (activeIssues.isEmpty()) {
            println("No books are currently issued.")
            return
        }

        println("\n" + "=".repeat(75))
        println(String.format("%-10s | %-30s | %-15s | %-10s", "Book ID", "Title", "Student ID", "Status"))
        println("-".repeat(75))
        for (record in activeIssues) {
            val title = books[record.bookId]?.title ?: "Unknown Title"
            val truncatedTitle = if (title.length > 28) title.take(25) + "..." else title
            println(String.format("%-10s | %-30s | %-15s | %-10s", record.bookId, truncatedTitle, record.studentId, record.status))
        }
        println("=".repeat(75))
    }
}

fun main() {
    val lms = LibraryManagementSystem()
    val scanner = Scanner(System.`in`)

    // Sample initial books
    lms.addBook("B101", "Kotlin in Action", "Dmitry Jemerov", 2017, 3)
    lms.addBook("B102", "Clean Code", "Robert C. Martin", 2008, 2)
    lms.addBook("B103", "Design Patterns", "Erich Gamma", 1994, 1)

    var running = true

    while (running) {
        println("\n========================================")
        println("       LIBRARY MANAGEMENT SYSTEM        ")
        println("========================================")
        println("1. Add a new book")
        println("2. Display all books")
        println("3. Search for a book")
        println("4. Issue a book")
        println("5. Return a book")
        println("6. Display currently issued books")
        println("7. Exit")
        print("Enter your choice (1-7): ")

        val choice = scanner.nextLine().trim()

        when (choice) {
            "1" -> {
                print("Enter Book ID: ")
                val id = scanner.nextLine()
                print("Enter Book Title: ")
                val title = scanner.nextLine()
                print("Enter Author Name: ")
                val author = scanner.nextLine()
                print("Enter Publication Year: ")
                val year = scanner.nextLine().toIntOrNull() ?: -1
                print("Enter Available Copies: ")
                val copies = scanner.nextLine().toIntOrNull() ?: -1
                lms.addBook(id, title, author, year, copies)
            }
            "2" -> lms.displayAllBooks()
            "3" -> {
                print("Enter Book ID or Title to search: ")
                val query = scanner.nextLine()
                lms.searchBook(query)
            }
            "4" -> {
                print("Enter Book ID: ")
                val bookId = scanner.nextLine()
                print("Enter Student ID: ")
                val studentId = scanner.nextLine()
                lms.issueBook(bookId, studentId)
            }
            "5" -> {
                print("Enter Book ID: ")
                val bookId = scanner.nextLine()
                print("Enter Student ID: ")
                val studentId = scanner.nextLine()
                lms.returnBook(bookId, studentId)
            }
            "6" -> lms.displayIssuedBooks()
            "7" -> {
                println("Exiting system. Goodbye!")
                running = false
            }
            else -> println("Invalid selection. Please choose an option between 1 and 7.")
        }
    }
}
