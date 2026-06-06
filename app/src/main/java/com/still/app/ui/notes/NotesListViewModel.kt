package com.still.app.ui.notes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.domain.model.Note
import com.still.app.domain.usecase.DeleteNoteUseCase
import com.still.app.domain.usecase.GetAllNotesUseCase
import com.still.app.domain.usecase.PinNoteUseCase
import com.still.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NoteViewMode  { CARD, LIST }
enum class NoteSortOrder { LAST_MODIFIED, CREATED, TITLE }

data class NotesListUiState(
    val pinnedNotes: List<Note>   = emptyList(),
    val unpinnedNotes: List<Note> = emptyList(),
    val viewMode: NoteViewMode    = NoteViewMode.LIST,
    val sortOrder: NoteSortOrder  = NoteSortOrder.LAST_MODIFIED,
    val pendingDeleteNote: Note?  = null,
    val isLoading: Boolean        = true,
)

sealed interface NotesListEvent {
    data class DeleteNote(val note: Note)    : NotesListEvent
    data object UndoDelete                   : NotesListEvent
    data object ConfirmDelete                : NotesListEvent
    data class TogglePin(val note: Note)     : NotesListEvent
    data class SetViewMode(val mode: NoteViewMode)    : NotesListEvent
    data class SetSortOrder(val order: NoteSortOrder) : NotesListEvent
}

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val getAllNotes: GetAllNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val pinNote: PinNoteUseCase,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val viewModeKey = stringPreferencesKey(Constants.PrefKeys.NOTE_VIEW_MODE)

    private val viewModeFlow = dataStore.data.map { prefs ->
        when (prefs[viewModeKey]) {
            "card" -> NoteViewMode.CARD
            "list" -> NoteViewMode.LIST
            else   -> NoteViewMode.LIST   // default: list
        }
    }

    private val sortOrderFlow     = MutableStateFlow(NoteSortOrder.LAST_MODIFIED)
    private val pendingDeleteFlow = MutableStateFlow<Note?>(null)

    val uiState = combine(
        getAllNotes(),
        viewModeFlow,
        sortOrderFlow,
        pendingDeleteFlow,
    ) { notes, viewMode, sortOrder, pendingDelete ->

        val sorted = when (sortOrder) {
            NoteSortOrder.LAST_MODIFIED -> notes.sortedByDescending { it.updatedAt }
            NoteSortOrder.CREATED       -> notes.sortedByDescending { it.createdAt }
            NoteSortOrder.TITLE         -> notes.sortedBy { it.title.lowercase() }
        }

        val visible = if (pendingDelete != null)
            sorted.filter { it.id != pendingDelete.id }
        else
            sorted

        NotesListUiState(
            pinnedNotes       = visible.filter { it.isPinned },
            unpinnedNotes     = visible.filter { !it.isPinned },
            viewMode          = viewMode,
            sortOrder         = sortOrder,
            pendingDeleteNote = pendingDelete,
            isLoading         = false,
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesListUiState(),
    )

    fun onEvent(event: NotesListEvent) {
        when (event) {
            is NotesListEvent.DeleteNote -> pendingDeleteFlow.update { event.note }

            NotesListEvent.UndoDelete    -> pendingDeleteFlow.update { null }

            NotesListEvent.ConfirmDelete -> {
                val note = pendingDeleteFlow.value ?: return
                viewModelScope.launch {
                    deleteNote(note.id)
                    pendingDeleteFlow.update { null }
                }
            }

            is NotesListEvent.TogglePin -> viewModelScope.launch {
                pinNote(event.note.id, !event.note.isPinned)
            }

            is NotesListEvent.SetViewMode -> viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[viewModeKey] = when (event.mode) {
                        NoteViewMode.CARD -> "card"
                        NoteViewMode.LIST -> "list"
                    }
                }
            }

            is NotesListEvent.SetSortOrder -> sortOrderFlow.update { event.order }
        }
    }
}