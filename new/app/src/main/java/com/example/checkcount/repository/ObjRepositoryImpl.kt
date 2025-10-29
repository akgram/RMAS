package com.example.checkcount.repository

import android.net.Uri
import com.example.checkcount.model.Obj
import com.example.checkcount.model.service.DatabaseService
import com.example.checkcount.model.service.StorageService
import com.example.checkcount.utils.await
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage

class ObjRepositoryImpl : ObjRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()

    private val databaseService = DatabaseService(firestoreInstance)
    private val storageService = StorageService(storageInstance)


    override suspend fun getAllObjs(): Resource<List<Obj>> {
        return try{
            val snapshot = firestoreInstance.collection("objs").get().await()
            val objs = snapshot.toObjects(Obj::class.java)
            Resource.Success(objs)
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun saveObjsData(
        description: String,
        crowd: Int,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: LatLng
    ): Resource<String> {
        return try{
            val currentUser = firebaseAuth.currentUser
            if(currentUser!=null){
                val mainImageUrl = storageService.uploadObjMainImage(mainImage)
                val galleryImagesUrls = storageService.uploadObjGalleryImages(galleryImages)
                val geoLocation = GeoPoint(
                    location.latitude,
                    location.longitude
                )
                val obj = Obj(
                    userId = currentUser.uid,
                    description = description,
                    crowd = crowd,
                    mainImage = mainImageUrl,
                    galleryImages = galleryImagesUrls,
                    location = geoLocation
                )
                databaseService.saveObjData(obj)
                databaseService.addPoints(currentUser.uid, 5)
            }
            Resource.Success("Uspesno sačuvani svi podaci o objektu")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUserObjs(uid: String): Resource<List<Obj>> {
        return try {
            val snapshot = firestoreInstance.collection("objs")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val objs = snapshot.toObjects(Obj::class.java)
            Resource.Success(objs)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
}