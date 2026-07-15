# ARAM Survey-Based Auto Swap — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a one-time, resumable ARAM preference survey that ranks the whole champion pool and drives a second, independent auto-swap system, alongside the existing manual priority list.

**Architecture:** Pure-logic core (JSON survey store, human-comparator merge sort, auto-swap order builder) with assert-based self-checks, then a Swing modal survey dialog and an ARAM-tab redesign on top. The survey persists to `~/.rift-helper/` JSON files; the interactive merge sort runs on a worker thread with a blocking comparator fed by the dialog, caching every answer so it resumes mid-sort.

**Tech Stack:** Java 23, Swing + MigLayout + FlatLaf, Gson (bundled via R4J), `DDragonParser`/`ChampionIcons` for champion data/icons. No test framework — pure logic gets `main()` assert self-checks (run with `java -ea`), UI is verified by building and running against a live client.

## Global Constraints

- JDK 23; build with the shaded jar via Maven (`rift-helper-<version>-shaded.jar`, main `main.RiftHelperMain`).
- Keep the maven-shade `ServicesResourceTransformer` (Ikonli icons break otherwise).
- No em dashes in UI copy. Integer font sizes only in Swing. All hand-set px scale via `UIScale.scale(...)` / the view's `px()`/`fpt()` helpers.
- LCU calls tolerate failure (GET `"{}"`, POST/PATCH `-1`); `204` = success.
- Survey data is file-based under `~/.rift-helper/` (NOT `java.util.prefs`). Atomic writes (temp + rename), failure-tolerant (log, never crash).
- Champions keyed by canonical name (stable across patches). Comparison cache key = the two names sorted and joined with `|`.
- Two independent toggles; Priority and Survey lists are NOT deduped. Priority is applied before Survey.
- Do NOT release or bump version; owner handles that after.

---

## File Structure

Create:
- `src/main/java/model/AramSurveyData.java` — plain data (tiers, rankedOrder, comparisons, stageDone) + Gson (de)serialize helpers.
- `src/main/java/model/AramSurveyStore.java` — load/save `aram-survey.json`, original snapshot, multi-level undo stack, revert/undo/isModified, metric counts.
- `src/main/java/model/SurveyRanker.java` — human-comparator merge sort with answer cache (pure; self-check).
- `src/main/java/model/AutoSwapPlanner.java` — build combined ordered champ-id list (priority ++ survey, no dedup) and pick best available bench id (pure; self-check).
- `src/main/java/view/SurveyDialog.java` — modal survey window (tier tap, staged duels, offer, result).
- `src/main/java/view/SwapController.java` — reusable "swap two entries" behavior for a list of `ChampionPicker`s.

Modify:
- `src/main/java/model/PreferenceManager.java` — rename auto-swap enable key with migration; add survey-enable key.
- `src/main/java/model/DDragonParser.java` — add `championPoolSize()` + ensure a name-list accessor (verify existing).
- `src/main/java/view/RiftHelperMainView.java` — ARAM tab redesign (onboarding banner, two auto-swap cards, scrollable survey picker list, metric label, Refine/Redo/Revert/Undo buttons, per-list Swap buttons); accessors/listeners.
- `src/main/java/view/Icons.java` — add `SURVEY` glyph (and any needed).
- `src/main/java/controller/RiftHelperMainController.java` — split autoSwap into priority+survey toggles; wire survey launch/refine/redo/revert/undo; rebuild swap logic via `AutoSwapPlanner`; feed the survey list into the view.

---

## Phase 0 — Preferences & champion pool

### Task 0.1: Auto-swap toggle keys + migration + survey toggle

**Files:**
- Modify: `src/main/java/model/PreferenceManager.java`

**Interfaces:**
- Produces: `setAutoSwapPriority(boolean)`, `getAutoSwapPriority()`, `setAutoSwapSurvey(boolean)`, `getAutoSwapSurvey()`.

- [ ] **Step 1: Replace the existing `setAutoSwap`/`getAutoSwap` with priority-named methods + migration.**

In `PreferenceManager.java`, replace:
```java
public static void setAutoSwap(boolean value) { putBooleanFlushed("autoSwapEnabled", value); }
public static boolean getAutoSwap() { return prefs.getBoolean("autoSwapEnabled", false); }
```
with:
```java
// Auto Swap Priority (was "autoSwapEnabled"; migrate the old key once).
public static void setAutoSwapPriority(boolean value) { putBooleanFlushed("autoSwapPriorityEnabled", value); }
public static boolean getAutoSwapPriority() {
    if (prefs.get("autoSwapPriorityEnabled", null) == null && prefs.get("autoSwapEnabled", null) != null) {
        boolean old = prefs.getBoolean("autoSwapEnabled", false);
        putBooleanFlushed("autoSwapPriorityEnabled", old);
        prefs.remove("autoSwapEnabled");
    }
    return prefs.getBoolean("autoSwapPriorityEnabled", false);
}

// Auto Swap Survey (new).
public static void setAutoSwapSurvey(boolean value) { putBooleanFlushed("autoSwapSurveyEnabled", value); }
public static boolean getAutoSwapSurvey() { return prefs.getBoolean("autoSwapSurveyEnabled", false); }
```

- [ ] **Step 2: Update controller references (compile check only here).**

Note for later tasks: `controller.RiftHelperMainController` currently calls `PreferenceManager.setAutoSwap/getAutoSwap` — those are updated in Task 2.2. Do not build yet.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/model/PreferenceManager.java
git commit -m "feat: split auto-swap pref into priority + survey toggles with migration"
```

### Task 0.2: Champion pool size accessor

**Files:**
- Modify: `src/main/java/model/DDragonParser.java`
- Test: self-check appended to `DDragonParser` OR a scratch check (see step 2)

**Interfaces:**
- Produces: `static int championPoolSize()` (count of champions in the current DDragon roster), `static java.util.List<String> allChampionNames()` (verify one already exists like `fetchChampionNames()`; reuse it if so).

- [ ] **Step 1: Inspect existing accessors.**

Run: `grep -n "fetchChampionNames\|getChampionName\|getChampionId\|public static" src/main/java/model/DDragonParser.java`
Expected: find the existing name/id lookups. If `fetchChampionNames()` returns `List<String>`, reuse it and skip adding `allChampionNames()`.

- [ ] **Step 2: Add `championPoolSize()`.**

```java
/** Number of champions in the current Data Dragon roster (the survey's "total"). */
public static int championPoolSize() {
    java.util.List<String> names = fetchChampionNames();
    return names == null ? 0 : names.size();
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/model/DDragonParser.java
git commit -m "feat: add DDragonParser.championPoolSize for survey metric"
```

---

## Phase 1 — Survey data + storage + ranking engine

### Task 1.1: AramSurveyData (data model + JSON)

**Files:**
- Create: `src/main/java/model/AramSurveyData.java`

**Interfaces:**
- Produces:
  - fields `Map<String,String> tiers` (name -> "main"|"like"|"fine"|"never"), `Map<String,List<String>> rankedOrder` (tier -> ordered names), `Map<String,String> comparisons` (sorted-pair -> winner name or "TIE"), `Map<String,Boolean> stageDone`, `long completedAt`, `String ddragonVersion`, `int schema`.
  - `static AramSurveyData empty()`, `String toJson()`, `static AramSurveyData fromJson(String)`, `AramSurveyData deepCopy()`.
  - `static String pairKey(String a, String b)` (sorted, joined `|`).

- [ ] **Step 1: Write the class with Gson serialize + a `main()` self-check.**

```java
package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Serializable state of the ARAM preference survey. Plain data; no I/O here. */
public class AramSurveyData {
    public int schema = 1;
    public String ddragonVersion = "";
    public Map<String, String> tiers = new LinkedHashMap<>();          // name -> tier
    public Map<String, List<String>> rankedOrder = new LinkedHashMap<>(); // tier -> ordered names
    public Map<String, String> comparisons = new LinkedHashMap<>();    // "a|b" (sorted) -> winner or "TIE"
    public Map<String, Boolean> stageDone = new LinkedHashMap<>();     // tier -> ranked?
    public long completedAt = 0L;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static AramSurveyData empty() { return new AramSurveyData(); }

    public String toJson() { return GSON.toJson(this); }

    public static AramSurveyData fromJson(String json) {
        AramSurveyData d = GSON.fromJson(json, AramSurveyData.class);
        if (d == null) d = new AramSurveyData();
        if (d.tiers == null) d.tiers = new LinkedHashMap<>();
        if (d.rankedOrder == null) d.rankedOrder = new LinkedHashMap<>();
        if (d.comparisons == null) d.comparisons = new LinkedHashMap<>();
        if (d.stageDone == null) d.stageDone = new LinkedHashMap<>();
        return d;
    }

    public AramSurveyData deepCopy() { return fromJson(toJson()); }

    /** Stable key for a pair of champions, order-independent. */
    public static String pairKey(String a, String b) {
        return (a.compareTo(b) <= 0) ? a + "|" + b : b + "|" + a;
    }

    /** Names assigned to a tier (in insertion order of the tiers map). */
    public List<String> namesInTier(String tier) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, String> e : tiers.entrySet()) {
            if (tier.equals(e.getValue())) out.add(e.getKey());
        }
        return out;
    }

    /** decided = champions with any tier assigned (incl. never). */
    public int decidedCount() { return tiers.size(); }

    // Self-check: java -ea model.AramSurveyData
    public static void main(String[] args) {
        AramSurveyData d = empty();
        d.tiers.put("Ahri", "main");
        d.tiers.put("Lux", "like");
        d.tiers.put("Garen", "never");
        d.comparisons.put(pairKey("Zed", "Ahri"), "Ahri");
        assert pairKey("Zed", "Ahri").equals("Ahri|Zed") : "pairKey must sort";
        assert pairKey("Ahri", "Zed").equals("Ahri|Zed");
        assert d.decidedCount() == 3;
        assert d.namesInTier("main").equals(List.of("Ahri"));
        AramSurveyData round = fromJson(d.toJson());
        assert round.decidedCount() == 3 : "json round-trip";
        assert round.comparisons.get("Ahri|Zed").equals("Ahri");
        System.out.println("AramSurveyData self-check passed.");
    }
}
```

- [ ] **Step 2: Run the self-check.**

Set `JAVA_HOME`, then:
Run: `javac -cp "<gson-on-classpath>" -d target/classes src/main/java/model/AramSurveyData.java` then `java -ea -cp "target/classes;<gson>" model.AramSurveyData`
Simplest: build the project (`mvn -B clean package -Dmaven.test.skip=true -q`) then `java -ea -cp target/classes model.AramSurveyData`.
Expected: `AramSurveyData self-check passed.`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/model/AramSurveyData.java
git commit -m "feat: AramSurveyData model with JSON round-trip and pair key"
```

### Task 1.2: AramSurveyStore (persistence + backups + undo/revert + metric)

**Files:**
- Create: `src/main/java/model/AramSurveyStore.java`

**Interfaces:**
- Consumes: `AramSurveyData`.
- Produces:
  - `static AramSurveyData load()` (current, or `empty()` if none/corrupt), `static void save(AramSurveyData)`.
  - `static void pushUndo(AramSurveyData before)` (cap 100), `static boolean canUndo()`, `static AramSurveyData undo(AramSurveyData current)` (returns restored, persists), `static void clearUndo()`.
  - `static void saveOriginalSnapshot(AramSurveyData)` (only when none exists yet OR on explicit completion), `static boolean hasOriginal()`, `static AramSurveyData original()`, `static boolean isModifiedFromOriginal(AramSurveyData current)`.
  - `static boolean exists()` (survey file present with >=1 decided), `static void deleteForRedo()` (wipe working file + undo stack; keep original).
  - Files live in `dir()` = `<user.home>/.rift-helper/`.

- [ ] **Step 1: Write the store with atomic writes + a `main()` self-check using a temp dir override.**

```java
package model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** File-based persistence for the ARAM survey: current file, original snapshot, and a bounded
 *  multi-level undo stack. Atomic writes; all failures are swallowed (logged) so the app never
 *  crashes on a bad disk/JSON. */
public class AramSurveyStore {
    private static final int UNDO_CAP = 100;
    static Path baseDir = Path.of(System.getProperty("user.home"), ".rift-helper"); // overridable in tests

    private static Path dir() { return baseDir; }
    private static Path current() { return dir().resolve("aram-survey.json"); }
    private static Path original() { return dir().resolve("aram-survey-original.json"); }
    private static Path undoFile() { return dir().resolve("aram-survey-undo.json"); }

    private static void writeAtomic(Path p, String content) {
        try {
            Files.createDirectories(dir());
            Path tmp = Files.createTempFile(dir(), "rh", ".tmp");
            Files.writeString(tmp, content, StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("[Survey] write failed: " + e.getMessage());
        }
    }

    private static String read(Path p) {
        try { return Files.exists(p) ? Files.readString(p, StandardCharsets.UTF_8) : null; }
        catch (IOException e) { System.out.println("[Survey] read failed: " + e.getMessage()); return null; }
    }

    public static AramSurveyData load() {
        String s = read(current());
        return s == null ? AramSurveyData.empty() : AramSurveyData.fromJson(s);
    }

    public static void save(AramSurveyData d) { writeAtomic(current(), d.toJson()); }

    public static boolean exists() {
        String s = read(current());
        if (s == null) return false;
        return AramSurveyData.fromJson(s).decidedCount() > 0;
    }

    // ---- original snapshot ----
    public static boolean hasOriginal() { return read(original()) != null; }
    public static AramSurveyData originalData() {
        String s = read(original());
        return s == null ? AramSurveyData.empty() : AramSurveyData.fromJson(s);
    }
    /** Save the baseline. Called when the survey first reaches "all tiers assigned" completion. */
    public static void saveOriginalSnapshot(AramSurveyData d) { writeAtomic(original(), d.toJson()); }
    public static boolean isModifiedFromOriginal(AramSurveyData current) {
        return hasOriginal() && !originalData().toJson().equals(current.toJson());
    }

    // ---- undo stack (persisted) ----
    private static Deque<String> loadUndo() {
        Deque<String> stack = new ArrayDeque<>();
        String s = read(undoFile());
        if (s != null) {
            for (String line : s.split("\n\n")) { // record separator
                if (!line.isBlank()) stack.push(line);
            }
        }
        return stack;
    }
    private static void persistUndo(Deque<String> stack) {
        List<String> items = new ArrayList<>(stack); // top first
        java.util.Collections.reverse(items);         // write bottom-first so push order restores
        writeAtomic(undoFile(), String.join("\n\n", items));
    }
    /** Snapshot `before` onto the undo stack prior to a modification. */
    public static void pushUndo(AramSurveyData before) {
        Deque<String> stack = loadUndo();
        stack.push(before.toJson());
        while (stack.size() > UNDO_CAP) stack.removeLast();
        persistUndo(stack);
    }
    public static boolean canUndo() { return !loadUndo().isEmpty(); }
    /** Pop the last snapshot, persist it as current, return it. Returns `current` unchanged if empty. */
    public static AramSurveyData undo(AramSurveyData current) {
        Deque<String> stack = loadUndo();
        if (stack.isEmpty()) return current;
        AramSurveyData restored = AramSurveyData.fromJson(stack.pop());
        persistUndo(stack);
        save(restored);
        return restored;
    }
    public static void clearUndo() { writeAtomic(undoFile(), ""); }

    /** Redo the survey from scratch: wipe working file + undo stack; keep original snapshot. */
    public static void deleteForRedo() {
        try { Files.deleteIfExists(current()); } catch (IOException ignored) {}
        clearUndo();
    }

    // Self-check: java -ea model.AramSurveyStore
    public static void main(String[] args) throws IOException {
        baseDir = Files.createTempDirectory("rh-survey-test");
        assert !exists();
        AramSurveyData d = AramSurveyData.empty();
        d.tiers.put("Ahri", "main");
        save(d);
        assert exists();
        assert load().decidedCount() == 1;

        saveOriginalSnapshot(d);
        assert hasOriginal();
        assert !isModifiedFromOriginal(d);

        AramSurveyData d2 = d.deepCopy();
        d2.tiers.put("Lux", "like");
        pushUndo(d);      // snapshot before change
        save(d2);
        assert isModifiedFromOriginal(d2);
        assert canUndo();
        AramSurveyData back = undo(d2);
        assert back.decidedCount() == 1 : "undo restores prior state";
        assert !canUndo();
        System.out.println("AramSurveyStore self-check passed.");
    }
}
```

- [ ] **Step 2: Run the self-check.**

Run: `mvn -B clean package -Dmaven.test.skip=true -q` then `java -ea -cp target/classes model.AramSurveyStore`
Expected: `AramSurveyStore self-check passed.`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/model/AramSurveyStore.java
git commit -m "feat: AramSurveyStore file persistence, original snapshot, multi-level undo"
```

### Task 1.3: SurveyRanker (human-comparator merge sort with cache)

**Files:**
- Create: `src/main/java/model/SurveyRanker.java`

**Interfaces:**
- Consumes: `AramSurveyData.pairKey`.
- Produces:
  - functional interface `SurveyRanker.Asker { int ask(String a, String b); }` returns `<0` a first, `>0` b first, `0` tie.
  - `static List<String> rank(List<String> items, Map<String,String> cache, Asker asker)` — merge sort; consults `cache` (mutating it with new answers, storing winner name or `"TIE"`); calls `asker` only for uncached pairs; stable.

- [ ] **Step 1: Write the ranker with a scripted-asker `main()` self-check.**

```java
package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Turns pairwise "A or B?" answers into a full order via merge sort, caching every answer so a
 *  re-run reuses prior answers (this is what makes the survey resumable mid-sort). */
public class SurveyRanker {
    public interface Asker { int ask(String a, String b); } // <0 a first, >0 b first, 0 tie

    public static List<String> rank(List<String> items, Map<String, String> cache, Asker asker) {
        if (items.size() <= 1) return new ArrayList<>(items);
        int mid = items.size() / 2;
        List<String> left = rank(items.subList(0, mid), cache, asker);
        List<String> right = rank(items.subList(mid, items.size()), cache, asker);
        List<String> out = new ArrayList<>(items.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), cache, asker) <= 0) out.add(left.get(i++));
            else out.add(right.get(j++));
        }
        while (i < left.size()) out.add(left.get(i++));
        while (j < right.size()) out.add(right.get(j++));
        return out;
    }

    private static int compare(String a, String b, Map<String, String> cache, Asker asker) {
        String key = AramSurveyData.pairKey(a, b);
        String cached = cache.get(key);
        if (cached == null) {
            int r = asker.ask(a, b);
            cached = (r == 0) ? "TIE" : (r < 0 ? a : b);
            cache.put(key, cached);
        }
        if (cached.equals("TIE")) return 0;
        return cached.equals(a) ? -1 : 1;
    }

    // Self-check: java -ea model.SurveyRanker
    public static void main(String[] args) {
        // Ground-truth order by a hidden score; asker returns comparison and counts calls.
        Map<String, Integer> score = Map.of("A", 5, "B", 4, "C", 3, "D", 2, "E", 1);
        int[] calls = {0};
        Map<String, String> cache = new java.util.HashMap<>();
        SurveyRanker.Asker asker = (a, b) -> { calls[0]++; return Integer.compare(score.get(b), score.get(a)); };

        List<String> items = new ArrayList<>(List.of("C", "A", "E", "B", "D"));
        List<String> ranked = rank(items, cache, asker);
        assert ranked.equals(List.of("A", "B", "C", "D", "E")) : "ranked=" + ranked;

        // Re-run with the populated cache: no new asker calls.
        int before = calls[0];
        List<String> again = rank(new ArrayList<>(List.of("E", "D", "C", "B", "A")), cache, asker);
        assert again.equals(List.of("A", "B", "C", "D", "E"));
        assert calls[0] == before : "cache must prevent re-asking (extra calls=" + (calls[0] - before) + ")";

        // Tie handling: equal pair returns stable (first stays first).
        Map<String, String> c2 = new java.util.HashMap<>();
        List<String> t = rank(new ArrayList<>(List.of("X", "Y")), c2, (a, b) -> 0);
        assert t.equals(List.of("X", "Y")) : "tie keeps input order";
        System.out.println("SurveyRanker self-check passed.");
    }
}
```

- [ ] **Step 2: Run the self-check.**

Run: `java -ea -cp target/classes model.SurveyRanker`
Expected: `SurveyRanker self-check passed.`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/model/SurveyRanker.java
git commit -m "feat: SurveyRanker human-comparator merge sort with answer cache"
```

---

## Phase 2 — Auto-swap order + controller integration

### Task 2.1: AutoSwapPlanner (combined order + bench pick)

**Files:**
- Create: `src/main/java/model/AutoSwapPlanner.java`

**Interfaces:**
- Produces:
  - `static List<Integer> buildOrder(int[] priorityIds, List<Integer> surveyIds)` — priority ids (skipping `<=0`) followed by survey ids; **no dedup**; preserves order.
  - `static int pickBenchTarget(List<Integer> order, List<Integer> benchIds, int currentRankReached)` — returns the champion id of the highest-ranked entry in `order` that is on the bench AND ranked strictly better (lower index) than `currentRankReached`; else `-1`. Also returns the new rank via `lastRank()` accessor pattern — see code (uses an int[] out param to stay pure/testable).

- [ ] **Step 1: Write the planner with a `main()` self-check.**

```java
package model;

import java.util.ArrayList;
import java.util.List;

/** Builds the effective auto-swap order (Priority list first, then Survey list, no dedup) and
 *  picks the best available bench champion, only improving on the best rank reached so far. */
public class AutoSwapPlanner {

    public static List<Integer> buildOrder(int[] priorityIds, List<Integer> surveyIds) {
        List<Integer> order = new ArrayList<>();
        if (priorityIds != null) for (int id : priorityIds) if (id > 0) order.add(id);
        if (surveyIds != null) order.addAll(surveyIds);
        return order;
    }

    /**
     * @param order effective ordered ids (best first)
     * @param benchIds champion ids currently on the bench
     * @param currentRankReached best index already swapped to this session (start with Integer.MAX_VALUE)
     * @param outRank length-1 array; set to the new best rank if a target is found
     * @return champion id to swap to, or -1 if nothing better is available
     */
    public static int pickBenchTarget(List<Integer> order, List<Integer> benchIds,
                                       int currentRankReached, int[] outRank) {
        for (int rank = 0; rank < order.size() && rank < currentRankReached; rank++) {
            int id = order.get(rank);
            if (benchIds.contains(id)) {
                if (outRank != null) outRank[0] = rank;
                return id;
            }
        }
        return -1;
    }

    // Self-check: java -ea model.AutoSwapPlanner
    public static void main(String[] args) {
        int[] priority = {10, 0, 20};                 // 0 skipped
        List<Integer> survey = List.of(20, 30, 40);   // 20 duplicated on purpose (no dedup)
        List<Integer> order = buildOrder(priority, survey);
        assert order.equals(List.of(10, 20, 20, 30, 40)) : "order=" + order;

        int[] out = {-1};
        // bench has 30 and 40; best available is 30 (rank 3), nothing reached yet.
        int t = pickBenchTarget(order, List.of(40, 30), Integer.MAX_VALUE, out);
        assert t == 30 && out[0] == 3 : "t=" + t + " rank=" + out[0];
        // once reached rank 3, only strictly-better (rank<3) qualifies; bench 40 only -> none.
        int t2 = pickBenchTarget(order, List.of(40), 3, out);
        assert t2 == -1 : "no improvement";
        // bench has 10 (rank 0) -> improves.
        int t3 = pickBenchTarget(order, List.of(10, 40), 3, out);
        assert t3 == 10 && out[0] == 0;
        System.out.println("AutoSwapPlanner self-check passed.");
    }
}
```

- [ ] **Step 2: Run the self-check.**

Run: `java -ea -cp target/classes model.AutoSwapPlanner`
Expected: `AutoSwapPlanner self-check passed.`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/model/AutoSwapPlanner.java
git commit -m "feat: AutoSwapPlanner combined order (no dedup) and bench pick"
```

### Task 2.2: Controller — two toggles + survey-driven swap

**Files:**
- Modify: `src/main/java/controller/RiftHelperMainController.java`

**Interfaces:**
- Consumes: `AutoSwapPlanner`, `AramSurveyStore`, `AramSurveyData`, `DDragonParser`, the view's new toggle buttons/listeners (Task 4.x), `PreferenceManager.getAutoSwapPriority/Survey`.
- Produces: field `volatile boolean autoSwapPriority, autoSwapSurvey;` and `List<Integer> surveySwapIds` (cached from the survey file, refreshed on load/edit); a rebuilt `autoSwap()` returning the swapped id.

- [ ] **Step 1: Replace the `autoSwap` field/flag pair with the two toggles.**

In the fields block, replace `private volatile boolean autoSwap;` with:
```java
private volatile boolean autoSwapPriority;
private volatile boolean autoSwapSurvey;
private volatile java.util.List<Integer> surveySwapIds = new java.util.ArrayList<>();
```

- [ ] **Step 2: Rebuild `autoSwap()` to use the planner over Priority + Survey.**

Replace the body of `public int autoSwap()` with:
```java
public int autoSwap() {
    if (!autoSwapPriority && !autoSwapSurvey) return -1;
    int[] priorityIds = new int[]{
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority1()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority2()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority3()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority4()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority5()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority6()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority7()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority8()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority9()),
        DDragonParser.getChampionId(riftHelperMainView.getComboBoxAutoSwapPriority10())
    };
    java.util.List<Integer> order = model.AutoSwapPlanner.buildOrder(
        autoSwapPriority ? priorityIds : new int[0],
        autoSwapSurvey ? surveySwapIds : java.util.List.of());
    java.util.List<Integer> benchIds = new java.util.ArrayList<>();
    for (BenchChampions b : benchChampions) benchIds.add(b.getChampionId());
    int[] out = {priority};
    int target = model.AutoSwapPlanner.pickBenchTarget(order, benchIds, priority, out);
    if (target > 0) {
        if (LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + target) == 204) {
            priority = out[0];
            return target;
        }
    }
    return -1;
}
```
Note: the existing `priority` int field is reused as "best rank reached"; keep its reset to a high value on new champ select (`priority = 10;` currently — change resets to `Integer.MAX_VALUE` where the ARAM branch resets it; verify the reset points and change `10` -> `Integer.MAX_VALUE` for correctness with longer lists).

- [ ] **Step 3: Load the survey ids into `surveySwapIds`.**

Add a helper and call it in `startProgram()` and after any survey change:
```java
private void refreshSurveySwapIds() {
    AramSurveyData d = AramSurveyStore.load();
    java.util.List<String> names = new java.util.ArrayList<>();
    for (String tier : new String[]{"main", "like", "fine"}) {
        java.util.List<String> ranked = d.rankedOrder.get(tier);
        if (ranked != null && !ranked.isEmpty()) names.addAll(ranked);
        else names.addAll(d.namesInTier(tier)); // fall back to tier membership if not fine-ranked
    }
    java.util.List<Integer> ids = new java.util.ArrayList<>();
    for (String n : names) { int id = DDragonParser.getChampionId(n); if (id > 0) ids.add(id); }
    surveySwapIds = ids;
}
```
Call `refreshSurveySwapIds();` at the end of `startProgram()`.

- [ ] **Step 4: Update toggle load + `PreferenceManager` calls.**

In `startProgram()` replace `this.autoSwap = PreferenceManager.getAutoSwap();` with:
```java
this.autoSwapPriority = PreferenceManager.getAutoSwapPriority();
this.autoSwapSurvey = PreferenceManager.getAutoSwapSurvey();
```
Wire the two enable/disable listeners in the constructor (see view Task 4.3 for button names) following the existing `setAutoMinimize` pattern: set the volatile, `applyNotifyButtons`-style enable/disable, and `PreferenceManager.setAutoSwapPriority/Survey`. Update `loadPreferences()` `applyToggleButtons(...)` calls to the two new button pairs. Remove references to the old single `buttonAutoSwapEnable/Disable` only after the view exposes the new pairs (Task 4.3) — keep names in sync.

- [ ] **Step 5: Build.**

Run: `mvn -B clean package -Dmaven.test.skip=true -q`
Expected: `BUILD SUCCESS` (exit 0). If the view changes (Task 4.x) are not yet done, this task depends on them — sequence Task 2.2 after Task 4.3, or stub the new view buttons first.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/controller/RiftHelperMainController.java
git commit -m "feat: controller two-toggle auto-swap via AutoSwapPlanner + survey ids"
```

> **Sequencing note:** Task 2.2 references view buttons/getters added in Phase 4. Implement Phase 4's view fields/accessors (Tasks 4.2, 4.3) before compiling Task 2.2, or introduce the new view button fields first as a tiny scaffolding commit.

---

## Phase 3 — Survey modal dialog

### Task 3.1: SurveyDialog skeleton + tier-tap phase

**Files:**
- Create: `src/main/java/view/SurveyDialog.java`
- Modify: `src/main/java/view/Icons.java` (add `SURVEY` glyph -> `FontAwesomeSolid.CLIPBOARD_LIST`)

**Interfaces:**
- Consumes: `AramSurveyStore`, `AramSurveyData`, `SurveyRanker`, `DDragonParser`, `ChampionIcons`, `Theme`, `UIScale`.
- Produces: `SurveyDialog(JFrame owner, Runnable onClose)`; `void openResume()` (start/resume). On close, calls `onClose` so the controller can `refreshSurveySwapIds()` + refresh the ARAM tab.

- [ ] **Step 1: Create the dialog class — window, phase state, tier-tap screen.**

Implement a `JDialog` (modal to the main window) laid out with MigLayout, dark theme via `Theme`. Mirror the validated prototype (`.superpowers/brainstorm/.../survey-prototype-v2.html`) exactly for flow and copy. Structure:
- Fields: `AramSurveyData data` (loaded via `AramSurveyStore.load()`), `List<String> roster` (from `DDragonParser.fetchChampionNames()`), `int idx` (next undecided champion index), a `JPanel content` swapped per screen, a top `JProgressBar`.
- `showTierTap()`: shows `roster.get(idx)` icon (via `ChampionIcons.load`) + four tier buttons (Main/Like/Fine/Never) + counts + Undo + "Finish tiers now". Tapping sets `data.tiers.put(name, tier)`, `AramSurveyStore.save(data)`, `idx++`, repaint. Keyboard 1-4 and Backspace via `InputMap`/`ActionMap`.
- When `idx >= roster.size()` OR "Finish tiers now" pressed -> `beginStages()` (Task 3.2).
- Resume: on open, set `idx` to the first roster champion not present in `data.tiers`.

Use integer fonts (`deriveFont(fpt(...))` equivalents — since this is a separate class, add local `px(int)`/`fpt(float)` helpers using `com.formdev.flatlaf.util.UIScale`). No em dashes in copy.

*(Full method bodies follow the prototype's `phase1()`; the implementer ports that JS to Swing. Because this is a large UI class with no unit tests, verification is by building and running — see Step 2.)*

- [ ] **Step 2: Build and run; verify tier tap + resume.**

Run: `mvn -B clean package -Dmaven.test.skip=true -q` then launch the jar with a live client and open the dialog from a temporary test button (or the ARAM onboarding once Task 4.1 lands).
Expected: tapping tiers advances; closing and reopening resumes at the same champion; `~/.rift-helper/aram-survey.json` grows.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/SurveyDialog.java src/main/java/view/Icons.java
git commit -m "feat: SurveyDialog tier-tap phase with autosave + resume"
```

### Task 3.2: SurveyDialog staged pairwise ranking + offer + result

**Files:**
- Modify: `src/main/java/view/SurveyDialog.java`

**Interfaces:**
- Consumes: `SurveyRanker`, `AramSurveyData.comparisons`.
- Produces: `beginStages()`, `runStage(String tier)`, `offerNext(String doneTier)`, `showResult()`.

- [ ] **Step 1: Implement staged ranking on a worker thread with a blocking comparator.**

- `runStage(tier)`: collect `data.namesInTier(tier)`. If `<=1`, set `data.rankedOrder.put(tier, list)`, mark `stageDone`, save, `offerNext(tier)`.
- Otherwise start a `Thread` that calls `SurveyRanker.rank(items, data.comparisons, asker)`. The `asker` posts the duel screen to the EDT (`SwingUtilities.invokeLater`) and **blocks** on a `SynchronousQueue<Integer>`; the duel buttons (and Left/Right/`=`) put `-1/1/0` on the queue. After each answer, `AramSurveyStore.save(data)` (comparisons cache now updated) on the EDT. On sort completion (back on worker), `invokeLater` to store `data.rankedOrder.put(tier, ranked)`, `stageDone`, save, `offerNext(tier)`.
- `offerNext(done)`: find the next tier in `["main","like","fine"]` with `>=2` champs (auto-set singletons/empties, saving). If none -> `showResult()`. Else show the encouragement screen (🎉 copy from the prototype: "Your <Prev> ranking is done! Keep going ... One-time setup ... Sharper auto-swap every game.") with "Rank <Next> (n)" and "Save & finish for now".
- On first full completion (all champions decided into a tier), if `!AramSurveyStore.hasOriginal()` OR this is the first completion, call `AramSurveyStore.saveOriginalSnapshot(data)` and set `data.completedAt`.
- `showResult()`: list each tier (ranked -> numbered rows; unranked -> alphabetical chips) + Never count + Close. Tag ranked vs grouped. On Close -> `dispose()` + `onClose.run()`.

- [ ] **Step 2: Build and run; verify staged flow + resume mid-sort.**

Run: build, launch, tier a handful (mark ~4 Main, some Like), rank Main, accept the Like offer, then close mid-duel and reopen.
Expected: duels resume without re-asking answered pairs (comparison cache); result screen tags tiers correctly; `aram-survey-original.json` written on first full completion.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/SurveyDialog.java
git commit -m "feat: SurveyDialog staged Main/Like/Fine ranking, offers, result"
```

---

## Phase 4 — ARAM tab redesign

### Task 4.1: Onboarding banner (three states)

**Files:**
- Modify: `src/main/java/view/RiftHelperMainView.java`

**Interfaces:**
- Consumes: survey status from the controller (`AramSurveyStore.exists()` + completeness).
- Produces: `void setSurveyOnboarding(String state, int decided, int total)` where `state` in `{"none","partial","done"}`; a `buttonSurveyStart` `JButton` + `addSurveyStartListener(ActionListener)`.

- [ ] **Step 1: Add the banner to the top of the ARAM section in `buildAram()`.**

Add a `Card`/panel `surveyBanner` above the Current Champion card. `setSurveyOnboarding`:
- `none`: text "Build your auto-swap ranking. A quick survey learns which champs you like ... One-time setup, and it sharpens every game." + button "Start Survey".
- `partial`: "You are `<decided>/<total>` through your survey. Continuing makes auto-swap sharper. One-time setup." + button "Continue Survey".
- `done`: `surveyBanner.setVisible(false)`.
Button fires `buttonSurveyStart`. Use `px()/fpt()` for sizes; no em dashes.

- [ ] **Step 2: Build; verify banner renders in each state (temporary calls).**

Run: build + run; temporarily call each state from the constructor to eyeball.
Expected: three variants render; done hides it.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/RiftHelperMainView.java
git commit -m "feat: ARAM survey onboarding banner (none/partial/done)"
```

### Task 4.2: Two Auto Swap cards + scrollable survey picker list + metric

**Files:**
- Modify: `src/main/java/view/RiftHelperMainView.java`

**Interfaces:**
- Produces:
  - rename the existing Auto Swap card title to "Auto Swap Priority"; keep its 10 pickers + toggle (`buttonAutoSwapEnable/Disable` reused as the Priority pair; add getters if missing).
  - new "Auto Swap Survey" card: `buttonAutoSwapSurveyEnable/Disable` (+ ToggleSwitch), a `JLabel surveyMetric`, `buttonSurveyRefine`, `buttonSurveyRedo` (danger), `buttonSurveyRevert`, `buttonSurveyUndo`, and a scrollable panel `surveyListPanel` holding `ChampionPicker[] surveyPickers` (sized to the champion pool).
  - accessors: `setSurveyMetric(int decided,int total)`, `setSurveyList(String[] names)` (fills pickers), `String[] getSurveyList()`, `setSurveyUndoEnabled(boolean)`, `setSurveyRevertVisible(boolean)`, add*Listener for refine/redo/revert/undo + survey toggle.

- [ ] **Step 1: Build the two cards in `buildAram()` (single column, existing style).**

Follow the validated mockup (`.superpowers/brainstorm/.../aram-tab-layout.html`). Metric label reads `"<decided> / <total> ranked"`. Survey list is a `JScrollPane` (vertical only, `HORIZONTAL_SCROLLBAR_NEVER`) wrapping a MigLayout column of numbered `ChampionPicker` rows. Redo styled `ButtonKind.DANGER`. Revert hidden by default (`setSurveyRevertVisible(false)`), Undo disabled by default.

- [ ] **Step 2: Build and run; verify layout + scroll at full roster.**

Run: build + run; `setSurveyList` with ~170 dummy names.
Expected: card renders, list scrolls, window stays within the compact cap.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/RiftHelperMainView.java
git commit -m "feat: Auto Swap Priority + Survey cards, scrollable survey list, metric"
```

### Task 4.3: Controller wiring for survey buttons + toggles + tab refresh

**Files:**
- Modify: `src/main/java/controller/RiftHelperMainController.java`

**Interfaces:**
- Consumes: view accessors from 4.1/4.2, `SurveyDialog`, `AramSurveyStore`.
- Produces: `refreshAramTab()` (recompute onboarding state + metric + survey list + revert/undo enablement), listeners for Start/Continue/Refine/Redo/Revert/Undo and the two toggles.

- [ ] **Step 1: Implement `refreshAramTab()` and wire it into `startProgram()` + after every survey change.**

```java
private void refreshAramTab() {
    AramSurveyData d = AramSurveyStore.load();
    int total = DDragonParser.championPoolSize();
    int decided = d.decidedCount();
    String state = !AramSurveyStore.exists() ? "none" : (decided >= total && total > 0 ? "done" : "partial");
    // survey list = ranked main+like+fine, fallback tier membership (same as refreshSurveySwapIds)
    java.util.List<String> names = surveyListNames(d);
    SwingUtilities.invokeLater(() -> {
        riftHelperMainView.setSurveyOnboarding(state, decided, total);
        riftHelperMainView.setSurveyMetric(decided, total);
        riftHelperMainView.setSurveyList(names.toArray(new String[0]));
        riftHelperMainView.setSurveyUndoEnabled(AramSurveyStore.canUndo());
        riftHelperMainView.setSurveyRevertVisible(AramSurveyStore.isModifiedFromOriginal(d));
    });
}
```
Add `surveyListNames(AramSurveyData)` (same logic as `refreshSurveySwapIds` but returning names).

- [ ] **Step 2: Wire the buttons.**

- Start/Continue/Refine: `new SurveyDialog(riftHelperMainView, () -> { refreshSurveySwapIds(); refreshAramTab(); }).openResume();`
- Redo: `JOptionPane` YES/NO confirm ("Completely redo your survey? This clears your current ranking. Revert to Original will still restore your last completed version."); on YES `AramSurveyStore.deleteForRedo()`, then open the dialog fresh, then refresh.
- Revert: confirm dialog; on YES load `AramSurveyStore.originalData()`, `AramSurveyStore.pushUndo(current)` then `save(original)`, refresh.
- Undo: `AramSurveyData restored = AramSurveyStore.undo(current);` refresh.
- Survey list manual edits (a picker changed): `AramSurveyStore.pushUndo(before); rebuild data.rankedOrder from the pickers; save; refresh` (enables Undo, may show Revert).
- Two toggles: standard enable/disable pattern -> set volatile, persist, `applyToggleButtons`.

- [ ] **Step 3: Build and run full flow.**

Run: build + run against live client.
Expected: onboarding -> survey -> result -> ARAM tab shows metric + populated survey list; Redo/Revert/Undo behave; toggles persist.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/controller/RiftHelperMainController.java
git commit -m "feat: wire ARAM survey buttons, toggles, and tab refresh"
```

---

## Phase 5 — Shared "swap two entries" affordance

### Task 5.1: SwapController reusable behavior

**Files:**
- Create: `src/main/java/view/SwapController.java`

**Interfaces:**
- Consumes: `ChampionPicker` (its `getSelectedName`/`setSelectedName`).
- Produces: `SwapController(JButton swapButton, ChampionPicker[] pickers, Runnable onSwap)`; internal click-two-to-exchange with highlight; calls `onSwap` after an exchange (so the owner can persist + push undo).

- [ ] **Step 1: Implement swap mode: arm on button, select first picker, select second, exchange names.**

Attach a click handler to `swapButton` toggling "swap mode". While armed, intercept picker selection (add a lightweight overlay/mouse listener per picker) to capture two indices, swap `setSelectedName`, then call `onSwap`. Provide visual arm/selected styling (border via a client property or repaint). Keep it generic so SR/Arena/Priority/Survey lists all use it.

- [ ] **Step 2: Attach to every list in `RiftHelperMainView`.**

Add a `⇄ Swap` button beside each champion list (SR lanes, SR ban, Arena lock/ban, Auto Swap Priority, Auto Swap Survey) and construct a `SwapController` per list. For the Survey list, `onSwap` triggers the controller's undo-push + save (via a listener the controller registers); for the small lists, `onSwap` just fires the existing autosave.

- [ ] **Step 3: Build and run; verify swap + undo on the Survey list and swap on a small list.**

Run: build + run.
Expected: Swap exchanges two entries on any list; on the Survey list Undo reverts it and Revert appears when modified.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/view/SwapController.java src/main/java/view/RiftHelperMainView.java
git commit -m "feat: shared swap-two-entries affordance on every champion list"
```

---

## Phase 6 — Integration verification

### Task 6.1: End-to-end build + live verification

- [ ] **Step 1: Run all pure-logic self-checks.**

Run: `mvn -B clean package -Dmaven.test.skip=true -q` then
`java -ea -cp target/classes model.AramSurveyData` /
`... model.AramSurveyStore` / `... model.SurveyRanker` / `... model.AutoSwapPlanner`
Expected: all four print `... self-check passed.`

- [ ] **Step 2: Live run against a client in an ARAM lobby.**

Launch the jar. Verify: onboarding -> full survey (tier + at least Main ranked) -> ARAM tab metric + list; enable Auto Swap Survey only, enter an ARAM game, confirm it swaps to the highest-ranked available bench champ; enable Auto Swap Priority too and confirm Priority wins; edit + Undo + Revert; Redo confirm.

- [ ] **Step 3: Final commit (no version bump, no release).**

```bash
git add -A
git commit -m "chore: ARAM survey auto-swap integration verified"
```

---

## Self-Review

**Spec coverage:** persistence/resume (1.1, 1.2, 3.x), staged Main→Like→Fine ranking (3.2), two independent toggles + Priority-first no-dedup (0.1, 2.1, 2.2), onboarding states (4.1), metric decided/total (0.2, 4.2, 4.3), Refine/Redo (4.3), Revert-only-when-modified + multi-level Undo (1.2, 4.2, 4.3), swap-on-every-list (5.1), modal survey (3.x), new-champ handling (metric via 0.2 + 4.3 state logic; unranked excluded via `refreshSurveySwapIds` fallback which only includes tier members — unranked champs are absent, so not swapped ✓), file storage + backups (1.2), migration (0.1). Covered.

**Placeholder scan:** UI method bodies in Phase 3/5 are described rather than fully coded because the repo has no UI test harness and the validated HTML prototypes are the reference; every logic task has complete code + self-check. Flag for the implementer: port the prototype JS faithfully.

**Type consistency:** `pairKey`, `namesInTier`, `decidedCount`, `rank(items,cache,asker)`, `buildOrder`, `pickBenchTarget(...,int[] outRank)`, store methods, and view accessors are named consistently across tasks. `priority` (controller field) reused as "best rank reached" — reset points must change `10` → `Integer.MAX_VALUE` (noted in 2.2 Step 2).
