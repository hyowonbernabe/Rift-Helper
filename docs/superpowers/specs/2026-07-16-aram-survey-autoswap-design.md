# ARAM Survey-Based Auto Swap — Design Spec

Date: 2026-07-16
Status: Approved (validated via interactive prototypes in the brainstorming visual companion)
Branch: `feature/aram-survey-autoswap`

## Problem

ARAM gives you a random champion each game and a bench of 10+ others you may swap to (only
those offered). Rift Helper's current **Auto Swap** automates this with a manual 10-slot ranked
priority list. Ranking hundreds of champions by hand into 10 slots is impractical, so auto-swap
rarely matches what the player actually wants.

## Goal

A one-time **interactive survey** that captures the player's preference over the *entire* champion
pool with minimal cognitive load ("just choosing, not thinking"), saved on their PC, producing a
full ranked list that drives auto-swap so every game grabs the best available champion. The output
is an editable list, and the survey is resumable across sittings.

Two things the UX must sell to the user: **(1) it is a one-time setup, and (2) each step improves
the auto-swap result** — used to encourage completing more of it.

## Non-goals

- No change to notifications, the auto game loop, or Arena/SR pick-ban logic (beyond adding the
  shared "swap two entries" affordance to their champion lists).
- Not building analytics or cloud sync. All local.
- Not auto-releasing. Version bump + release is the owner's separate step after implementation.

## Research basis

Pairwise "A or B?" is the lowest-cognitive-load elicitation method (natural, consistent). A
merge-sort human comparator turns pairwise picks into a full order with ~N·log N comparisons.
Ranking the whole roster by pairwise alone is ~1000+ picks, so we filter first (tier tap) and only
pairwise-rank within tiers. (Sources: 1000minds pairwise, OpinionX best-worst scaling, ihaque.org
interactive preference ranking, ACM 3300742.)

## The survey

Runs in a **modal dialog window**, launched from the ARAM tab (onboarding / Refine / Redo).

### Phase 1 — Tier tap (one pass over all champions)
One champion at a time (icon + name) with four buttons: **Main / Like / Fine / Never**.
- Main = best champs; Like = happy to play; Fine = if nothing better; Never = never swap (excluded).
- Keyboard 1-4; Undo (Backspace) steps back one champ.
- "Finish tiers now" lets the user stop tiering and proceed with whatever is decided.

### Phase 2 — Staged pairwise ranking (Main → Like → Fine)
Rank the tiers in order, each stage optional and offered separately:
1. Rank **Main** via pairwise duels ("Which would you rather play, A or B?", plus "no preference").
2. After Main, an **offer screen**: rank **Like** next? Shows the two hooks — *one-time setup*,
   *sharper auto-swap every game*. Buttons: Rank Like / Save & finish for now.
3. Same offer for **Fine**.
- A stage with ≤1 champion is auto-ordered and skipped silently.
- Never is never ranked (excluded from swap).
- Merge-sort comparator; each answered pair is cached (see storage) so re-running the sort reuses
  answers — this is what makes Phase 2 resumable without serializing sort recursion.

### Result / output
A single flat **ordered list** for auto-swap: ranked Main (exact) → Like → Fine. Tiers the user
did not pairwise-rank are ordered alphabetically within the tier (stable) and can be sharpened
later. Never excluded. The result screen tags each tier "✓ ranked in order" or "grouped (rank later
to sharpen)" so the reward for continuing is visible.

### Resume / encouragement
Every choice (tier tap, duel answer) auto-saves immediately. Reopening (Refine) resumes exactly
where left off — including partway through a stage (via the comparison cache). Copy throughout
reinforces one-time + improves-experience.

## Storage

Champion pool and icons come from `DDragonParser` (already used). Survey data is too large for
`java.util.prefs` (8KB value cap), so it lives in **JSON files** under `~/.rift-helper/`
(same home dir as the icon cache):

- `aram-survey.json` — current working survey (source of truth).
- `aram-survey-original.json` — snapshot taken when the survey is **first completed** (all tiers
  assigned). Drives "Revert to Original".
- `aram-survey-undo.json` — the multi-level undo stack for Survey-list edits.

### `aram-survey.json` schema (illustrative)
```json
{
  "schema": 1,
  "ddragonVersion": "15.x.x",
  "tiers": { "Ahri": "main", "Lux": "like", "Garen": "never", ... },
  "rankedOrder": { "main": ["Ahri","Zed",...], "like": [...], "fine": [...] },
  "comparisons": { "Ahri|Zed": "Ahri", "Lux|Zoe": "Zoe", ... },
  "stageDone": { "main": true, "like": false, "fine": false },
  "completedAt": 1784138797000
}
```
Champions are keyed by **canonical name** (stable across patches). `comparisons` keys are the two
names sorted + joined, value = winner (or "tie"). Writes are atomic (temp file + rename) and
failure-tolerant (never crash the app; log and continue).

## Auto Swap — two independent features

The existing single Auto Swap becomes **two independently-toggled** systems in the ARAM tab:

- **Auto Swap Priority** — the existing manual 10-slot ranked list. Pref key renamed
  (`autoSwapEnabled` → `autoSwapPriorityEnabled`); the 10 slots (`SWAP_KEYS`) are unchanged.
- **Auto Swap Survey** — the survey's ordered list, toggled by `autoSwapSurveyEnabled`.

Both toggles are independent (on/off each). Behavior when the bench updates:

```
effectiveOrder = []
if priorityEnabled: append Priority list (in slot order)
if surveyEnabled:   append Survey list (in ranked order)
# NO dedup — the two lists are independent; a champ may appear in both.
# Priority is appended first, so if a champ is in both, its Priority position wins naturally.
for champ in effectiveOrder:
    if champ is on the bench and higher-priority than the current pick:
        swap to it; stop
```
This preserves the existing "swap to the best available" loop, just over a combined ordered list.
No dedup: Priority and Survey stay separate by design (a user may deliberately force a champ in
Priority regardless of its survey rank).

## ARAM tab redesign (single column, existing style)

Top to bottom:
1. **Onboarding banner** (only when the survey is unfinished):
   - Not started → explanation + **Start Survey**.
   - Partially done → **Continue survey** (+ the same "improves it / one-time" copy).
   - Fully done → banner hidden.
2. **Current Champion** (existing, unchanged).
3. **Auto Swap Priority** card — independent toggle; the 10 pickers; a **⇄ Swap** button.
4. **Auto Swap Survey** card:
   - Title with **metric**: `<decided> / <total> ranked` (decided = any tier incl. Never; total =
     champion pool size from DDragon). Shows partial completion and flags newly-released champs.
   - **Refine** (resume/continue the survey) and **Redo** (danger-styled; confirmation dialog;
     wipes and restarts).
   - **↩ Revert to Original** — shown/enabled **only when the survey differs from the original
     snapshot** (mirrors Undo). Confirmation dialog.
   - **⤺ Undo** — multi-level; enabled only when the undo stack is non-empty.
   - Independent toggle.
   - **Scrollable** list of the full ranked survey as standard `ChampionPicker` rows (built like the
     existing lists so they are individually editable). Scroll container so 170 rows never overflow
     the window.

## Shared "swap two entries" affordance (every champion list)

A reusable interaction added to every champion list in the GUI (SR lanes, SR ban, Arena lock/ban,
Auto Swap Priority, Auto Swap Survey):
- A **⇄ Swap** button per list. Click it → swap mode → click first row (highlights) → click second
  row → the two entries exchange (works across a long scroll: click one, scroll, click the other).
- Auto-saved like any edit.
- **Undo / Revert-to-Original** safety net applies to the **Auto Swap Survey** list specifically
  (the large, expensive one). Other lists are small and their swaps are immediate (no per-list undo
  stack) — consistent with today's behavior where those lists auto-save on change.

## Metrics & new champions

- `total` = current champion pool count (DDragon). `decided` = champions with any tier assigned.
- When Riot releases a champion, `total` rises; the metric reads e.g. `171/172`, signalling
  incompleteness. Unranked champions are **not** auto-swapped until ranked. **Refine** presents the
  unranked champions first so topping up is quick.

## Edge cases

- Survey never completed → no `original` snapshot yet; Revert-to-Original hidden. Auto Swap Survey
  uses whatever is decided; empty ⇒ Survey contributes nothing (Priority still works).
- Redo → confirmation, then wipes the working file and starts fresh. The original snapshot is left
  as the last-completed baseline and is overwritten only when the new survey next completes. (So
  between a Redo and the next completion, Revert-to-Original still points at the previous baseline.)
- Corrupt/missing JSON → treat as "not started", log, do not crash.
- DDragon offline at survey time → survey needs the roster; if unavailable, show a friendly
  "couldn't load champions, try again" and keep any saved progress.
- Champion renamed/removed by Riot (rare) → unknown names in saved data are ignored for swapping and
  can be pruned on load.
- Performance: 170 `ChampionPicker` rows in a scroll pane — pickers are lightweight custom
  components with lazy popups and cached icons; acceptable, but load icons lazily on scroll if
  needed.
- Undo stack size — persisted; cap at a sane depth (e.g. 100) to bound file size.
- Two toggles both off → no auto swap (matches "Auto Swap off" today).

## Backwards compatibility

- Existing `autoSwapEnabled` pref migrates to `autoSwapPriorityEnabled` (read old key once if new
  key absent). The 10 priority slots are untouched.

## Testing / verification (no framework; assert-based self-checks, matching repo convention)

Pure logic gets a `main()` self-check (like `Honor.friendsToHonor`):
- Merge-sort with a cached comparator: deterministic order from fixed comparisons; verifies the
  cache short-circuits and no pair is asked twice.
- Effective-order builder: Priority-then-Survey, no dedup, correct precedence.
- Metric counter: decided/total incl. Never; new-champ case.
- Survey JSON round-trip (write → read → equal).
UI verified by building and running against a live client (repo convention).

## Rollout

Implement on `feature/aram-survey-autoswap`. Do not release; the owner bumps version (likely 1.6.0)
and packages the MSI afterward using the documented jpackage + WiX process.
