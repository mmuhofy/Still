package com.still.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.domain.model.Note
import com.still.app.domain.usecase.CreateNoteUseCase
import com.still.app.domain.usecase.DeleteNoteUseCase
import com.still.app.domain.usecase.GetNoteByIdUseCase
import com.still.app.domain.usecase.PinNoteUseCase
import com.still.app.domain.usecase.UpdateNoteUseCase
import com.still.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class NoteEditorUiState(
    val noteId: Long = -1L,
    val content: TextFieldValue = TextFieldValue(""),
    val isPinned: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleted: Boolean = false,
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface NoteEditorEvent {
    data class ContentChanged(val value: TextFieldValue) : NoteEditorEvent
    data object TogglePin : NoteEditorEvent
    data object DeleteNote : NoteEditorEvent
    data object ApplyBold : NoteEditorEvent
    data object ApplyItalic : NoteEditorEvent
    data object ApplyUnderline : NoteEditorEvent
    data object ApplyHeading : NoteEditorEvent
    data object ApplyBullet : NoteEditorEvent
    data object Undo : NoteEditorEvent
    data object Redo : NoteEditorEvent
}

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteById: GetNoteByIdUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val pinNote: PinNoteUseCase,
) : ViewModel() {

    private val noteId: Long = savedStateHandle[Routes.ARG_NOTE_ID] ?: -1L

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = noteId))
    val uiState = _uiState.asStateFlow()

    // ── Undo / Redo stack ─────────────────────────────────────────────────────
    private val undoStack = ArrayDeque<TextFieldValue>()
    private val redoStack = ArrayDeque<TextFieldValue>()
    private val MAX_STACK = 100

    init {
        if (noteId == -1L) {
            _uiState.update { it.copy(isLoading = false) }
        } else {
            getNoteById(noteId)
                .filterNotNull()
                .onEach { note ->
                    if (_uiState.value.isLoading) {
                        val text = buildDisplayText(note)
                        _uiState.update { state ->
                            state.copy(
                                content = TextFieldValue(text, selection = TextRange(text.length)),
                                isPinned = note.isPinned,
                                isLoading = false,
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }

        // Save on every content change — no debounce, only when text actually differs
        _uiState
            .drop(1) // skip initial emission
            .map { it.content.text }
            .distinctUntilChanged()
            .onEach { save() }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ContentChanged -> {
                pushUndo(_uiState.value.content)
                redoStack.clear()
                _uiState.update { it.copy(content = event.value) }
            }

            NoteEditorEvent.Undo -> {
                if (undoStack.isNotEmpty()) {
                    redoStack.addLast(_uiState.value.content)
                    _uiState.update { it.copy(content = undoStack.removeLast()) }
                }
            }

            NoteEditorEvent.Redo -> {
                if (redoStack.isNotEmpty()) {
                    undoStack.addLast(_uiState.value.content)
                    _uiState.update { it.copy(content = redoStack.removeLast()) }
                }
            }

            NoteEditorEvent.TogglePin -> {
                val newPinState = !_uiState.value.isPinned
                _uiState.update { it.copy(isPinned = newPinState) }
                val id = _uiState.value.noteId
                if (id != -1L) {
                    viewModelScope.launch { pinNote(id, newPinState) }
                }
            }

            NoteEditorEvent.DeleteNote -> {
                viewModelScope.launch {
                    val id = _uiState.value.noteId
                    if (id != -1L) deleteNote(id)
                    _uiState.update { it.copy(isDeleted = true) }
                }
            }

            NoteEditorEvent.ApplyBold -> applyInlineFormat("**")
            NoteEditorEvent.ApplyItalic -> applyInlineFormat("_")
            NoteEditorEvent.ApplyUnderline -> applyInlineFormat("__")
            NoteEditorEvent.ApplyHeading -> applyLinePrefix("## ")
            NoteEditorEvent.ApplyBullet -> applyLinePrefix("- ")
        }
    }

    // ── Save logic ────────────────────────────────────────────────────────────

    private suspend fun save() {
        val state = _uiState.value
        if (state.isLoading || state.isDeleted) return

        val fullText = state.content.text.trim()
        if (fullText.isBlank()) return // don't persist empty notes

        val lines = fullText.lines()
        val title = lines.firstOrNull()?.take(200) ?: ""
        val body = lines.drop(1).joinToString("\n").trim()

        _uiState.update { it.copy(isSaving = true) }

        if (state.noteId == -1L) {
            val newId = createNote(title, body)
            _uiState.update { it.copy(noteId = newId, isSaving = false) }
        } else {
            val existing = Note(
                id = state.noteId,
                title = title,
                content = body,
                createdAt = System.currentTimeMillis(), // overwritten by UpdateNoteUseCase
                updatedAt = System.currentTimeMillis(),
                isPinned = state.isPinned,
            )
            updateNote(existing)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    // ── Formatting helpers ────────────────────────────────────────────────────

    private fun applyInlineFormat(marker: String) {
        val current = _uiState.value.content
        val sel = current.selection
        val text = current.text

        val newText: String
        val newCursor: Int

        if (sel.length > 0) {
            val selected = text.substring(sel.start, sel.end)
            newText = text.substring(0, sel.start) + "$marker$selected$marker" +
                    text.substring(sel.end)
            newCursor = sel.end + marker.length * 2
        } else {
            newText = text.substring(0, sel.start) + "$marker$marker" +
                    text.substring(sel.start)
            newCursor = sel.start + marker.length
        }

        val newValue = TextFieldValue(newText, selection = TextRange(newCursor))
        pushUndo(current)
        _uiState.update { it.copy(content = newValue) }
    }

    private fun applyLinePrefix(prefix: String) {
        val current = _uiState.value.content
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val already = text.startsWith(prefix, lineStart)

        val newText: String
        val newCursor: Int

        if (already) {
            newText = text.substring(0, lineStart) + text.substring(lineStart + prefix.length)
            newCursor = (cursor - prefix.length).coerceAtLeast(lineStart)
        } else {
            newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
            newCursor = cursor + prefix.length
        }

        val newValue = TextFieldValue(newText, selection = TextRange(newCursor))
        pushUndo(current)
        _uiState.update { it.copy(content = newValue) }
    }

    private fun pushUndo(value: TextFieldValue) {
        if (undoStack.size >= MAX_STACK) undoStack.removeFirst()
        undoStack.addLast(value)
    }

    private fun buildDisplayText(note: Note): String {
        return if (note.content.isBlank()) note.title
        else "${note.title}\n${note.content}"
    }
}