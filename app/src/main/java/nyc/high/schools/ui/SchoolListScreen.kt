package nyc.high.schools.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import nyc.high.schools.R
import nyc.high.schools.viewmodel.ListViewModel
import nyc.high.schools.viewmodel.SchoolItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolListScreen(
    viewModel: ListViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToSchool: (SchoolItem) -> Unit
) {
    val lazyPagingItems = viewModel.state.collectAsLazyPagingItems()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            HomeTopAppBar(
                topAppBarState = topAppBarState
            )
        },
        modifier = modifier
    ) { innerPadding ->

        val contentModifier = Modifier
            .padding(innerPadding)
            .nestedScroll(scrollBehavior.nestedScrollConnection)

        LazyColumn(modifier = contentModifier) {
            items(
                items = lazyPagingItems,
                key = { it.dbn }
            ) { school ->
                if (school != null) {
                    SchoolListItem(school) {
                        onNavigateToSchool(school)
                    }
                } else {
                    SchoolItemPlaceholder()
                }
            }

            with(lazyPagingItems) {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { LoadingView(modifier = Modifier.fillParentMaxSize()) }
                    }
                    loadState.append is LoadState.Loading -> {
                        item { LoadingItem() }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val e = loadState.refresh as LoadState.Error
                        item {
                            ErrorItem(
                                message = e.error.localizedMessage!!,
                                modifier = Modifier.fillParentMaxSize(),
                                onClickRetry = { retry() }
                            )
                        }
                    }
                    lazyPagingItems.loadState.append is LoadState.Error -> {
                        val e = lazyPagingItems.loadState.append as LoadState.Error
                        item {
                            ErrorItem(
                                message = e.error.localizedMessage!!,
                                onClickRetry = { retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SchoolListItem(item: SchoolItem, onClick: (String) -> Unit) {

    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .logger(DebugLogger())
        .components {
            add(SvgDecoder.Factory())
        }.build()

    println(item.favIcon)

    Row(modifier = Modifier
        .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
        .clickable {
            onClick(item.dbn)
        }) {
        AsyncImage(
            model = item.favIcon,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(32.dp),
            imageLoader = imageLoader
        )
        Column(modifier = Modifier
            .padding(vertical = 6.dp)
            .weight(1f)) {
            Text(text = item.name, modifier = Modifier,
                style = MaterialTheme.typography.titleMedium)

            Row(modifier = Modifier.padding(top = 4.dp)) {
                Icon(
                    painter =  rememberVectorPainter(image = Icons.Outlined.Place),
                    contentDescription = null, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                Text(text = item.location, modifier = Modifier,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Divider(
        modifier = Modifier.padding(start = 48.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}

@Composable
fun SchoolItemPlaceholder() {
    Row(modifier = Modifier.defaultMinSize(minHeight = 40.dp)) {
        Text(text = "Loading...")
    }
}


@Composable
fun LoadingView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoadingItem() {
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

@Composable
fun ErrorItem(
    message: String,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Red
        )
        OutlinedButton(onClick = onClickRetry) {
            Text(text = "Try again")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    modifier: Modifier = Modifier,
    topAppBarState: TopAppBarState = rememberTopAppBarState(),
    scrollBehavior: TopAppBarScrollBehavior? =
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            IconButton(onClick = { /* TODO: Open search */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@Preview
@Composable
fun SchoolListPreview() {
    Column {
        SchoolListItem(item = SchoolItem(dbn = "1234", name = "School name", favIcon = "http://www.google.com/s2/favicons?domain=www.google.com", location = "1 Hacker way, Facebook, CA"), onClick = {})
    }
}