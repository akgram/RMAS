package com.example.checkcount.screens

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.checkcount.model.Obj
import com.example.checkcount.model.User
import com.example.checkcount.navigation.Routes
import com.example.checkcount.repository.Resource
import com.example.checkcount.screens.components.OtherPlaces
import com.example.checkcount.screens.components.FirstThreePlaces
import com.example.checkcount.screens.components.MapFooter
import com.example.checkcount.viewModels.AuthViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun RankingScreen(
    viewModel: AuthViewModel,
    navController: NavController
){
    viewModel.getAllUserData()
    val allUsersResource = viewModel.allUsers.collectAsState()

    val allUsers = remember {
        mutableListOf<User>()
    }
    val objMarkers = remember {
        mutableStateListOf<Obj>()
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .padding(vertical = 25.dp, horizontal = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "RANG LISTA",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        LazyColumn(
            modifier = Modifier.padding(top = 70.dp)
        ) {
            item { OtherPlaces(
                users = allUsers,
                navController = navController
            ) }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            MapFooter(
                openAddNewObj = {
                },
                active = 2,
                onHomeClick = {
                    navController.navigate(Routes.indexScreen)
                },
                onTableClick = {
                    val objsJson = Gson().toJson(objMarkers)
                    val encodedObjsJson = URLEncoder.encode(objsJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("tableScreen/$encodedObjsJson")
                },
                onRankingClick = {},
                onSettingsClick = {
                    navController.navigate(Routes.settingsScreen)
                },
                showAddButton = false
            )
        }
    }

    allUsersResource.value.let {
        when(it){
            is Resource.Failure -> {}
            is Resource.Success -> {
                allUsers.clear()
                allUsers.addAll(it.result.sortedByDescending { x -> x.points })
            }
            Resource.loading -> {}
            null -> {}
        }
    }
}