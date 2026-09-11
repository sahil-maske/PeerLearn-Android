package com.sahilmaske.peerlearn.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahilmaske.peerlearn.data.model.SkillMatchModel
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.repository.SkillMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SkillMatchViewModel(
    private val repository: SkillMatchRepository = SkillMatchRepository()
) : ViewModel() {

    private val _matches = MutableStateFlow<List<SkillMatchModel>>(emptyList())
    val matches: StateFlow<List<SkillMatchModel>> = _matches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadMatches(currentUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _matches.value = repository.getRuleBasedMatches(currentUser)
            _isLoading.value = false
        }
    }
}