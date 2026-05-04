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
    val uiState by viewModel.uiState.collectAsState()
    val posts = uiState.posts
    val isLoading = uiState.status is PostsStatus.Loading
    val error = (uiState.status as? PostsStatus.Error)?.error

    var userIdText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val softPinkBorder = Color(0xFFF2BFD9)
    val softPinkButton = Color(0xFFF2BFD9)

    LaunchedEffect(Unit) {
        viewModel.ensureLoaded()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "POSTS",
            style = MaterialTheme.typography.headlineMedium,
            color = softPinkButton,
            fontWeight = FontWeight.SemiBold,
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

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\u26A0",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFB71C1C)
                        )
                        Text(
                            text = error.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF424242)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = softPinkButton,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Tentar novamente")
                    }
                }
            }
        }

        if (!isLoading && posts.isEmpty() && error == null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\u2139",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "Nenhum resultado. Tente outro userId ou limpe o filtro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                }
            }
        }

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
    val cardBackground = Color(0xFFF8DEED)
    val cardBorder = Color(0xFFF8BBD0)
    val titleGray = Color(0xFF5A5A5A)
    val bodyGray = Color(0xFF7A7A7A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User ${userId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = bodyGray
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleGray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = bodyGray
            )
        }
    }
}