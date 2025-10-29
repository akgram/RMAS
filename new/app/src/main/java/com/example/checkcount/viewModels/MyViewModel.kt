package com.example.checkcount.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyViewModel : ViewModel() {

    private val _nameValue = MutableStateFlow("")
    val nameValue: StateFlow<String> = _nameValue

    private val _nameValue1 = MutableStateFlow("")
    val nameValue1: StateFlow<String> = _nameValue1

    private val _nameValue2 = MutableStateFlow("")
    val nameValue2: StateFlow<String> = _nameValue2

    private val _nameValue3 = MutableStateFlow("")
    val nameValue3: StateFlow<String> = _nameValue3

    private val _passwordValue = MutableStateFlow("")
    val passwordValue: StateFlow<String> = _passwordValue

    fun setNameValue(newValue: String) {
        _nameValue.value = newValue
    }

    fun setNameValue1(newValue: String) {
        _nameValue1.value = newValue
    }

    fun setNameValue2(newValue: String) {
        _nameValue2.value = newValue
    }

    fun setNameValue3(newValue: String) {
        _nameValue3.value = newValue
    }

    fun setPasswordValue(newValue: String) {
        _passwordValue.value = newValue
    }
}
