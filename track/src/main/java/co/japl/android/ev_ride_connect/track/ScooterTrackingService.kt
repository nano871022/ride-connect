package co.japl.android.ev_ride_connect.track

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.domain.MotionState
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
import co.japl.android.ev_ride_connect.core.ports.MotionDetectorPort
import co.japl.android.ev_ride_connect.core.ports.SessionStatePort
import co.japl.android.ev_ride_connect.core.ports.TripDatabasePort
import co.japl.android.ev_ride_connect.core.usecase.FetchEvInfoUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScooterTrackingService : Service() {

    @Inject
    lateinit var tripDatabasePort: TripDatabasePort

    @Inject
    lateinit var sessionStatePort: SessionStatePort

    @Inject
    lateinit var bleScooterPort: BleScooterPort

    @Inject
    lateinit var llmClientPort: LlmClientPort

    @Inject
    lateinit var evConfigPort: EvConfigPort

    @Inject
    lateinit var motionDetectorPort: MotionDetectorPort

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var trackingTracker: ScooterTrackingTracker
    private var previousMotionState: MotionState = MotionState.STOPPED

    override fun onCreate() {
        super.onCreate()
        trackingTracker = ScooterTrackingTracker(tripDatabasePort, sessionStatePort, serviceScope)
        createNotificationChannel()
        observeBleState()
        observeMotionState()
    }

    private fun observeBleState() {
        bleScooterPort.observeScooterState()
            .onEach { scooterState ->
                if (trackingTracker.isTracking.value && !trackingTracker.isPaused.value) {
                    trackingTracker.recordTelemetry(
                        x = 0.0,
                        y = 0.0,
                        speed = scooterState.currentSpeed.toDouble(),
                        distanceDelta = 0.01,
                        motionState = motionDetectorPort.motionState.value
                    )
                }
            }
            .launchIn(serviceScope)
    }

    private fun observeMotionState() {
        motionDetectorPort.motionState
            .onEach { newState ->
                val wasStopped = (previousMotionState == MotionState.STOPPED)
                val isStopped = (newState == MotionState.STOPPED)
                if (trackingTracker.isTracking.value && !trackingTracker.isPaused.value && wasStopped != isStopped) {
                    val loc = fetchCurrentLocation()
                    if (loc != null) {
                        trackingTracker.recordTelemetry(
                            x = loc.first,
                            y = loc.second,
                            speed = 0.0,
                            distanceDelta = 0.0,
                            motionState = newState
                        )
                    }
                }
                previousMotionState = newState
            }
            .launchIn(serviceScope)
    }

    private fun fetchCurrentLocation(): Pair<Double, Double>? {
        return try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return null

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: TrackingSettings.ACTION_START_TRACKING

        when (action) {
            TrackingSettings.ACTION_START_TRACKING -> {
                val notification = createNotification()
                startForegroundCompat(notification)
                motionDetectorPort.start()
                trackingTracker.startTracking()
            }
            TrackingSettings.ACTION_PAUSE_TRACKING -> {
                trackingTracker.pauseTracking()
            }
            TrackingSettings.ACTION_RESUME_TRACKING -> {
                trackingTracker.resumeTracking()
            }
            TrackingSettings.ACTION_STOP_TRACKING -> {
                serviceScope.launch {
                    motionDetectorPort.stop()
                    trackingTracker.stopTracking()
                    val session = sessionStatePort.getActiveSession()
                    if (session == null || (!session.isRideActive && !session.isLlmProcessing)) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
            TrackingSettings.ACTION_PROCESS_LLM_PROMPT -> {
                val notification = createNotification()
                startForegroundCompat(notification)
                val prompt = intent?.getStringExtra(TrackingSettings.EXTRA_PROMPT) ?: ""
                val modelName = intent?.getStringExtra(TrackingSettings.EXTRA_MODEL_NAME) ?: ""
                val apiKey = intent?.getStringExtra(TrackingSettings.EXTRA_API_KEY) ?: ""
                val template = intent?.getStringExtra(TrackingSettings.EXTRA_PROMPT_TEMPLATE)
                if (prompt.isNotBlank() && apiKey.isNotBlank()) {
                    processLlmPromptInBackground(prompt, modelName, apiKey, template)
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var types = 0
            val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (hasLocation) {
                types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            }
            if (hasBluetooth) {
                types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            }

            if (types != 0) {
                startForeground(TrackingSettings.NOTIFICATION_ID, notification, types)
            } else {
                startForeground(TrackingSettings.NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(TrackingSettings.NOTIFICATION_ID, notification)
        }
    }

    fun processLlmPromptInBackground(prompt: String, modelName: String, apiKey: String, template: String?) {
        serviceScope.launch(Dispatchers.IO) {
            val existing = sessionStatePort.getActiveSession() ?: ActiveSession()
            sessionStatePort.saveActiveSession(
                existing.copy(
                    pendingLlmPrompt = prompt,
                    isLlmProcessing = true,
                    lastUpdatedTmst = System.currentTimeMillis()
                )
            )

            try {
                val fetchEvInfoUseCase = FetchEvInfoUseCase(llmClientPort)
                val currentEvConfig = evConfigPort.getEvConfig() ?: EvConfig(request = prompt)
                val llmConfig = LlmConfig(
                    modelName = modelName,
                    selectedVersion = modelName,
                    apiKey = apiKey,
                    isActive = true
                )

                val updatedEvConfig = fetchEvInfoUseCase.execute(prompt, llmConfig, currentEvConfig, template)
                val savedId = evConfigPort.saveEvConfig(updatedEvConfig)

                val currentSession = sessionStatePort.getActiveSession() ?: ActiveSession()
                val finalSession = currentSession.copy(
                    pendingLlmResponse = "SUCCESS:$savedId",
                    isLlmProcessing = false,
                    lastUpdatedTmst = System.currentTimeMillis()
                )
                sessionStatePort.saveActiveSession(finalSession)
            } catch (e: Exception) {
                val currentSession = sessionStatePort.getActiveSession() ?: ActiveSession()
                val finalSession = currentSession.copy(
                    pendingLlmResponse = "ERROR:${e.localizedMessage ?: "FAILED"}",
                    isLlmProcessing = false,
                    lastUpdatedTmst = System.currentTimeMillis()
                )
                sessionStatePort.saveActiveSession(finalSession)
            } finally {
                val session = sessionStatePort.getActiveSession()
                if (session != null && !session.isRideActive) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        motionDetectorPort.stop()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TrackingSettings.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(Context.LOCATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, TrackingSettings.CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
