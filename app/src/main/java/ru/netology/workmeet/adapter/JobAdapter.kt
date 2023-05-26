package ru.netology.workmeet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardJobBinding
import ru.netology.workmeet.databinding.CardJobBinding.inflate
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.util.AndroidUtils.toDate

interface OnInteractionSmallListener {
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
    }


class JobAdapter(
    private val onInteractionSmallListener: OnInteractionSmallListener,
    private val appAuth: AppAuth
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionSmallListener, appAuth)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)



    }
}
class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionSmallListener: OnInteractionSmallListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        binding.apply {
            name.text = job.name
            position.text = job.position
            range.text = itemView.context.getString(R.string.range, toDate(job.start), job.finish?.let {
                toDate(
                    it
                )
            }
                ?: itemView.context.getText(R.string.finish))
            link.text = job.link ?: itemView.context.getText(R.string.no_link)

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionSmallListener.onRemove(job)
                                true
                            }
                            R.id.edit -> {
                                onInteractionSmallListener.onEdit(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            menu.isVisible = appAuth.state.replayCache.last().id == job.userId

        }
    }
}
class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }

}