package co.japl.android.ev_ride_connect.core.domain

data class LlmConfig(
    val id: Long = 0,
    val modelName: String = "",
    val apiKey: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val isActive: Boolean = true
)
