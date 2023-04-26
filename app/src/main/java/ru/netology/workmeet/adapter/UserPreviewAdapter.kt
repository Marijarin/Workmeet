package ru.netology.workmeet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.BuildConfig
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardUserPreviewBinding
import ru.netology.workmeet.dto.*

interface OnUserInteractionListener {
    fun onClick(user: UserPreview) {}

}

class UserPreviewAdapter(
    usersData: Map<String, UserPreview>,
    private val onUserInteractionListener: OnUserInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.Adapter<UserPreviewViewHolder>() {

    private var usersList: List<UserPreview> = emptyList()

    init{
        this.usersList = usersData.values.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPreviewViewHolder {
        val binding =
            CardUserPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPreviewViewHolder(binding, onUserInteractionListener, appAuth)
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
                    .load("${userPreview.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.twotone_error_outline_24)
                    .timeout(10_000)
                    .into(avatar)
            }
            avatar.setOnClickListener { onUserInteractionListener.onClick(userPreview) }
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
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem == newItem
    }
}