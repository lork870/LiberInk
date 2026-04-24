package com.zaitsev.liberink.data

import com.zaitsev.liberink.models.Book
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ApiService {

    @GET("api/Books/user/{userId}")
    suspend fun getBooks(@Path("userId") userId: String): List<Book>

    @DELETE("api/Books/{id}")
    suspend fun deleteBook(@Path("id") id: Int): retrofit2.Response<Unit>
}