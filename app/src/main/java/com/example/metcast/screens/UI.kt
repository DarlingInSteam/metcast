package com.example.metcast.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.metcast.data.WeatherModule
import com.example.metcast.ui.theme.BlueLight

@Composable
fun MainList(list: List<WeatherModule>, currDays: MutableState<WeatherModule>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            list
        ) { _, item ->
            ListItem(item, currDays)
        }
    }
}

@Composable
fun ListItem(item: WeatherModule, currDays: MutableState<WeatherModule>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp).clickable {
                if (item.hours.isEmpty()) return@clickable
                currDays.value = item
            },
        backgroundColor = BlueLight,
        elevation = 0.dp,
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.time,
                    color = Color.White
                )
                Text(
                    text = item.condition,
                    color = Color.White
                )
            }
            Text(
                text = item.tempCurrent.ifEmpty { "${item.maxTemp}/${item.minTemp}" },
                color = Color.White,
                style = TextStyle(fontSize = 24.sp)
            )
            AsyncImage(
                model = "https:${item.icon}",
                contentDescription = "im5",
                modifier = Modifier
                    .size(35.dp)
            )
        }
    }
}