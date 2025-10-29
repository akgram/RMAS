package com.example.checkcount.repository

import com.example.checkcount.model.Obj
import com.example.checkcount.model.Rate
import com.example.checkcount.model.service.DatabaseService
import com.example.checkcount.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RateRepositoryImpl : RateRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val databaseService = DatabaseService(firestoreInstance)
    override suspend fun getObjRates(
        oid: String
    ): Resource<List<Rate>> {
        return try {
            val rateDocRef = firestoreInstance.collection("rates")
            val querySnapshot = rateDocRef.get().await()
            val ratesList = mutableListOf<Rate>()
            for (document in querySnapshot.documents) {
                val objId = document.getString("objId") ?: ""
                if (objId == oid) {
                    ratesList.add(
                        Rate(
                            id = document.id,
                            userId = document.getString("userId") ?: "",
                            objId = oid,
                            rate = document.getLong("rate")?.toInt() ?: 0
                        )
                    )
                }
            }
            Resource.Success(ratesList)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    override suspend fun getUserRates(): Resource<List<Rate>> {
        return try{
            val rateDocRef = firestoreInstance.collection("rates")
            val querySnapshot = rateDocRef.get().await()
            val ratesList = mutableListOf<Rate>()
            for(document in querySnapshot.documents){
                val userId = document.getString("userId") ?: ""
                if(userId == firebaseAuth.currentUser?.uid){
                    ratesList.add(Rate(
                        id = document.id,
                        objId = document.getString("objId") ?: "",
                        userId = userId,
                        rate = document.getLong("rate")?.toInt() ?: 0
                    ))
                }
            }
            Resource.Success(ratesList)
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUserAdForObj(): Resource<List<Rate>> {
        TODO("Not yet implemented")
    }

    override suspend fun addRate(
        bid: String,
        rate: Int,
        obj: Obj
    ): Resource<String> {
        return try{
            val myRate = Rate(
                userId = firebaseAuth.currentUser!!.uid,
                objId = bid,
                rate = rate
            )
            databaseService.addPoints(obj.userId, rate * 3)
            val result = databaseService.saveRateData(myRate)
            result
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun updateRate(rid: String, rate: Int): Resource<String> {
        return try{
            val result = databaseService.updateRate(rid, rate)
            result
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
}