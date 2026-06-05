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
import com.still.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class NoteEditorUiState(
    val noteId: Long = -1L,
    // Raw markdown string — single source of truth for storage
    val rawText: String = "",
    // Cursor/selection state — kept separate so AnnotatedString can be built in UI
    val selection: TextRange = TextRange.Zero,
    val isPinned: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleted: Boolean = false,
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface NoteEditorEvent {
    // Called on every keystroke — carries raw text + current selection
    data class ContentChanged(val raw: String, val selection: TextRange) : NoteEditorEvent
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
@OptIn(FlowPreview::class)
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

    // Undo/Redo stack stores raw text + selection snapshots
    private data class EditorSnapshot(val raw: String, val selection: TextRange)
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()
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
                        _uiState.update { s ->
                            s.copy(
                                rawText = text,
                                selection = TextRange(text.length),
                                isPinned = note.isPinned,
                                isLoading = false,
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }

        // Silent autosave — 1.5s debounce after last text change
        _uiState
            .drop(1)
            .debounce(Constants.AUTOSAVE_DEBOUNCE_MS)
            .distinctUntilChanged { old, new -> old.rawText == new.rawText }
            .onEach { save() }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ContentChanged -> {
                pushUndo()
                redoStack.clear()
                _uiState.update { it.copy(rawText = event.raw, selection = event.selection) }
            }

            NoteEditorEvent.Undo -> {
                if (undoStack.isNotEmpty()) {
                    pushRedo()
                    val snap = undoStack.removeLast()
                    _uiState.update { it.copy(rawText = snap.raw, selection = snap.selection) }
                }
            }

            NoteEditorEvent.Redo -> {
                if (redoStack.isNotEmpty()) {
                    pushUndo()
                    val snap = redoStack.removeLast()
                    _uiState.update { it.copy(rawText = snap.raw, selection = snap.selection) }
                }
            }

            NoteEditorEvent.TogglePin -> {
                val newPin = !_uiState.value.isPinned
                _uiState.update { it.copy(isPinned = newPin) }
                val id = _uiState.value.noteId
                if (id != -1L) viewModelScope.launch { pinNote(id, newPin) }
            }

            NoteEditorEvent.DeleteNote -> {
                viewModelScope.launch {
                    val id = _uiState.value.noteId
                    if (id != -1L) deleteNote(id)
                    _uiState.update { it.copy(isDeleted = true) }
                }
            }

            NoteEditorEvent.ApplyBold      -> applyInlineFormat("**")
            NoteEditorEvent.ApplyItalic    -> applyInlineFormat("_")
            NoteEditorEvent.ApplyUnderline -> applyInlineFormat("__")
            NoteEditorEvent.ApplyHeading   -> applyLinePrefix("## ")
            NoteEditorEvent.ApplyBullet    -> applyLinePrefix("- ")
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private suspend fun save() {
        val state = _uiState.value
        if (state.isLoading || state.isDeleted) return

        val fullText = state.rawText.trim()
        if (fullText.isBlank()) return

        val lines = fullText.lines()
        val title = lines.firstOrNull()?.take(200) ?: ""
        val body  = lines.drop(1).joinToString("\n").trim()

        _uiState.update { it.copy(isSaving = true) }

        if (state.noteId == -1L) {
            val newId = createNote(title, body)
            _uiState.update { it.copy(noteId = newId, isSaving = false) }
        } else {
            updateNote(
                Note(
                    id        = state.noteId,
                    title     = title,
                    content   = body,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isPinned  = state.isPinned,
                )
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    private fun applyInlineFormat(marker: String) {
        val state  = _uiState.value
        val text   = state.rawText
        val sel    = state.selection

        pushUndo()
        redoStack.clear()

        val (newText, newCursor) = if (sel.length > 0) {
            val selected = text.substring(sel.start, sel.end)
            val t = text.substring(0, sel.start) + "$marker$selected$marker" + text.substring(sel.end)
            t to sel.end + marker.length * 2
        } else {
            val t = text.substring(0, sel.start) + "$marker$marker" + text.substring(sel.start)
            t to sel.start + marker.length
        }

        _uiState.update { it.copy(rawText = newText, selection = TextRange(newCursor)) }
    }

    private fun applyLinePrefix(prefix: String) {
        val state  = _uiState.value
        val text   = state.rawText
        val cursor = state.selection.start

        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val already   = text.startsWith(prefix, lineStart)

        pushUndo()
        redoStack.clear()

        val (newText, newCursor) = if (already) {
            val t = text.substring(0, lineStart) + text.substring(lineStart + prefix.length)
            t to (cursor - prefix.length).coerceAtLeast(lineStart)
        } else {
            val t = text.substring(0, lineStart) + prefix + text.substring(lineStart)
            t to cursor + prefix.length
        }

        _uiState.update { it.copy(rawText = newText, selection = TextRange(newCursor)) }
    }

    // ── Undo/Redo helpers ─────────────────────────────────────────────────────

    private fun pushUndo() {
        val s = _uiState.value
        if (undoStack.size >= MAX_STACK) undoStack.removeFirst()
        undoStack.addLast(EditorSnapshot(s.rawText, s.selection))
    }

    private fun pushRedo() {
        val s = _uiState.value
        if (redoStack.size >= MAX_STACK) redoStack.removeFirst()
        redoStack.addLast(EditorSnapshot(s.rawText, s.selection))
    }

    private fun buildDisplayText(note: Note): String =
        if (note.content.isBlank()) note.title else "${note.title}\n${note.content}"
}