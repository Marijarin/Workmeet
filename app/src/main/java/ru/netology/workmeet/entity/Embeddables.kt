package ru.netology.workmeet.entity

import ru.netology.workmeet.dto.Attachment
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.Coordinates

data class AttachmentEmbeddable (
    var url: String,
    var type: AttachmentType,
) {
    fun toDto () = Attachment (url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}
data class CoordsEmbeddable (
    var lat: String,
    var long: String,
) {
    fun toDto () = Coordinates (lat, long)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordsEmbeddable(it.lat, it.long)
        }
    }
}
