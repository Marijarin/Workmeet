package ru.netology.workmeet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.R
import ru.netology.workmeet.databinding.CardUserBinding
import ru.netology.workmeet.dto.User
interface OnUserInteractionListener{
    fun onAvatar(userId:Long){}
    fun onPick(userId:Long){}
}

class UserAdapter(
    private val onUserInteractionListener: OnUserInteractionListener,
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)


    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUserInteractionListener: OnUserInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        binding.apply {
            author.text = user.name
            when (user.avatar) {
                null -> Glide.with(avatar)
                    .load(R.drawable.avatar3)
                    .circleCrop()
                    .error(R.drawable.twotone_error_outline_24)
                    .into(avatar)
                else -> Glide.with(avatar)
                    .load("${user.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.avatar3)
                    .error(R.drawable.twotone_error_outline_24)
                    .timeout(10_000)
                    .into(avatar)
            }
            avatar.setOnClickListener {
                onUserInteractionListener.onAvatar(user.id)
            }
            pick.setOnClickListener {
                onUserInteractionListener.onPick(user.id)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}