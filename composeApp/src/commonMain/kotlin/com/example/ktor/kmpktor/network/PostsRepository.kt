package com.example.ktor.kmpktor.network

import com.example.ktor.kmpktor.PostsError
import com.example.ktor.kmpktor.PostsErrorType
import com.example.ktor.kmpktor.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json

sealed class PostsResult {
    data class Success(val posts: List<Post>) : PostsResult()
    data class Failure(val error: PostsError) : PostsResult()
}

class PostsRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): PostsResult {
        val url = "https://jsonplaceholder.typicode.com/posts"
        return try {
            val response = client.get(url) {
                parameter("_page", page)
                parameter("_limit", limit)
                if (userId != null) parameter("userId", userId)
            }
            mapResponse(response)
        } catch (e: Throwable) {
            PostsResult.Failure(mapException(e))
        }
    }

    private suspend fun mapResponse(response: HttpResponse): PostsResult {
        val status = response.status
        if (status.value !in 200..299) {
            return PostsResult.Failure(mapStatus(status))
        }
        return PostsResult.Success(response.body())
    }

    private fun mapStatus(status: HttpStatusCode): PostsError {
        return when {
            status == HttpStatusCode.NotFound -> PostsError(
                type = PostsErrorType.NotFound,
                message = "Nao encontramos esse recurso (404). Verifique o filtro e tente novamente."
            )
            status.value >= 500 -> PostsError(
                type = PostsErrorType.Server,
                message = "O servidor apresentou instabilidade (${status.value}). Tente novamente em instantes."
            )
            status.value >= 400 -> PostsError(
                type = PostsErrorType.Client,
                message = "Requisicao invalida (${status.value}). Revise o userId e tente novamente."
            )
            else -> PostsError(
                type = PostsErrorType.Unknown,
                message = "Algo inesperado aconteceu (${status.value})."
            )
        }
    }

    private fun mapException(e: Throwable): PostsError {
        return when (e) {
            is IOException,
            is HttpRequestTimeoutException -> PostsError(
                type = PostsErrorType.Network,
                message = "Sem conexao ou tempo esgotado. Verifique a internet e toque em \"Tentar novamente\"."
            )
            else -> PostsError(
                type = PostsErrorType.Unknown,
                message = "Nao foi possivel carregar os posts agora. Tente novamente."
            )
        }
    }
}