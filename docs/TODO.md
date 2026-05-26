# TODO — Still

> Tasks are ordered by priority within each phase. Complete Phase 1 fully before touching Phase 2.
> Status: 🔴 Not started | 🟡 In progress | 🟢 Done | ⛔ Blocked

---

## Phase 1 — MVP

### 🏗️ Project Setup

- 🟢 Create Android project (Kotlin, Jetpack Compose, Min SDK 26, Target SDK 36)
- 🟢 Set up `gradle/libs.versions.toml` with all verified dependency versions
- 🟢 Configure Hilt (2.57.1) — app-level setup, `@HiltAndroidApp`
- 🟢 Configure KSP plugin
- 🟢 Configure Room (2.8.4)
- 🟢 Set up project folder structure (`ui/`, `domain/`, `data/`, `di/`, `util/`)
- 🟢 Set up Compose BOM (2026.04.01) and Material 3
- 🟢 Configure Navigation Compose
- 🟢 Add `Constants.kt` to `util/`
- 🟢 Set up base theme (Calm Luxury dark/light)

### 🗄️ Data Layer

- 🟢 Define `Note` entity (Room) — `id`, `title`, `content`, `createdAt`, `updatedAt`, `isPinned`
- 🟢 Create `NoteDao` — insert, update, delete, getAll, getById, search
- 🟢 Create `StillDatabase` — Room database class
- 🟢 Define `NoteRepository` interface in `domain/`
- 🟢 Implement `NoteRepositoryImpl` in `data/`
- 🟢 Create Hilt module to bind repository

### 🧠 Domain Layer

- 🟢 `GetAllNotesUseCase`
- 🟢 `GetNoteByIdUseCase`
- 🟢 `CreateNoteUseCase`
- 🟢 `UpdateNoteUseCase`
- 🟢 `DeleteNoteUseCase`
- 🟢 `PinNoteUseCase`
- 🟢 `SearchNotesUseCase`

### 🎨 Theme

- 🟢 Define Calm Luxury color palette (dark + light)
- 🟢 Define typography scale (title serif-weight, body, caption)
- 🟢 Define shape scheme
- 🟢 Wire theme to system dark/light setting

### 📱 UI — Onboarding

- 🟢 Onboarding screen 1: Visual theme selection (Calm Luxury default)
- 🟢 Onboarding screen 2: Dark/Light confirm (pre-selected based on system)
- 🟢 Onboarding screen 3: Feature mode — "Sade başla" vs "AI ile başla"
- 🟢 Save onboarding state to DataStore — never show again after completion
- 🟢 Wire onboarding to app start navigation

### 📱 UI — Notes List

- 🟢 `NotesListScreen` — scaffold, FAB, top bar with search icon
- 🟢 `NotesListViewModel` — load notes, handle pin/delete actions
- 🟢 Card view component — title, 2-line preview, date, pin indicator
- 🟢 List view component — title, 1-line preview, date, pin indicator
- 🟢 Card / List toggle (icon in top bar, persisted in DataStore)
- 🟢 Pinned section (appears above regular notes when any note is pinned)
- 🟢 Swipe-to-delete on note card (with confirmation bottom sheet)
- 🟢 Swipe-to-pin on note card
- 🟢 Empty state — friendly message when no notes

### 📱 UI — Note Editor

- 🟢 `NoteEditorScreen` — full-screen, back button, `···` overflow menu
- 🟢 `NoteEditorViewModel` — load note, autosave, undo/redo stack
- 🟢 First line = title (larger weight, subtle divider below)
- 🟢 Silent autosave — triggers instantly on content change (distinctUntilChanged)
- 🟢 Formatting toolbar above keyboard (Bold, Italic, Underline, Heading, Bullet list)
- 🟢 Toolbar rises with keyboard, stays fixed
- 🟢 Undo / Redo in toolbar
- 🟢 Overflow menu (`···`): Pin/Unpin, Delete, Share (placeholder)
- 🟢 Delete with confirmation bottom sheet

### 📱 UI — Search

- 🟢 `SearchScreen` — full-screen overlay, activated from Notes list icon
- 🟢 `SearchViewModel` — real-time search as user types
- 🟢 Results list — same card style as Notes list
- 🟢 Empty state for no results
- 🟢 Tap result → opens Note Editor

### 📱 UI — Settings

- 🟢 `SettingsScreen` — grouped list
- 🟢 Appearance group: Theme (Calm Luxury only in Phase 1), Dark/Light toggle
- 🟢 Writing group: (placeholder for Phase 2 features, all OFF)
- 🟢 About group: app version, licenses

### ✅ Phase 1 Complete When

- [x] Can create, edit, delete, pin notes
- [x] Autosave works silently
- [x] Card and List view both work
- [x] Search returns real-time results
- [x] Onboarding shown once on first launch
- [x] Theme follows system dark/light
- [x] Undo/Redo works in editor
- [x] Formatting toolbar renders markdown visually (bold, italic, underline, heading, bullet)

---

## 🐛 Known Bugs (Phase 1 — deferred)

- ⛔ Formatting toolbar buttons not working reliably — state conflict between toolbar actions and `ContentChanged` event, italic regex collision, selection loss after format apply. Deferred to post-Phase 2.

---

## Phase 2 — Experience

- 🔴 AI inline completion (Gemini API integration — `gemini-3.1-flash-lite`)
- 🔴 Ghost text rendering in editor
- 🔴 Accept on tap, variants on long-press
- 🔴 No-internet graceful degradation for AI
- 🔴 Focus mode (hide all chrome, only text)
- 🔴 Typewriter mode (active line centered vertically)
- 🔴 Writing statistics screen (words today, streak, best session)
- 🔴 Streak logic + persistence
- 🔴 Liquid Glass theme implementation
- 🔴 Flat Minimal theme implementation
- 🔴 Accent color picker (5–6 curated options)
- 🔴 Font selection (3 options)

---

## Phase 3 — Power

- 🔴 Natural language search integration
- 🔴 In-note AI query (`[[` trigger → AI search across notes)
- 🔴 Bi-directional linking (`[[note name]]` syntax)
- 🔴 Smart export — PDF, email format, presentation outline
- 🔴 Voice-to-note (microphone → transcript → cleaned text)

---

## Phase 4 — Social & Sync

- 🔴 Read-only share link generation
- 🔴 Google Drive sync (user-opt-in)
- 🔴 Collaborative editing (real-time)

---

## Notes & Blockers

- Room 3.0 is in alpha — revisit when stable, currently using 2.8.4
- Google Drive sync deferred to Phase 4 due to API complexity
- App icon final design pending