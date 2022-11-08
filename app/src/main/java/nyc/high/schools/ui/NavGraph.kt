package nyc.high.schools.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object AppDestinations {
    const val list = "list"
    const val details = "details"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.list
    ) {
        composable(AppDestinations.list) {
            SchoolListScreen(
                onNavigateToSchool = { school ->
                    navController.navigate("${AppDestinations.details}/${school.dbn}?name=${school.name}")
                },
            )
        }
        composable("${AppDestinations.details}/{dbn}?name={name}") { backStackEntry ->
            SchoolDetailsScreen(
                dbn = backStackEntry.arguments?.getString("dbn"),
                name = backStackEntry.arguments?.getString("name"),
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}