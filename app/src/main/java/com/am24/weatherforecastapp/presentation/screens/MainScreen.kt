package com.am24.weatherforecastapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.am24.weatherforecastapp.DialogManager
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import com.am24.weatherforecastapp.presentation.WeatherIconHelper
import com.am24.weatherforecastapp.presentation.WeatherUiEvent
import com.am24.weatherforecastapp.presentation.theme.Black
import com.am24.weatherforecastapp.presentation.theme.BlueBg
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun MainScreen(viewModel: MainViewModel, onLocationRequest: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WeatherUiEvent.ShowError -> {
                    Toast.makeText(
                        context,
                        context.getString(event.error.messageResource()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF512DA8),
                        Color(0xFF2196F3)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            MainCard(
                weather = uiState.displayedWeather,
                onSyncClick = onLocationRequest,
                onSearchClick = {
                    DialogManager.citySearchDialog(context, object : DialogManager.Listener {
                        override fun onClick(name: String?) {
                            name?.let { viewModel.requestCityWeather(city = it) }
                        }
                    })
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            WeatherTabs(
                displayedWeather = uiState.displayedWeather,
                dailyWeather = uiState.dailyWeather,
                isLoading = uiState.isLoading && !uiState.hasWeather,
                onDayClick = viewModel::setSelectedDay
            )
        }
    }
}

@Composable
fun MainCard(
    weather: WeatherModel?,
    onSyncClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = BlueBg.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather?.time ?: "--/--/---- --:--",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                weather?.let {
                    Image(
                        painter = painterResource(id = WeatherIconHelper.getWeatherIcon(it.imageURL)),
                        contentDescription = stringResource(R.string.current_weather_icon),
                        modifier = Modifier.size(48.dp)
                    )
                } ?: Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = weather?.city ?: stringResource(R.string.your_city),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = weather?.currentTemperature ?: "--°C",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = weather?.condition ?: "-",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = if (weather != null) "${weather.maximumTemperature}°C / ${weather.minimumTemperature}°C" else "--°C / --°C",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search_your_city),
                        tint = Color.White
                    )
                }
                IconButton(onClick = onSyncClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sync),
                        contentDescription = stringResource(R.string.temperature_synchronization),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherTabs(
    displayedWeather: WeatherModel?,
    dailyWeather: List<WeatherModel>,
    isLoading: Boolean,
    onDayClick: (WeatherModel) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val titles = listOf(stringResource(R.string.hours), stringResource(R.string.days))

    Column(modifier = Modifier.clip(RoundedCornerShape(10.dp))) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = BlueBg,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = Color.White
                )
            }
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = title.uppercase()) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> HoursList(displayedWeather, isLoading)
                1 -> DaysList(dailyWeather, isLoading, onDayClick)
            }
        }
    }
}

@Composable
fun HoursList(weather: WeatherModel?, isLoading: Boolean) {
    val hours = remember(weather) {
        if (weather == null || weather.hours.isEmpty()) emptyList<WeatherModel>()
        else {
            try {
                val array = JSONArray(weather.hours)
                val list = mutableListOf<WeatherModel>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        WeatherModel(
                            weather.city,
                            obj.getString("date").split("T").last().substring(0, 5),
                            obj.getString("summary"),
                            obj.getDouble("temperature").toInt().toString() + "°C",
                            "", "",
                            obj.getString("icon"),
                            ""
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    if (hours.isEmpty() && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No hourly data available", color = Color.White.copy(alpha = 0.6f))
        }
    } else if (isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp, top = 4.dp)
        ) {
            items(hours) { item ->
                WeatherListItem(item, onClick = {})
            }
        }
    }
}

@Composable
fun DaysList(days: List<WeatherModel>, isLoading: Boolean, onDayClick: (WeatherModel) -> Unit) {
    if (days.isEmpty() && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No hourly data available", color = Color.White.copy(alpha = 0.6f))
        }
    } else if (isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp, top = 4.dp)
        ) {
            items(days) { item ->
                WeatherListItem(item, onClick = { onDayClick(item) })
            }
        }
    }
}

@Composable
fun WeatherListItem(item: WeatherModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f),
            contentColor = Black
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = item.condition,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
            }
            Text(
                text = item.currentTemperature.ifEmpty { "${item.maximumTemperature}°C / ${item.minimumTemperature}°C" },
                style = MaterialTheme.typography.titleLarge,
                color = BlueBg,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Image(
                painter = painterResource(id = WeatherIconHelper.getWeatherIcon(item.imageURL)),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
