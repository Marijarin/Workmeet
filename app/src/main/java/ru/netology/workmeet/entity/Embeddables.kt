package ru.netology.workmeet.entity

import com.google.gson.annotations.SerializedName
import ru.netology.workmeet.dto.Attachment
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.Coordinates

data class AttachmentEmbeddable (
    @SerializedName("id")
    var url: String,
    @SerializedName("type")
    var typeA: AttachmentType,
) {
    fun toDto () = Attachment (url, typeA)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.typeA)
        }
    }
}
data class CoordsEmbeddable (
    @SerializedName("lat")
    var lat: String,
    @SerializedName("long")
    var longi: String,
) {
    fun toDto () = Coordinates (lat, longi)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordsEmbeddable(it.lat, it.longi)
        }
    }
}
