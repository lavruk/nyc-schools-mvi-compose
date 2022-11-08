@file:OptIn(ExperimentalMaterial3Api::class)

package nyc.high.schools.ui

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect
import nyc.high.schools.R
import nyc.high.schools.ui.ErrorItem
import nyc.high.schools.ui.LoadingView
import nyc.high.schools.ui.theme.NYCHighSchoolsTheme
import nyc.high.schools.viewmodel.*

@Composable
fun SchoolDetailsScreen(
    dbn: String?,
    name: String?,
    onBack: () -> Unit,
    viewModel: DetailsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = name.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                // innerPadding takes into account the top and bottom bar
                .padding(innerPadding)
        ) {

            if (dbn == null) {
                Text(text = "School id should not be empty")
            } else {

                LaunchedEffect(Unit) {
                    viewModel.onIntent(SchoolDetailsIntent.Init(dbn = dbn))
                }

                SchoolDetailsContent(state = state, onRetry = {
                    viewModel.onIntent(SchoolDetailsIntent.Retry)
                }, onNavigate = {
                    viewModel.onIntent(SchoolDetailsIntent.Navigate)
                })
            }
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.events.collect {event ->
            when(event){
                is SchoolDetailsEvent.Navigate -> {
                    val uri = "geo:?q=${event.location}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                }
            }
        }
    }
}

@Composable
fun SchoolDetailsContent(state: SchoolDetailsState, onRetry: () -> Unit, onNavigate: () -> Unit) {
    if (state.error != null) {
        ErrorItem(message = "${state.error}", onClickRetry = onRetry)
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            if(state.loadingScores) {
                LoadingView(modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp))
            } else {
                Text(
                    text = "Average SAT scores",
                    modifier = Modifier
                        .padding(16.dp)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleMedium
                )
                ScoreItem(title = "Writing:", value = "${state.satScores?.writingAvgScore}")
                ScoreItem(title = "Reading:", value = "${state.satScores?.readingAvgScore}")
                ScoreItem(title = "Math:", value = "${state.satScores?.mathAvgScore}")
            }

            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            if (state.loadingDescription) {
                LoadingView(modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    text = "School details",
                    modifier = Modifier
                        .padding(16.dp)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleMedium
                )

                SchoolDescriptionItem(title = "Overview", value = "${state.description?.overview}")
                SchoolDescriptionItem(title = "Location", value = "${state.description?.location}", onClick = onNavigate)
                SchoolDescriptionItem(title = "Email", value = "${state.description?.email}")
                SchoolDescriptionItem(title = "Phone", value = "${state.description?.phoneNumber}")
                SchoolDescriptionItem(title = "Website", value = "${state.description?.website}")
            }

        }
    }
}

@Composable
fun ScoreItem(title: String, value: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = value,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SchoolDescriptionItem(title: String, value: String, onClick: () -> Unit = {}) {
    Column(Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .weight(1f), // Break line if the title is too long
                style = MaterialTheme.typography.titleMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f), // Break line if the title is too long
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Divider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Preview("School details light", "Details", device = Devices.PIXEL_C)
@Preview(
    "School details dark", "Details",
    uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL_C
)
@Composable
fun PreviewSchoolDetailsContent() {
    NYCHighSchoolsTheme {

        SchoolDetailsContent(
            state = SchoolDetailsState(
                name = "School name",
                error = null,
                loadingScores = false,
                loadingDescription = false,
                satScores = SchoolSATScores(
                    numberOfSatTestTakers = "4560",
                    readingAvgScore = "3.8",
                    mathAvgScore = "4.3",
                    writingAvgScore = "1.2"
                ),
                description = SchoolDescription(
                    overview = "school overview",
                    location = "School location",
                    phoneNumber = "(300) 400 7654",
                    faxNumber = null,
                    email = "school@nyc.com",
                    website = "school.nyc"
                )
            )
        , onRetry = {}, onNavigate = {})
    }
}