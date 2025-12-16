package com.example.cryptotracker.data.repository

import app.cash.turbine.test
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.data.local.CoinDao
import com.example.cryptotracker.data.remote.CoinGeckoApi
import com.example.cryptotracker.data.remote.dto.CoinDto
import com.example.cryptotracker.data.remote.dto.SearchCoinDto
import com.example.cryptotracker.data.remote.dto.SearchResponseDto
import com.google.common.truth.ExpectFailure.assertThat
import com.google.common.truth.Truth.assertThat
import io.mockk.awaits
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.channels.ChannelResult.Companion.success
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

class CoinRepositoryImplTest {

    private lateinit var repository: CoinRepositoryImpl
    private val api: CoinGeckoApi = mockk()
    private val dao : CoinDao = mockk(relaxed = true) // relaxed=true means void methods do nothing by default

    @Before
    fun setUp(){
        repository = CoinRepositoryImpl(api,dao)
    }

    @Test
    fun `searchCoins emit Success when API returns valid data`()= runTest {


        // Mock data
        val query ="bitcoin"
        val searchResponse = SearchResponseDto(
            coins = listOf(
                SearchCoinDto("bitcoin", "Bitcoin", "BTC", "thumb.url"),
                SearchCoinDto("bitcoin-cash", "Bitcoin Cash", "BCH", "thumb.url")
            )
        )

        val coinDto = mockk<CoinDto>(relaxed = true){
            coEvery { id } returns "bitcoin"
            coEvery {name} returns "Bitcoin"
        }

        val marketResponse = listOf(coinDto)


        coEvery { api.searchCoins(query) } returns searchResponse
        coEvery { api.getCoins(ids = "bitcoin,bitcoin-cash",
            currency = any(), order = any(), perPage = any(), page = any(), sparkline = any()
        ) } returns marketResponse
        coEvery { dao.getFavoriteCoinIds() } returns emptyList()
        coEvery { dao.insertCoins(any()) } just runs

        repository.searchCoins(query).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(1)
            assertThat(success.data!!.first().name).isEqualTo("Bitcoin")

            val finishLoading = awaitItem()
            assertThat(finishLoading).isInstanceOf(Resource.Loading::class.java)

            cancelAndIgnoreRemainingEvents()
        }

        //Verify
        coVerify(exactly = 1) {dao.insertCoins(any())}
    }

    @Test
    fun `searchCoins emits Error when API fails`() = runTest{
        coEvery{ api.searchCoins(any())} throws RuntimeException("Network Error")

        repository.searchCoins("btc").test {
            awaitItem()

            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).contains("Network Error")

            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}