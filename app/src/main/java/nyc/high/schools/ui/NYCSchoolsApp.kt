package nyc.high.schools.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import nyc.high.schools.ui.theme.NYCHighSchoolsTheme

@Composable
fun NYCSchoolsApp(){
    NYCHighSchoolsTheme {
        val navController = rememberNavController()
        AppNavGraph(navController)
    }
}