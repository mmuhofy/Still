package com.still.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.domain.model.Note
import com.still.app.domain.usecase.CreateNoteUseCase
import com.still.app.domain.usecase.DeleteNoteUseCase
import com.still.app.domain.usecase.GetAiCompletionUseCase
import com.still.app.domain.usecase.GetAiCompletionVariantsUseCase
import com.still.app.domain.usecase.GetNoteByIdUseCase
import com.still.app.domain.usecase.PinNoteUseCase
import com.still.app.domain.usecase.UpdateNoteUseCase
import com.still.app.ui.navigation.Routes
import com.still.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    val ghostText: String = "",
    val realTextLength: Int = 0,
    val isAiLoading: Boolean = false,
    val aiError: String? = null,
    val showVariants: Boolean = false,
    val variants: List<String> = emptyList(),
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface NoteEditorEvent {
    data class ContentChanged(val value: TextFieldValue) : NoteEditorEvent
    data object TogglePin : NoteEditorEvent
    data object DeleteNote : NoteEditorEvent
    data object ApplyBold : NoteEditorEvent
    data object ApplyItalic : NoteEditorEvent
    data object ApplyUnderline : NoteEditorEvent
    // level: 1 = "# ", 2 = "## ", 3 = "### "
    data class ApplyHeading(val level: Int) : NoteEditorEvent
    data object ApplyBullet : NoteEditorEvent
    data object Undo : NoteEditorEvent
    data object Redo : NoteEditorEvent
    data object AcceptGhost : NoteEditorEvent
    data object DismissGhost : NoteEditorEvent
    data object RequestVariants : NoteEditorEvent
    data class AcceptVariant(val text: String) : NoteEditorEvent
    data object DismissVariants : NoteEditorEvent
}

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNoteById: GetNoteByIdUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val pinNote: PinNoteUseCase,
    private val getAiCompletion: GetAiCompletionUseCase,
    private val getAiVariants: GetAiCompletionVariantsUseCase,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val noteId: Long = savedStateHandle[Routes.ARG_NOTE_ID] ?: -1L

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = noteId))
    val uiState = _uiState.asStateFlow()

    private val aiEnabledFlow = dataStore.data
        .map { it[booleanPreferencesKey(Constants.PrefKeys.AI_ENABLED)] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val aiEnabled get() = aiEnabledFlow.value

    private var skipNextUndoPush = false
    private var aiJob: Job? = null
    private var lastAiRequestAt: Long = 0L

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
                                realTextLength = text.length,
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }

        _uiState
            .drop(1)
            .map { it.content.text }
            .distinctUntilChanged()
            .onEach { save() }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ContentChanged -> {
                val state = _uiState.value
                val incoming = event.value

                // Enter while ghost active → accept ghost
                if (state.ghostText.isNotBlank()) {
                    val realLen = state.realTextLength
                    val realText = incoming.text.take(realLen)
                    val pressedEnter = incoming.text.length > realLen &&
                            incoming.text.getOrNull(realLen) == '\n'
                    if (pressedEnter) {
                        acceptGhostInternal()
                        return
                    }
                    val cleaned = TextFieldValue(
                        text = realText + incoming.text.drop(
                            realLen + state.ghostText.length
                        ).let { if (incoming.text.length > realLen + state.ghostText.length) it else incoming.text.drop(realLen) },
                        selection = incoming.selection.let {
                            TextRange(it.start.coerceAtMost(realText.length + 1))
                        },
                        composition = incoming.composition,
                    )
                    clearGhostAndProcess(cleaned)
                    return
                }

                val current = state.content
                if (skipNextUndoPush) {
                    skipNextUndoPush = false
                } else {
                    pushUndo(current)
                    redoStack.clear()
                }

                val newValue = handleBulletContinuation(current, incoming)
                _uiState.update {
                    it.copy(
                        content = newValue,
                        ghostText = "",
                        realTextLength = newValue.text.length,
                        aiError = null,
                    )
                }
                scheduleAiCompletion(newValue.text)
            }

            NoteEditorEvent.Undo -> {
                if (undoStack.isNotEmpty()) {
                    redoStack.addLast(_uiState.value.content)
                    _uiState.update { it.copy(content = undoStack.removeLast(), ghostText = "") }
                }
            }

            NoteEditorEvent.Redo -> {
                if (redoStack.isNotEmpty()) {
                    undoStack.addLast(_uiState.value.content)
                    _uiState.update { it.copy(content = redoStack.removeLast(), ghostText = "") }
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

            // H1 = "# ", H2 = "## ", H3 = "### "
            is NoteEditorEvent.ApplyHeading -> {
                val prefix = "#".repeat(event.level.coerceIn(1, 3)) + " "
                applyLinePrefix(prefix)
            }

            NoteEditorEvent.ApplyBullet    -> applyLinePrefix("- ")

            NoteEditorEvent.AcceptGhost    -> acceptGhostInternal()

            NoteEditorEvent.DismissGhost -> {
                aiJob?.cancel()
                _uiState.update { it.copy(ghostText = "", isAiLoading = false) }
            }

            NoteEditorEvent.RequestVariants -> {
                val context = _uiState.value.content.text
                if (context.isBlank()) return
                viewModelScope.launch {
                    _uiState.update { it.copy(isAiLoading = true) }
                    getAiVariants(context, Constants.AI_COMPLETION_VARIANTS)
                        .onSuccess { variants ->
                            _uiState.update {
                                it.copy(
                                    variants = variants,
                                    showVariants = variants.isNotEmpty(),
                                    isAiLoading = false,
                                )
                            }
                        }
                        .onFailure {
                            _uiState.update { it.copy(isAiLoading = false) }
                        }
                }
            }

            is NoteEditorEvent.AcceptVariant -> {
                val current = _uiState.value.content
                val newText = current.text + event.text
                val newValue = TextFieldValue(newText, selection = TextRange(newText.length))
                pushUndo(current)
                _uiState.update {
                    it.copy(
                        content = newValue,
                        ghostText = "",
                        realTextLength = newValue.text.length,
                        showVariants = false,
                        variants = emptyList(),
                    )
                }
            }

            NoteEditorEvent.DismissVariants -> {
                _uiState.update { it.copy(showVariants = false, variants = emptyList()) }
            }
        }
    }

    // ── Accept ghost ──────────────────────────────────────────────────────────

    private fun acceptGhostInternal() {
        val state = _uiState.value
        val ghost = state.ghostText
        if (ghost.isBlank()) return
        val current = state.content
        val newText = current.text + ghost
        val newValue = TextFieldValue(newText, selection = TextRange(newText.length))
        pushUndo(current)
        _uiState.update {
            it.copy(content = newValue, ghostText = "", realTextLength = newValue.text.length)
        }
    }

    // ── Dismiss ghost + apply keystroke ───────────────────────────────────────

    private fun clearGhostAndProcess(value: TextFieldValue) {
        val current = _uiState.value.content
        pushUndo(current)
        redoStack.clear()
        val newValue = handleBulletContinuation(current, value)
        _uiState.update {
            it.copy(content = newValue, ghostText = "", realTextLength = newValue.text.length, aiError = null)
        }
        scheduleAiCompletion(newValue.text)
    }

    // ── AI trigger ────────────────────────────────────────────────────────────

    private fun scheduleAiCompletion(text: String) {
        if (!aiEnabled) {
            _uiState.update { it.copy(isAiLoading = false, ghostText = "") }
            return
        }
        if (text.length < Constants.AI_MIN_TEXT_LENGTH) return

        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            delay(Constants.AI_TRIGGER_DEBOUNCE_MS)
            val now = System.currentTimeMillis()
            val elapsed = now - lastAiRequestAt
            if (elapsed < Constants.AI_MIN_REQUEST_INTERVAL_MS) {
                delay(Constants.AI_MIN_REQUEST_INTERVAL_MS - elapsed)
            }
            lastAiRequestAt = System.currentTimeMillis()
            _uiState.update { it.copy(isAiLoading = true, aiError = null) }
            getAiCompletion(text)
                .onSuccess { suggestion ->
                    _uiState.update {
                        it.copy(ghostText = suggestion?.trim() ?: "", isAiLoading = false)
                    }
                }
                .onFailure { err ->
                    val isNetwork = err is java.net.UnknownHostException ||
                            err is java.net.SocketTimeoutException ||
                            err is java.net.ConnectException
                    val isRateLimit = err.message?.contains("429") == true
                    _uiState.update {
                        it.copy(
                            ghostText = "",
                            isAiLoading = false,
                            aiError = when {
                                isRateLimit -> null
                                isNetwork   -> "İnternet bağlantısı gerekli"
                                else        -> null
                            },
                        )
                    }
                }
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private suspend fun save() {
        val state = _uiState.value
        if (state.isLoading || state.isDeleted) return
        val fullText = state.content.text.trim()
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
                    id = state.noteId,
                    title = title,
                    content = body,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isPinned = state.isPinned,
                )
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    // ── Bullet auto-continue ──────────────────────────────────────────────────

    private fun handleBulletContinuation(previous: TextFieldValue, next: TextFieldValue): TextFieldValue {
        val prev = previous.text
        val new  = next.text
        val cursor = next.selection.start

        if (new.length != prev.length + 1) return next
        if (cursor == 0 || new[cursor - 1] != '\n') return next

        val lineStart = prev.lastIndexOf('\n', cursor - 2) + 1
        val completedLine = prev.substring(lineStart, cursor - 1)

        return if (completedLine.startsWith("- ")) {
            if (completedLine.trim() == "-") {
                val stripped = new.substring(0, cursor - 1) + "\n" + new.substring(cursor)
                TextFieldValue(stripped, selection = TextRange(cursor))
            } else {
                val inserted = new.substring(0, cursor) + "- " + new.substring(cursor)
                TextFieldValue(inserted, selection = TextRange(cursor + 2))
            }
        } else next
    }

    // ── Inline format (wraps selection or inserts empty markers) ─────────────

    private fun applyInlineFormat(marker: String) {
        val current = _uiState.value.content
        val sel  = current.selection
        val text = current.text

        val newText: String
        val newCursor: Int

        if (sel.length > 0) {
            val selected = text.substring(sel.start, sel.end)
            newText   = text.substring(0, sel.start) + "$marker$selected$marker" + text.substring(sel.end)
            newCursor = sel.end + marker.length * 2
        } else {
            newText   = text.substring(0, sel.start) + "$marker$marker" + text.substring(sel.start)
            newCursor = sel.start + marker.length
        }

        pushUndo(current)
        skipNextUndoPush = true
        _uiState.update {
            it.copy(
                content = TextFieldValue(newText, selection = TextRange(newCursor)),
                ghostText = "",
                realTextLength = newText.length,
            )
        }
    }

    // ── Line prefix (toggle: adds or removes) ─────────────────────────────────

    private fun applyLinePrefix(prefix: String) {
        val current = _uiState.value.content
        val text    = current.text
        val cursor  = current.selection.start

        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1

        // If line already has this exact prefix, remove it
        // If line has a DIFFERENT heading prefix, replace it
        val existingHeadingRegex = Regex("""^#{1,3} """)
        val existingMatch = existingHeadingRegex.find(text.substring(lineStart))

        val newText: String
        val newCursor: Int

        when {
            text.startsWith(prefix, lineStart) -> {
                // Toggle off — same prefix already present
                newText   = text.substring(0, lineStart) + text.substring(lineStart + prefix.length)
                newCursor = (cursor - prefix.length).coerceAtLeast(lineStart)
            }
            existingMatch != null && prefix.matches(Regex("""#{1,3} """)) -> {
                // Replace existing heading level with new one
                val oldPrefix = existingMatch.value
                newText   = text.substring(0, lineStart) + prefix + text.substring(lineStart + oldPrefix.length)
                newCursor = cursor + (prefix.length - oldPrefix.length)
            }
            else -> {
                // Add prefix
                newText   = text.substring(0, lineStart) + prefix + text.substring(lineStart)
                newCursor = cursor + prefix.length
            }
        }

        pushUndo(current)
        skipNextUndoPush = true
        _uiState.update {
            it.copy(
                content = TextFieldValue(newText, selection = TextRange(newCursor.coerceAtLeast(0))),
                ghostText = "",
                realTextLength = newText.length,
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun pushUndo(value: TextFieldValue) {
        if (undoStack.size >= MAX_STACK) undoStack.removeFirst()
        undoStack.addLast(value)
    }

    private fun buildDisplayText(note: Note): String =
        if (note.content.isBlank()) note.title else "${note.title}\n${note.content}"
}