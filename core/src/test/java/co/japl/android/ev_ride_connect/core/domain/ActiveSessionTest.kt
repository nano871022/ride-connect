package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class ActiveSessionTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateActiveSessionWithPodam() {
        val session = podamFactory.manufacturePojo(ActiveSession::class.java)

        assertThat(session).isNotNull
        assertThat(session.id).isNotNull
    }

    @Test
    fun shouldCreateDefaultActiveSession() {
        val session = ActiveSession()

        assertThat(session.id).isEqualTo(1L)
        assertThat(session.isRideActive).isFalse
        assertThat(session.isLlmProcessing).isFalse
    }
}
