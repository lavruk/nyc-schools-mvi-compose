package nyc.high.schools.viewmodel

import androidx.paging.PagingSource
import androidx.paging.PagingState
import nyc.high.schools.service.SchoolsService
import nyc.high.schools.service.models.SchoolResponseItem

class SchoolsPagingSource(
    val service: SchoolsService,
    val query: String,
    val mapper:(SchoolResponseItem) -> SchoolItem
) : PagingSource<Int, SchoolItem>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, SchoolItem> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 0
            val response = service.schools(query, limit = params.loadSize, offset = nextPageNumber * params.loadSize)
            return LoadResult.Page(
                data = response.map { mapper(it) },
                prevKey = null, // Only paging forward.
                nextKey = nextPageNumber + 1
            )
        } catch (e: Exception) {
            println("Error paging: ${e.message}")
            // Handle errors in this block and return LoadResult.Error if it is an
            // expected error (such as a network failure).
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SchoolItem>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}