package com.azmat.bytechat.models

import java.util.Date

data class Message(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var messageId: String = "",
    var time: Long = Date().time,
    var seen: Boolean = false
){
    override fun equals(other: Any?): Boolean {
        return if(other is Message){
            other.messageId == messageId
        } else return false
    }

    override fun hashCode(): Int {
        var result = senderId.hashCode()
        result = 31 * result + receiverId.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + seen.hashCode()
        return result
    }
}
