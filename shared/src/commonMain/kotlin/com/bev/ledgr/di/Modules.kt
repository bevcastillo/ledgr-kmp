package com.bev.ledgr.di

import com.bev.ledgr.data.AppDatabase
import com.bev.ledgr.data.ExpenseRepository
import com.bev.ledgr.data.ExpenseRepositoryImpl
import com.bev.ledgr.data.getRoomDatabase
import com.bev.ledgr.ui.AddExpenseViewModel
import com.bev.ledgr.ui.ExpenseListViewModel
import com.bev.ledgr.ui.SummaryViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().expenseDao() }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    viewModelOf(::ExpenseListViewModel)
    viewModel { params ->
        AddExpenseViewModel(
            expenseId = params.getOrNull(),
            repository = get()
        )
    }
    viewModel { SummaryViewModel(repository = get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, platformModule)
    }
}
