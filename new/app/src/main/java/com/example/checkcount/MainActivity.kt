package com.example.checkcount

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.checkcount.app.PostApp
import com.example.checkcount.viewModels.AuthViewModelFactory
import com.example.checkcount.viewModels.AuthViewModel
import com.example.checkcount.viewModels.ObjViewModel
import com.example.checkcount.viewModels.MyViewModel
import com.example.checkcount.viewModels.ObjViewModelFactory

class MainActivity : ComponentActivity() {
    private val userViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }
    private val objViewModel: ObjViewModel by viewModels{
        ObjViewModelFactory()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostApp(userViewModel, objViewModel)
        }
    }
}