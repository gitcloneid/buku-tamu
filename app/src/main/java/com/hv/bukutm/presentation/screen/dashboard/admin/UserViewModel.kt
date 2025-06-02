package com.hv.bukutm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.CreateUserRequest
import com.hv.bukutm.domain.model.UpdateUserRequest
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.model.UserResponse
import com.hv.bukutm.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    data class UserUiState(
        val users: List<User> = emptyList(),
        val filteredUsers: List<User> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedUser: User? = null,
        val roleFilter: String? = null,
        val searchQuery: String = "",
        val showCreateDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val validationErrors: Map<String, String> = emptyMap()
    )

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        fetchUsers()
    }

    // Input validation with user-friendly messages
    private fun validateInputs(
        nama: String,
        email: String,
        password: String? = null,
        role: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (nama.isBlank()) {
            errors["nama"] = "Nama tidak boleh kosong"
        }
        if (email.isBlank()) {
            errors["email"] = "Email tidak boleh kosong"
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            errors["email"] = "Email tidak valid"
        }
        if (password != null) {
            if (password.isBlank()) {
                errors["password"] = "Kata sandi tidak boleh kosong"
            } else if (password.length < 6) {
                errors["password"] = "Kata sandi minimal 6 karakter"
            }
        }
        if (role !in listOf("Guru", "PenerimaTamu")) {
            errors["role"] = "Pilih peran Guru atau Penerima Tamu"
        }
        return errors
    }

    fun fetchUsers(role: String? = _uiState.value.roleFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.getToken() ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sesi Anda telah habis. Silakan login kembali."
                    )
                    return@launch
                }

                userRepository.getUsers(token, role).fold(
                    onSuccess = { response: UserResponse ->
                        val filtered = if (_uiState.value.searchQuery.isBlank()) {
                            response.data
                        } else {
                            response.data.filter {
                                it.nama.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                                        it.email.contains(_uiState.value.searchQuery, ignoreCase = true)
                            }
                        }
                        _uiState.value = _uiState.value.copy(
                            users = response.data,
                            filteredUsers = filtered,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = when (error) {
                                is HttpException -> when (error.code()) {
                                    401 -> "Sesi Anda telah habis. Silakan login kembali."
                                    403 -> "Anda tidak memiliki izin untuk melihat data ini."
                                    404 -> "Data pengguna tidak ditemukan."
                                    else -> "Gagal memuat data pengguna. Silakan coba lagi."
                                }
                                is IOException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                                else -> "Terjadi kesalahan. Silakan coba lagi nanti."
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan. Silakan coba lagi nanti."
                )
            }
        }
    }

    fun searchUsers(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        val filtered = if (query.isBlank()) {
            _uiState.value.users
        } else {
            _uiState.value.users.filter { user ->
                user.nama.contains(query, ignoreCase = true) ||
                        user.email.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(filteredUsers = filtered)
    }

    fun createUser(nama: String, email: String, password: String, role: String) {
        val validationErrors = validateInputs(nama, email, password, role)
        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                validationErrors = emptyMap()
            )
            try {
                val token = tokenManager.getToken() ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sesi Anda telah habis. Silakan login kembali."
                    )
                    return@launch
                }

                val request = CreateUserRequest(nama, email, password, role)
                userRepository.createUser(token, request).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showCreateDialog = false
                        )
                        fetchUsers()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = when (error) {
                                is HttpException -> when (error.code()) {
                                    400 -> "Data yang dimasukkan tidak valid. Periksa kembali."
                                    401 -> "Sesi Anda telah habis. Silakan login kembali."
                                    403 -> "Anda tidak memiliki izin untuk membuat pengguna."
                                    409 -> "Email ini sudah terdaftar. Gunakan email lain."
                                    else -> "Gagal membuat pengguna. Silakan coba lagi."
                                }
                                is IOException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                                else -> "Terjadi kesalahan. Silakan coba lagi nanti."
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan. Silakan coba lagi nanti."
                )
            }
        }
    }

    fun updateUser(id: Int, nama: String, email: String, role: String) {
        val validationErrors = validateInputs(nama, email, null, role)
        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                validationErrors = emptyMap()
            )
            try {
                val token = tokenManager.getToken() ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sesi Anda telah habis. Silakan login kembali."
                    )
                    return@launch
                }

                val request = UpdateUserRequest(nama, email, role)
                userRepository.updateUser(token, id, request).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showEditDialog = false,
                            selectedUser = null
                        )
                        fetchUsers()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = when (error) {
                                is HttpException -> when (error.code()) {
                                    400 -> "Data yang dimasukkan tidak valid. Periksa kembali."
                                    401 -> "Sesi Anda telah habis. Silakan login kembali."
                                    403 -> "Anda tidak memiliki izin untuk mengubah data ini."
                                    404 -> "Pengguna tidak ditemukan."
                                    else -> "Gagal memperbarui pengguna. Silakan coba lagi."
                                }
                                is IOException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                                else -> "Terjadi kesalahan. Silakan coba lagi nanti."
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan. Silakan coba lagi nanti."
                )
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.getToken() ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sesi Anda telah habis. Silakan login kembali."
                    )
                    return@launch
                }

                userRepository.deleteUser(token, id).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showDeleteDialog = false,
                            selectedUser = null
                        )
                        fetchUsers()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = when (error) {
                                is HttpException -> when (error.code()) {
                                    401 -> "Sesi Anda telah habis. Silakan login kembali."
                                    403 -> "Anda tidak memiliki izin untuk menghapus pengguna."
                                    404 -> "Pengguna tidak ditemukan."
                                    else -> "Gagal menghapus pengguna. Silakan coba lagi."
                                }
                                is IOException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                                else -> "Terjadi kesalahan. Silakan coba lagi nanti."
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan. Silakan coba lagi nanti."
                )
            }
        }
    }

    fun setRoleFilter(role: String?) {
        _uiState.value = _uiState.value.copy(roleFilter = role)
        fetchUsers(role)
    }

    fun showCreateDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDialog = show, validationErrors = emptyMap())
    }

    fun showEditDialog(user: User?) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = user != null,
            selectedUser = user,
            validationErrors = emptyMap()
        )
    }

    fun showDeleteDialog(show: Boolean, user: User? = null) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = show,
            selectedUser = if (show) user else null
        )
    }

    fun retry() {
        fetchUsers()
    }

}