package ru.netology.workmeet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.R
import ru.netology.workmeet.databinding.CardUserPreviewBinding
import ru.netology.workmeet.dto.UserPreview

interface OnUserListener {
    fun onAvatar(userPreview: UserPreview) {}
}

class UserPreviewAdapter(
    usersData: Map<String, UserPreview>,
    private val onUserListener: OnUserListener
    ) : RecyclerView.Adapter<UserPreviewViewHolder>() {

    private var usersList: List<UserPreview> = emptyList()

    init {
        this.usersList = usersData.values.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPreviewViewHolder {
        val binding =
            CardUserPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPreviewViewHolder(binding, onUserListener)
    }

    override fun getItemCount(): Int = usersList.size

    override fun onBindViewHolder(holder: UserPreviewViewHolder, position: Int) {
        if (usersList.isNotEmpty()) {
            holder.bind(usersList[position])
        }

    }
}

class UserPreviewViewHolder(
    private val binding: CardUserPreviewBinding,
    private val onUserListener: OnUserListener
    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(userPreview: UserPreview) {
        binding.apply {
            when (userPreview.avatar) {
                null -> Glide.with(avatar)
                    .load(R.drawable.avatar1)
                    .circleCrop()
                    .error(R.drawable.twotone_error_outline_24)
                    .into(avatar)
                else -> Glide.with(avatar)
                    .load("${userPreview.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.twotone_error_outline_24)
                    .timeout(10_000)
                    .into(avatar)
            }
            avatar.setOnClickListener {
                onUserListener.onAvatar(userPreview)
            }
        }
    }
}


