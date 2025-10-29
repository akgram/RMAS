package com.example.checkcount.viewModels

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checkcount.repository.ObjRepositoryImpl
import com.example.checkcount.repository.RateRepositoryImpl
import com.example.checkcount.repository.Resource
import com.example.checkcount.model.Obj
import com.example.checkcount.model.Rate
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ObjViewModel: ViewModel() {
    val repository = ObjRepositoryImpl()
    val rateRepository = RateRepositoryImpl()

    private val _objFlow = MutableStateFlow<Resource<String>?>(null)
    val objFlow: StateFlow<Resource<String>?> = _objFlow

    private val _newRate = MutableStateFlow<Resource<String>?>(null)
    val newRate: StateFlow<Resource<String>?> = _newRate

    private val _objs = MutableStateFlow<Resource<List<Obj>>>(Resource.Success(emptyList()))
    val objs: StateFlow<Resource<List<Obj>>> get() = _objs

    private val _rates = MutableStateFlow<Resource<List<Rate>>>(Resource.Success(emptyList()))
    val rates: StateFlow<Resource<List<Rate>>> get() = _rates


    private val _userObj = MutableStateFlow<Resource<List<Obj>>>(Resource.Success(emptyList()))
    val userObj: StateFlow<Resource<List<Obj>>> get() = _userObj

    init {
        getAllObjs()
    }

    fun getAllObjs() = viewModelScope.launch {
        _objs.value = repository.getAllObjs()
    }

    fun saveObjData(
        description: String,
        crowd: Int,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: MutableState<LatLng?>
    ) = viewModelScope.launch{
        _objFlow.value = Resource.loading
        repository.saveObjsData(
            description = description,
            crowd = crowd,
            mainImage = mainImage,
            galleryImages = galleryImages,
            location = location.value!!
        )
        _objFlow.value = Resource.Success("Uspešno dodat objekat")
    }


    fun getObjAllRates(
        oid: String
    ) = viewModelScope.launch {
        _rates.value = Resource.loading
        val result = rateRepository.getObjRates(oid)
        _rates.value = result
    }

    fun addRate(
        oid: String,
        rate: Int,
        obj: Obj
    ) = viewModelScope.launch {
        _newRate.value = rateRepository.addRate(oid, rate, obj)
    }

    fun updateRate(
        rid: String,
        rate: Int
    ) = viewModelScope.launch{
        _newRate.value = rateRepository.updateRate(rid, rate)
    }

    fun getUserObjs(
        uid: String
    ) = viewModelScope.launch {
        _userObj.value = repository.getUserObjs(uid)
    }
}

class ObjViewModelFactory:ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ObjViewModel::class.java)){
            return ObjViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}