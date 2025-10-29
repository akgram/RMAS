package com.example.checkcount.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetLayout
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Terrain
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import com.example.checkcount.R
import com.example.checkcount.location.LocationService
import com.example.checkcount.model.Obj
import com.example.checkcount.model.User
import com.example.checkcount.navigation.Routes
import com.example.checkcount.repository.Resource
import com.example.checkcount.screens.components.bitmapDescriptorFromUrlWithRoundedCorners
import com.example.checkcount.screens.components.BitmapDescriptorFromVector
import com.example.checkcount.screens.components.MapFooter
import com.example.checkcount.screens.components.MapNavigationBar
import com.example.checkcount.screens.objs.AddNewObjBottomSheet
import com.example.checkcount.screens.objs.FiltersBottomSheet
import com.example.checkcount.ui.theme.greyTextColor
import com.example.checkcount.ui.theme.mainColor
import com.example.checkcount.viewModels.AuthViewModel
import com.example.checkcount.viewModels.ObjViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun IndexScreen(
    viewModel: AuthViewModel?,
    navController: NavController?,
    objViewModel: ObjViewModel?,

    isCameraSet: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.3209, 21.8958), 17f)
    },
    objMarkers: MutableList<Obj>,
    isFilteredParam: Boolean = false
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val options = sharedPreferences.getString("options", null)
    val crowd = sharedPreferences.getString("crowd", null)
    val range = sharedPreferences.getFloat("range", 1000f)

    val isFiltered = remember {
        mutableStateOf(false)
    }
    val isFilteredIndicator = remember{
        mutableStateOf(false)
    }

    if(isFilteredParam && (options != null || crowd != null || range != 1000f)){
        isFilteredIndicator.value = true
    }

    val objsResource = objViewModel?.objs?.collectAsState()
    val allObjs = remember {
        mutableListOf<Obj>()
    }
    objsResource?.value.let {
        when(it){
            is Resource.Success -> {
                allObjs.clear()
                allObjs.addAll(it.result)
            }
            is Resource.loading -> {

            }
            is Resource.Failure -> {
                Log.e("Podaci", it.toString())
            }
            null -> {}
        }
    }

    viewModel?.getUserData()

    val userDataResource = viewModel?.currentUserFlow?.collectAsState()

    val filteredObjs = remember {
        mutableListOf<Obj>()
    }

    val searchValue = remember {
        mutableStateOf("")
    }
    val userData = remember {
        mutableStateOf<User?>(null)
    }
    val profileImage = remember {
        mutableStateOf("")
    }

    val myLocation = remember {
        mutableStateOf<LatLng?>(null)
    }

    //val objMarkerCopy = objMarkers

    val showFilterDialog = remember {
        mutableStateOf(false)
    }

    val isAddNewBottomSheet = remember {
        mutableStateOf(true)
    }
    
    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    val latitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LATITUDE, 0.0)
                    val longitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LONGITUDE, 0.0)
                    // Update the camera position
                    myLocation.value = LatLng(latitude, longitude)
                    Log.d("Nova lokacija", myLocation.toString())
                }
            }
        }
    }

    DisposableEffect(context) {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(LocationService.ACTION_LOCATION_UPDATE))
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    val uiSettings = remember {
        mutableStateOf(MapUiSettings(compassEnabled = true))
    }

    val properties = remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = true))
    }

    val markers = remember { mutableStateListOf<LatLng>() }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)


    LaunchedEffect(myLocation.value) {
        myLocation.value?.let {
            Log.d("Nova lokacija gore", myLocation.toString())
            if(!isCameraSet.value) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
                isCameraSet.value = true
            }
            markers.clear()
            markers.add(it)
        }
    } // refresh lokacije

    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (isAddNewBottomSheet.value) {
                AddNewObjBottomSheet(
                    objViewModel = objViewModel!!,
                    location = myLocation
                )
            }
            else {
                FiltersBottomSheet(objViewModel!!, viewModel!!, allObjs, sheetState, isFiltered,isFilteredIndicator, filteredObjs, objMarkers, myLocation.value)
            }
        }
    )
    {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties.value,
                uiSettings = uiSettings.value
            ){ // google ima svoj marker plavi

                markers.forEach { marker ->
                    val icon = BitmapDescriptorFromVector(
                        context, R.drawable.currentlocation
                    )
                    Marker(
                        state = rememberMarkerState(position = marker),
                        title = "My location",
                        //icon = icon,
                        snippet = "",
                    )
                }
                //Log.d("Is Filtered", isFiltered.value.toString())
                if(!isFiltered.value) {
                    objMarkers.forEach { marker ->
                        val icon = bitmapDescriptorFromUrlWithRoundedCorners(
                            marker.mainImage,
                            10f,
                        )
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(
                                    marker.location.latitude,
                                    marker.location.longitude
                                )
                            ),
                            title = "Moja Lokacija",
                            icon = icon.value ?: BitmapDescriptorFactory.defaultMarker(),
                            snippet = marker.description,
                            onClick = {
                                val objJson = Gson().toJson(marker)
                                val encodedObjJson =
                                    URLEncoder.encode(objJson, StandardCharsets.UTF_8.toString())

                                val objsJson = Gson().toJson(objMarkers)
                                val encodedObjsJson = URLEncoder.encode(objsJson, StandardCharsets.UTF_8.toString())

                                navController?.navigate(Routes.objScreen + "/$encodedObjJson/$encodedObjsJson")
                                true
                            }
                        )
                    }
                }else{
                    Log.d("Filtered", filteredObjs.count().toString())
                    filteredObjs.forEach { marker ->
                        val icon = bitmapDescriptorFromUrlWithRoundedCorners(
                            marker.mainImage,
                            10f,
                        )
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(
                                    marker.location.latitude,
                                    marker.location.longitude
                                )
                            ),
                            title = "Moja Lokacija",
                            icon = icon.value ?: BitmapDescriptorFactory.defaultMarker(),
                            snippet = marker.description,
                            onClick = {
                                val objJson = Gson().toJson(marker)
                                val encodedObjJson =
                                    URLEncoder.encode(objJson, StandardCharsets.UTF_8.toString())

                                val objsJson = Gson().toJson(filteredObjs)
                                val encodedObjsJson = URLEncoder.encode(objsJson, StandardCharsets.UTF_8.toString())

                                navController?.navigate(Routes.objScreen + "/$encodedObjJson/$encodedObjsJson")
                                true
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                MapNavigationBar(
                    searchValue = searchValue,
                    profileImage = profileImage.value.ifEmpty { "" },
                    onImageClick = {

                        val userJson = Gson().toJson(userData.value)
                        val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
                        navController?.navigate(Routes.userProfileScreen + "/$encodedUserJson")

                    },
                    objs = objMarkers,
                    navController = navController,
                    cameraPositionState = cameraPositionState
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .clickable  {
                            isAddNewBottomSheet.value = false
                            scope.launch {
                                sheetState.show()
                            }
                        }
                        .background(
                            if(isFiltered.value || isFilteredIndicator.value)
                                mainColor
                            else
                                Color.White
                            ,RoundedCornerShape(15.dp)
                        )
                        .padding(horizontal = 15.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = "",
                            tint =
                                if(isFiltered.value || isFilteredIndicator.value)
                                    Color.White
                                else
                                    mainColor
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Filters",
                            style = TextStyle(
                                color = if(isFiltered.value || isFilteredIndicator.value)
                                    Color.White
                                else
                                    mainColor
                            )
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    IconButton(
                        onClick = {
                            properties.value = MapProperties(mapType = MapType.TERRAIN)
                        },
                        modifier = Modifier
                            .background(
                                if(properties.value == MapProperties(mapType = MapType.TERRAIN))
                                    mainColor
                                else
                                    Color.White
                                ,RoundedCornerShape(15.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SatelliteAlt,
                            contentDescription = "",
                            tint =
                            if(properties.value == MapProperties(mapType = MapType.TERRAIN))
                                Color.White
                            else
                                greyTextColor
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    IconButton(
                        onClick = {
                            properties.value = MapProperties(mapType = MapType.SATELLITE)
                        },
                        modifier = Modifier
                            .background(
                                if(properties.value == MapProperties(mapType = MapType.SATELLITE))
                                    mainColor
                                else
                                    Color.White
                                ,RoundedCornerShape(15.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terrain,
                            contentDescription = "",
                            tint =
                            if(properties.value == MapProperties(mapType = MapType.SATELLITE))
                                Color.White
                            else
                                greyTextColor
                        )
                    }
                }
                MapFooter(
                    openAddNewObj = {
                        isAddNewBottomSheet.value = true
                        scope.launch {
                            sheetState.show()
                        }
                    },
                    active = 0,
                    onHomeClick = {},
                    onTableClick = {
                        navController?.navigate(Routes.tableScreen)
                        val objsJson = Gson().toJson(
                            if(!isFiltered.value)
                                objMarkers
                            else
                                filteredObjs
                        )
                        val encodedObjsJson = URLEncoder.encode(objsJson, StandardCharsets.UTF_8.toString())
                        navController?.navigate("tableScreen/$encodedObjsJson")
                    },
                    onRankingClick = {
                        navController?.navigate(Routes.rankingScreen)
                    },
                    onSettingsClick = {
                        navController?.navigate(Routes.settingsScreen)
                    },
                    showAddButton = true
                )
            }
        }
    }
    
    if (showFilterDialog.value){
        FilterDialog(onApply = { /*TODO*/ }) {
            
        }
    }

   userDataResource?.value.let {
       when(it){
           is Resource.Success -> {
               userData.value = it.result
               profileImage.value = it.result.profileImage
           }
           null -> {
               userData.value = null
               profileImage.value = ""
           }

           is Resource.Failure -> {}
           Resource.loading -> {}
       }
   }


}

@Composable
fun FilterDialog(
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Filteri") },
        confirmButton = {
            Button(
                onClick = onApply,
            ) {
                Text(text = "Primeni")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
            ) {
                Text(text = "Odustani")
            }
        },
    )
}