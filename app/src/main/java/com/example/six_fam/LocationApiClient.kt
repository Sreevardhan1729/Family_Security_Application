package com.example.six_fam

import okhttp3.*
import java.io.IOException
class LocationApiClient {
    private val client = OkHttpClient()
    fun getLocationByCoordinates(latitude: Double, longitude: Double, callback: (String?) -> Unit) {

        val url = ("https://api.geoapify.com/v1/geocode/reverse?lat=$latitude&lon=$longitude&apiKey=9f04d4666702475e907688f8b57a525c")

        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure, e.g., notify the user or log the error
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle the response
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    callback(responseBody)
                } else {
                    // Handle unsuccessful response, e.g., notify the user or log the error
                    callback(null)
                }
            }
        })
    }
}