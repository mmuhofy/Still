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
| Status | Pre-development — planning complete |

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
| Animation speed | 280–320ms, no bounce, no elastic |
| Typography | Serif-weight for titles, clean for body |
| Available themes | Calm Luxury (default), Liquid Glass, Flat Minimal, Glassmorphism |

### Calm Luxury Principles
- Deep dark background (not pure black)
- Subtle gold accent dividers
- Large, breathable typography
- Minimal chrome — content is king
- Transitions feel like breathing, not clicking

---

## Navigation

- **Bottom tab bar — 2 tabs only:**
  - 🏠 Notes
  - ⚙️ Settings
- New note: FAB (+) on Notes list screen
- Search: icon in Notes list top bar → full-screen search overlay

---

## Screen Map

| Screen | Description |
|--------|-------------|
| Onboarding | 3 screens: theme, dark/light confirm, feature mode |
| Notes List | Card/List toggle, pinned section, search icon, FAB |
| Note Editor | Full-screen, title = first line, formatting toolbar above keyboard |
| Search | Full-screen overlay, real-time results |
| Settings | Grouped: Appearance, Writing, Advanced (advanced hidden by default) |

---

## Feature Registry

### Phase 1 — MVP

| Feature | Status | Default |
|---------|--------|---------|
| Note CRUD | 🔴 Not started | ON |
| Silent autosave | 🔴 Not started | ON |
| Card / List toggle | 🔴 Not started | Card |
| Pinned notes | 🔴 Not started | — |
| Date display | 🔴 Not started | ON |
| Dark / Light (auto) | 🔴 Not started | Auto |
| Calm Luxury theme | 🔴 Not started | ON |
| Formatting toolbar | 🔴 Not started | ON |
| Undo / Redo | 🔴 Not started | ON |
| Onboarding (3 screens) | 🔴 Not started | — |
| Search (real-time) | 🔴 Not started | — |

### Phase 2 — Experience

| Feature | Status | Default |
|---------|--------|---------|
| AI inline completion | 🔴 Not started | OFF |
| Focus mode | 🔴 Not started | OFF |
| Typewriter mode | 🔴 Not started | OFF |
| Writing stats + streak | 🔴 Not started | OFF |
| Theme picker (Liquid Glass etc.) | 🔴 Not started | — |
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

- **Model:** `claude-sonnet-4-20250514`
- **Trigger:** 400ms pause after typing
- **Display:** Ghost text — muted, italic, inline after cursor
- **Accept:** Tap ghost text
- **Variants:** Long-press ghost text → 2–3 alternatives
- **Dismiss:** Continue typing (automatic)
- **No internet:** AI icon desaturates → tap shows "İnternet bağlantısı gerekli" inline

---

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.2.20 |
| UI | Jetpack Compose | BOM 2026.04.01 |
| Material | Material 3 | 1.4.0 |
| DI | Hilt | 2.57.1 |
| Navigation | Navigation Compose | via BOM |
| Database | Room | 2.8.4 |
| Async | Coroutines + Flow | 1.10.x |
| AI | Anthropic API | claude-sonnet-4-20250514 |
| Annotation Processing | KSP | 2.2.20-1.0.x |
| Build | Gradle KTS + Version Catalog | — |

> ⚠️ Room 3.0 is alpha — staying on 2.8.4 until stable.

---

## Project Structure

```
com.still.app/
├── ui/
│   ├── notes/          # Notes list screen + ViewModel
│   ├── editor/         # Note editor screen + ViewModel
│   ├── search/         # Search screen + ViewModel
│   ├── settings/       # Settings screen + ViewModel
│   ├── onboarding/     # Onboarding screens
│   ├── theme/          # Calm Luxury + other theme definitions
│   └── components/     # Shared Compose components
├── domain/
│   ├── model/          # Note, NotePreview, etc.
│   ├── repository/     # Repository interfaces
│   └── usecase/        # One file per use case
├── data/
│   ├── local/
│   │   ├── dao/        # Room DAOs
│   │   ├── entity/     # Room entities
│   │   └── db/         # Database class
│   └── repository/     # Repository implementations
├── di/                 # Hilt modules
└── util/               # Constants, extensions, helpers
```

---

## Key Decisions Log

| Date | Decision | Reason |
|------|----------|--------|
| 2026-05 | App name: **Still** | Timeless, universal, matches Calm Power vision |
| 2026-05 | 2-tab bottom nav | Simplicity — more tabs = visual noise |
| 2026-05 | Search not in tab bar | Search is an action, not a place |
| 2026-05 | First line = title | No separate title field — reduces friction |
| 2026-05 | Toolbar fixed above keyboard | Floatable toolbar adds complexity, not value |
| 2026-05 | Typewriter mode OFF by default | New concept, onboarding complexity |
| 2026-05 | Room 2.8.4 (not 3.0 alpha) | Stability first — migrate when 3.0 is stable |
| 2026-05 | No notifications | Keeps app calm and non-intrusive |
| 2026-05 | Local storage first, Drive sync in Phase 4 | Drive API adds complexity — ship core first |
| 2026-05 | AI features OFF by default | User starts simple, opts in to complexity |
| 2026-05 | Autosave debounce: **1.5s** | 500ms = too frequent (battery/perf), 1.5s is imperceptible to user |
| 2026-05 | Onboarding has skip button | User choice — but Calm Luxury remains default if skipped |
| 2026-05 | Delete = swipe-left → custom snackbar + undo | Modern, Calm Luxury-style floating panel, 3s timeout |
| 2026-05 | Snackbar style: custom Calm Luxury component | Frosted glass, rounded, subtle — not stock Android snackbar |
| 2026-05 | Long-press note card → context menu | Pin/Unpin, Rename (inline), Delete |
| 2026-05 | Rename = inline title edit | Triggered from long-press context menu |
| 2026-05 | Sort button in notes list | Default: last modified. User can change. Separate from filter (filter = Phase 2) |
| 2026-05 | No note length limit | Room handles large text; API cost managed later via credit system |
| 2026-05 | Default accent color: `#B8A369` (mat altın) | Calm Luxury ruhuna en uygun — sessiz lüks hissi. User picks from 5-6 curated options in Phase 2 |

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