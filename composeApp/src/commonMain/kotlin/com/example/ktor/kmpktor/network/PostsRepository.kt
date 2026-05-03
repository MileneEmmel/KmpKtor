package com.example.ktor.kmpktor.network

import com.example.ktor.kmpktor.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PostsRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): List<Post> {
        val url = "https://jsonplaceholder.typicode.com/posts"
        return client.get(url) {
            parameter("_page", page)
            parameter("_limit", limit)
            if (userId != null) parameter("userId", userId)
        }.body()
    }
}

