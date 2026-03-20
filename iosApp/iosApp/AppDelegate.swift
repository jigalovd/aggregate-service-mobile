import UIKit
import shared

/**
 * AppDelegate для iOS приложения.
 *
 * **Responsibilities:**
 * - Инициализация Config
 * - Инициализация Koin DI
 * - Настройка корневого ViewController
 */
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {

        // 1. Инициализируем Config
        initializeConfig()

        // 2. Инициализируем Koin DI
        KoinHelperKt.initializeKoin()

        // 3. Создаем окно
        window = UIWindow(frame: UIScreen.main.bounds)

        // 4. Настраиваем корневой ViewController
        // TODO: Создать ComposeViewController с MainScreen
        // Для начала используем простой UIViewController
        let viewController = UIViewController()
        viewController.view.backgroundColor = .white
        window?.rootViewController = viewController
        window?.makeKeyAndVisible()

        return true
    }

    /**
     * Инициализирует конфигурацию приложения.
     *
     * **IMPORTANT:** В production эти значения должны загружаться из:
     * - Info.plist
     * - Environment variables
     *
     * **Current:** Hardcoded для демонстрации
     */
    private func initializeConfig() {
        let config = AppConfig(
            apiBaseUrl: "\(Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String ?? "https://api.dev.aggregateservice.com")",
            apiKey: "\(Bundle.main.object(forInfoDictionaryKey: "API_KEY") as? String ?? "")",
            environmentString: "\(Bundle.main.object(forInfoDictionaryKey: "ENVIRONMENT") as? String ?? "DEV")",
            languageCode: Locale.current.languageCode ?? "ru",
            isDebug: (Bundle.main.object(forInfoDictionaryKey: "IS_DEBUG") as? Bool) ?? true,
            enableLogging: (Bundle.main.object(forInfoDictionaryKey: "ENABLE_LOGGING") as? Bool) ?? true,
            networkTimeoutMs: 30000,
            apiVersion: "\(Bundle.main.object(forInfoDictionaryKey: "API_VERSION") as? String ?? "v1")"
        )

        ConfigKt.initialize(config: config)
    }
}
