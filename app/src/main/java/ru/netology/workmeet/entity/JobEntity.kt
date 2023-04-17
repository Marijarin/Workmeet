package ru.netology.workmeet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.workmeet.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
    val userId: Long,
){
    fun toDto() = Job(
        id,
        name,
        position,
        start,
        finish,
        link
    )
    companion object{
        fun fromDto(dto: Job, id: Long) =
            JobEntity(
                dto.id,
                dto.name,
                dto.position,
                dto.start,
                dto.finish,
                dto.link,
                id
            )
    }
}
fun List<Job>.toEntity(id: Long): List<JobEntity> = map{JobEntity.fromDto(it,id) }