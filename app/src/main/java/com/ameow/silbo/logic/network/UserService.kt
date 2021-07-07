package com.ameow.silbo.logic.network

import com.ameow.silbo.logic.model.Request
import com.ameow.silbo.logic.model.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {
    @POST("user/login")
    fun login(@Body request: Request): Call<Response>

    @POST("user/register")
    fun register(@Body request: Request) : Call<Response>
}