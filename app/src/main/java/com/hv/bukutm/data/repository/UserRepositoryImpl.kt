package com.hv.bukutm.data

import android.util.Log
import com.google.gson.Gson
import com.hv.bukutm.data.api.UsersApi
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.model.UserResponse
import com.hv.bukutm.domain.repository.UserRepository
import com.hv.bukutm.domain.model.CreateUserRequest
import com.hv.bukutm.domain.model.UpdateUserRequest
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.Result

class UserRepositoryImpl @Inject constructor(
    private val api: UsersApi,
) : UserRepository {

    override suspend fun getUsers(token: String, role: String?): Result<UserResponse> {
        return try {
            if (token.isBlank()) throw IllegalArgumentException("Token cannot be empty")
            Log.d("UserRepositoryImpl", "getUsers: token=$token, role=$role")
            val response = api.getUsers("Bearer $token", role = role)
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("UserRepositoryImpl", "getUsers HTTP error: ${e.code()} ${e.message()}")
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("UserRepositoryImpl", "getUsers network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "getUsers error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getUserById(token: String, id: Int): Result<User> {
        return try {
            if (token.isBlank()) throw IllegalArgumentException("Token cannot be empty")
            if (id <= 0) throw IllegalArgumentException("ID must be positive")
            Log.d("UserRepositoryImpl", "getUserById: token=$token, id=$id")
            val user = api.getUserById("Bearer $token", id.toString())
            Result.success(user)
        } catch (e: HttpException) {
            Log.e("UserRepositoryImpl", "getUserById HTTP error: ${e.code()} ${e.message()}")
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("UserRepositoryImpl", "getUserById network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "getUserById error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun createUser(token: String, request: CreateUserRequest): Result<User> {
        return try {
            if (token.isBlank()) throw IllegalArgumentException("Token cannot be empty")
            if (request == null) throw IllegalArgumentException("Request cannot be null")
            Log.d("UserRepositoryImpl", "createUser: token=$token, request=$request")
            val user = api.createUser("Bearer $token", request)
            Result.success(user)
        } catch (e: HttpException) {
            Log.e("UserRepositoryImpl", "createUser HTTP error: ${e.code()} ${e.message()}")
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("UserRepositoryImpl", "createUser network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "createUser error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateUser(token: String, id: Int, request: UpdateUserRequest): Result<User> {
        return try {
            if (token.isBlank()) throw IllegalArgumentException("Token cannot be empty")
            if (id <= 0) throw IllegalArgumentException("ID must be positive")
            if (request == null) throw IllegalArgumentException("Request cannot be null")
            Log.d("UserRepositoryImpl", "updateUser: token=$token, id=$id, request=$request")
            val user = api.updateUser("Bearer $token", id.toString(), request)
            Result.success(user)
        } catch (e: HttpException) {
            Log.e("UserRepositoryImpl", "updateUser HTTP error: ${e.code()} ${e.message()}")
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("UserRepositoryImpl", "updateUser network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "updateUser error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(token: String, id: Int): Result<Unit> {
        return try {
            if (token.isBlank()) throw IllegalArgumentException("Token cannot be empty")
            if (id <= 0) throw IllegalArgumentException("ID must be positive")
            Log.d("UserRepositoryImpl", "deleteUser: token=$token, id=$id")
            api.deleteUser("Bearer $token", id.toString())
            Result.success(Unit)
        } catch (e: HttpException) {
            Log.e("UserRepositoryImpl", "deleteUser HTTP error: ${e.code()} ${e.message()}")
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("UserRepositoryImpl", "deleteUser network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "deleteUser error: ${e.message}")
            Result.failure(e)
        }
    }
}