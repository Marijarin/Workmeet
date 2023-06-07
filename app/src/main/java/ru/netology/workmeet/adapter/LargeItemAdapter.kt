package ru.netology.workmeet.adapter


import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardEventBinding
import ru.netology.workmeet.databinding.CardPostBinding
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.util.AndroidUtils.toDate


interface OnInteractionListener {
    fun onLike(item: FeedItem) {}
    fun onEdit(item: FeedItem) {}
    fun onRemove(item: FeedItem) {}
    fun onAuth() {}
    fun onAvatar(userId: Long) {}
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

    fun bind(post: Post) {

        val childAdapterLikes = UserPreviewAdapter(
            post.users.filterKeys {
                post.likeOwnerIds.contains(it.toLong())
            }, object : OnUserListener {
                override fun onAvatar(userPreview: UserPreview) {
                    val userId = post.users
                        .filterValues { it == userPreview }
                        .keys
                        .first()
                        .toLong()
                    onInteractionListener.onAvatar(userId)
                }
            }
        )
        val childAdapterMentioned = UserPreviewAdapter(
            post.users.filterKeys {
                post.mentionIds.contains(it.toLong())
            }, object : OnUserListener {
                override fun onAvatar(userPreview: UserPreview) {
                    val userId = post.users
                        .filterValues { it == userPreview }
                        .keys
                        .first()
                        .toLong()
                    onInteractionListener.onAvatar(userId)
                }
            }
        )

        binding.apply {
            author.text = post.author
            authorJob.text = post.authorJob ?: itemView.context.getText(R.string.default_job)
            published.text = toDate(post.published)
            content.text = post.content
            like.isChecked = post.likedByMe
            attachmentContainer.isVisible = post.attachment != null
            attachment.visibility = View.GONE
            video.visibility = View.GONE
            pause.visibility = View.GONE
            play.visibility = View.GONE

            likeOwners.adapter = childAdapterLikes
            likeOwners.addItemDecoration(CustomItemDecorator(itemView.context))
            likeOwners.layoutManager =
               LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)


            mentionedOwners.adapter = childAdapterMentioned
            mentionedOwners.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

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
            attachmentContainer.let {
                if (post.attachment != null) {
                    when (post.attachment.typeA) {
                        AttachmentType.IMAGE -> {
                            attachment.visibility = View.VISIBLE
                            Glide.with(attachment)
                                .load(post.attachment.url)
                                .placeholder(R.drawable.baseline_attachment_24)
                                .error(R.drawable.twotone_error_outline_24)
                                .timeout(10_000)
                                .into(attachment)
                        }
                        AttachmentType.AUDIO -> {
                            play.visibility = View.VISIBLE
                        }
                        AttachmentType.VIDEO -> {
                            video.visibility = View.VISIBLE
                            play.visibility = View.VISIBLE
                            video.setOnClickListener {
                                onInteractionListener.onPlay(post)
                                video.apply {
                                    setMediaController(MediaController(context))
                                    setVideoURI(
                                        Uri.parse(post.attachment.url)
                                    )
                                    setOnPreparedListener {
                                        start()
                                    }
                                    setOnCompletionListener {
                                        stopPlayback()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            play.setOnClickListener {
                play.visibility = View.GONE

                if (post.attachment?.typeA == AttachmentType.AUDIO) {
                    pause.visibility = View.VISIBLE
                    onInteractionListener.onPlay(post)
                } else if (post.attachment?.typeA == AttachmentType.VIDEO) {
                    pause.visibility = View.GONE
                    video.apply {
                        onInteractionListener.onPlay(post)
                        setMediaController(MediaController(context))
                        setVideoURI(
                            Uri.parse(post.attachment.url)
                        )
                        setOnPreparedListener {
                            start()
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }
                }
            }
            pause.setOnClickListener {
                pause.visibility = View.GONE
                play.visibility = View.VISIBLE
                onInteractionListener.onPause()
            }

            if (!pause.isFocusable) {
                pause.visibility = View.GONE
                play.visibility = View.VISIBLE
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
                onInteractionListener.onAvatar(post.authorId)
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
            event.users, object : OnUserListener {
                override fun onAvatar(userPreview: UserPreview) {
                    val userId = event.users
                        .filterValues { it == userPreview }
                        .keys
                        .first()
                        .toLong()
                    onInteractionListener.onAvatar(userId)
                }
            }
        )
        binding.apply {
            author.text = event.author
            authorJob.text = event.authorJob ?: itemView.context.getText(R.string.default_job)
            datetime.text = toDate(event.datetime)
            type.text = event.type.name
            content.text = event.content
            speakersPreview.adapter = childAdapter
            participantsPreview.adapter = childAdapter
            attachmentContainer.isVisible = event.attachment != null
            attachment.visibility = View.GONE
            video.visibility = View.GONE
            pause.visibility = View.GONE
            play.visibility = View.GONE
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

            attachmentContainer.let {
                if (event.attachment != null) {
                    when (event.attachment.typeA) {
                        AttachmentType.IMAGE -> {
                            attachment.visibility = View.VISIBLE
                            Glide.with(attachment)
                                .load(event.attachment.url)
                                .placeholder(R.drawable.baseline_attachment_24)
                                .error(R.drawable.twotone_error_outline_24)
                                .timeout(10_000)
                                .into(attachment)
                        }
                        AttachmentType.AUDIO -> {
                            play.visibility = View.VISIBLE
                        }
                        AttachmentType.VIDEO -> {
                            video.visibility = View.VISIBLE
                            play.visibility = View.VISIBLE
                            video.setOnClickListener {
                                onInteractionListener.onPlay(event)
                                video.apply {
                                    setMediaController(MediaController(context))
                                    setVideoURI(
                                        Uri.parse(event.attachment.url)
                                    )
                                    setOnPreparedListener {
                                        start()
                                    }
                                    setOnCompletionListener {
                                        stopPlayback()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            play.setOnClickListener {
                play.visibility = View.GONE

                if (event.attachment?.typeA == AttachmentType.AUDIO) {
                    pause.visibility = View.VISIBLE
                    onInteractionListener.onPlay(event)
                } else if (event.attachment?.typeA == AttachmentType.VIDEO) {
                    pause.visibility = View.GONE
                    video.apply {
                        onInteractionListener.onPlay(event)
                        setMediaController(MediaController(context))
                        setVideoURI(
                            Uri.parse(event.attachment.url)
                        )
                        setOnPreparedListener {
                            start()
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }
                }
            }
            pause.setOnClickListener {
                pause.visibility = View.GONE
                play.visibility = View.VISIBLE
                onInteractionListener.onPause()
            }

            if (!pause.isFocusable) {
                pause.visibility = View.GONE
                play.visibility = View.VISIBLE
            }


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
            like.setOnClickListener {
                if (appAuth.state.value.id != 0L) {
                    onInteractionListener.onLike(event)
                } else if (appAuth.state.value.id == 0L) {
                    like.isChecked = false
                    like.isEnabled = false
                    onInteractionListener.onAuth()
                }
            }
            avatar.setOnClickListener {
                onInteractionListener.onAvatar(event.authorId)
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