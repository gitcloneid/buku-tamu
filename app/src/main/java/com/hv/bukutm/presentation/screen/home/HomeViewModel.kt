package com.hv.bukutm.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val tokenManager: TokenManager
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearTokens()
        }
    }
}