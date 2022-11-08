package nyc.high.schools.service

import nyc.high.schools.service.models.SchoolResponseItem
import nyc.high.schools.service.models.SchoolSATResultsResponseItem

interface SchoolsService {
    suspend fun schools(query: String, limit: Int, offset: Int): List<SchoolResponseItem>
    suspend fun satResults(dbn: String): SchoolSATResultsResponseItem
    suspend fun school(dbn: String): SchoolResponseItem
}