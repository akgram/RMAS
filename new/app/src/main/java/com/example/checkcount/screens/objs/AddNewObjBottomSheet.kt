package com.example.checkcount.screens.objs

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.checkcount.R
import com.example.checkcount.repository.Resource
import com.example.checkcount.screens.components.*
import com.example.checkcount.viewModels.ObjViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewObjBottomSheet(
    objViewModel: ObjViewModel?,
    location: MutableState<LatLng?>
) {
    val context = LocalContext.current
    val showLocationDialog = remember { mutableStateOf(false) }
    val showSuccessDialog = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context, location)
        } else {
            Log.e("AddObj", "Korisnik nije dozvolio lokaciju.")
        }
    }

    LaunchedEffect(Unit) {
        if (!isLocationEnabled(context)) {
            showLocationDialog.value = true
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation(context, location)
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    if (showLocationDialog.value) {
        AlertDialog(
            onDismissRequest = { showLocationDialog.value = false },
            title = { Text("Lokacija je isključena") },
            text = { Text("Da biste dodali objekat, potrebno je da uključite lokaciju.") },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    showLocationDialog.value = false
                }) { Text("Uključi") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog.value = false }) { Text("Otkaži") }
            }
        )
    }

    val objFlow = objViewModel?.objFlow?.collectAsState()
    val inputDescription = remember { mutableStateOf("") }
    val isDescriptionError = remember { mutableStateOf(false) }
    val descriptionError = remember { mutableStateOf("Ovo polje je obavezno") }
    val selectedOption = remember { mutableIntStateOf(0) }
    val buttonIsEnabled = remember { mutableStateOf(true) }
    val buttonIsLoading = remember { mutableStateOf(false) }
    val selectedImage = remember { mutableStateOf<Uri?>(Uri.EMPTY) }
    val selectedGallery = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val showedAlert = remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp, horizontal = 20.dp)
    ) {
        item { HeadingText(textValue = stringResource(id = R.string.add_new_obj_heading)) }
        //item { Spacer(modifier = Modifier.height(20.dp)) }
        //item { CustomImageForNewObj(selectedImageUri = selectedImage) }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { InputTextIndicator(textValue = "Description") }
        item { Spacer(modifier = Modifier.height(5.dp)) }
        item {
            CustomRichTextInput(
                inputValue = inputDescription,
                inputText = "Enter a description",
                isError = isDescriptionError,
                errorText = descriptionError
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { InputTextIndicator(textValue = "Crowd") }
        item { Spacer(modifier = Modifier.height(5.dp)) }
        item { CustomCrowd(selectedOption) }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { InputTextIndicator(textValue = "Gallery") }
        item { Spacer(modifier = Modifier.height(5.dp)) }
        item { CustomGalleryForAddNewObj(selectedImages = selectedGallery) }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            LoginRegisterCustomButton(
                buttonText = "Add object",
                isEnabled = buttonIsEnabled,
                isLoading = buttonIsLoading
            ) {
                if (!isLocationEnabled(context)) {
                    showLocationDialog.value = true
                    return@LoginRegisterCustomButton
                }
                if (location.value == null) {
                    Log.e("AddObj", "Lokacija još nije dostupna!")
                    return@LoginRegisterCustomButton
                }

                showedAlert.value = false
                buttonIsLoading.value = true
                objViewModel?.saveObjData(
                    description = inputDescription.value,
                    crowd = selectedOption.intValue,
                    mainImage = selectedImage.value ?: Uri.EMPTY,
                    galleryImages = selectedGallery.value,
                    location = location
                )
            }
        }
        item { Spacer(modifier = Modifier.height(5.dp)) }
    }

    // Praćenje toka resursa i prikaz obaveštenja
    objFlow?.value.let {
        when (it) {
            is Resource.Failure -> {
                buttonIsLoading.value = false
                if (!showedAlert.value) {
                    showedAlert.value = true
                    objViewModel?.getAllObjs()
                }
            }
            is Resource.Success -> {
                buttonIsLoading.value = false
                if (!showedAlert.value) {
                    showedAlert.value = true

                    // Resetuj polja
                    inputDescription.value = ""
                    selectedOption.intValue = 0
                    selectedImage.value = Uri.EMPTY
                    selectedGallery.value = emptyList()

                    // Prikaži uspeh
                    //showSuccessDialog.value = true
                }
            }
            is Resource.loading -> {}
            null -> {}
        }
    }

    // AlertDialog za uspešno dodavanje objekta
    if (showSuccessDialog.value) {
        AlertDialog(
            onDismissRequest = { /* ne radi ništa */ },
            title = { Text("Uspeh") },
            text = { Text("Objekat je uspešno dodat!") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}


//  Funkcija koja zapravo dohvaća trenutnu lokaciju
fun getCurrentLocation(context: Context, location: MutableState<LatLng?>) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                location.value = LatLng(loc.latitude, loc.longitude)
                Log.d("AddObj", "Lokacija dobijena: ${location.value}")
            } else {
                Log.e("AddObj", "Ne mogu da dobijem trenutnu lokaciju.")
            }
        }
    } catch (e: SecurityException) {
        Log.e("AddObj", "Nema dozvolu za lokaciju: ${e.message}")
    }
}
