package com.azmat.bytechat.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.azmat.bytechat.R
import com.azmat.bytechat.models.User
import com.azmat.bytechat.databinding.ChatItemBinding

class ChatRecyclerViewAdapter: RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {
    private var dataList: List<User> = emptyList()

    var onItemClick: ((User)-> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<User>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = binding.root.verticalScrollbarPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(dataList[position])
                }
            }
            itemView.background = AppCompatResources.getDrawable( itemView.context, R.drawable.click_ripple)
        }

        fun bind(item: User) {
            binding.name.text = item.name
            binding.lastMessage.text = item.lastMessage
            binding.lastSeen.text = item.lastSeen
        }
    }

}