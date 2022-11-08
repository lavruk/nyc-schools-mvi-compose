package nyc.high.schools.service.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolsListResponse(val data: List<SchoolResponseItem>)