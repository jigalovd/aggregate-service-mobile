package com.aggregateservice.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.location.LocationProvider
import com.aggregateservice.app.navigation.AppBottomNavHost
import com.aggregateservice.core.theme.appTheme
import com.aggregateservice.feature.catalog.presentation.screen.CatalogScreen
import org.koin.java.KoinJavaComponent.inject
import androidx.activity.enableEdgeToEdge

/**
 * Главная Activity для Android приложения.
 *
 * **Architecture:**
 * - Entry point для Android
 * - Использует Compose для UI
 * - Навигация через Voyager (AppNavHost)
 * - Start screen = CatalogScreen (guest mode by default)
 *
 * **Guest Mode:**
 * - Unregistered users can browse catalog
 * - Registration required for booking/reviews
 */
class MainActivity : ComponentActivity() {

    private val i18nProvider: I18nProvider by inject(I18nProvider::class.java)
    private val locationProvider: LocationProvider by inject(LocationProvider::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Set Activity for LocationProvider per D-07
        locationProvider.setActivity(this)

        setContent {
            appTheme(
                languageCode = i18nProvider.currentLocale.code,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AppBottomNavHost(
                        startScreen = CatalogScreen,
                    )
                }
            }
        }
    }
}
