package com.example.ktor.kmpktor

import com.example.ktor.kmpktor.network.PostsRepository
import com.example.ktor.kmpktor.network.PostsResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostsViewModel(
    private val repo: PostsRepository = PostsRepository(),
    private val pageLimit: Int = 20
) {
    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState

    private var page = 1
    private var hasMore = true
    private var currentUserId: Int? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun ensureLoaded() {
        if (_uiState.value.posts.isEmpty() && _uiState.value.status !is PostsStatus.Loading) {
            loadNextPage(force = true)
        }
    }

    fun resetAndLoad(userId: Int?) {
        currentUserId = userId
        page = 1
        hasMore = true
        _uiState.value = PostsUiState(
            posts = emptyList(),
            status = PostsStatus.Idle,
            canLoadMore = true,
            currentUserId = userId
        )
        loadNextPage(force = true)
    }

    fun retry() {
        loadNextPage(force = true)
    }

    fun loadNextPage(force: Boolean = false) {
        val state = _uiState.value
        if (state.status is PostsStatus.Loading) return
        if (!hasMore) return
        if (!force && state.status is PostsStatus.Error) return

        _uiState.value = state.copy(status = PostsStatus.Loading)

        scope.launch {
            when (val result = repo.getPosts(page = page, limit = pageLimit, userId = currentUserId)) {
                is PostsResult.Success -> {
                    val existingPosts = _uiState.value.posts
                    val newPosts = existingPosts + result.posts
                    if (result.posts.isEmpty()) {
                        hasMore = false
                    } else {
                        page += 1
                    }
                    _uiState.value = _uiState.value.copy(
                        posts = newPosts,
                        status = PostsStatus.Success,
                        canLoadMore = hasMore,
                        currentUserId = currentUserId
                    )
                }
                is PostsResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        status = PostsStatus.Error(result.error),
                        canLoadMore = hasMore,
                        currentUserId = currentUserId
                    )
                }
            }
        }
    }
}