# Auto Game-Start Loop (Auto Honor, Auto Skip, Auto Queue)

Date: 2026-07-15
Status: Approved design, pending spec review
Branch: redesign/dense-ui

## Overview

Automate the whole post-game to next-game loop for Rift Helper so the user does not
have to click through end screens or press Find Match. The loop is composed of four
independent, opt-in toggles that fire in sequence as the League client moves through its
gameflow phases:

```
game ends -> Auto Honor -> Auto Skip Progression & Scoreboard -> Auto Queue (Group or Solo)
```

Each toggle is independent. Enabling all four gives a hands-off loop; enabling a subset
does just those steps. Everything is driven off LCU gameflow-phase events, the same
mechanism the app already uses for champ-select and matchmaking.

## Goals

- Auto-honor friends after a game (up to the 4-vote maximum).
- Auto-skip the progression and scoreboard screens, landing back in the lobby immediately.
- Auto-queue from the lobby, with separate Solo and Group behavior so a solo player does
  not get trapped in an unattended infinite loop unless they explicitly opt in.

## Non-goals

- Waiting for pending invites before a Group queue (explicitly deferred by the user).
- Honoring non-friends. Rule is strictly: if a ballot-eligible player is on the friends
  list, honor them.
- Any change to existing auto-accept / auto-pick / auto-ban behavior.

## Feature: four toggles

All four are opt-in and default OFF. Each follows the existing view pattern exactly: a
hidden enable/disable `JButton` pair, a `ToggleSwitch`, controller `addXEnable/DisableListener`
wiring, and a `PreferenceManager` boolean.

1. **Auto Honor** (`autoHonor`)
2. **Auto Skip Progression & Scoreboard** (`autoSkipScreens`)
3. **Group Auto Queue** (`groupAutoQueue`)
4. **Solo Auto Queue** (`soloAutoQueue`)

Group and Solo are mutually exclusive: enabling one disables and un-persists the other, so
a group that drops to one member cannot silently start queuing the user alone.

## LCU endpoints

| Purpose | Method + path |
|---|---|
| Drive the loop | subscribe `OnJsonApiEvent_lol-gameflow_v1_gameflow-phase` |
| Honor ballot | `GET /lol-honor-v2/v1/ballot` |
| Friends list | `GET /lol-chat/v1/friends` |
| Cast honor | `POST /lol-honor-v2/v1/honor-player` (per player, max 4) |
| Skip screens | `POST /lol-end-of-game/v1/state/dismiss-stats`, then `POST /lol-lobby/v2/play-again` |
| Read lobby | `GET /lol-lobby/v2/lobby` (parse `members` array length) |
| Start queue | `POST /lol-lobby/v2/lobby/matchmaking/search` |

Exact request-body shapes and identifier fields (puuid vs summonerId) for the ballot and
honor-player call are confirmed against a live client during implementation by inspecting
the ballot response. This is a normal detail, not a feasibility risk.

## State machine

Subscribe to gameflow-phase. The event payload is the phase string. Track `lastPhase` and
act only on a transition into a phase, so repeated events do not double-fire.

- **PreEndOfGame** (honor screen active): if `autoHonor`, run honor (see below).
- **EndOfGame** (progression / scoreboard): if `autoSkipScreens`, call `dismiss-stats`
  then `play-again` to return to the lobby.
- **Lobby**: evaluate auto-queue (see below).

Ordering note: the client advances WaitingForStats -> PreEndOfGame -> EndOfGame -> Lobby.
Honor happens at PreEndOfGame; the skip happens at EndOfGame.

## Honor logic

On PreEndOfGame with `autoHonor` on:

1. `GET /lol-honor-v2/v1/ballot`. If empty or no eligible players (for example Arena, which
   has no honor, or honor already cast), skip quietly.
2. `GET /lol-chat/v1/friends` and build a set of friend identifiers.
3. For each eligible ballot player who is a friend, `POST /lol-honor-v2/v1/honor-player`,
   stopping at the 4-vote cap.

The friend-matching step (ballot players intersect friends, capped at 4) is pure logic and
gets one runnable self-check.

## Queue logic

On Lobby phase, read member count from `GET /lol-lobby/v2/lobby` (`members.length`; treat a
missing/`{}` lobby as count 0, do nothing):

- `soloAutoQueue` on AND `members == 1` -> `POST .../matchmaking/search`.
- `groupAutoQueue` on AND `members >= 2` -> `POST .../matchmaking/search`.

Leader handling: the search POST simply fails if the user is not the lobby leader. We do not
try to detect leadership; we attempt the call and ignore a non-success response. Only the
leader's client actually starts the search.

Assumption to confirm: Group threshold is `members >= 2` (two or more), matching the earlier
"if two people are in the lobby, queue starts." If the user meant strictly more than two,
this is a one-line change.

## Components

- **`Honor.java`** (model): `honorFriends()` orchestrates ballot + friends + honor calls,
  capped at 4, returns the count honored. Static helper `friendsToHonor(ballotIds, friendIds)`
  is pure and unit-checkable.
- **`Lobby.java`** (model): `memberCount()`, `playAgain()`, `dismissStats()`, `startSearch()`.
  Parses lobby JSON via the existing Gson helper.
- **Controller**: four volatile booleans, the gameflow-phase subscription and its transition
  handler, the four toggle listener pairs, and mutual-exclusion between Group and Solo. LCU
  call logic lives in the models; wiring stays in the controller, matching the existing seam.
- **View / PreferenceManager**: four toggles and four booleans in the established pattern,
  grouped in an "Automation" section (Lobby tab). No em dashes in any UI copy.

## Preferences

Four booleans, all default false: `autoHonor`, `autoSkipScreens`, `groupAutoQueue`,
`soloAutoQueue`. Solo carries no game or time cap (user chose "no limit"); the danger is
accepted and isolated behind its own opt-in switch.

## Edge cases

- Both Group and Solo cannot be on at once (mutual exclusion enforced in the listeners).
- Auto Queue with Auto Skip off: the user never reaches Lobby automatically, so the queue
  step will not run until they click through manually. Toggles are independent by design.
- Arena and other no-honor modes: ballot is empty, honor step skips.
- Not lobby leader: queue POST fails and is ignored.
- `play-again` unavailable in a given mode: fall back to
  `POST /lol-lobby/v1/last-queued-lobby/play-again`.

## Testing

No automated test suite exists (per CLAUDE.md); verify against a live client. Add one pure
self-check for `friendsToHonor` (intersection + 4-cap). All phase and LCU behavior is
verified by running the loop through a real game.

## Deferred

- Waiting for pending invites before a Group queue.
- Solo safety cap (game count or time window).
- Honoring non-friend teammates.
