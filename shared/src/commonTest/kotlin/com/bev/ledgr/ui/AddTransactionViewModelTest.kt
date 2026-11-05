package com.bev.ledgr.ui

import com.bev.ledgr.data.FakeTransactionRepository
import com.bev.ledgr.data.Transaction
import com.bev.ledgr.data.TransactionType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun existingTransaction(type: TransactionType = TransactionType.EXPENSE) = Transaction(
        id = 7,
        title = "Coffee",
        amountCents = 15075,
        category = "Food",
        dateMillis = 1000,
        type = type
    )

    @Test
    fun save_withEmptyFields_showsErrorsAndDoesNotSave() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = null
        )

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertNotNull(state.titleError)
        assertNotNull(state.amountError)
        assertFalse(state.isSaved)
        assertTrue(repository.current.isEmpty())
    }

    @Test
    fun save_withZeroAmount_showsAmountError() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = null
        )

        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("0")
        viewModel.onSave()

        assertEquals("Amount must be more than zero", viewModel.uiState.value.amountError)
        assertTrue(repository.current.isEmpty())
    }

    @Test
    fun editingFields_clearsErrors() = runTest {
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = FakeTransactionRepository(),
            type = null
        )

        viewModel.onSave()
        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("150")

        val state = viewModel.uiState.value
        assertNull(state.titleError)
        assertNull(state.amountError)
    }

    @Test
    fun save_withValidInput_addsExpense() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = null
        )

        viewModel.onTitleChange("Lunch")
        viewModel.onAmountChange("150.75")
        viewModel.onSave()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(1, repository.current.size)
        val saved = repository.current.first()
        assertEquals(15075L, saved.amountCents)
        assertEquals(TransactionType.EXPENSE, saved.type)
    }

    @Test
    fun save_trimsTitleAndStoresBlankNoteAsNull() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = null
        )

        viewModel.onTitleChange("  Lunch  ")
        viewModel.onAmountChange("150")
        viewModel.onNoteChange("   ")
        viewModel.onSave()

        val saved = repository.current.first()
        assertEquals("Lunch", saved.title)
        assertNull(saved.note)
    }

    @Test
    fun save_usesSelectedCategory() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = null
        )

        viewModel.onTitleChange("Bus fare")
        viewModel.onAmountChange("13")
        viewModel.onCategoryChange("Transport")
        viewModel.onSave()

        assertEquals("Transport", repository.current.first().category)
    }

    @Test
    fun newTransaction_withNullType_defaultsToExpense() = runTest {
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = FakeTransactionRepository(),
            type = null
        )

        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.type)
    }

    @Test
    fun newTransaction_withIncomeType_savesAsIncome() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = TransactionType.INCOME
        )

        viewModel.onTitleChange("Salary")
        viewModel.onAmountChange("25000")
        viewModel.onSave()

        assertEquals(TransactionType.INCOME, viewModel.uiState.value.type)
        assertEquals(TransactionType.INCOME, repository.current.first().type)
    }

    @Test
    fun editMode_usesLoadedTransactionType() = runTest {
        val repository = FakeTransactionRepository(
            listOf(existingTransaction(type = TransactionType.INCOME))
        )
        val viewModel = AddTransactionViewModel(
            transactionId = 7,
            repository = repository,
            type = null
        )

        viewModel.onAmountChange("200")
        viewModel.onSave()

        assertEquals(TransactionType.INCOME, viewModel.uiState.value.type)
        assertEquals(TransactionType.INCOME, repository.current.first().type)
    }

    @Test
    fun editMode_loadsExistingExpense() = runTest {
        val viewModel = AddTransactionViewModel(
            transactionId = 7,
            repository = FakeTransactionRepository(listOf(existingTransaction())),
            type = null
        )

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("Coffee", state.title)
        assertEquals("150.75", state.amount)
        assertEquals("Food", state.category)
        assertEquals(1000L, state.dateMillis)
    }

    @Test
    fun editMode_withMissingId_staysInCreateMode() = runTest {
        val viewModel = AddTransactionViewModel(
            transactionId = 99,
            repository = FakeTransactionRepository(listOf(existingTransaction())),
            type = null
        )

        val state = viewModel.uiState.value
        assertFalse(state.isEditing)
        assertEquals("", state.title)
    }

    @Test
    fun editMode_save_updatesInsteadOfDuplicating() = runTest {
        val repository = FakeTransactionRepository(listOf(existingTransaction()))
        val viewModel = AddTransactionViewModel(
            transactionId = 7,
            repository = repository,
            type = null
        )

        viewModel.onAmountChange("200")
        viewModel.onSave()

        assertEquals(1, repository.current.size)
        val saved = repository.current.first()
        assertEquals(7L, saved.id)
        assertEquals(20000L, saved.amountCents)
        assertEquals(1000L, saved.dateMillis)
    }

    @Test
    fun delete_removesExpense() = runTest {
        val repository = FakeTransactionRepository(listOf(existingTransaction()))
        val viewModel = AddTransactionViewModel(
            transactionId = 7,
            repository = repository,
            type = null
        )

        viewModel.onDelete()

        assertTrue(repository.current.isEmpty())
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun delete_inCreateMode_doesNothing() = runTest {
        val repository = FakeTransactionRepository(listOf(existingTransaction()))
        val viewModel = AddTransactionViewModel(
            transactionId = null,
            repository = repository,
            type = null
        )

        viewModel.onDelete()

        assertEquals(1, repository.current.size)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun parseAmountToCents_handlesValidAndInvalidInput() {
        assertEquals(15000L, parseAmountToCents("150"))
        assertEquals(15050L, parseAmountToCents("150.5"))
        assertEquals(15075L, parseAmountToCents(" 150.75 "))
        assertNull(parseAmountToCents(""))
        assertNull(parseAmountToCents("abc"))
        assertNull(parseAmountToCents("150.755"))
        assertNull(parseAmountToCents("-5"))
        assertNull(parseAmountToCents(".5"))
    }

    @Test
    fun centsToInput_formatsWithTwoDecimals() {
        assertEquals("150.75", centsToInput(15075))
        assertEquals("150.05", centsToInput(15005))
        assertEquals("0.00", centsToInput(0))
    }
}
