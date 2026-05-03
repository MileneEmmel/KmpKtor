package com.example.ktor.kmpktor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Entry composable for the app. Uses the PostsScreen implemented in commonMain.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        PostsScreen()
    }
}

