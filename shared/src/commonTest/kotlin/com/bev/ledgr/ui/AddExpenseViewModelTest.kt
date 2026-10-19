package com.bev.ledgr.ui

import com.bev.ledgr.data.Expense
import com.bev.ledgr.data.FakeExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun save_withEmptyFields_showsErrorsAndDoesNotSave() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = AddExpenseViewModel(expenseId = null, repository = repository)

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertNotNull(state.titleError)
        assertNotNull(state.amountError)
        assertFalse(state.isSaved)
        assertTrue(repository.current.isEmpty())
    }

    @Test
    fun save_withValidInput_addsExpense() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = AddExpenseViewModel(expenseId = null, repository = repository)

        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("150.75")
        viewModel.onSave()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(1, repository.current.size)
        assertEquals(15075L, repository.current.first().amountCents)
    }

    @Test
    fun editMode_loadsExistingExpense() = runTest {
        val existing = Expense(id = 7, title = "Coffee", amountCents = 15075, category = "Food", dateMillis = 1000)
        val viewModel = AddExpenseViewModel(expenseId = 7, repository = FakeExpenseRepository(listOf(existing)))

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("Coffee", state.title)
        assertEquals("150.75", state.amount)
    }

    @Test
    fun editMode_save_updatesInsteadOfDuplicating() = runTest {
        val existing = Expense(id = 7, title = "Coffee", amountCents = 15075, category = "Food", dateMillis = 1000)
        val repository = FakeExpenseRepository(listOf(existing))
        val viewModel = AddExpenseViewModel(expenseId = 7, repository = repository)

        viewModel.onAmountChange("200")
        viewModel.onSave()

        assertEquals(1, repository.current.size)
        assertEquals(20000L, repository.current.first().amountCents)
        assertEquals(1000L, repository.current.first().dateMillis)
    }

    @Test
    fun delete_removeExpenses() = runTest {
        val existing = Expense(id = 7, title = "Coffee", amountCents = 15075, category = "Food", dateMillis = 1000)
        val repository = FakeExpenseRepository(listOf(existing))
        val viewModel = AddExpenseViewModel(expenseId = 7, repository = repository)

        viewModel.onDelete()

        assertTrue(repository.current.isEmpty())
        assertTrue(viewModel.uiState.value.isSaved)
    }
}