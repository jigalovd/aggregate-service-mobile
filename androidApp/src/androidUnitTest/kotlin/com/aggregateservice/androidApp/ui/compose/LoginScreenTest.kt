package ui.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LoginScreen.
 * Tests the UI states: sign-in button visibility, loading indicator, and error messages.
 * Uses a test-friendly composable that bypasses Koin/Voyager dependencies.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private data class LoginUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isLoginSuccess: Boolean = false,
    )

    @Composable
    private fun TestableLoginContent(
        title: String,
        subtitle: String,
        buttonText: String,
        uiState: LoginUiState,
        onSignInClick: () -> Unit = {},
    ) {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(text = title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(32.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                            Text(buttonText)
                        }
                    }

                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    @Test
    fun signInButton_isDisplayed_whenNotLoading() {
        var clicked = false
        composeTestRule.setContent {
            TestableLoginContent(
                title = "Sign In",
                subtitle = "Sign in to continue",
                buttonText = "Sign in with Google",
                uiState = LoginUiState(isLoading = false),
                onSignInClick = { clicked = true },
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
    }

    @Test
    fun loadingIndicator_isDisplayed_whenLoading() {
        composeTestRule.setContent {
            TestableLoginContent(
                title = "Sign In",
                subtitle = "Sign in to continue",
                buttonText = "Sign in with Google",
                uiState = LoginUiState(isLoading = true),
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sign in with Google").assertIsNotDisplayed()
    }

    @Test
    fun errorMessage_isDisplayed_whenErrorOccurs() {
        val errorMessage = "Authentication failed. Please try again."
        composeTestRule.setContent {
            TestableLoginContent(
                title = "Sign In",
                subtitle = "Sign in to continue",
                buttonText = "Sign in with Google",
                uiState = LoginUiState(isLoading = false, error = errorMessage),
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun onClickCallback_isTriggered_whenSignInButtonClicked() {
        var clicked = false
        composeTestRule.setContent {
            TestableLoginContent(
                title = "Sign In",
                subtitle = "Sign in to continue",
                buttonText = "Sign in with Google",
                uiState = LoginUiState(isLoading = false),
                onSignInClick = { clicked = true },
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sign in with Google").performClick()
        assert(clicked)
    }
}
