package ru.netology.workmeet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.BuildConfig
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardUserPreviewBinding
import ru.netology.workmeet.dto.*

interface OnUserInteractionListener {
    fun onClick(user: UserPreview) {}
    fun onAuth() {}
}

class UserPreviewAdapter(
    private val onUserInteractionListener: OnUserInteractionListener,
    private val appAuth: AppAuth
) : PagingDataAdapter<UserPreview, UserPreviewViewHolder>(UserPreviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPreviewViewHolder {
        val binding =
            CardUserPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPreviewViewHolder(binding, onUserInteractionListener, appAuth)
    }

    override fun onBindViewHolder(holder: UserPreviewViewHolder, position: Int) {
        val userPreview = getItem(position)
        if (userPreview != null) {
            holder.bind(userPreview)
        }

    }
}

class UserPreviewViewHolder(
    private val binding: CardUserPreviewBinding,
    private val onUserInteractionListener: OnUserInteractionListener,
    private val appAuth: AppAuth
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
                    .load("${BuildConfig.BASE_URL}/${userPreview.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.twotone_error_outline_24)
                    .timeout(10_000)
                    .into(avatar)
            }
            name.text = userPreview.name
            name.isFocused != name.isGone
        }
    }
}

class UserPreviewDiffCallback : DiffUtil.ItemCallback<UserPreview>() {
    override fun areItemsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem == newItem
    }
}