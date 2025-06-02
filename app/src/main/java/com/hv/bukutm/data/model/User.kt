package com.hv.bukutm.domain.model

data class User(
    val idPengguna: Int,
    val nama: String,
    val email: String,
    val role: String,
    val token: String,
    val refreshToken: String
)

data class UserResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val data: List<User>
)

data class CreateUserRequest(
    val nama: String,
    val email: String,
    val password: String,
    val role: String
)

data class UpdateUserRequest(
    val nama: String,
    val email: String,
    val role: String
)