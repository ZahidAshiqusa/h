package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun splashToDashboard_andPairingWorkspaceFlow_worksCorrectly() {
        // 1. Verify First-Start Splash Screen displays application name and subtitle
        composeTestRule.onNodeWithText("ZAID ASHIQ X ULTRA V. CODER").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Personal WhatsApp Automation Suite").assertIsDisplayed()

        // 2. Enter Main Dashboard (no login or signup screen)
        composeTestRule.onNodeWithTag("enter_dashboard_button").performClick()
        composeTestRule.onNodeWithTag("hero_connect_whatsapp_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hero_start_engine_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hero_settings_button").assertIsDisplayed()

        // 3. Navigate to Device Pairing Workspace and switch to Pairing Code tab
        composeTestRule.onNodeWithTag("hero_connect_whatsapp_button").performClick()
        composeTestRule.onNodeWithTag("tab_code_pairing").performClick()
        composeTestRule.onNodeWithTag("request_pairing_code_button").performScrollTo().assertIsDisplayed()
    }
}
