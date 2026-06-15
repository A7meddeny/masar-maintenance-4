package com.masar.maintenance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.masar.maintenance.data.Net
import com.masar.maintenance.ui.screens.*
import com.masar.maintenance.ui.theme.MasarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MasarTheme {
                val nav = rememberNavController()
                val start = if (Net.session.isLoggedIn) "home" else "login"
                NavHost(navController = nav, startDestination = start) {

                    composable("login") {
                        LoginScreen(onLoggedIn = {
                            nav.navigate("home") { popUpTo("login") { inclusive = true } }
                        })
                    }

                    composable("home") { HomeScreen(nav) }

                    composable(
                        "requests?scope={scope}",
                        arguments = listOf(navArgument("scope") { defaultValue = "" })
                    ) { back ->
                        RequestsScreen(nav, back.arguments?.getString("scope") ?: "")
                    }

                    composable(
                        "request/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { back ->
                        RequestDetailScreen(nav, back.arguments?.getInt("id") ?: 0)
                    }

                    composable("newRequest") { NewRequestScreen(nav) }
                    composable("periodic") { PeriodicScreen(nav) }

                    composable("cars") { CarsScreen(nav) }

                    composable(
                        "carForm?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })
                    ) { back ->
                        CarFormScreen(nav, back.arguments?.getInt("id") ?: 0)
                    }

                    composable(
                        "carHistory/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { back ->
                        CarHistoryScreen(nav, back.arguments?.getInt("id") ?: 0)
                    }

                    composable("companies") { CompaniesScreen(nav) }
                    composable("employees") { EmployeesScreen(nav) }
                    composable("staff") { StaffScreen(nav) }
                    composable("dashboard") { DashboardScreen(nav) }
                }
            }
        }
    }
}
