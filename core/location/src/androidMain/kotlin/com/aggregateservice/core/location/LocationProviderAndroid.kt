package com.aggregateservice.core.location

import android.Manifest
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.aggregateservice.feature.catalog.domain.model.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENT")
actual class LocationProvider actual constructor() {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(
            (contextProvider.context as Activity)
        )

    private var contextProvider: ContextProvider = EmptyContextProvider
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    actual fun setActivity(activity: Any) {
        val act = activity as? Activity
            ?: throw IllegalArgumentException("Activity must be an Activity")
        contextProvider = AndroidContextProvider(act)

        val componentActivity = act as? ComponentActivity
            ?: throw IllegalArgumentException("Activity must be a ComponentActivity to use Activity Result API")

        permissionLauncher = componentActivity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle result - continuation is resumed in requestPermission
        }
    }

    actual suspend fun getCurrentLocation(accuracy: LocationAccuracy): Result<Location> =
        suspendCancellableCoroutine { continuation ->
            if (!continuation.isActive) return@suspendCancellableCoroutine

            val priority = when (accuracy) {
                LocationAccuracy.HIGH -> Priority.PRIORITY_HIGH_ACCURACY
                LocationAccuracy.MEDIUM -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
                LocationAccuracy.LOW -> Priority.PRIORITY_LOW_POWER
            }

            try {
                val activity = contextProvider.context as Activity
                val cancellationSignal = android.os.CancellationSignal()

                fusedClient.getCurrentLocation(priority, cancellationSignal)
                    .addOnSuccessListener { androidLocation ->
                        if (continuation.isActive) {
                            if (androidLocation != null) {
                                continuation.resume(
                                    Result.success(
                                        Location(
                                            latitude = androidLocation.latitude,
                                            longitude = androidLocation.longitude,
                                            address = "",
                                            city = ""
                                        )
                                    )
                                )
                            } else {
                                continuation.resume(
                                    Result.failure(Exception("Location is null"))
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(e))
                        }
                    }

                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }
            } catch (e: SecurityException) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
        }

    actual suspend fun requestPermission(): LocationPermissionStatus =
        suspendCancellableCoroutine { continuation ->
            val activity = contextProvider.context as Activity

            permissionLauncher?.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            // For simplicity, we check the permission status directly
            // The actual continuation resume happens via the launcher callback
            val fineGranted = activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseGranted = activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

            val status = when {
                fineGranted == Activity.PERMISSION_GRANTED || coarseGranted == Activity.PERMISSION_GRANTED ->
                    LocationPermissionStatus.Granted
                activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                    LocationPermissionStatus.Denied
                else -> LocationPermissionStatus.DeniedPermanently
            }

            if (continuation.isActive) {
                continuation.resume(status)
            }
        }

    private class EmptyContextProvider : ContextProvider {
        override val context: Any
            get() = throw IllegalStateException("Activity not set. Call setActivity() first.")
    }
}

actual object LocationProviderFactory {
    actual fun create(): LocationProvider = LocationProvider()
}