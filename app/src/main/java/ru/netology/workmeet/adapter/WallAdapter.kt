package ru.netology.workmeet.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.UserCardPostBinding
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.dto.UserPreview
import ru.netology.workmeet.util.AndroidUtils.toDate

class WallAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : PagingDataAdapter<Post, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position) is Post) R.layout.user_card_post else error("unknown item type")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding =
            UserCardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WallViewHolder(binding, onInteractionListener, appAuth)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item is Post) {
            (holder as? WallViewHolder)?.bind(item)

        } else error("unknown view type")
    }

}

class WallViewHolder(
    private val binding: UserCardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {

        val childAdapter = UserPreviewAdapter(
            post.users, object: OnUserListener {
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
            published.text = toDate(post.published)
            content.text = post.content
            like.isChecked = post.likedByMe
            attachmentContainer.isVisible = post.attachment != null
            attachment.visibility = View.GONE
            video.visibility = View.GONE
            pause.visibility = View.GONE
            play.visibility = View.GONE

            likeOwners.adapter = childAdapter
            likeOwners.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

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
                if (appAuth.state.replayCache.last().id != 0L) {
                    onInteractionListener.onLike(post)
                } else if (appAuth.state.replayCache.last().id == 0L) {
                    like.isChecked = false
                    like.isEnabled = false
                    onInteractionListener.onAuth()
                }
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
