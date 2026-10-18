package app.saathi.trip

import app.saathi.core.Fix
import app.saathi.core.Mode
import app.saathi.core.TripEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class Place(val city: String? = null, val state: String? = null, val countryCode: String? = null, val label: String? = null)

data class UiState(
    val guarding: Boolean = false,
    val onTrip: Boolean = false,
    val mode: Mode? = null,
    val app: String? = null,
    val plate: String? = null,
    val driver: String? = null,
    val events: List<TripEvent> = emptyList(),
    val lastFix: Fix? = null,
    val lastFixAtMs: Long = 0,
    val place: Place = Place(),
    val cityOverride: String? = null,
    val checkInEndsAtMs: Long? = null,
    val alertActive: Boolean = false,
    val told: List<String> = emptyList(),
    val smsFailed: List<String> = emptyList(),
    val recording: Boolean = false,
    val plateCheck: String? = null,
)

/** One shared state for the service, the screens, the widget. */
object TripState {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui
    fun update(f: (UiState) -> UiState) = _ui.update(f)
    val now get() = _ui.value

    fun city(): String? = now.cityOverride ?: now.place.city
}
