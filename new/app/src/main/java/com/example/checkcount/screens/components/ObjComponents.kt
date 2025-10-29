package com.example.checkcount.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.checkcount.ui.theme.buttonDisabledColor
import com.example.checkcount.ui.theme.mainColor
import com.google.android.gms.maps.model.LatLng

@Composable
fun ObjMainImage(
    imageUrl: String
){
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
    ){
        AsyncImage(
            model = imageUrl,
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun CustomObjLocation(
    location: LatLng
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
       Icon(
           imageVector = Icons.Filled.LocationOn,
           contentDescription = "",
           tint = mainColor
       )
        Spacer(modifier = Modifier.width(5.dp))
        GreyText(textValue = "${location.latitude}, ${location.longitude}")
    }
}

@Composable
fun CustomObjRate(
    average: Number
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "",
            tint = Color.Yellow
        )
        Spacer(modifier = Modifier.width(5.dp))
        InputTextIndicator(textValue = "$average / 5")
    }
}

@Composable
fun CustomCrowdIndicator(
    crowd: Int
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .background(
                    when (crowd) {
                        0 -> Color.Green
                        1 -> Color.Yellow
                        else -> Color.Red
                    }, shape = RoundedCornerShape(5.dp)
                )
                .border(
                    1.dp,
                    Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(5.dp))
                InputTextIndicator(textValue =
                when (crowd) {
                    0 -> "Low"
                    1 -> "Medium"
                    else -> "High"
                }
                )
            }
        }
    }
}

@Composable
fun CustomObjGallery(
    images: List<String>
){
    LazyRow {
        for (image in images){
            item {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    AsyncImage(
                        model = image,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
    }
}

@Composable
fun CustomRateButton(
    onClick: () -> Unit,
    enabled: Boolean
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(mainColor, RoundedCornerShape(30.dp)),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = mainColor,
            contentColor = Color.White,
            disabledContainerColor = buttonDisabledColor,
            disabledContentColor = Color.White
        ),
        
    ) {
        Text(
            "Rate this club/hotel/caffe",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun CustomBackButton(
    onClick: () -> Unit
){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                Color.White,
                RoundedCornerShape(5.dp)
            )
            .padding(0.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = ""
        )
    }
}