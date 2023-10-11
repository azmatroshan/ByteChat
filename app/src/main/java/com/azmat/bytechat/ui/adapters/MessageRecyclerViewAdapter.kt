package com.azmat.bytechat.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azmat.bytechat.databinding.ReceivedMessageItemBinding
import com.azmat.bytechat.databinding.SentMessageItemBinding
import com.azmat.bytechat.models.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@SuppressLint("NotifyDataSetChanged")
class MessageRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataList = mutableListOf<Message>()

    fun setData(dataList: List<Message>) {
        this.dataList = dataList as MutableList<Message>
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        if (!dataList.contains(message)) {
            dataList.add(message)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                ViewHolderReceived(
                    ReceivedMessageItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                ViewHolderSent(
                    SentMessageItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = dataList[position]
        when (holder) {
            is ViewHolderReceived -> {
                holder.bind(currentItem)
            }
            is ViewHolderSent -> {
                holder.bind(currentItem, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        val firebaseUser = Firebase.auth.currentUser
        return if (dataList[position].senderId == firebaseUser?.uid) {
            1
        } else 0
    }

    inner class ViewHolderReceived(private val binding: ReceivedMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            binding.tvMessage.text = item.message
        }
    }

    inner class ViewHolderSent(private val binding: SentMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message, position: Int) {
            binding.tvMessage.text = item.message
            /*
            if(position==dataList.size-1){
                if(item.seen){
                    binding.seen.visibility = View.VISIBLE
                }
                else{
                    binding.seen.text = itemView.context.getString(R.string.sent)
                    binding.seen.visibility = View.VISIBLE
                }
            }
            */
        }
    }
}
