package com.example.ktor.kmpktor

import com.example.ktor.kmpktor.model.Post

// UI state exposed by the ViewModel to keep list + loading/error status in sync.
data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val status: PostsStatus = PostsStatus.Idle,
    val canLoadMore: Boolean = true,
    val currentUserId: Int? = null
)

sealed class PostsStatus {
    object Idle : PostsStatus()
    object Loading : PostsStatus()
    object Success : PostsStatus()
    data class Error(val error: PostsError) : PostsStatus()
}

data class PostsError(
    val type: PostsErrorType,
    val message: String
)

enum class PostsErrorType {
    Network,
    NotFound,
    Server,
    Client,
    Unknown
}
