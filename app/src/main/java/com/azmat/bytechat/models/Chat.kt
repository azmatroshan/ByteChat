package com.azmat.bytechat.models

data class Chat(
    var id: String="",
    var lastMessage: String="",
    var timestamp: Long=0,
    var senderId: String=""
)
