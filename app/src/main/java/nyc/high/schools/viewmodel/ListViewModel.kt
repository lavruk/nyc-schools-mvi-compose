package nyc.high.schools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import nyc.high.schools.service.SchoolsService
import nyc.high.schools.service.internal.SchoolsServiceImpl

class ListViewModel(
    private val service: SchoolsService = SchoolsServiceImpl(),
) : ViewModel() {

    val state = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = {
            SchoolsPagingSource(service, query = "") {
                SchoolItem(
                    dbn = it.dbn,
                    name = it.school_name,
                    favIcon = "https://www.google.com/s2/favicons?domain=${it.website}&sz=32",
                    location = "${it.location}"
                )
            }
        }).flow.cachedIn(viewModelScope)
}

data class SchoolItem(val dbn: String, val name: String, val location: String, val favIcon: String)

data class ListState(val items: List<SchoolItem>)