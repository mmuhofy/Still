package com.still.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.domain.model.Note
import com.still.app.domain.usecase.SearchNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface SearchUiState {
    data object Idle : SearchUiState                       // query is blank
    data object Loading : SearchUiState                    // debounce in-flight
    data class Results(val notes: List<Note>) : SearchUiState
    data object Empty : SearchUiState                      // query non-blank, 0 results
}

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent
    data object ClearQuery : SearchEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

private const val SEARCH_DEBOUNCE_MS = 200L

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchNotes: SearchNotesUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val uiState = _query
        .debounce(SEARCH_DEBOUNCE_MS)
        .flatMapLatest { q ->
            if (q.isBlank()) {
                kotlinx.coroutines.flow.flowOf(SearchUiState.Idle)
            } else {
                searchNotes(q).map { results ->
                    if (results.isEmpty()) SearchUiState.Empty
                    else SearchUiState.Results(results)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Idle,
        )

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> _query.update { event.query }
            SearchEvent.ClearQuery -> _query.update { "" }
        }
    }
}