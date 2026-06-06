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
- 🟢 Define typography scale — Inter (body) + Lora (titles) via Google Fonts
- 🟢 Define shape scheme
- 🟢 Wire theme to system dark/light setting

### 📱 UI — Onboarding

- 🟢 Onboarding screen 1: Visual theme selection with card previews (Calm Luxury default)
- 🟢 Onboarding screen 2: Dark/Light confirm (pre-selected based on system)
- 🟢 Onboarding screen 3: Feature mode — "Sadece ben" vs "Ben + AI"
- 🟢 Save onboarding state to DataStore — never show again after completion
- 🟢 Wire onboarding to app start navigation
- 🟢 Animated page indicator + pill CTA button

### 📱 UI — Notes List

- 🟢 `NotesListScreen` — scaffold, top bar with search icon
- 🟢 `NotesListViewModel` — load notes, handle pin/delete actions
- 🟢 Card view component — title, preview, date, pin indicator
- 🟢 List view component — title, preview, date, pin indicator — default view
- 🟢 Card / List toggle (icon in top bar, persisted in DataStore)
- 🟢 Pinned section (appears above regular notes when any note is pinned)
- 🟢 Swipe-to-delete on note card (custom background, rounded corners match item)
- 🟢 Empty state — friendly message when no notes
- 🟢 StillDropdownMenu — Calm Luxury styled dropdown used throughout app
- 🟢 StillSnackbar — custom frosted glass snackbar with undo

### 📱 UI — Note Editor

- 🟢 `NoteEditorScreen` — full-screen, back button, `···` bottom sheet menu
- 🟢 `NoteEditorViewModel` — load note, autosave, undo/redo stack
- 🟢 Title field (headlineSmall, SemiBold) + gold gradient divider + body field
- 🟢 Silent autosave — 1.5s debounce after last keystroke
- 🟢 Formatting toolbar above keyboard — Bold, Italic, Underline, H1/H2/H3 dropdown, Bullet, Undo, Redo
- 🟢 Liquid Glass toolbar styling
- 🟢 Undo / Redo in toolbar
- 🟢 Bottom sheet menu: Pin/Unpin, Delete
- 🟢 Markdown visual transformation — marker-hidden, segment-based OffsetMapping
- 🟢 Bullet auto-continue on Enter
- 🟢 Tab key / toolbar Tab button accepts ghost text

### 📱 UI — Search

- 🟢 `SearchScreen` — full-screen overlay, activated from Notes list icon
- 🟢 `SearchViewModel` — real-time search as user types
- 🟢 Results list — same card style as Notes list
- 🟢 Empty state for no results
- 🟢 Tap result → opens Note Editor

### 📱 UI — Settings

- 🟢 `SettingsScreen` — grouped list
- 🟢 Appearance group: Dark/Light/Auto toggle
- 🟢 Writing group: AI completion, Focus mode, Typewriter mode toggles (Phase 2 placeholders)
- 🟢 About group: app version

### 📱 UI — Navigation & Components

- 🟢 Pill bottom nav — Notes | FAB(+) | Settings
- 🟢 Press feedback on nav items (collectIsPressedAsState, spring bounce)
- 🟢 Active tab: gold tint + dot indicator + icon bg circle
- 🟢 FAB: gold gradient, glow shadow, press scale
- 🟢 Nav transitions — fade for tabs, slide for push/pop
- 🟢 Onboarding → Notes list: fade transition

### ✅ Phase 1 Complete

- [x] Can create, edit, delete, pin notes
- [x] Autosave works silently
- [x] Card and List view both work (default: List)
- [x] Search returns real-time results
- [x] Onboarding shown once on first launch
- [x] Theme follows system dark/light
- [x] Undo/Redo works in editor
- [x] Formatting toolbar renders markdown visually

---

## 🐛 Known Bugs

- 🟡 Tab accept button in toolbar — visible logic implemented, build not yet verified
- ⛔ App icon final design pending

---

## Phase 2 — Experience

- 🟢 AI inline completion (Gemini API — `gemini-3.1-flash-lite`)
- 🟢 Ghost text rendering — muted italic, inline after cursor via AnnotatedString
- 🟢 Accept ghost: Tab key or toolbar Tab button
- 🟢 Variants: long-press ghost hint → bottom sheet with alternatives
- 🟢 No-internet graceful degradation — "İnternet bağlantısı gerekli" inline error
- 🟢 AI loading indicator in top bar (CircularProgressIndicator)
- 🔴 Focus mode (hide all chrome, only text visible)
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
- Inter + Lora loaded via Google Fonts (compose-ui-google-fonts) — requires GMS on device