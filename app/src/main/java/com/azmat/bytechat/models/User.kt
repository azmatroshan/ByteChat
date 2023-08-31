package com.azmat.bytechat.models

data class User(
    val id:String="",
    val name:String="",
    val email:String="",
    val lastMessage:String="",
    val lastSeen:String="",
    val imgProfile:String=""
)
