package com.bev.ledgr.di

import com.bev.ledgr.data.AppDatabase
import com.bev.ledgr.data.TransactionRepository
import com.bev.ledgr.data.TransactionRepositoryImpl
import com.bev.ledgr.data.TransactionType
import com.bev.ledgr.data.getRoomDatabase
import com.bev.ledgr.ui.AddTransactionViewModel
import com.bev.ledgr.ui.SummaryViewModel
import com.bev.ledgr.ui.TransactionListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().transactionDao() }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    viewModel { TransactionListViewModel(repository = get()) }
    viewModel { (transactionId: Long?, type: TransactionType?) ->
        AddTransactionViewModel(
            transactionId = transactionId,
            type = type,
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
