package co.japl.android.ev_ride_connect.track

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import co.japl.android.ev_ride_connect.core.domain.ActiveSession
import co.japl.android.ev_ride_connect.core.domain.EvConfig
import co.japl.android.ev_ride_connect.core.domain.LlmConfig
import co.japl.android.ev_ride_connect.core.ports.BleScooterPort
import co.japl.android.ev_ride_connect.core.ports.EvConfigPort
import co.japl.android.ev_ride_connect.core.ports.LlmClientPort
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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var trackingTracker: ScooterTrackingTracker

    override fun onCreate() {
        super.onCreate()
        trackingTracker = ScooterTrackingTracker(tripDatabasePort, sessionStatePort, serviceScope)
        createNotificationChannel()
        observeBleState()
    }

    private fun observeBleState() {
        bleScooterPort.observeScooterState()
            .onEach { scooterState ->
                if (trackingTracker.isTracking.value) {
                    trackingTracker.recordTelemetry(
                        x = 0.0,
                        y = 0.0,
                        speed = scooterState.currentSpeed.toDouble(),
                        distanceDelta = 0.01
                    )
                }
            }
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: TrackingSettings.ACTION_START_TRACKING

        when (action) {
            TrackingSettings.ACTION_START_TRACKING -> {
                val notification = createNotification()
                startForeground(TrackingSettings.NOTIFICATION_ID, notification)
                trackingTracker.startTracking()
            }
            TrackingSettings.ACTION_STOP_TRACKING -> {
                serviceScope.launch {
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
                startForeground(TrackingSettings.NOTIFICATION_ID, notification)
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
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
