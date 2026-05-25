package com.still.app.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.still.app.ui.components.NoteCard
import com.still.app.ui.components.NoteListItem
import com.still.app.ui.components.StillSnackbar
import com.still.app.ui.components.SwipeToDeleteBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuNote by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Still",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    // Search
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Ara",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Sort
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                Icons.Outlined.Sort,
                                contentDescription = "Sırala",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        SortDropdownMenu(
                            expanded = sortMenuExpanded,
                            currentOrder = state.sortOrder,
                            onOrderSelected = {
                                viewModel.onEvent(NotesListEvent.SetSortOrder(it))
                                sortMenuExpanded = false
                            },
                            onDismiss = { sortMenuExpanded = false },
                        )
                    }

                    // View mode toggle
                    IconButton(
                        onClick = {
                            val next = if (state.viewMode == NoteViewMode.CARD)
                                NoteViewMode.LIST else NoteViewMode.CARD
                            viewModel.onEvent(NotesListEvent.SetViewMode(next))
                        }
                    ) {
                        Icon(
                            imageVector = if (state.viewMode == NoteViewMode.CARD)
                                Icons.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = "Görünümü değiştir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Settings
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Ayarlar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewNote,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni not")
            }
        },
    ) { innerPadding ->

        // Loading — skeleton or just blank
        if (state.isLoading) return@Scaffold

        val allEmpty = state.pinnedNotes.isEmpty() && state.unpinnedNotes.isEmpty()

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = allEmpty,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                EmptyState(modifier = Modifier.padding(innerPadding))
            }

            AnimatedVisibility(
                visible = !allEmpty,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                NotesList(
                    state = state,
                    innerPadding = innerPadding,
                    onNoteClick = onNoteClick,
                    onLongClick = { contextMenuNote = it },
                    viewModel = viewModel,
                )
            }

            // Calm Luxury snackbar — floats above FAB
            StillSnackbar(
                visible = state.pendingDeleteNote != null,
                message = "Not silindi",
                onAction = { viewModel.onEvent(NotesListEvent.UndoDelete) },
                onDismiss = { viewModel.onEvent(NotesListEvent.ConfirmDelete) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp), // above FAB
            )
        }
    }

    // Context menu — long press
    if (contextMenuNote != null) {
        val note = (state.pinnedNotes + state.unpinnedNotes)
            .firstOrNull { it.id == contextMenuNote }
        if (note != null) {
            NoteContextMenu(
                note = note,
                onDismiss = { contextMenuNote = null },
                onPin = {
                    viewModel.onEvent(NotesListEvent.TogglePin(note))
                    contextMenuNote = null
                },
                onDelete = {
                    viewModel.onEvent(NotesListEvent.DeleteNote(note))
                    contextMenuNote = null
                },
            )
        }
    }
}

// ── Notes List ────────────────────────────────────────────────────────────────

@Composable
private fun NotesList(
    state: NotesListUiState,
    innerPadding: PaddingValues,
    onNoteClick: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
    viewModel: NotesListViewModel,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 88.dp, // FAB clearance
            start = if (state.viewMode == NoteViewMode.CARD) 16.dp else 16.dp,
            end = if (state.viewMode == NoteViewMode.CARD) 16.dp else 16.dp,
        ),
        verticalArrangement = if (state.viewMode == NoteViewMode.CARD)
            Arrangement.spacedBy(10.dp) else Arrangement.Top,
    ) {
        // Pinned section
        if (state.pinnedNotes.isNotEmpty()) {
            item {
                SectionLabel(text = "Sabitlenmiş")
            }
            items(state.pinnedNotes, key = { it.id }) { note ->
                NoteItem(
                    note = note,
                    viewMode = state.viewMode,
                    isLast = note == state.pinnedNotes.last(),
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { onLongClick(note.id) },
                    onDelete = { viewModel.onEvent(NotesListEvent.DeleteNote(note)) },
                )
            }
            if (state.unpinnedNotes.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)) }
                item { SectionLabel(text = "Notlar") }
            }
        }

        // Unpinned notes
        items(state.unpinnedNotes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                viewMode = state.viewMode,
                isLast = note == state.unpinnedNotes.last(),
                onClick = { onNoteClick(note.id) },
                onLongClick = { onLongClick(note.id) },
                onDelete = { viewModel.onEvent(NotesListEvent.DeleteNote(note)) },
            )
        }
    }
}

@Composable
private fun NoteItem(
    note: com.still.app.domain.model.Note,
    viewMode: NoteViewMode,
    isLast: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    SwipeToDeleteBox(onDeleted = onDelete) {
        when (viewMode) {
            NoteViewMode.CARD -> NoteCard(
                note = note,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            NoteViewMode.LIST -> NoteListItem(
                note = note,
                onClick = onClick,
                onLongClick = onLongClick,
                showDivider = !isLast,
            )
        }
    }
}

// ── Section Label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 6.dp, top = 4.dp),
    )
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Henüz not yok",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "+ ile yeni bir not oluştur",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Sort Dropdown ─────────────────────────────────────────────────────────────

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentOrder: NoteSortOrder,
    onOrderSelected: (NoteSortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        listOf(
            NoteSortOrder.LAST_MODIFIED to "Son düzenleme",
            NoteSortOrder.CREATED to "Oluşturma tarihi",
            NoteSortOrder.TITLE to "Başlık",
        ).forEach { (order, label) ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = label,
                        color = if (currentOrder == order)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                onClick = { onOrderSelected(order) },
            )
        }
    }
}

// ── Context Menu ──────────────────────────────────────────────────────────────

@Composable
private fun NoteContextMenu(
    note: com.still.app.domain.model.Note,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
) {
    // Rendered as a simple DropdownMenu anchored to Box at origin.
    // Full bottom sheet context menu comes in polish pass.
    Box {
        DropdownMenu(
            expanded = true,
            onDismissRequest = onDismiss,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (note.isPinned) "Sabitlemeyi kaldır" else "Sabitle",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                onClick = onPin,
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Sil",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = onDelete,
            )
        }
    }
}