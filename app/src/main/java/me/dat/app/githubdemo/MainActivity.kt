package me.dat.app.githubdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import me.dat.app.githubdemo.ui.repositories.RepositoriesScreen
import me.dat.app.githubdemo.ui.theme.GithubDemoTheme
import me.dat.app.githubdemo.ui.users.UsersScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            GithubDemoTheme {
                Surface {
                    NavHost(navController = navController, startDestination = "users") {
                        composable("users") {
                            UsersScreen(onUserClick = { user ->
                                navController.navigate("repositories/${user.username}")
                            })
                        }
                        composable(
                            "repositories/{username}",
                            arguments = listOf(navArgument("username") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""
                            RepositoriesScreen(username = username, onRepositoryClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.htmlUrl))
                                startActivity(intent)
                            }, onBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}
