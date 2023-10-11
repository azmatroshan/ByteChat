package com.azmat.bytechat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id:String="",
    val name:String="",
    val phone:String="",
    val imgProfile:String="",
    val fcmToken: String=""
): Parcelable
