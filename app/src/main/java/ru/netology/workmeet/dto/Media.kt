package ru.netology.workmeet.dto

import java.io.InputStream

data class Media(val url: String)

data class MediaUpload(
    val inputStream: InputStream?,
    val name: String
)