package com.example.checkcount.repository

import android.net.Uri
import com.example.checkcount.model.Obj
import com.google.android.gms.maps.model.LatLng

interface ObjRepository {

    suspend fun getAllObjs(): Resource<List<Obj>>
    suspend fun saveObjsData(
        description: String,
        crowd: Int,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: LatLng
    ): Resource<String>

    suspend fun getUserObjs(
        uid: String
    ): Resource<List<Obj>>
}