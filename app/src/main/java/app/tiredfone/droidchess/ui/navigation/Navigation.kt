package app.tiredfone.droidchess.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.tiredfone.droidchess.ui.screens.BotSelectionScreen
import app.tiredfone.droidchess.ui.screens.GameReviewScreen
import app.tiredfone.droidchess.ui.screens.GameScreen
import app.tiredfone.droidchess.ui.screens.HomeScreen
import app.tiredfone.droidchess.ui.screens.LessonsScreen
import app.tiredfone.droidchess.ui.screens.PuzzlesScreen
import app.tiredfone.droidchess.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object BotSelection : Screen("bot_selection")
    object Game : Screen("game/{botId}") {
        fun createRoute(botId: Int) = "game/$botId"
    }
    object GameReview : Screen("game_review/{gameId}") {
        fun createRoute(gameId: Long) = "game_review/$gameId"
    }
    object Puzzles : Screen("puzzles")
    object Lessons : Screen("lessons")
    object Settings : Screen("settings")
}

@Composable
fun ChessNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPlayBot = { navController.navigate(Screen.BotSelection.route) },
                onPuzzles = { navController.navigate(Screen.Puzzles.route) },
                onLessons = { navController.navigate(Screen.Lessons.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.BotSelection.route) {
            BotSelectionScreen(
                onBotSelected = { botId ->
                    navController.navigate(Screen.Game.createRoute(botId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("botId") { type = NavType.IntType })
        ) { backStackEntry ->
            val botId = backStackEntry.arguments?.getInt("botId") ?: 1
            GameScreen(
                botId = botId,
                onBack = { navController.popBackStack() },
                onGameEnd = { gameId ->
                    navController.navigate(Screen.GameReview.createRoute(gameId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(
            route = Screen.GameReview.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L
            GameReviewScreen(
                gameId = gameId,
                onBack = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }}
            )
        }
        composable(Screen.Puzzles.route) {
            PuzzlesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Lessons.route) {
            LessonsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
