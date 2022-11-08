package nyc.high.schools.service.internal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import nyc.high.schools.service.SchoolsService
import nyc.high.schools.service.models.SchoolResponseItem
import nyc.high.schools.service.models.SchoolSATResultsResponseItem

class SchoolsServiceImpl : SchoolsService {

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    override suspend fun schools(query: String, limit: Int, offset: Int): List<SchoolResponseItem> =
        httpClient.get {
            url("https://data.cityofnewyork.us/resource/s3k6-pzi2.json")
            parameter("\$limit", limit)
            parameter("\$offset", offset)
        }.body()

    override suspend fun school(dbn: String): SchoolResponseItem {
        val results: List<SchoolResponseItem> = httpClient.get {
            url("https://data.cityofnewyork.us/resource/s3k6-pzi2.json")
            parameter("dbn", dbn)
        }.body()
        return results.firstOrNull() ?: error("Received empty response for requested $dbn")
    }

    override suspend fun satResults(dbn: String): SchoolSATResultsResponseItem {
        val results: List<SchoolSATResultsResponseItem> = httpClient.get {
            url("https://data.cityofnewyork.us/resource/f9bf-2cp4.json")
            parameter("dbn", dbn)
        }.body()
        return results.firstOrNull() ?: error("Received empty response for requested $dbn")
    }
}