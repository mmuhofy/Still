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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.still.app.ui.components.NoteCard
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.still.app.ui.components.NoteListItem
import com.still.app.ui.components.StillSnackbar
import com.still.app.ui.components.SwipeToDeleteBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    topPadding: Dp,
    bottomPadding: Dp,
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

    // FAB height (56dp) + spacing (20dp) + bottom nav
    val fabSpacing = 56.dp + 20.dp + bottomPadding

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        IconButton(onClick = onSearchClick) {
                            Icon(Lucide.Search, contentDescription = "Ara", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(Lucide.ArrowUpDown, contentDescription = "Sırala", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        IconButton(onClick = {
                            val next = if (state.viewMode == NoteViewMode.CARD) NoteViewMode.LIST else NoteViewMode.CARD
                            viewModel.onEvent(NotesListEvent.SetViewMode(next))
                        }) {
                            Icon(
                                imageVector = if (state.viewMode == NoteViewMode.CARD) Lucide.List else Lucide.LayoutGrid,
                                contentDescription = "Görünümü değiştir",
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
        ) { innerPadding ->
            if (state.isLoading) return@Scaffold

            val allEmpty = state.pinnedNotes.isEmpty() && state.unpinnedNotes.isEmpty()

            AnimatedVisibility(visible = allEmpty, enter = fadeIn(), exit = fadeOut()) {
                EmptyState(modifier = Modifier.padding(innerPadding))
            }

            AnimatedVisibility(visible = !allEmpty, enter = fadeIn(), exit = fadeOut()) {
                NotesList(
                    state = state,
                    topPadding = innerPadding.calculateTopPadding(),
                    bottomPadding = fabSpacing,
                    onNoteClick = onNoteClick,
                    onLongClick = { contextMenuNote = it },
                    viewModel = viewModel,
                )
            }
        }

        StillSnackbar(
            visible = state.pendingDeleteNote != null,
            message = "Not silindi",
            onAction = { viewModel.onEvent(NotesListEvent.UndoDelete) },
            onDismiss = { viewModel.onEvent(NotesListEvent.ConfirmDelete) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = fabSpacing + 8.dp),
        )
    }

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
    topPadding: Dp,
    bottomPadding: Dp,
    onNoteClick: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
    viewModel: NotesListViewModel,
) {
    if (state.viewMode == NoteViewMode.CARD) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding + 8.dp,
                bottom = bottomPadding,
                start = 12.dp,
                end = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.pinnedNotes.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) { SectionLabel("Sabitlenmiş") }
                items(state.pinnedNotes, key = { it.id }) { note ->
                    SwipeToDeleteBox(onDeleted = { viewModel.onEvent(NotesListEvent.DeleteNote(note)) }) {
                        NoteCard(note = note, onClick = { onNoteClick(note.id) }, onLongClick = { onLongClick(note.id) })
                    }
                }
                if (state.unpinnedNotes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) { SectionLabel("Notlar", topPadding = true) }
                }
            }
            items(state.unpinnedNotes, key = { it.id }) { note ->
                SwipeToDeleteBox(onDeleted = { viewModel.onEvent(NotesListEvent.DeleteNote(note)) }) {
                    NoteCard(note = note, onClick = { onNoteClick(note.id) }, onLongClick = { onLongClick(note.id) })
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding + 8.dp,
                bottom = bottomPadding,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.Top,
        ) {
            if (state.pinnedNotes.isNotEmpty()) {
                item { SectionLabel("Sabitlenmiş") }
                items(state.pinnedNotes, key = { it.id }) { note ->
                    SwipeToDeleteBox(onDeleted = { viewModel.onEvent(NotesListEvent.DeleteNote(note)) }) {
                        NoteListItem(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onLongClick = { onLongClick(note.id) },
                            showDivider = note != state.pinnedNotes.last(),
                        )
                    }
                }
                if (state.unpinnedNotes.isNotEmpty()) {
                    item { SectionLabel("Notlar", topPadding = true) }
                }
            }
            items(state.unpinnedNotes, key = { it.id }) { note ->
                SwipeToDeleteBox(onDeleted = { viewModel.onEvent(NotesListEvent.DeleteNote(note)) }) {
                    NoteListItem(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onLongClick = { onLongClick(note.id) },
                        showDivider = note != state.unpinnedNotes.last(),
                    )
                }
            }
        }
    }
}

// ── Section Label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, topPadding: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = if (topPadding) 12.dp else 4.dp, bottom = 6.dp),
    )
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
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
                        else MaterialTheme.colorScheme.onSurface,
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
    Box {
        DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
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
                    Text("Sil", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                },
                onClick = onDelete,
            )
        }
    }
}