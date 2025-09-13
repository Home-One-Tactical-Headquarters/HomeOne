package dk.holonet.ui.editor.borderpane

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import dk.holonet.core.Position
import dk.holonet.ui.editor.EditorViewModel
import dk.holonet.ui.editor.borderpane.component.ColumnComponent
import dk.holonet.ui.editor.borderpane.component.RowComponent
import dk.holonet.ui.fromNavigatorString
import dk.holonet.ui.isVertical
import dk.holonet.ui.toNavigatorString

@Composable
fun BorderPaneScreen(
    modifier: Modifier,
    viewModel: EditorViewModel,
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()
    val state by viewModel.positions.collectAsState()

    NavHost(
        navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(route = Screen.Main.route) {
            BorderPane(
                onPositionClick = { position ->
                    viewModel.setCurrentPosition(position)
                    navController.navigate("/config/${position.toNavigatorString()}")
                }
            )
        }

        composable(
            Screen.Config.ROUTE_PATTERN,
            arguments = listOf(navArgument("position") { type = NavType.StringType }),
            enterTransition = {
                scaleIn(
                    animationSpec = tween(300),
                    initialScale = 0.8f
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                scaleOut(
                    animationSpec = tween(300),
                    targetScale = 0.8f
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val positionString = backStackEntry.arguments?.read { getString("position") }
            val position = positionString?.fromNavigatorString() ?: Position.TOP_CENTER

            // Clear current position when leaving this screen
            DisposableEffect(Unit) {
                onDispose {
                    viewModel.setCurrentPosition(null)
                }
            }

            if (position.isVertical()) {
                ColumnComponent(
                    viewModel = viewModel,
                    position = position,
                    state = state
                )
            } else {
                RowComponent(
                    viewModel,
                    modifier = Modifier,
                    position,
                    state
                )
            }
        }
    }

    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }
}