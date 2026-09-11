package com.sahilmaske.peerlearn.PeerRecomdaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sahilmaske.peerlearn.data.model.SkillMatchModel
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.repository.SkillMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: SkillMatchRepository = SkillMatchRepository()
) : ViewModel() {
    private val _matches = MutableStateFlow<List<SkillMatchModel>>(emptyList())
    val matches: StateFlow<List<SkillMatchModel>> = _matches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMatches(currentUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _matches.value = repository.getRuleBasedMatches(currentUser)
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }
}


@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel = viewModel(),
    currentUser: User
) {
    val matches by viewModel.matches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Screen pehli baar dikhte hi ye ek baar chalega
    LaunchedEffect(Unit) {
        viewModel.loadMatches(currentUser)
    }

    RecommendationContent(
        matches = matches,
        isLoading = isLoading
    )
}

@Composable
fun RecommendationContent(
    matches: List<SkillMatchModel>,
    isLoading: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            matches.isEmpty() -> {
                Text(
                    text = "No matches found yet",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    items(matches) { match ->
                        MatchCard(match = match)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: SkillMatchModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = match.matchedUserName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = match.matchReason, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Match: ${(match.matchScore * 100).toInt()}%")
        }
    }


}

@Preview(showBackground = true)
@Composable
fun RecommendationScreenPreview() {
    MaterialTheme {
        RecommendationContent(
            matches = listOf(
                SkillMatchModel("1", "Sahil Maske", 0.95f, "Expert in Jetpack Compose"),
                SkillMatchModel("2", "John Doe", 0.8f, "Strong backend skills"),
                SkillMatchModel("3", "Jane Smith", 0.6f, "Interested in UI/UX")
            ),
            isLoading = false
        )
    }
}
