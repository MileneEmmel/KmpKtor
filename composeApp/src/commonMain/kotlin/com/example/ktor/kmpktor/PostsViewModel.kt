package com.example.ktor.kmpktor

import com.example.ktor.kmpktor.model.Post
import com.example.ktor.kmpktor.network.PostsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PostsViewModel(
    private val repo: PostsRepository = PostsRepository(),
    private val pageLimit: Int = 20
) {
    val posts = MutableStateFlow<List<Post>>(emptyList())
    val isLoading = MutableStateFlow(false)

    private var page = 1
    private var hasMore = true
    private var currentUserId: Int? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun resetAndLoad(userId: Int?) {
        currentUserId = userId
        page = 1
        hasMore = true
        posts.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading.value || !hasMore) return
        isLoading.value = true
        scope.launch {
            try {
                val result = repo.getPosts(page = page, limit = pageLimit, userId = currentUserId)
                if (result.isEmpty()) {
                    hasMore = false
                } else {
                    posts.value = posts.value + result
                    page += 1
                }
            } catch (_: Exception) {
                // ignore for now; could expose an error state
            } finally {
                isLoading.value = false
            }
        }
    }
}

