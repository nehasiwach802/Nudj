package com.tpc.nudj.utils

object Validator {
    fun isValidEmail(email: String): Result<String> {
        val emailSplit = email.split("@")

        if (emailSplit.size < 2 || emailSplit[1].isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid Email"))
        }

        if (emailSplit[1] != "iiitdmj.ac.in") {
            return Result.failure(IllegalArgumentException("Enter institute Email Only."))
        }

        return Result.success(email)
    }
}