package ru.netology.workmeet.dto

import android.net.Uri
import okhttp3.MediaType
import java.io.InputStream

data class Media(val url: String)

data class MediaUpload(
    val uri: Uri?,
    val inputStream: InputStream?,
    val uploadType: String?
)