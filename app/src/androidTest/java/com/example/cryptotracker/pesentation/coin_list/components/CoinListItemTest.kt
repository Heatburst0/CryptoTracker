package com.example.cryptotracker.pesentation.coin_list.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.cryptotracker.domain.model.Coin
import com.example.cryptotracker.presentation.coin_list.components.CoinListItem
import org.junit.Rule
import org.junit.Test

class CoinListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun coinListItem_displayCorrectContent(){
        val coin = Coin(
            id = "bitcoin",
            name = "Bitcoin",
            symbol = "BTC",
            currentPrice = 50000.0,
            marketCap = 123456789.0,
            priceChangePercentage24h = 2.5, // Positive change (Green)
            low24h = 49000.0,
            high24h = 51000.0,
            image = "http://image.url",
            isFavorite = false,
            )

        composeTestRule.setContent {
            CoinListItem(
                coin= coin
            ) { }
        }

        composeTestRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeTestRule.onNodeWithText("BTC").assertIsDisplayed()
        composeTestRule.onNodeWithText("$50000.0").assertIsDisplayed()
    }

    @Test
    fun coinListItem_clickTriggerCallback() {

        val coin = Coin(
            id = "ethereum",
            name = "Ethereum",
            symbol = "ETH",
            currentPrice = 3000.0,
            marketCap = 1.0, priceChangePercentage24h = -1.0,
            low24h = 1.0, high24h = 1.0, image = "", isFavorite = false
        )

        var isClicked = false
        composeTestRule.setContent {
            CoinListItem(coin=coin) {
                isClicked=true
            }
        }

        composeTestRule.onNodeWithText("Ethereum").performClick()

        assert(isClicked)
    }
}