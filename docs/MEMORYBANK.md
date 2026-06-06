# Memory Bank — Still

> Single source of truth. Read this at the start of every session. Update after every confirmed change.

---

## Project Identity

| Field | Value |
|-------|-------|
| App Name | **Still** |
| Package | `com.still.app` |
| Platform | Android (first), PC (later — do not discuss yet) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 36 (Android 16) |
| Status | **Phase 2 in progress** |

---

## Vision

> "Açtığında rahatlatıyor, yazdığında hız kesmiyor, baktığında gurur duyuyorsun."

A calm, focused, AI-powered note-taking app. Not another feature-heavy tool. A writing experience. Every design decision serves the feeling: **Calm Power**.

---

## Design Language

| Property | Value |
|----------|-------|
| Default theme | Calm Luxury |
| Color mode | Auto (follows system dark/light) |
| Animation speed | 220–300ms, subtle spring, no aggressive bounce |
| Typography | Lora (titles/headings) + Inter Light (body) — Google Fonts |
| Available themes | Calm Luxury (default), Liquid Glass, Flat Minimal (Phase 2) |

### Calm Luxury Principles
- Deep dark background (`#14131C` pill, `#16151F` cards)
- Gold accent `#B8A369` — dividers, active states, FAB
- Large, breathable typography — Inter Light body, never system font weight
- Minimal chrome — content is king
- Transitions feel like breathing, not clicking

---

## Navigation

- **Pill bottom nav — 2 tabs + center FAB:**
  - 🏠 Notes (left)
  - ➕ New note FAB (center, gold gradient, offset up)
  - ⚙️ Settings (right)
- Active tab: gold tint + 3dp dot indicator + icon bg circle
- Press feedback: immediate scale down via `collectIsPressedAsState`
- Search: icon in Notes list top bar → full-screen overlay
- Nav transitions: fade for tab switches, slide for push/pop

---

## Screen Map

| Screen | Description |
|--------|-------------|
| Onboarding | 3 screens: theme card previews, dark/light confirm, feature mode |
| Notes List | List view default, Card toggle, pinned section, search icon, sort |
| Note Editor | Full-screen, title field + gold divider + body field, formatting toolbar |
| Search | Full-screen overlay, real-time results |
| Settings | Grouped: Appearance, Writing, About |

---

## Feature Registry

### Phase 1 — MVP

| Feature | Status | Default |
|---------|--------|---------|
| Note CRUD | 🟢 Done | ON |
| Silent autosave (1.5s debounce) | 🟢 Done | ON |
| Card / List toggle | 🟢 Done | **List** |
| Pinned notes | 🟢 Done | — |
| Date display | 🟢 Done | ON |
| Dark / Light (auto) | 🟢 Done | Auto |
| Calm Luxury theme | 🟢 Done | ON |
| Formatting toolbar | 🟢 Done | ON |
| Undo / Redo | 🟢 Done | ON |
| Onboarding (3 screens) | 🟢 Done | — |
| Search (real-time) | 🟢 Done | — |

### Phase 2 — Experience

| Feature | Status | Default |
|---------|--------|---------|
| AI inline completion | 🟢 Done | OFF |
| Ghost text rendering | 🟢 Done | — |
| Accept ghost (Tab key + toolbar button) | 🟢 Done | — |
| Variants (long-press → bottom sheet) | 🟢 Done | — |
| No-internet degradation | 🟢 Done | — |
| Focus mode | 🔴 Not started | OFF |
| Typewriter mode | 🔴 Not started | OFF |
| Writing stats + streak | 🔴 Not started | OFF |
| Liquid Glass theme | 🔴 Not started | — |
| Flat Minimal theme | 🔴 Not started | — |
| Accent color picker | 🔴 Not started | — |
| Font selection | 🔴 Not started | — |

### Phase 3 — Power

| Feature | Status | Default |
|---------|--------|---------|
| Natural language search | 🔴 Not started | OFF |
| In-note AI query | 🔴 Not started | OFF |
| Bi-directional linking | 🔴 Not started | OFF |
| Smart export | 🔴 Not started | OFF |
| Voice-to-note | 🔴 Not started | OFF |

### Phase 4 — Social & Sync

| Feature | Status | Default |
|---------|--------|---------|
| Read-only share link | 🔴 Not started | — |
| Google Drive sync | 🔴 Not started | OFF |
| Collaborative editing | 🔴 Not started | — |

---

## AI Completion Behavior

- **Model:** `gemini-3.1-flash-lite`
- **API:** `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=API_KEY`
- **Trigger:** 400ms pause after typing (+ min request interval guard)
- **Display:** Ghost text — muted italic `#60B8A369`, inline after cursor via AnnotatedString
- **Accept:** Tab key (physical) or Tab button in formatting toolbar
- **Variants:** Long-press hint → `RequestVariants` event → bottom sheet with 2–3 alternatives
- **Dismiss:** Continue typing (automatic — ghost stripped in onValueChange)
- **No internet:** Inline error "İnternet bağlantısı gerekli"
- **Loading:** `CircularProgressIndicator` in top bar actions

---

## Editor Architecture

- Single `BasicTextField` — title + body in one `TextFieldValue`
- Title = first line, styled SemiBold headlineSmall via `AnnotatedString`
- Gold gradient divider rendered in `decorationBox` Column when `\n` present
- Body uses `MarkdownVisualTransformation` — segment-based, marker-hidden, stable `OffsetMapping`
- Ghost text appended to `AnnotatedString` display only — stripped in `onValueChange` via `realTextLength`
- Bullet auto-continue on Enter
- `skipNextUndoPush` flag prevents double undo push on toolbar actions

---

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.2.20 |
| UI | Jetpack Compose | BOM 2026.04.01 |
| Material | Material 3 | 1.4.0 (via BOM) |
| DI | Hilt | 2.57.1 |
| Navigation | Navigation Compose | via BOM |
| Database | Room | 2.8.4 |
| Async | Coroutines + Flow | 1.10.x |
| AI | Gemini API | `gemini-3.1-flash-lite` |
| Fonts | Google Fonts (Compose) | via BOM |
| Icons | Lucide (composables) | 2.2.1 |
| Annotation Processing | KSP | 2.2.20-2.0.3 |
| Build | Gradle KTS + Version Catalog | — |

> ⚠️ Room 3.0 is alpha — staying on 2.8.4 until stable.

---

## Project Structure

```
com.still.app/
├── ui/
│   ├── notes/          # NotesListScreen + ViewModel
│   ├── editor/         # NoteEditorScreen + ViewModel
│   ├── search/         # SearchScreen + ViewModel
│   ├── settings/       # SettingsScreen + ViewModel
│   ├── onboarding/     # OnboardingScreen + ViewModel
│   ├── navigation/     # StillNavHost, Routes
│   ├── theme/          # StillTheme, Color, Type, Shape
│   └── components/     # StillBottomNav, NoteCard, NoteListItem,
│                       # StillDropdownMenu, StillSnackbar, SwipeToDeleteBox
├── domain/
│   ├── model/          # Note
│   ├── repository/     # NoteRepository, GeminiRepository interfaces
│   └── usecase/        # One file per use case
├── data/
│   ├── local/          # NoteDao, StillDatabase, NoteEntity, NoteMapper
│   └── repository/     # NoteRepositoryImpl, GeminiRepositoryImpl
├── di/                 # DatabaseModule, RepositoryModule, GeminiModule, DataStoreModule
└── util/               # Constants, DataFormatter, MarkdownRenderer
```

---

## Key Decisions Log

| Date | Decision | Reason |
|------|----------|--------|
| 2026-05 | App name: **Still** | Timeless, universal, matches Calm Power vision |
| 2026-05 | 2-tab bottom nav | Simplicity — more tabs = visual noise |
| 2026-05 | FAB in pill nav center | New note always accessible, no floating FAB overlap |
| 2026-05 | Search not in tab bar | Search is an action, not a place |
| 2026-05 | First line = title | No separate title field — reduces friction |
| 2026-05 | Toolbar fixed above keyboard | Floatable toolbar adds complexity, not value |
| 2026-05 | Typewriter mode OFF by default | New concept, onboarding complexity |
| 2026-05 | Room 2.8.4 (not 3.0 alpha) | Stability first |
| 2026-05 | No notifications | Keeps app calm and non-intrusive |
| 2026-05 | Local storage first, Drive sync in Phase 4 | Drive API adds complexity — ship core first |
| 2026-05 | AI features OFF by default | User starts simple, opts in to complexity |
| 2026-05 | Autosave debounce: **1.5s** | Imperceptible to user, good battery/perf balance |
| 2026-05 | Delete = swipe-left → StillSnackbar + undo | Modern, 3s timeout |
| 2026-05 | Default accent color: `#B8A369` (mat altın) | Calm Luxury — sessiz lüks hissi |
| 2026-05 | Default view: **List** (not Card) | More information density, modern feel |
| 2026-05 | Inter Light for body text | System font too heavy — eye strain on long reads |
| 2026-05 | Marker-hidden markdown via VisualTransformation | Obsidian-style — clean writing experience |
| 2026-05 | Ghost text via AnnotatedString, not separate field | Single field = stable cursor, no offset issues |
| 2026-05 | Tab key accepts ghost | Natural, fast, no hand movement needed |
| 2026-06 | Nav transitions: fade tabs, slide push/pop | Tab = same level, push/pop = depth |

---

## Open Questions

| Question | Status |
|----------|--------|
| App icon design | 🔴 Pending — direction: thin horizontal wave on deep dark bg |

---

## Session Log

| Session | What was done |
|---------|--------------|
| Session 1 | Full product planning — vision, features, UI, navigation, stack, naming |
| Session 2 | Phase 1 implementation — data layer, domain, theme, all screens |
| Session 3 | Bug fixes — cursor reset, swipe-to-delete, instant delete, markdown render, H dropdown, floating pill nav, StillDropdownMenu, liquid glass toolbar |
| Session 4 | FAB → pill nav center, Search/Settings wired, double padding fix, AnnotatedString markdown refactor, pin→bottom sheet, Inter+Lora fonts, onboarding redesign, nav transitions, bottom nav redesign (press feedback, dot indicator), list view default + card redesign, editor title divider, AI ghost text restore, Tab accept |