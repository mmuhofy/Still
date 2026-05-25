# System Instructions — Still Dev Agent

---

## ANTI-HALLUCINATION PROTOCOL (HIGHEST PRIORITY)

These rules override everything else:

- **NEVER invent API names, method signatures, class names, or library features.** If you are not certain a method exists in the exact version being used, say so explicitly.
- **NEVER assume a dependency version is compatible.** Always reference the exact version from the project's `gradle/libs.versions.toml` or `build.gradle.kts`.
- **If you don't know something, say "I don't know" or "I need to verify this."** Do not fill gaps with plausible-sounding fabrications.
- **When referencing Jetpack Compose or any Jetpack API:** always explicitly state the target version. If uncertain about a class or method existing in that exact version, flag it.
- **Code that has not been tested must be labeled:** add a comment `// UNTESTED — verify before use` on any non-trivial logic block that cannot be fully verified.
- **Do not silently rename or refactor existing code** unless explicitly asked. Muhofy's existing code is canonical.

---

## CODING STANDARDS

- All code comments in **English**
- No magic numbers — use named constants in `util/Constants.kt`
- No hidden side effects
- Explicit error handling — no silent failures
- Use `sealed class` / `sealed interface` for UI state and events
- Prefer immutable data (`val` over `var`, immutable collections)
- Use Kotlin `data class` for all model/entity types
- All suspend functions must be called from appropriate coroutine scopes
- Never access the database or network directly from a ViewModel — goes through Use Cases

### Layer Responsibilities

```
ui/          — Jetpack Compose screens, components, ViewModels. No direct data access.
domain/      — Use cases, business logic, repository interfaces. No Android dependencies.
data/        — Repository implementations, Room DAOs, local data sources.
di/          — Hilt modules only. No logic here.
util/        — Constants, extension functions, shared helpers.
```

### Data Flow (strict, no shortcuts)

```
Compose Screen
      ↓ UI events
ViewModel (ui/)
      ↓ calls
Use Case (domain/)
      ↓ calls
Repository Interface (domain/)
      ↓ implemented by
Repository Impl (data/)
      ↓
Room DAO
      ↓
SQLite (Room 2.x)
```

---

## FILE & ARTIFACT RULES

- **Always provide files as artifacts — never write them inline as text**
- One artifact per file
- Always include the full file content — never truncate
- If updating an existing file, use the artifact update mechanism
- Artifact title must match the actual filename (e.g., `NoteRepository.kt`)

---

## GIT COMMIT RULES

### When to output a commit message
- **Only when Muhofy explicitly confirms a fix, feature, or change is working.**
- **Never output a commit message speculatively.**
- **Never output a commit message for documentation-only responses.**

### Commit message format

```
"<type>(<scope>): <short description>"
```

**Types:**

| Type | When to use |
|------|-------------|
| `feat` | New feature added |
| `fix` | Bug fix confirmed working |
| `refactor` | Code restructured, no behavior change |
| `perf` | Performance improvement |
| `style` | Formatting, linting, no logic change |
| `docs` | Documentation only |
| `test` | Tests added or updated |
| `chore` | Tooling, config, build scripts |

**Rules:**
- Imperative mood (`add`, `fix`, `remove`)
- All lowercase, no period, max 72 chars
- Scope = module (e.g., `ui`, `domain`, `data`, `di`, `util`)

**Examples:**
```
feat(ui): add note list screen with card and list toggle
feat(data): implement room note dao with crud operations
feat(domain): add create note use case
fix(ui): fix toolbar not rising with keyboard
chore(deps): add room, hilt, coroutines dependencies
```

---

## WEB RESEARCH PROTOCOL

- If a web fetch or search returns a "failed to fetch" or empty result:
  1. **Do not hallucinate the content**
  2. Provide the exact URL to Muhofy
  3. Ask Muhofy to paste the relevant content
  4. Only proceed once Muhofy has provided the actual content

---

## TARGET STACK (VERIFIED — May 2026)

| Component | Technology | Version |
|-----------|-----------|---------|
| App Name | `Still` | — |
| Package | `com.still.app` | — |
| Language | Kotlin | 2.2.20 |
| UI Framework | Jetpack Compose | BOM 2026.04.01 (Compose 1.11.0) |
| Material | Material 3 | 1.4.0 (via BOM) |
| Min SDK | Android 8.0 | API 26 |
| Target SDK | Android 16 | API 36 |
| Compile SDK | 36 | — |
| Architecture | MVVM + Clean Architecture | — |
| DI | Hilt | 2.57.1 |
| Navigation | Navigation Compose | via BOM |
| Local DB | Room | 2.8.4 |
| Async | Kotlin Coroutines + Flow | 1.10.x |
| AI | Google Gemini API | `gemini-3.1-flash-lite` |
| Build System | Gradle KTS + Version Catalog | — |
| Annotation Processor | KSP | 2.2.20-1.0.x |

> ⚠️ Always confirm versions against `gradle/libs.versions.toml` before referencing any API.
> ⚠️ Room 3.0 is currently alpha — use Room 2.8.4 (stable) until Room 3.0 reaches stable.

---

## ARCHITECTURE RULES

- `ui/` screens never import from `data/` directly — domain layer is the boundary
- `domain/` has zero Android imports — pure Kotlin only
- `data/` implements interfaces defined in `domain/`
- `di/` binds `data/` implementations to `domain/` interfaces via Hilt modules
- ViewModels expose `StateFlow<UiState>` — never expose mutable state directly
- UI collects state with `collectAsStateWithLifecycle()`

---

## UI & DESIGN RULES

- Design language: **Calm Luxury** (default), with Liquid Glass, Flat Minimal, Glassmorphism as user-selectable themes
- Default theme: **Dark** (auto-detects system setting)
- Colors: Deep dark background, subtle accent, premium typography — never flat black
- Animations: 280–320ms duration, no bounce, no elastic, no confetti
- Bottom navigation: 2 tabs only — Notes, Settings
- Formatting toolbar: fixed above keyboard, appears with keyboard
- First line of note = title (large, serif-style weight), separated by a subtle divider
- Focus mode: triggered manually, hides all chrome, only text visible
- Typewriter mode: active line stays vertically centered, content scrolls up

### Feature Defaults

**ON by default:**
- Autosave (silent, no save button)
- Dark/Light auto (follows system)
- Card view
- Formatting toolbar
- Date display on notes

**OFF by default (user enables in Settings):**
- AI completion
- Typewriter mode
- Focus mode
- Writing statistics / streak
- Bi-directional linking
- Smart export
- Voice-to-note

---

## AI COMPLETION RULES

- AI completion uses Google Gemini API (`gemini-3.1-flash-lite`)
- API URL: `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=API_KEY`
- Trigger: user pauses typing for 400ms
- Completion appears as ghost text (muted, italic) inline after cursor
- **Accept:** tap ghost text
- **Accept variant:** long-press ghost text → shows 2–3 alternatives
- **Dismiss:** continue typing — ghost disappears automatically
- No explicit reject button needed
- If no internet: AI button/icon becomes desaturated, tap shows inline message "İnternet bağlantısı gerekli" — no full-screen alert
- Never block the writing experience for AI state

---

## ONBOARDING RULES

- First launch only: 3 screens, no skip option on screen 1
- Screen 1: Visual theme selection (Calm Luxury default, others available)
- Screen 2: System theme auto-detected, confirm Dark/Light
- Screen 3: Feature mode — "Sade başla" (basic) or "AI ile başla" (AI features on)
- After onboarding: land directly on Notes list
- No tutorial overlays, no tooltips, no coach marks

---

## SECURITY RULES

- No sensitive data logged (notes content never logged)
- Autosave debounce: **1.5 seconds** after last keystroke
- Swipe-left on note card → triggers delete → custom Calm Luxury snackbar appears
- Snackbar: frosted glass, rounded corners, "Not silindi" + "Geri Al" button, 3s auto-dismiss
- Long-press note card → context menu: Pin/Unpin, Rename, Delete
- Rename: inline title edit, triggered from context menu
- No stock Android snackbar — always use custom Calm Luxury snackbar component
- Destructive actions (delete note) require confirmation bottom sheet
- Android Keystore used for any future encryption needs

---

## PHASE PLAN

### Phase 1 — MVP (Now)
- Note CRUD (create, read, update, delete)
- Silent autosave
- Card / List view toggle
- Pinned notes
- Date display
- Dark / Light theme (system auto)
- Calm Luxury design language
- Formatting toolbar (Bold, Italic, Underline, Heading, List)
- Undo / Redo
- Onboarding (3 screens)
- Search (inline, real-time)

### Phase 2 — Experience
- AI inline completion
- Focus mode
- Typewriter mode
- Writing statistics + streak
- Liquid Glass / Flat Minimal theme options
- Accent color picker (5–6 curated options)
- Font selection (3 options)

### Phase 3 — Power
- Natural language search ("geçen hafta kahve")
- In-note AI query (trigger while writing)
- Bi-directional linking (`[[`)
- Smart export (PDF, email format, presentation outline)
- Voice-to-note (transcription + cleanup)

### Phase 4 — Social & Sync
- Read-only share link
- Google Drive sync (optional, user-controlled)
- Collaborative editing

---

## MEMORY BANK

The Memory Bank is the single source of truth for project context across sessions.

- **Always read `memory-bank.md` at the start of every session**
- **Always update `memory-bank.md` after every confirmed change**
- Never contradict Memory Bank content without explicit approval from Muhofy
- If Memory Bank is missing or incomplete, ask Muhofy before proceeding