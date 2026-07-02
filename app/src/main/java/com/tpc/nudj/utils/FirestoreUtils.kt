package com.tpc.nudj.utils

import com.tpc.nudj.model.ClubUser
import com.tpc.nudj.model.NormalUser
import com.tpc.nudj.model.enums.ClubCategory
import com.tpc.nudj.model.enums.Role

object FirestoreUtils{

    inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, defaultValue: T): T {
        if (value == null) return defaultValue
        return try {
            java.lang.Enum.valueOf(T::class.java, value.uppercase())
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun getEnumDisplayName(enum: Any?): String {
        return when(enum) {
            is ClubCategory -> enum.categoryName
            else -> enum?.toString() ?: ""
        }
    }


    fun toMap(clubUser: ClubUser): Map<String, Any?> {
        return mapOf(
            "clubId" to clubUser.clubId,
            "clubName" to clubUser.clubName,
            "description" to clubUser.description,
            "achievementsList" to clubUser.achievementsList,
            "clubEmail" to clubUser.clubEmail,
            "clubLogo" to clubUser.clubLogo,
            "clubCategory" to clubUser.clubCategory.name,
            "clubCategoryName" to getEnumDisplayName(clubUser.clubCategory),
            "additionalDetails" to clubUser.additionalDetails,
            "role" to clubUser.role.name.lowercase(),
            "verificationStatus" to clubUser.verificationStatus
        )
    }

    fun toClubUser(data: Map<String,Any?>) : ClubUser{
        val roleStr = (data["role"] as? String)?.uppercase() ?: "CLUB"
        return ClubUser(
            clubId = (data["clubId"] as? String) ?: "",
            clubName = (data["clubName"] as? String) ?: "",
            description = (data["description"] as? String) ?: "",
            achievementsList = (data["achievementsList"] as? List<String>) ?: emptyList(),
            clubEmail = (data["clubEmail"] as? String) ?: "",
            clubLogo = data["clubLogo"] as? String,
            clubCategory = enumValueOrDefault(data["clubCategory"] as? String, ClubCategory.MISCELLANEOUS),
            additionalDetails = (data["additionalDetails"] as? String) ?: "",
            role = enumValueOrDefault(roleStr, Role.CLUB),
            verificationStatus = (data["verificationStatus"] as? String) ?: "pending"
        )
    }

    fun toMap(normalUser: NormalUser): Map<String, Any?> {
        return mapOf(
            "userid" to normalUser.userid,
            "name" to normalUser.name,
            "email" to normalUser.email,
            "rollNo" to normalUser.rollNo,
            "batch" to normalUser.batch,
            "profilePictureUrl" to normalUser.profilePictureUrl,
            "bio" to normalUser.bio,
            "role" to normalUser.role.name.lowercase()
        )
    }

    fun toNormalUser(data: Map<String, Any?>): NormalUser {
        val roleStr = (data["role"] as? String)?.uppercase() ?: "USER"
        return NormalUser(
            userid = (data["userid"] as? String) ?: "",
            name = (data["name"] as? String) ?: "",
            email = (data["email"] as? String) ?: "",
            rollNo = (data["rollNo"] as? String) ?: "",
            batch = (data["batch"] as? Long)?.toInt() ?: 2024,
            profilePictureUrl = data["profilePictureUrl"] as? String,
            bio = (data["bio"] as? String) ?: "",
            role = enumValueOrDefault(roleStr, Role.USER)
        )
    }
}