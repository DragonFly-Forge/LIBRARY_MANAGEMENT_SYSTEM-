package com.example.libraryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Data models
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibraryAppTheme {
                LibraryAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryAppRoot() {
    val books = remember {
        mutableStateMapOf(
            "B101" to Book("B101", "Kotlin in Action", "Dmitry Jemerov", 2017, 3),
            "B102" to Book("B102", "Clean Code", "Robert C. Martin", 2008, 2),
            "B103" to Book("B103", "Design Patterns", "Erich Gamma", 1994, 1)
        )
    }
    val issueRecords = remember { mutableStateListOf<IssueRecord>() }
    var selectedTab by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Management App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Catalogue") },
                    label = { Text("Books") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text("Add") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Circulation") },
                    label = { Text("Circulate") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Issued") },
                    label = { Text("Issued") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            when (selectedTab) {
                0 -> BookListScreen(books.values.toList())
                1 -> AddBookScreen { id, title, author, year, copies ->
                    if (books.containsKey(id)) {
                        message = "Error: Book ID already exists"
                    } else {
                        books[id] = Book(id, title, author, year, copies)
                        message = "Book added successfully!"
                        selectedTab = 0
                    }
                }
                2 -> SearchScreen(books.values.toList())
                3 -> CirculationScreen(
                    onIssue = { bookId, studentId ->
                        val b = books[bookId]
                        if (b == null) "Error: Book not found"
                        else if (b.availableCopies <= 0) "Error: Out of stock"
                        else if (issueRecords.any { it.bookId == bookId && it.studentId == studentId && it.status == "ISSUED" })
                            "Error: Already issued to this student"
                        else {
                            b.availableCopies -= 1
                            issueRecords.add(IssueRecord(bookId, studentId, "ISSUED"))
                            "Issued successfully"
                        }
                    },
                    onReturn = { bookId, studentId ->
                        val rec = issueRecords.find { it.bookId == bookId && it.studentId == studentId && it.status == "ISSUED" }
                        if (rec == null) "Error: No matching issue record"
                        else {
                            rec.status = "RETURNED"
                            books[bookId]?.let { it.availableCopies += 1 }
                            "Returned successfully"
                        }
                    }
                )
                4 -> IssuedBooksScreen(issueRecords.filter { it.status == "ISSUED" }, books)
            }
        }
    }
}

@Composable
fun BookListScreen(books: List<Book>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(books) { book ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Author: ${book.author} | Year: ${book.publicationYear}")
                    Text(text = "Available: ${book.availableCopies} copies", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun AddBookScreen(onAdd: (String, String, String, Int, Int) -> Unit) {
    var id by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var copies by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Book ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = copies, onValueChange = { copies = it }, label = { Text("Copies") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val y = year.toIntOrNull() ?: 2026
                val c = copies.toIntOrNull() ?: 1
                if (id.isNotBlank() && title.isNotBlank()) onAdd(id, title, author, y, c)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Book")
        }
    }
}

@Composable
fun SearchScreen(books: List<Book>) {
    var query by remember { mutableStateOf("") }
    val filtered = books.filter { it.bookId.contains(query, ignoreCase = true) || it.title.contains(query, ignoreCase = true) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by ID or Title") },
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { b ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "${b.title} (${b.bookId})", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Copies Available: ${b.availableCopies}")
                    }
                }
            }
        }
    }
}

@Composable
fun CirculationScreen(
    onIssue: (String, String) -> String,
    onReturn: (String, String) -> String
) {
    var bookId by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = bookId, onValueChange = { bookId = it }, label = { Text("Book ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = studentId, onValueChange = { studentId = it }, label = { Text("Student ID") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { resultText = onIssue(bookId, studentId) },
                modifier = Modifier.weight(1f)
            ) { Text("Issue") }
            Button(
                onClick = { resultText = onReturn(bookId, studentId) },
                modifier = Modifier.weight(1f)
            ) { Text("Return") }
        }
        if (resultText.isNotEmpty()) {
            Text(text = resultText, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun IssuedBooksScreen(issued: List<IssueRecord>, books: Map<String, Book>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(issued) { rec ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Book: ${books[rec.bookId]?.title ?: rec.bookId}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Issued To: ${rec.studentId}")
                    Text(text = "Status: ${rec.status}", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
fun LibraryAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
