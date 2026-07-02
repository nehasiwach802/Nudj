package com.tpc.nudj.model

import com.tpc.nudj.model.enums.ClubCategory
import com.tpc.nudj.model.enums.Role

data class ClubUser(
    val clubId: String = "",
    val clubName: String = "",
    val description: String = "",
    val achievementsList: List<String> = emptyList(),
    val clubEmail: String = "",
    val clubLogo: String? = null,
    val clubCategory: ClubCategory = ClubCategory.MISCELLANEOUS,
    val additionalDetails: String = "",
    val role: Role = Role.CLUB,
    val verificationStatus: String = "pending"
)
