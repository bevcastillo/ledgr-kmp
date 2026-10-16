package com.bev.ledgr.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object ExpenseListDestination

@Serializable
object AddExpenseDestination

@Composable
fun LedgrNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ExpenseListDestination
    ) {
        composable<ExpenseListDestination> {
            ExpenseListRoute(
                onAddClick = { navController.navigate(AddExpenseDestination) }
            )
        }
        composable<AddExpenseDestination> {
            AddExpenseRoute(
                onDone = { navController.popBackStack() }
            )
        }
    }
}