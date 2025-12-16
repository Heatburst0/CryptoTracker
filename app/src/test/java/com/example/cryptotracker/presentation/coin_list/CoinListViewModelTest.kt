package com.example.cryptotracker.presentation.coin_list

import com.example.cryptotracker.MainCoroutineRule
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.model.Coin
import com.example.cryptotracker.domain.repository.CoinRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoinListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: CoinListViewModel
    private val repository : CoinRepository = mockk(relaxed = true)

    @Before
    fun setUp(){
        viewModel = CoinListViewModel(repository)
    }

    @Test
    fun `onSearch with valid query updates state with success`() =runTest{

        // 1. Mock Data
        val coins = listOf(
            Coin(
                id = "btc",
                name = "Bitcoin",
                symbol = "BTC",
                currentPrice = 50000.0,
                marketCap = 1.0,
                priceChangePercentage24h = 1.0,
                low24h = 1.0,
                high24h = 1.0,
                image = "",
                isFavorite = false,)
        )

        coEvery{ repository.searchCoins("btc")} returns flowOf(
            Resource.Loading(true),
            Resource.Success(coins),
            Resource.Loading(false)
        )


        viewModel.onSearch("btc")
        // Advance the test clock by 600ms to pass the debounce delay
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.coins).hasSize(1)
        assertThat(state.coins.first().name).contains("Bitcoin")
        assertThat(state.isLoading).isFalse()
        assertThat(state.searchQuery).isEqualTo("btc")
    }
}