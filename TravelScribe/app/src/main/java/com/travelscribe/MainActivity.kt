package com.travelscribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.travelscribe.presentation.navigation.TravelScribeNavHost
import com.travelscribe.presentation.theme.TravelScribeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity entry point for TravelScribe.
 * Uses Jetpack Compose for all UI with Navigation Compose for routing.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TravelScribeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TravelScribeNavHost()
                }
            }
        }
    }
}
