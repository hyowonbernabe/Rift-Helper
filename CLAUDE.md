# CLAUDE.md

Guidance for agents working in this repo. Keep it short and current.

## What this is

**Rift Helper** — a Java desktop app (Swing GUI) that automates *League of Legends*
champ-select / lobby actions by talking to the local **LCU API** (League Client Update
API) over `https://127.0.0.1:<port>`. No injection, no memory reading — just authenticated
HTTP + a WebSocket to the client the user already has running.

Distributed as a self-contained Windows `.exe` (bundled JRE via jpackage). End users run the
exe; they do not need Java installed.

## How it talks to League (the important part)

1. **Auth** — [`model/LCUAuth.java`](src/main/java/model/LCUAuth.java) finds the running
   `LeagueClientUx.exe` process and scrapes its command line for `--app-port=` and
   `--remoting-auth-token=`. Every LCU request uses Basic auth `riot:<token>` and hits
   `https://127.0.0.1:<port>`.
   - ⚠️ **Do not use `wmic` to read the command line.** Microsoft removed `wmic.exe` starting
     **Windows 11 24H2 (build 26x00)**; on those machines `wmic` throws and the app can't find
     the client. Use PowerShell `Get-CimInstance Win32_Process` (the official replacement,
     present on all Win10+). This bug is exactly why v1.3.1 "stopped opening" on updated
     Windows while older machines still worked. `LCUAuth` tries CIM first, `wmic` as a legacy
     fallback.
2. **TLS** — the LCU uses a self-signed cert, so
   [`model/SSLBypass.java`](src/main/java/model/SSLBypass.java) installs a trust-all
   `SSLContext` at startup. Required; not a mistake.
3. **HTTP verbs** — `model/LCUGet`, `LCUPost`, `LCUPatch`, `LCUPut` are thin
   `HttpURLConnection` wrappers. GET returns `"{}"` on error (callers must tolerate that).
4. **Events** — the controller subscribes to LCU WebSocket events via **R4J**
   (`no.stelar7.api.r4j...LCUSocketReader`, pulled from JitPack). Champ-select session changes
   drive auto-pick / auto-ban / auto-swap.

## Layout (MVC-ish)

```
src/main/java/
  main/RiftHelperMain.java        entry point (main). Sets LAF, SSL bypass, auth, launches GUI,
                                  then polls LCU port to detect client disconnect.
  controller/RiftHelperMainController.java
                                  the brain. Wires every button/listener, subscribes to
                                  champ-select events, runs auto-lock/ban/swap/reroll logic.
  view/RiftHelperMainView.java    Swing window, hand-coded with MigLayout (see "UI" below).
  view/Theme, Icons, Card,        reusable UI pieces (palette, icons, cards, toggle switch,
    ToggleSwitch, ChampionPicker,   searchable champion picker, bench button, async icon loader).
    ChampionButton, ChampionIcons
  view/FileChooserView.java       import/export preference file picker.
  model/                          everything else:
    LCUAuth, SSLBypass            client discovery + TLS (see above)
    LCUGet/Post/Patch/Put         HTTP verbs
    Session, Actions, MyTeam, Team, TheirTeam, Bans, BenchChampions, Matchmaking,
      ReadyCheck, ShardLoot       JSON models for LCU payloads (parsed via Gson)
    GsonParser, DDragonParser     JSON + champion id/name lookup (DDragon = Riot static data)
    PreferenceManager             java.util.prefs wrapper; also import/export to
                                  rift_helper_preferences.xml
    UpdateChecker                 compares CURRENT_VERSION against version.json on GitHub
    CurrentDirectory, Timer       small helpers
src/main/resources/               Kindred icon assets + META-INF/MANIFEST.MF
```

Champion display names ↔ ids go through `DDragonParser`. Preferences are per-lane priority
lists (top/jungle/mid/bot/support) plus arena/ban/swap lists, persisted via `java.util.prefs`.

## Build / run / release

- **Requires JDK 23** (`maven.compiler.source/target = 23`) and Maven.
- Build the runnable fat jar:
  ```
  mvn -B clean package -Dmaven.test.skip=true
  ```
  Output: `target/rift-helper-1.3.2-shaded.jar` (main class `main.RiftHelperMain`, set by the
  shade plugin). Deps: R4J (JitPack), FlatLaf, Apache HttpClient, SLF4J, MigLayout, Ikonli
  (swing + fontawesome5-pack), Gson (via R4J). The shade config uses a
  `ServicesResourceTransformer` — required so Ikonli's `META-INF/services` icon resolvers survive
  shading (otherwise icons throw `Cannot resolve fas-*` at startup).
- **Run for testing** (League client must be running first):
  ```
  java -jar target/rift-helper-1.3.2-shaded.jar
  ```
- **Tooling note**: if `java`/`mvn` are missing, install JDK 23 via
  `winget install EclipseAdoptium.Temurin.23.JDK` (lands in `C:\Program Files\Eclipse Adoptium\jdk-23*`),
  and Maven has no winget package — download the binary zip from
  `https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip`.
  Set `JAVA_HOME` to the Temurin dir before running `mvn`.
- **Distributable exe**: built manually with `jlink` + `jpackage` (no committed script). The repo
  owner handles packaging + GitHub releases. Do not rebuild the exe or cut a release unless asked.
- **Version lives in three places** — keep in sync on release: `pom.xml` `<version>`,
  `UpdateChecker.CURRENT_VERSION`, `version.json` `latest`. `version.json` drives the in-app
  update prompt, so only bump it once a matching release is actually downloadable.

## UI (redesigned, `redesign/dense-ui` branch)

Rail-nav dense layout, Swing + FlatLaf + MigLayout. The `view` package holds reusable pieces:

- `RiftHelperMainView` — main window. Left rail switches a `CardLayout` of section panels
  (Lobby / Summoner's Rift / ARAM / Arena / Loot / Settings / Info). **Preserves the old public
  API** (all `getComboBoxX`/`setComboBoxX`/`addXListener` methods + the enable/disable `JButton`
  fields) so the controller is nearly unchanged.
- `Theme` — palette read from the active FlatLaf `UIManager` colors at runtime (so it matches the
  native widgets: grey `#3C3F41`, blue accent `#4B6EAF`). Call `Theme.init()` if the LAF changes.
- `Icons` — vector icons via Ikonli FontAwesome (`Icons.of(G.X, size, color)`).
- `ToggleSwitch` — the on/off switches. Adapter: constructed from the hidden Enable/Disable
  `JButton` pair the controller still wires; it reflects `!enableButton.isEnabled()` and forwards
  clicks via `doClick()`. This is why the controller keeps its two-button model unchanged.
- `ChampionPicker` — searchable champion dropdown (String API behind `getSelectedName`/
  `setSelectedName`). First list entry is `NONE` and clears the pick.
- `ChampionButton` — bench button that loads the champ icon on `setText(name)`.
- `ChampionIcons` / `DDragonParser` — champion square icons from Data Dragon (dynamic latest
  version), cached in memory and on disk under `~/.rift-helper/champion-icons`; warmed at startup
  via `DDragonParser.prefetchIcons(...)`.

## Conventions / gotchas

- **No `.form` file** — the view is hand-coded with MigLayout. Do not reintroduce GUI Designer.
- **Integer font sizes only.** Fractional sizes (e.g. `11.5f`) make Swing's label renderer open a
  gap mid-word (looked like "gam e's"). Always `deriveFont(12f)`, `14f`, etc.
- **Auto-save, no Save buttons.** Champion changes fire the (now silent) save handlers via a hidden
  save `JButton` wired in `RiftHelperMainView.autoSave(...)`. `PreferenceManager` removes a key when
  a slot is null/empty so cleared picks persist.
- Controller talks to the view through explicit `addXxxListener` / getter methods; keep that pattern.
- LCU calls can fail silently (GET returns `"{}"`, POST/PATCH return `-1`); check return codes
  (`204` = success for action PATCH/POST).
- No automated tests. Verify by building and running the jar against a live League client.
