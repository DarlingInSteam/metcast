package com.example.metcast

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.metcast.data.WeatherModule
import com.example.metcast.geolocation.LocationHelper
import com.example.metcast.screens.DialogSearch
import com.example.metcast.screens.MainCard
import com.example.metcast.screens.TabLayout
import com.example.metcast.ui.theme.MetcastTheme
import org.json.JSONObject

const val API_KEY = "b57591f88e1640c188b123207231604"

class MainActivity : ComponentActivity(), LocationHelper.LocationCallback {
    private lateinit var locationHelper: LocationHelper
    private var cityFromGeo: String = "Москва"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetcastTheme {
                locationHelper = LocationHelper(this, this)
                locationHelper.requestLocationUpdates()

                val daysList = remember {
                    mutableStateOf(listOf<WeatherModule>())
                }
                val dialogState = remember {
                    mutableStateOf(false)
                }

                val currDay = remember {
                    mutableStateOf(WeatherModule(
                        "null",
                        "0",
                        "0",
                        "null",
                        "",
                        "0",
                        "0",
                        "",
                    ))
                }

                if (dialogState.value) {
                    DialogSearch(dialogState, onSubmit = {
                        GetData(it, this, daysList, currDay)
                    })
                }

                GetData(cityFromGeo, this, daysList, currDay)
                Image(
                    painter = painterResource(id = R.drawable.background),
                    contentDescription = "im1",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.9f),
                    contentScale = ContentScale.FillBounds
                )
                Column {
                    MainCard(currDay, onClickSync = {
                        GetData(cityFromGeo, this@MainActivity, daysList, currDay)
                    },
                    onClickSearch = {
                        dialogState.value = true
                    })
                    TabLayout(daysList, currDay)
                }
            }
        }
    }

    override fun onLocationReceived(cityName: String) {
        cityFromGeo = cityName
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationHelper.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Остановка получения обновлений геолокации при завершении активити
        locationHelper.stopLocationUpdates()
    }
}

private fun GetData(city: String, context: Context, daysList: MutableState<List<WeatherModule>>,
                    currDay: MutableState<WeatherModule>) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "14" +
            "&aqi=no&alerts=no" +
            "&lang=en"

    val queue = Volley.newRequestQueue(context)

    val request = StringRequest(
        Request.Method.GET,
        url,
        {
            responseBody ->
            val list = GetWeatherByDays(responseBody)
            daysList.value = list
            currDay.value = list[0]
        },
        {
            Log.d("Error", "Error in request: $it" )
        },
    )

    queue.add(request)
}

private fun GetWeatherByDays(response: String): List<WeatherModule> {
    if (response.isEmpty()) return listOf()

    val list = ArrayList<WeatherModule>()
    val mainObj = JSONObject(response)

    val buf = mainObj.getJSONObject("location").getString("tz_id")
    var a = false
    var cityBuf = ""
    for (i in buf.indices) {
        if (a) {
            cityBuf += buf[i]
        }

        if (buf[i] == '/') a = true
    }

    val city = cityBuf
    val days = mainObj.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject

        list.add(
            WeatherModule(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString(),
            )
        )
    }

    list[0] = list[0].copy(
        time = mainObj.getJSONObject("current").getString("last_updated"),
        tempCurrent = mainObj.getJSONObject("current").getString("temp_c")
    )

    return list
}