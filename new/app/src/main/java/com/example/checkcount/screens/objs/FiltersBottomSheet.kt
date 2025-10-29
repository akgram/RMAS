package com.example.checkcount.screens.objs

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkcount.model.Obj
import com.example.checkcount.model.User
import com.example.checkcount.repository.Resource
import com.example.checkcount.ui.theme.buttonDisabledColor
import com.example.checkcount.ui.theme.lightGreyColor
import com.example.checkcount.ui.theme.lightMainColor2
import com.example.checkcount.ui.theme.mainColor
import com.example.checkcount.ui.theme.redTextColor
import com.example.checkcount.viewModels.AuthViewModel
import com.example.checkcount.viewModels.ObjViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.math.RoundingMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun FiltersBottomSheet(
    objViewModel: ObjViewModel,
    viewModel: AuthViewModel,
    objs: MutableList<Obj>,
    sheetState: ModalBottomSheetState,
    isFiltered: MutableState<Boolean>,
    isFilteredIndicator: MutableState<Boolean>,
    filteredObj: MutableList<Obj>,
    objMarkers: MutableList<Obj>,
    userLocation: LatLng?
){
    val context = LocalContext.current

    viewModel.getAllUserData()
    val allUsersResource = viewModel.allUsers.collectAsState()

    val allUsersNames = remember {
        mutableListOf<String>()
    }

    val sharedPreferences = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val options = sharedPreferences.getString("options", null)
    val crowd = sharedPreferences.getString("crowd", null)
    val range = sharedPreferences.getFloat("range", 1000f)


    val initialCheckedState = remember {
        mutableStateOf(List(allUsersNames.size) { false })
    }
    val rangeValues = remember { mutableFloatStateOf(500f) }

    val selectedCrowd = remember { mutableStateOf(-1) }

    val filtersSet = remember { mutableStateOf(false) }

    if (isFilteredIndicator.value && crowd != null) {
        val type = object : TypeToken<List<Int>>() {}.type
        val savedCrowd: Int = Gson().fromJson(crowd, type) ?: 0
        selectedCrowd.value = savedCrowd
    }
    if (isFilteredIndicator.value && options != null) {
        val type = object : TypeToken<List<Boolean>>() {}.type
        val savedOptions: List<Boolean> = Gson().fromJson(options, type) ?: emptyList()
        initialCheckedState.value = savedOptions
    }
    if(!filtersSet.value) {
        if (isFilteredIndicator.value) {
            rangeValues.floatValue = range
        }
        filtersSet.value = true
    }

    val allUsersData = remember {
        mutableListOf<User>()
    }


    val selectedOptions = remember {
        mutableStateOf(initialCheckedState.value)
    }

    val isSet = remember { mutableStateOf(false) }

    allUsersResource.value.let {
        when(it){
            is Resource.Failure -> {}
            is Resource.Success -> {
                allUsersNames.clear()
                allUsersData.clear()
                allUsersNames.addAll(it.result.map { user -> user.fullName})
                allUsersData.addAll(it.result)
                if(!isSet.value) {
                    initialCheckedState.value =
                        List(allUsersNames.count()) { false }.toMutableList()
                    isSet.value = true
                }
                Log.d("DropDownInitial", initialCheckedState.toString())
            }
            Resource.loading -> {}
            null -> {}
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "The author",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))


        Column{
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded.value = !expanded.value })
                    .background(lightGreyColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Select authors", style = MaterialTheme.typography.body1)
                Icon(
                    if (expanded.value) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown icon"
                )
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                allUsersNames.forEachIndexed { index, option ->
                    DropdownMenuItem(onClick = {
                        val updatedCheckedState = initialCheckedState.value.toMutableList()
                        updatedCheckedState[index] = !updatedCheckedState[index]
                        initialCheckedState.value = updatedCheckedState
                        selectedOptions.value = updatedCheckedState
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Log.d("Checked", initialCheckedState.value[index].toString())
                            Checkbox(
                                checked = initialCheckedState.value[index],
                                onCheckedChange = {
                                    val updatedCheckedState = initialCheckedState.value.toMutableList()
                                    updatedCheckedState[index] = it
                                    initialCheckedState.value = updatedCheckedState
                                    selectedOptions.value = updatedCheckedState
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Current attendance",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        CustomCrowdSelector(selectedCrowd)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Distance",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text =
                    if(rangeValues.floatValue != 1000f)
                        rangeValues.floatValue.toBigDecimal().setScale(1, RoundingMode.UP).toString() + "m"
                    else
                        "Unlimited"
                ,style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RangeSliderExample(rangeValues = rangeValues)
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomResetFilters {
                objMarkers.clear()
                objMarkers.addAll(objs)

                initialCheckedState.value =
                    List(allUsersNames.count()) { false }.toMutableList()
                rangeValues.floatValue = 500f

                selectedCrowd.value = -1

                isFiltered.value = true
                isFiltered.value = false
                isFilteredIndicator.value = false

                with(sharedPreferences.edit()) {
                    putFloat("range", 1000f)
                    putString("crowd", null)
                    putString("options", null)
                    apply()
                }

                coroutineScope.launch {
                    sheetState.hide()
                }
            }
            //Spacer(modifier = Modifier.height(10.dp))
            CustomFilterButton {
                val filteredObjs = objs.toMutableList()

                Log.d("MojaLokacija", filteredObjs.count().toString())

                // Filtriranje prema udaljenosti
                if (rangeValues.floatValue != 1000f) {
                    filteredObjs.retainAll { obj ->
                        calculateDistance(
                            userLocation!!.latitude,
                            userLocation.longitude,
                            obj.location.latitude,
                            obj.location.longitude
                        ) <= rangeValues.floatValue
                    }
                    with(sharedPreferences.edit()) {
                        putFloat("range", rangeValues.floatValue)
                        apply()
                    }
                }

                // Filtriranje prema crowd
                if (selectedCrowd.value != -1) {  // -1 neizabrani index
                    filteredObjs.retainAll { it.crowd == selectedCrowd.value }
                    val crowdJson = Gson().toJson(selectedCrowd.value)
                    with(sharedPreferences.edit()) {
                        putString("crowd", crowdJson)
                        apply()
                    }
                }

                // Filtriranje prema opcijama
                if (selectedOptions.value.indexOf(true) != -1) {
                    val selectedAuthors = allUsersData.filterIndexed { index, _ ->
                        selectedOptions.value[index]
                    }
                    val selectedIndices = selectedAuthors.map { item -> item.id }
                    filteredObjs.retainAll { it.userId in selectedIndices }

                    val selectedOptionsJson = Gson().toJson(selectedOptions.value)
                    with(sharedPreferences.edit()) {
                        putString("options", selectedOptionsJson)
                        apply()
                    }
                }

                filteredObj.clear()
                filteredObj.addAll(filteredObjs)

                isFiltered.value = false
                isFiltered.value = true

                coroutineScope.launch {
                    sheetState.hide()
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun DropdownWithCheckboxes(
    options: MutableList<String>,
    initiallyChecked: MutableList<Boolean>,
    onSelectionChanged: (List<Boolean>) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    var checkedStates = remember {
        mutableListOf<Boolean>()
    }
    checkedStates.clear()
    checkedStates.addAll(initiallyChecked)

    Column{
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded.value = !expanded.value })
                .background(lightGreyColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text("Izaberi autore", style = MaterialTheme.typography.body1)
            Icon(
                if (expanded.value) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown icon"
            )
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            if(options.isNotEmpty() && checkedStates.isNotEmpty())
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(onClick = {
                        checkedStates = checkedStates.toMutableList().apply {
                            this[index] = !this[index]
                        }
                        onSelectionChanged(checkedStates)
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = {
                                    checkedStates = checkedStates.toMutableList().apply {
                                        this[index] = it
                                    }
                                    onSelectionChanged(checkedStates)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
        }
    }
}

@Composable
fun CustomCrowdSelector(
    selected: MutableState<Int>
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CrowdOption("Low", 0, selected)
        CrowdOption("Medium", 1, selected)
        CrowdOption("High", 2, selected)
    }
}

@Composable
fun CrowdOption(
    text: String,
    index: Int,
    selected: MutableState<Int>
) {
    val isSelected = selected.value == index

    Box(
        modifier = Modifier
            .background(
                if (isSelected) lightGreyColor else Color.White,
                RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) mainColor else lightGreyColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                selected.value = index
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.People, contentDescription = "")
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = text)
        }
    }
}

@Composable
fun RangeSliderExample(
    rangeValues: MutableState<Float>
) {
    Slider(
        value = rangeValues.value,
        onValueChange = { rangeValues.value = it },
        valueRange = 0f..1000f,
        steps = 50,
        colors = SliderDefaults.colors(
            thumbColor = mainColor,
            activeTrackColor = lightMainColor2,
            inactiveTrackColor = lightGreyColor
        )
    )
}

@Composable
fun CustomFilterButton(
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(60.dp)
            .background(mainColor, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = mainColor,
            contentColor = Color.White,
            disabledContainerColor = buttonDisabledColor,
            disabledContentColor = Color.White
        ),

        ) {
        Text(
            "Filter",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun CustomResetFilters(
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(60.dp)
            .background(mainColor, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = mainColor,
            contentColor = Color.White,
            disabledContainerColor = buttonDisabledColor,
            disabledContentColor = Color.White
        ),

        ) {
        Text(
            "Reset Filters",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}