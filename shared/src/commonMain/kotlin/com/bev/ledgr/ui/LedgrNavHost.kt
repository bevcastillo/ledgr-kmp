package com.bev.ledgr.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
object ExpenseListDestination

@Serializable
data class ExpenseFormDestination(val expenseId: Long? = null)

@Composable
fun LedgrNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ExpenseListDestination
    ) {
        composable<ExpenseListDestination> {
            ExpenseListRoute(
                onAddClick = {
                    navController.navigate(ExpenseFormDestination())
                    println("Ledgr: =====> should do something here")
                },
                onExpenseClick = {
                    id -> navController.navigate(ExpenseFormDestination(expenseId = id))
                }
            )
        }
        composable<ExpenseFormDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ExpenseFormDestination>()
            AddExpenseRoute(
                expenseId = destination.expenseId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}