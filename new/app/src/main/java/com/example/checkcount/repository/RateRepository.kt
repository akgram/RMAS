package com.example.checkcount.repository

import com.example.checkcount.model.Obj
import com.example.checkcount.model.Rate

interface RateRepository {
    suspend fun getObjRates(
        bid: String
    ): Resource<List<Rate>>
    suspend fun getUserRates(): Resource<List<Rate>>
    suspend fun getUserAdForObj(): Resource<List<Rate>>
    suspend fun addRate(
        bid: String,
        rate: Int,
        obj: Obj
    ): Resource<String>

    suspend fun updateRate(
        rid: String,
        rate: Int,
    ): Resource<String>
}