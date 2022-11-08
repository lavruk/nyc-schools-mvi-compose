package nyc.high.schools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import nyc.high.schools.service.SchoolsService
import nyc.high.schools.service.internal.SchoolsServiceImpl
import nyc.high.schools.service.models.SchoolResponseItem
import nyc.high.schools.service.models.SchoolSATResultsResponseItem

class DetailsViewModel(
    private val service: SchoolsService = SchoolsServiceImpl()
) : ViewModel() {

    private val intents = MutableSharedFlow<SchoolDetailsIntent>(replay = 10)
    private val _state = MutableStateFlow(
        value = SchoolDetailsState(
            name = null,
            error = null,
            loadingScores = true,
            satScores = null,
            loadingDescription = true,
            description = null
        )
    )
    private val _event = MutableSharedFlow<SchoolDetailsEvent>(extraBufferCapacity = 10)

    init {
        // async requests
        combine(
            intents.filterIsInstance<SchoolDetailsIntent.Init>(),
            intents.filterIsInstance<SchoolDetailsIntent.Retry>()
                .onStart { emit(SchoolDetailsIntent.Retry) }
        ) { init, _ -> init.dbn }.flatMapLatest { dbn ->
            merge(
                flow {
                    kotlinx.coroutines.delay(500)
                    try {
                        emit(SchoolDetailsIntent.SchoolSatResults(service.satResults(dbn)))
                    } catch (err: ClientRequestException) {
                        emit(SchoolDetailsIntent.Error("Failed to load sat results"))
                    }
                },
                flow {
                    kotlinx.coroutines.delay(1500)
                    try {
                        emit(SchoolDetailsIntent.SchoolDetails(service.school(dbn)))
                    } catch (err: ClientRequestException) {
                        emit(SchoolDetailsIntent.Error("Failed to load school details"))
                    }
                })
        }.catch<SchoolDetailsIntent> { err ->
            emit(SchoolDetailsIntent.Error("Failed to load the data"))
        }.onEach { intents.emit(it) }.launchIn(viewModelScope)

        // events
        intents.filterIsInstance<SchoolDetailsIntent.Navigate>().onEach {
            _state.value.description?.location?.let {
                _event.emit(SchoolDetailsEvent.Navigate(location = it))
            }
        }.launchIn(viewModelScope)

        // reducer
        intents.scan(_state.value) { state, intent ->
            reduce(state, intent)
        }.onEach {
            _state.value = it
        }.launchIn(viewModelScope)
    }

    private fun reduce(state: SchoolDetailsState, intent: SchoolDetailsIntent): SchoolDetailsState {
        return when (intent) {
            is SchoolDetailsIntent.SchoolSatResults -> state.copy(
                name = intent.results.school_name,
                loadingScores = false,
                satScores = SchoolSATScores(
                    numberOfSatTestTakers = intent.results.num_of_sat_test_takers,
                    readingAvgScore = intent.results.sat_critical_reading_avg_score,
                    mathAvgScore = intent.results.sat_math_avg_score,
                    writingAvgScore = intent.results.sat_writing_avg_score
                )
            )
            is SchoolDetailsIntent.SchoolDetails -> state.copy(
                loadingDescription = false,
                description = SchoolDescription(
                    overview = intent.school.overview_paragraph,
                    location = intent.school.location,
                    phoneNumber = intent.school.phone_number,
                    faxNumber = intent.school.fax_number,
                    email = intent.school.school_email,
                    website = intent.school.website,
                )
            )
            is SchoolDetailsIntent.Error -> state.copy(error = intent.error)
            // no-op
            SchoolDetailsIntent.Navigate -> state
            is SchoolDetailsIntent.Init -> state
            SchoolDetailsIntent.Retry -> state.copy(error = null)
        }
    }

    val state: StateFlow<SchoolDetailsState>
        get() = _state

    val events: Flow<SchoolDetailsEvent>
        get() = _event

    fun onIntent(intent: SchoolDetailsIntent) {
        intents.tryEmit(intent)
    }

}

sealed class SchoolDetailsIntent {
    // public intent
    data class Init(val dbn: String) : SchoolDetailsIntent()
    object Navigate : SchoolDetailsIntent()
    object Retry : SchoolDetailsIntent()

    // internal intent
    data class SchoolSatResults(val results: SchoolSATResultsResponseItem) : SchoolDetailsIntent()
    data class SchoolDetails(val school: SchoolResponseItem) : SchoolDetailsIntent()
    data class Error(val error: String) : SchoolDetailsIntent()
}

sealed class SchoolDetailsEvent {
    data class Navigate(val location: String) : SchoolDetailsEvent()
}

data class SchoolSATScores(
    val numberOfSatTestTakers: String,
    val readingAvgScore: String,
    val mathAvgScore: String,
    val writingAvgScore: String
)

data class SchoolDescription(
    val overview: String?,
    val location: String?,
    val phoneNumber: String?,
    val faxNumber: String?,
    val email: String?,
    val website: String?,
)

data class SchoolDetailsState(
    val name: String?,
    val error: String?,
    val loadingScores: Boolean,
    val loadingDescription: Boolean,
    val satScores: SchoolSATScores?,
    val description: SchoolDescription?
)