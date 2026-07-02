package com.tpc.nudj.model

import com.tpc.nudj.model.enums.Role


data class NormalUser(
    val userid :String = "",
    val name: String = "",
    val email :String = "",
    val rollNo: String = "",
    val batch : Int = 2024,
    val profilePictureUrl :String? = null,
    val bio :String = "",
    val role: Role = Role.USER
)