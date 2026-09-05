# Phase 1: Hardcoded Color Analysis Report - PeerLearn

This report identifies all hardcoded color literals found in the `app` module, grouped by file, and cross-referenced with the brand color palette.

## Brand Color Palette Reference
- **Teal (Primary):** `#0F6E6E` (Found as `0xFF0F6E6E` or `AppColors.DarkGreen`)
- **Purple (Secondary):** `#534AB7` (Found as `0xFF534AB7` or `IconTint` in settings)
- **Amber (Tertiary):** `#E8A33D` (Matches closely with `#D9822B` or `#F7971E`)
- **Background:** `#FAF8F5` (Matches closely with `0xFFF8F8FC` or `0xFFF8F8F8`)

---

## Detailed Findings by File

### 1. Settings Module
These screens consistently use a specific Purple/Teal set for icons and UI elements.

| File | Line | Hardcoded Value | UI Element | Role / Brand Match |
| :--- | :--- | :--- | :--- | :--- |
| `AccountScreen.kt` | 60 | `0xFFEEEDFE` | Background | Icon Background (Purple Tint) |
| | 61 | `0xFF534AB7` | Tint | **Purple Brand Color** |
| | 308 | `0xFF2E7D32` | Text | Success Green |
| | 309 | `0xFFC62828` | Text | Error Red |
| `ChangePassScreen.kt` | 88 | `0xFFEEEDFE` | Background | Icon Background |
| | 123 | `0xFF4CAF50` | Text | Success Green |
| `Settingsscreen.kt` | 302, 338 | `0xFFFFEBEE` | Background | Warning/Delete Background |
| | 308, 316, 344, 352 | `0xFFE53935` | Tint/Text | Warning/Delete Red |
| `VerifyEmailScreen.kt` | 39, 281 | `0xFF0F6E6E` | Text | **Teal Brand Color** |
| | 38 | `0xFF534AB7` | Tint | **Purple Brand Color** |

### 2. Home & Chat Module
High usage of Teal and custom Grays for messaging and status indicators.

| File | Line | Hardcoded Value | UI Element | Role / Brand Match |
| :--- | :--- | :--- | :--- | :--- |
| `ChatConversationScreen.kt`| 204 | `0xFF0F6E6E` | Icon | **Teal Brand Color** (Online) |
| | 258 | `0xFF534AB7` | Border | **Purple Brand Color** |
| `ChatScreen.kt` | 81, 89, 234 | `0xFF0F6E6E` | Tint/Text | **Teal Brand Color** |
| | 105 | `0xFFE9E7E0` | Background | Off-white background |
| | 164 | `0xFFFCE4CC` | Background | Notification background |
| `HomeScreen.kt` | 144, 152, 239 | `0xFF0F6E6E` | Tint/Text | **Teal Brand Color** |
| | 368 | `0xFF3C3489` | Text | Deep Purple |
| | 380, 442 | `0xFFD9822B` | Text/Bg | **Amber Brand Color** (Need Help) |
| | 480 | `0xFFE0245E` | Tint | Like Heart (Pink/Red) |

### 3. Components (UI Elements)
Common UI elements with hardcoded colors.

| File | Line | Hardcoded Value | UI Element | Role / Brand Match |
| :--- | :--- | :--- | :--- | :--- |
| `PeerRowCard.kt` | 78, 131, 140 | `0xFF6C63FF` | Text/Tint | Secondary Purple |
| | 61, 125 | `0xFFF2F1FA` | Background | Light Lavender Bg |
| `PeerSuggestionCard.kt` | 44, 46 | `0xFF0F6E6E` | Text | **Teal Brand Color** |
| | 45 | `0xFFB9E8E4` | Background | Teal Chip Background |
| | 47 | `0xFFF8D9AE` | Background | Peach/Amber Chip Background |
| `Slidetoswapbutton.kt` | 72 | `0xFF0F6E6E` | Gradient | **Teal Brand Color** |
| | 153 | `0xFF0F6E6E` | Tint | **Teal Brand Color** |

### 4. Common Literals (App-wide)
Found in almost every file:
- `Color.White`: Used for card backgrounds, button text, and screen backgrounds.
- `Color.Black` / `Color.Gray` / `0xFF6B6B6B`: Used for primary and secondary text.
- `Color.Transparent`: Used for containers.

---

## Brand Consistency Flags
- ** Teal #0F6E6E:** Consistently used across Chat, Home, and Suggestions. Maps well to `MaterialTheme.colorScheme.primary`.
- ** Purple #534AB7:** Used heavily in Settings and Navigation. Maps well to `MaterialTheme.colorScheme.secondary`.
- ** Amber #E8A33D:** Found as `#D9822B` in help features. Maps well to `MaterialTheme.colorScheme.tertiary`.
- ** Background #FAF8F5:** The app uses various off-whites (`#F8F8FC`, `#F8F8F8`, `#EEEDFE`). These should all be unified to `MaterialTheme.colorScheme.background`.

## Next Steps (Phase 2)
1. **Unify `Theme.kt`:** Define a proper `DarkColorScheme` using muted versions of Teal/Purple and a dark surface (#121212).
2. **Systematic Replacement:** Move through the files listed above, replacing literals with semantic roles.

**End of Phase 1 Report.**
