package com.danceflow.app.data.dto

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("urls")
    val urls: List<String>
)
