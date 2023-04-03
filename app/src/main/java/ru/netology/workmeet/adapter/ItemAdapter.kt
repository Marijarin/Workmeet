package ru.netology.workmeet.adapter



import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.res.TypedArrayUtils.getText

import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.workmeet.BuildConfig
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardEventBinding
import ru.netology.workmeet.databinding.CardJobBinding
import ru.netology.workmeet.databinding.CardPostBinding
import ru.netology.workmeet.dto.Event
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.dto.Post

interface OnInteractionListener {
    fun onLike(item: FeedItem) {}
    fun onEdit(item: FeedItem) {}
    fun onRemove(item: FeedItem) {}
    fun onImage(item: FeedItem) {}
    fun onAuth() {}
}

class FeedItemAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Post -> R.layout.card_post
            is Event -> R.layout.card_event
            is Job -> R.layout.card_job
            null -> error("unknown item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener, appAuth)
            }
            R.layout.card_event -> {
                val binding =
                    CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventViewHolder(binding, onInteractionListener, appAuth)
            }
            R.layout.card_job -> {
                val binding =
                CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                JobViewHolder(binding, onInteractionListener, appAuth)
            }
            else -> error("unknown view type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is Event -> (holder as? EventViewHolder)?.bind(item)
            is Job -> (holder as? JobViewHolder)?.bind(item)
            null -> error("unknown item type")
        }

    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
val s = R.string.default_job
        binding.apply {
            author.text = post.author
            authorJob.text = post.authorJob ?: itemView.context.getText(R.string.default_job)
            published.text = post.published
            content.text = post.content
            like.isChecked = post.likedByMe
likeOwners

            Glide.with(avatar)
                .load("${BuildConfig.BASE_URL}/${post.authorAvatar}")
                .circleCrop()
                .placeholder(R.drawable.avatar3)
                .error(R.drawable.twotone_error_outline_24)
                .timeout(10_000)
                .into(avatar)

            attachment.let {
                if (post.attachment != null) {
                    Glide.with(attachment)
                        .load("${BuildConfig.BASE_URL}/media/${post.attachment.url}")
                        .placeholder(R.drawable.baseline_attachment_24)
                        .error(R.drawable.twotone_error_outline_24)
                        .timeout(10_000)
                        .into(attachment)
                }
            }
            attachment.isVisible = post.attachment != null

            menu.isVisible = post.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                if (appAuth.state.value.id != 0L) {
                    onInteractionListener.onLike(post)
                } else if (appAuth.state.value.id == 0L) {
                    like.isChecked = false
                    like.isEnabled = false
                    onInteractionListener.onAuth()
                }
            }

        }
    }
}
class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    fun bind (event: Event){
        binding.apply {

        }
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    fun bind (job: Job) {
        binding.apply {

        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}