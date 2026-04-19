package ui.compose

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Infrastructure test to verify the Compose testing framework is properly configured.
 * Uses Robolectric to run without an emulator (hybrid approach for CI/local dev).
 * 
 * Note: Uses createAndroidComposeRule<ComponentActivity>() which requires
 * an activity with LAUNCHER intent to be defined in the test manifest.
 */
@RunWith(AndroidJUnit4::class)
class InfrastructureTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun composeTestInfrastructure_isAvailable() {
        composeTestRule.setContent {
            MaterialTheme {
                Text("Test")
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun basic_compose_semantics_test() {
        composeTestRule.setContent {
            MaterialTheme {
                Text(text = "Infrastructure Test")
            }
        }
        composeTestRule.onNodeWithText("Infrastructure Test")
            .assertExists()
    }

    @Test
    fun verify_material3_components_render() {
        composeTestRule.setContent {
            MaterialTheme {
                Text(
                    text = "Material3 Theme Test",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        composeTestRule.onNodeWithText("Material3 Theme Test")
            .assertExists()
    }
}
