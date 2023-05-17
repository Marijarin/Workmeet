package ru.netology.workmeet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.workmeet.R
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.databinding.CardJobBinding
import ru.netology.workmeet.dto.Job

interface OnInteractionSmallListener {
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
    }


class JobAdapter(
    private val onInteractionSmallListener: OnInteractionSmallListener,
    private val appAuth: AppAuth
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionSmallListener, appAuth)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)



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
            range.text = itemView.context.getString(R.string.range, job.start, job.finish?: itemView.context.getText(R.string.finish))
            link.text = job.link ?: itemView.context.getText(R.string.no_link)
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