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

@Serializable
object SummaryDestination

@Composable
fun LedgrNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ExpenseListDestination
    ) {
        composable<ExpenseListDestination> {
            ExpenseListDetail(
                onAddClickCompact = { navController.navigate(ExpenseFormDestination()) },
                onExpenseListCompact = { id ->
                    navController.navigate(ExpenseFormDestination(expenseId = id))
                },
                onSummaryClick = { navController.navigate(SummaryDestination) }
            )
        }
        composable<ExpenseFormDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<ExpenseFormDestination>()
            AddExpenseRoute(
                expenseId = destination.expenseId,
                onDone = { navController.popBackStack() }
            )
        }
        composable<SummaryDestination> {
            SummaryRoute(onBack = { navController.popBackStack() })
        }
    }
}
