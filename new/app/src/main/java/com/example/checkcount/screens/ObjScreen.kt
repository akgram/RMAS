package com.example.checkcount.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.checkcount.model.Obj
import com.example.checkcount.model.Rate
import com.example.checkcount.navigation.Routes
import com.example.checkcount.repository.Resource
import com.example.checkcount.screens.components.CustomBackButton
import com.example.checkcount.screens.components.CustomCrowdIndicator
import com.example.checkcount.screens.components.CustomObjGallery
import com.example.checkcount.screens.components.CustomObjLocation
import com.example.checkcount.screens.components.CustomObjRate
import com.example.checkcount.screens.components.CustomRateButton
import com.example.checkcount.screens.components.ObjMainImage
import com.example.checkcount.screens.components.GreyTextBigger
import com.example.checkcount.screens.components.HeadingText
import com.example.checkcount.screens.dialogs.RateObjDialog
import com.example.checkcount.viewModels.AuthViewModel
import com.example.checkcount.viewModels.ObjViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.math.RoundingMode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ObjScreen(
    navController: NavController,
    objViewModel: ObjViewModel,
    obj: Obj,
    viewModel: AuthViewModel,
    objs: MutableList<Obj>?
){
    val ratesResources = objViewModel.rates.collectAsState()
    val newRateResource = objViewModel.newRate.collectAsState()

    val rates = remember {
        mutableListOf<Rate>()
    }
    val averageRate = remember {
        mutableDoubleStateOf(0.0)
    }
    val showRateDialog = remember {
        mutableStateOf(false)
    }

    val isLoading = remember {
        mutableStateOf(false)
    }

    val myPrice = remember {
        mutableIntStateOf(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ObjMainImage(obj.mainImage)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            item{ CustomBackButton {
                if(objs == null) {
                    navController.popBackStack()
                }else{
                    val isCameraSet = true
                    val latitude = obj.location.latitude
                    val longitude = obj.location.longitude

                    val objsJson = Gson().toJson(objs)
                    val encodedObjsJson = URLEncoder.encode(objsJson, StandardCharsets.UTF_8.toString())
                    navController.navigate(Routes.indexScreenWithParams + "/$isCameraSet/$latitude/$longitude/$encodedObjsJson")
                }
            }
            }
            //item{Spacer(modifier = Modifier.height(220.dp))}
            item{ CustomCrowdIndicator(crowd = obj.crowd) }
            item{Spacer(modifier = Modifier.height(20.dp))}
            item{HeadingText(textValue = "Facility nearby") }
            item{Spacer(modifier = Modifier.height(10.dp))}
            item{ CustomObjLocation(location = LatLng(obj.location.latitude, obj.location.longitude)) }
            item{Spacer(modifier = Modifier.height(10.dp))}
            item{CustomObjRate(average = averageRate.doubleValue) }
            item{Spacer(modifier = Modifier.height(10.dp))}
            item{GreyTextBigger(textValue = obj.description.replace('+', ' ')) }
            item{Spacer(modifier = Modifier.height(20.dp))}
            item{Text(text = "Gallery of the object", style= TextStyle(fontSize = 20.sp))}
//            item{ CustomCrowdIndicator(crowd = 1)}
            item{Spacer(modifier = Modifier.height(10.dp))}
            item { CustomObjGallery(images = obj.galleryImages ?: emptyList()) }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 15.dp, vertical = 20.dp)
        ) {
            CustomRateButton(
                enabled = obj.userId != viewModel.currentUser?.uid, // ne moze svoj da oceni??
                onClick = {
                val rateExist = rates.firstOrNull{
                    it.objId == obj.id && it.userId == viewModel.currentUser?.uid
                }
                if(rateExist != null)
                    myPrice.intValue = rateExist.rate
                showRateDialog.value = true
            })
        }


        if(showRateDialog.value){
            RateObjDialog(
                showRateDialog = showRateDialog,
                rate = myPrice,
                rateObj = {

                    val rateExist = rates.firstOrNull{
                        it.objId == obj.id && it.userId == viewModel.currentUser!!.uid
                    }
                    if(rateExist != null){
                        isLoading.value = true
                        objViewModel.updateRate(
                            rid = rateExist.id,
                            rate = myPrice.intValue
                        )
                    }else {
                        isLoading.value = true
                        objViewModel.addRate(
                            oid = obj.id,
                            rate = myPrice.intValue,
                            obj = obj
                        )
                    }
                    showRateDialog.value = false
                },
                isLoading = isLoading
            )
        }
    }

    ratesResources.value.let {
        when(it){
            is Resource.Success -> {
                rates.addAll(it.result)
                var sum = 0.0
                for (rate in it.result){
                    sum += rate.rate.toDouble()
                }
                if(sum != 0.0) {
                    val rawPositive = sum / it.result.count()
                    val rounded = rawPositive.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
                    averageRate.doubleValue = rounded
                }  else {}
            }
            is Resource.loading -> {

            }
            is Resource.Failure -> {
                Log.e("Podaci", it.toString())
            }
        }
    }
    newRateResource.value.let {
        when(it){
            is Resource.Success -> {
                isLoading.value = false

                val rateExist = rates.firstOrNull{rate ->
                    rate.id == it.result
                }
                if(rateExist != null){
                    rateExist.rate = myPrice.intValue
                }
            }
            is Resource.loading -> {
                //isLoading.value = false
            }
            is Resource.Failure -> {
                val context = LocalContext.current
                Toast.makeText(context, "An error occurred while evaluating the object", Toast.LENGTH_LONG).show()
                isLoading.value = false
            }
            null -> {
                isLoading.value = false
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreviewObj() {
    Obj()
}