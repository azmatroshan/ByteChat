package com.azmat.bytechat.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.azmat.bytechat.R
import com.azmat.bytechat.databinding.ChatItemBinding
import com.azmat.bytechat.models.User
import com.azmat.bytechat.models.UserChat
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatRecyclerViewAdapter: RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {
    private var dataList = mutableListOf<UserChat>()

    var onItemClick: ((User)-> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<UserChat>) {
        this.dataList = dataList as MutableList<UserChat>
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
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(dataList[position].user)
                }
            }
            itemView.background = AppCompatResources.getDrawable(itemView.context, R.drawable.click_ripple)
        }

        fun bind(item: UserChat) {
            binding.name.text = item.user.name
            if(item.user.imgProfile!=""){
                Glide.with(itemView.context)
                    .load(item.user.imgProfile)
                    .centerCrop()
                    .dontAnimate()
                    .placeholder(R.drawable.img_profile)
                    .into(binding.profileImg)
            }
            if(item.chat.id==item.chat.senderId){
                binding.lastMessage.text = item.chat.lastMessage
            }
            else{
                binding.lastMessage.text = String.format(itemView.context.getString(R.string.last_message), item.chat.lastMessage)
            }
            binding.timestamp.text = formatTimestamp(item.chat.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val currentTimeMillis = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            val date = Date(timestamp)
            val currentDate = Date(currentTimeMillis)

            val calendarDate = Calendar.getInstance()
            val calendarCurrentDate = Calendar.getInstance()
            calendarDate.time = date
            calendarCurrentDate.time = currentDate

            val dayDate = calendarDate.get(Calendar.DAY_OF_MONTH)
            val monthDate = calendarDate.get(Calendar.MONTH)
            val yearDate = calendarDate.get(Calendar.YEAR)

            val dayCurrentDate = calendarCurrentDate.get(Calendar.DAY_OF_MONTH)
            val monthCurrentDate = calendarCurrentDate.get(Calendar.MONTH)
            val yearCurrentDate = calendarCurrentDate.get(Calendar.YEAR)

            return if (dayDate == dayCurrentDate && monthDate == monthCurrentDate && yearDate == yearCurrentDate) {
                timeFormat.format(date)
            }
            else if(dayDate+1 == dayCurrentDate && monthDate == monthCurrentDate && yearDate == yearCurrentDate) {
                itemView.context.getString(R.string.yesterday)
            } else {
                dateFormat.format(date)
            }
        }
    }

}