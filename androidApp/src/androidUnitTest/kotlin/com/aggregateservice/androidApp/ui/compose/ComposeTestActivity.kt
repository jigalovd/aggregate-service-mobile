package ui.compose

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import org.robolectric.annotation.Config

/**
 * Robolectric test Application class.
 * Provides a minimal application context for Compose UI tests.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

/**
 * Robolectric test Activity for Compose UI testing.
 * Provides a minimal activity context for Compose tests without requiring
 * the full application setup with Koin DI.
 */
class ComposeTestActivity : ComponentActivity()
