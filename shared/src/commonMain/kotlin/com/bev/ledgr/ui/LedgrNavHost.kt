package com.bev.ledgr.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bev.ledgr.data.Category
import com.bev.ledgr.data.TransactionType
import kotlinx.serialization.Serializable

@Serializable
object HomeDestination

@Serializable
data class TransactionFormDestination(val expenseId: Long? = null, val type: String? = null)

@Serializable
object SummaryDestination

// type = null means All Transactions for the selected month/year
@Serializable
data class TransactionListDestination(
    val type: String? = null,
    val category: Category? = null,
    val year: Int,
    val month: Int
)

@Composable
fun LedgrNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeDestination
    ) {
        composable<HomeDestination> {
            TransactionListDetail(
                onAddClickCompact = { type ->
                    navController.navigate(TransactionFormDestination(type = type.name))
                },
                onExpenseListCompact = { id ->
                    navController.navigate(TransactionFormDestination(expenseId = id))
                },
                onSummaryClick = { navController.navigate(SummaryDestination) },
                onTransactionClick = { type, category, year, month ->
                    navController.navigate(
                        TransactionListDestination(
                            type = type?.name,
                            category = category,
                            year = year,
                            month = month
                        )
                    )
                }
            )
        }
        composable<TransactionFormDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<TransactionFormDestination>()
            AddTransactionRoute(
                transactionId = destination.expenseId,
                type = destination.type?.let { TransactionType.valueOf(it) },
                onDone = { navController.popBackStack() }
            )
        }
        composable<SummaryDestination> {
            SummaryRoute(
                onBack = { navController.popBackStack() },
                onSeeAllTransactions = { type, category, year, month ->
                    navController.navigate(
                        TransactionListDestination(
                            type = type?.name,
                            category = category,
                            year = year,
                            month = month
                        )
                    )
                },
                onCategoryClick = { }
            )
        }
        composable<TransactionListDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<TransactionListDestination>()
            TransactionListRoute(
                type = destination.type?.let { TransactionType.valueOf(it) },
                year = destination.year,
                month = destination.month,
                category = destination.category,
                onTransactionClick = { id ->
                    navController.navigate(TransactionFormDestination(expenseId = id))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
