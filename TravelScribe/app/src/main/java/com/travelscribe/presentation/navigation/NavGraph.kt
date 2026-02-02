package com.travelscribe.presentation.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelscribe.core.common.Constants
import com.travelscribe.presentation.screens.addday.AddDayScreen
import com.travelscribe.presentation.screens.editlog.EditLogScreen
import com.travelscribe.presentation.screens.recording.RecordingScreen
import com.travelscribe.presentation.screens.timeline.TripTimelineScreen

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Trips : Screen(Constants.NavRoutes.TRIPS)
    data object TripDetail : Screen(Constants.NavRoutes.TRIP_DETAIL) {
        fun createRoute(tripId: Long) = "trip/$tripId"
    }
    data object AddDay : Screen("trip/{tripId}/day/{dayId}/add") {
        fun createRoute(tripId: Long, dayId: Long) = "trip/$tripId/day/$dayId/add"
    }
    data object DayDetail : Screen(Constants.NavRoutes.DAY_DETAIL) {
        fun createRoute(tripId: Long, dayId: Long) = "trip/$tripId/day/$dayId"
    }
    data object Recording : Screen(Constants.NavRoutes.RECORDING) {
        fun createRoute(tripId: Long, dayId: Long) = "trip/$tripId/day/$dayId/record"
    }
    data object LogDetail : Screen(Constants.NavRoutes.LOG_DETAIL) {
        fun createRoute(logId: Long) = "log/$logId"
    }
    data object Settings : Screen(Constants.NavRoutes.SETTINGS)
}

/**
 * Main navigation host for TravelScribe.
 */
@Composable
fun TravelScribeNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Trips.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Trips List Screen
        composable(route = Screen.Trips.route) {
            // TODO: TripsListScreen - navigate to first trip for now
            PlaceholderScreen("Trips List - Tap to go to Timeline") {
                // For demo, navigate to trip 1
                navController.navigate(Screen.TripDetail.createRoute(1L))
            }
        }

        // Trip Detail Screen (Timeline - main_code.html)
        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(
                navArgument(Constants.NavArgs.TRIP_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong(Constants.NavArgs.TRIP_ID) ?: return@composable
            TripTimelineScreen(
                tripId = tripId,
                onDayClick = { dayId ->
                    navController.navigate(Screen.DayDetail.createRoute(tripId, dayId))
                },
                onAddDayClick = {
                    // Navigate to add day screen (creates new day first)
                    navController.navigate(Screen.AddDay.createRoute(tripId, 0L))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onMapClick = { /* TODO: Map screen */ },
                onProfileClick = { /* TODO: Profile screen */ }
            )
        }

        // Add Day Screen (single_code.html)
        composable(
            route = Screen.AddDay.route,
            arguments = listOf(
                navArgument(Constants.NavArgs.TRIP_ID) { type = NavType.LongType },
                navArgument(Constants.NavArgs.DAY_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong(Constants.NavArgs.TRIP_ID) ?: return@composable
            val dayId = backStackEntry.arguments?.getLong(Constants.NavArgs.DAY_ID) ?: return@composable
            AddDayScreen(
                tripId = tripId,
                dayId = dayId,
                onBackClick = { navController.popBackStack() },
                onRecordClick = {
                    navController.navigate(Screen.Recording.createRoute(tripId, dayId))
                }
            )
        }

        // Day Detail Screen
        composable(
            route = Screen.DayDetail.route,
            arguments = listOf(
                navArgument(Constants.NavArgs.TRIP_ID) { type = NavType.LongType },
                navArgument(Constants.NavArgs.DAY_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong(Constants.NavArgs.TRIP_ID) ?: return@composable
            val dayId = backStackEntry.arguments?.getLong(Constants.NavArgs.DAY_ID) ?: return@composable
            // Navigate to Add Day for now (same UI for viewing/adding)
            AddDayScreen(
                tripId = tripId,
                dayId = dayId,
                onBackClick = { navController.popBackStack() },
                onRecordClick = {
                    navController.navigate(Screen.Recording.createRoute(tripId, dayId))
                }
            )
        }

        // Recording Screen (recording_code.html)
        composable(
            route = Screen.Recording.route,
            arguments = listOf(
                navArgument(Constants.NavArgs.TRIP_ID) { type = NavType.LongType },
                navArgument(Constants.NavArgs.DAY_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong(Constants.NavArgs.TRIP_ID) ?: return@composable
            val dayId = backStackEntry.arguments?.getLong(Constants.NavArgs.DAY_ID) ?: return@composable
            RecordingScreen(
                tripId = tripId,
                dayId = dayId,
                onCloseClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onRecordingComplete = { audioPath ->
                    // After recording, navigate to edit/review screen
                    // In real implementation, this would create a log and navigate to it
                    navController.popBackStack()
                    navController.navigate(Screen.LogDetail.createRoute(1L)) // Demo: log ID 1
                }
            )
        }

        // Log Detail/Edit Screen (edit_code.html)
        composable(
            route = Screen.LogDetail.route,
            arguments = listOf(
                navArgument(Constants.NavArgs.LOG_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val logId = backStackEntry.arguments?.getLong(Constants.NavArgs.LOG_ID) ?: return@composable
            EditLogScreen(
                logId = logId,
                onBackClick = { navController.popBackStack() },
                onShareClick = { /* TODO: Share functionality */ },
                onSaveClick = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(route = Screen.Settings.route) {
            PlaceholderScreen("Settings") {
                navController.popBackStack()
            }
        }
    }
}

/**
 * Placeholder screen for screens not yet implemented.
 */
@Composable
private fun PlaceholderScreen(
    name: String,
    onTap: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (onTap != null) Modifier.clickable(onClick = onTap) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TODO: $name",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
