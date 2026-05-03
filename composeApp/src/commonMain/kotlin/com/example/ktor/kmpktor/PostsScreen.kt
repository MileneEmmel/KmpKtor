package com.example.ktor.kmpktor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PostsScreen() {
    val viewModel = remember { PostsViewModel() }
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var userIdText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val softPinkBorder = Color(0xFFF48FB1)
    val softPinkButton = Color(0xFFF48FB1)

    LaunchedEffect(Unit) {
        // initial load
        viewModel.loadNextPage()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Posts",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userIdText,
                onValueChange = { input ->
                    userIdText = input.filter { it.isDigit() }
                },
                label = { Text("userId (opcional)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = softPinkBorder,
                    unfocusedIndicatorColor = softPinkBorder,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = softPinkBorder,
                    unfocusedLabelColor = softPinkBorder
                )
            )
            Button(
                onClick = {
                    val id = userIdText.trim().toIntOrNull()
                    viewModel.resetAndLoad(id)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = softPinkButton,
                    contentColor = Color.White
                )
            ) {
                Text("Filtrar")
            }
        }

        Text(
            text = if (userIdText.isBlank()) "Filtro atual: todos" else "Filtro atual: userId=${userIdText}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                PostItem(index = index + 1, title = post.title, body = post.body, userId = post.userId)
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Infinite scroll: when near end, load next page
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.totalItemsCount to listState.firstVisibleItemIndex }
            .collect { (total, firstVisible) ->
                // if within 5 items from end, load more
                if (total > 0 && firstVisible + listState.layoutInfo.visibleItemsInfo.size >= total - 5) {
                    viewModel.loadNextPage()
                }
            }
    }
}

@Composable
private fun PostItem(index: Int, title: String, body: String, userId: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F6))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "#${index}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                }
                Text(
                    text = "User ${userId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF424242)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
