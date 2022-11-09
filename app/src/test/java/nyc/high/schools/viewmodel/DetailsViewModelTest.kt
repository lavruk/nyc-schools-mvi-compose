package nyc.high.schools.viewmodel

import io.ktor.client.plugins.*
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nyc.high.schools.service.SchoolsService
import nyc.high.schools.service.models.SchoolResponseItem
import nyc.high.schools.service.models.SchoolSATResultsResponseItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DetailsViewModelTest {

    @MockK
    lateinit var service: SchoolsService

    lateinit var viewModel: DetailsViewModel

    init {
        MockKAnnotations.init(this)
    }

    val dbn = "testDbn"
    val testSatResponse = SchoolSATResultsResponseItem(
        dbn = dbn,
        school_name = "tes name",
        num_of_sat_test_takers = "20",
        sat_critical_reading_avg_score = "300",
        sat_math_avg_score = "200",
        sat_writing_avg_score = "400"
    )
    val testSchoolResponse = SchoolResponseItem(dbn = dbn, school_name = "test name", location = "1 Hacker way, CA")

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        coEvery { service.satResults(any()) } returns testSatResponse
        coEvery { service.school(any()) } returns testSchoolResponse

        viewModel = DetailsViewModel(service = service)
    }

    @Test
    fun `verify requests sat results`() = runTest {
        // setup:
        // when:
        viewModel.onIntent(SchoolDetailsIntent.Init(dbn))
        advanceUntilIdle()
        // then:
        coVerify {
            service.satResults(dbn)
        }
    }

    @Test
    fun `verify requests school details`() = runTest {
        // setup:
        // when:
        viewModel.onIntent(SchoolDetailsIntent.Init(dbn))
        advanceUntilIdle()
        // then:
        coVerify {
            service.school(dbn)
        }
    }

    @Test
    fun `verify state has sat results`() = runTest {
        // setup:
        // when:
        viewModel.onIntent(SchoolDetailsIntent.Init(dbn))
        advanceUntilIdle()
        // then:
        val state = viewModel.state.value
        assertEquals(state.satScores?.mathAvgScore, testSatResponse.sat_math_avg_score)
        assertEquals(state.satScores?.readingAvgScore, testSatResponse.sat_critical_reading_avg_score)
        assertEquals(state.satScores?.writingAvgScore, testSatResponse.sat_writing_avg_score)
    }

    @Test
    fun `verify error is returned in the state on request failure`() = runTest {
        // setup:
        coEvery { service.satResults(any()) } throws mockk<ClientRequestException>()
        // when:
        viewModel.onIntent(SchoolDetailsIntent.Init(dbn))
        advanceUntilIdle()
        // then:
        val state = viewModel.state.value
        assertEquals(state.error, "Failed to load sat results")
    }
}