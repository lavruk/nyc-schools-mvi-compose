package nyc.high.schools.service.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolSATResultsResponseItem(
    val dbn: String,
    val school_name: String,
    val num_of_sat_test_takers: String,
    val sat_critical_reading_avg_score: String,
    val sat_math_avg_score: String,
    val sat_writing_avg_score: String
)