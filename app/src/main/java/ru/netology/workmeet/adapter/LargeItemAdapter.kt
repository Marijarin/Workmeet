package ru.netology.workmeet.adapter


import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardEventBinding
import ru.netology.workmeet.databinding.CardPostBinding
import ru.netology.workmeet.dto.*
import java.util.*


interface OnInteractionListener {
    fun onLike(item: FeedItem) {}
    fun onEdit(item: FeedItem) {}
    fun onRemove(item: FeedItem) {}
    fun onAuth() {}
    fun onUser() {}
    fun onPlay(item: FeedItem) {}
    fun onPause() {}
}

class LargeItemAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Post -> R.layout.card_post
            is Event -> R.layout.card_event
            else -> error("unknown item type")
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
            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is Event -> (holder as? EventViewHolder)?.bind(item)
            else -> error("unknown item type")
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    private fun toDate(published: String): String {

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val formattedDate = formatter.format(parser.parse(published) ?: "")

        return formattedDate
    }

    fun bind(post: Post) {

        val childAdapter = UserPreviewAdapter(
            post.users,
            object : OnUserInteractionListener {
                override fun onClick(user: UserPreview) {
                    super.onClick(user)
                }
            }, appAuth
        )

        binding.apply {
            author.text = post.author
            authorJob.text = post.authorJob ?: itemView.context.getText(R.string.default_job)
            published.text = toDate(post.published)
            content.text = post.content
            like.isChecked = post.likedByMe

            likeOwners.adapter = childAdapter
            likeOwners.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL,false)
            when (post.authorAvatar) {
                null -> Glide.with(avatar)
                    .load(R.drawable.avatar3)
                    .circleCrop()
                    .error(R.drawable.twotone_error_outline_24)
                    .into(avatar)
                else -> Glide.with(avatar)
                    .load("${post.authorAvatar}")
                    .circleCrop()
                    .placeholder(R.drawable.avatar3)
                    .error(R.drawable.twotone_error_outline_24)
                    .timeout(10_000)
                    .into(avatar)
            }
            attachment.let {
                if (post.attachment != null) {
                    when (post.attachment.typeA) {
                        AttachmentType.IMAGE -> {
                            Glide.with(attachment)
                            .load(post.attachment.url)
                            .placeholder(R.drawable.baseline_attachment_24)
                            .error(R.drawable.twotone_error_outline_24)
                            .timeout(10_000)
                            .into(attachment)
                        play.visibility = View.GONE
                        videoGroup.visibility = View.GONE
                        }
                        AttachmentType.AUDIO, AttachmentType.VIDEO -> {
                            attachment.setOnClickListener { onInteractionListener.onPlay(post) }
                        }
                    }
                }
            }
            attachment.isVisible = post.attachment != null
            play.setOnClickListener { onInteractionListener.onPlay(post) }

            if (play.isEnabled || play.isActivated || play.isChecked && video.isFocused){
                pause.visibility = View.VISIBLE
                pause.setOnClickListener { onInteractionListener.onPause() }
            }

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
            avatar.setOnClickListener {
                onInteractionListener.onUser()
            }

        }
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        val childAdapter = UserPreviewAdapter(
            event.users,
            object : OnUserInteractionListener {
                //TODO
            }, appAuth
        )

        binding.apply {
            author.text = event.author
            authorJob.text = event.authorJob ?: itemView.context.getText(R.string.default_job)
            datetime.text = event.datetime
            type.text = event.type.name
            content.text = event.content
            speakersPreview.adapter = childAdapter
            participantsPreview.adapter = childAdapter
            when (event.authorAvatar) {
                null -> Glide.with(avatar)
                    .load(R.drawable.avatar3)
                    .circleCrop()
                    .error(R.drawable.twotone_error_outline_24)
                    .into(avatar)
                else -> Glide.with(avatar)
                    .load("${event.authorAvatar}")
                    .circleCrop()
                    .placeholder(R.drawable.avatar3)
                    .error(R.drawable.twotone_error_outline_24)
                    .timeout(10_000)
                    .into(avatar)
            }

            attachment.let {
                if (event.attachment != null) {
                    Glide.with(attachment)
                        .load(event.attachment.url)
                        .placeholder(R.drawable.baseline_attachment_24)
                        .error(R.drawable.twotone_error_outline_24)
                        .timeout(10_000)
                        .into(attachment)
                }
            }
            attachment.isVisible = event.attachment != null

            menu.isVisible = event.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
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