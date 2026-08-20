# Building and running the reproduction

Work in `<ROOT>` (this flow-components checkout): a multi-module Maven project, Java 21+, Vaadin Flow 25+. Run all `mvn` commands from `<ROOT>` and write the View under it — never rely on relative paths.

## 1. Module layout

```
vaadin-<component>-flow-parent/
├── vaadin-<component>-flow/                    # component implementation (root-cause search)
├── vaadin-<component>-flow-integration-tests/  # put the repro View here
└── vaadin-<component>-testbench/               # TestBench elements
```

## 2. Create the repro View

Add a View under the integration-tests module:

```
<ROOT>/vaadin-<component>-flow-parent/vaadin-<component>-flow-integration-tests/src/main/java/com/vaadin/flow/component/<component>/tests/Repro<issue>View.java
```

Copy the exact imports, copyright header, and base class from an existing View in that package (e.g. `<Component>View.java`). Minimal shape:

```java
package com.vaadin.flow.component.<component>.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.<component>.<Component>;
import com.vaadin.flow.router.Route;

@Route("repro-<issue>")
public class Repro<issue>View extends Div {
    public Repro<issue>View() {
        // minimal setup that reproduces the bug; give components setId(...)
        <Component> component = new <Component>();
        add(component);
    }
}
```

Served at `http://localhost:8080/repro-<issue>`. A JUnit IT class is **not** needed for manual reproduction — driving the View with playwright-cli is enough. Add one (extending `com.vaadin.tests.AbstractComponentIT`, `@TestPath("repro-<issue>")`) only if the user wants an automated regression test.

## 3. Design the View for iteration — avoid server restarts

Jetty does **not** hot-reload Java: every View edit costs a full ~90 s package-and-restart cycle. Build the first version so likely variations need no edits:

- Give an `id` to every element you assert on. Prefer server-side log targets — a `Span`/`Div` updated from listeners (value-change with `isFromClient`, click counters, data-provider query logs) — over sniffing client component state.
- Add a control `NativeButton` for every server-side action you may need: set value, refresh, detach/reattach, toggle visibility.
- Put variants **side by side** (failing setup + control in one view) instead of editing sequentially — the minimal pair also isolates the trigger for root-cause analysis.
- Switch multiplying variants with query parameters (read `QueryParameters`, e.g. `?columns=50&headerRow=true`) instead of recompiling.
- **Batch every anticipated fix before the first build** — missing dependencies (§4), API substitutions (§5), seed data — each missed one costs a full restart.

## 4. Add missing component dependencies

An integration-tests module depends only on **its own** component plus `flow-html-components` (`Div`, `Span`, etc.). A cross-component reproduction fails to compile with `package … does not exist` / `cannot find symbol` for components the module lacks.

Scan your View's imports first. When a component is incidental (layout, a trigger button, a text input), prefer what the module already has — `Div`, `Span`, `NativeButton`, native DOM — over adding a dependency. For each remaining `com.vaadin.flow.component.<x>` package not already a `<dependency>` in the IT module's `pom.xml`, add it with `<version>${project.version}</version>`. The artifactId mirrors the package with hyphenation — `…component.dialog` → `vaadin-dialog-flow`, `…component.textfield` → `vaadin-text-field-flow`, `…component.orderedlayout` → `vaadin-ordered-layout-flow`; `…component.html` (Div, Span, …) is already present via `flow-html-components`.

This pom edit is part of the scaffold. When the bug reproduces it is **committed** on the `repro/<issue>` branch (see [share.md](share.md)) so the branch is runnable — do not revert it. Only when the bug does **not** reproduce do you revert it in cleanup. The `-am` build flag compiles the newly added sibling modules from source, so the first build is longer.

## 5. Porting an old reporter example

- **A compile failure on a faithful port is weak, obsolete evidence** (removed APIs, e.g. `PolymerTemplate` → `LitTemplate`) — note it in the summary, then reproduce the behavior with the modern API as resolved in Phase 1 (issue-analysis.md §3–4).

## 6. Run the server

The first run compiles the frontend and is slow. Start in the background and poll for real readiness — never guess with `sleep`. **Set expectations:** the first build of a module in a session takes ~3–6 min (frontend bundle); later restarts ~60–90 s. Warn the user so a quiet log isn't mistaken for a hang.

```bash
cd "<ROOT>" && CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests \
  -pl vaadin-<component>-flow-parent/vaadin-<component>-flow-integration-tests
```

- **`CI=true` is required in this headless (no-TTY) environment.** Without it, pnpm aborts when purging `node_modules` (`ERR_PNPM_ABORTED_REMOVE_MODULES_DIR_NO_TTY`) — the HTTP port still comes up but dev mode never initializes, so the page is a blank spinner that looks like a real bug.
- **Watch for build failure, not just success.** Wait on the task's output file with one call that breaks on failure markers too, or it hangs forever on a broken build:
  ```bash
  timeout 360 bash -c 'until grep -qEa "Frontend compiled successfully|BUILD FAILURE|ERR_PNPM|Dependency ERROR|does not exist|Address already in use" <task-output-file>; do sleep 2; done'; \
  grep -aE "Frontend compiled successfully|BUILD FAILURE|ERR_PNPM|Address already in use" <task-output-file> | head -3
  ```
  If the harness blocks foreground waits, run the same loop as a background task and block on its completion. **A listening port 8080 is not proof of readiness** — confirm `Frontend compiled successfully` appeared.

## 7. Pitfalls

- **Stale dev bundle after a branch/version switch — the silent trap.** The generated frontend (`<IT-module>/frontend/generated/`, including the `jar-resources/*Connector.ts` the page actually serves), plus `node_modules/` and `node_modules/.vite`, are **git-ignored, so `git checkout` never touches them** and `-Dvaadin.frontend.hotdeploy=true` does **not** re-extract the jar-resources. Switching between **version lines** (e.g. `24.10` ↔ `25.x`/`main`, or a shared checkout pulled/reset under you — check `git reflog`) advances `src/` and `package.json` but leaves the connector frozen, so it calls web-component internals the fresh `@vaadin/*` no longer has.
  - **Signature:** a client-console `TypeError: <element>.<method> is not a function` whose stack is in `VAADIN/generated/jar-resources/*Connector.*`. It fires on data updates, appears on **every** page of that component including maintained ITs, and is a **build artifact — never a product bug**.
  - **Force a clean regeneration** (only when switching version lines, or when the signature fires). `mvn clean` alone is insufficient: `frontend/generated/` lives at the module root, not under `target/`.
    ```bash
    mvn clean -q -am -pl vaadin-<component>-flow-parent/vaadin-<component>-flow-integration-tests
    rm -rf vaadin-<component>-flow-parent/vaadin-<component>-flow-integration-tests/frontend/generated \
           vaadin-<component>-flow-parent/vaadin-<component>-flow-integration-tests/node_modules/.vite
    ```
  - **Confirm it's fresh:** fetch the served connector and grep for the method the TypeError named — `curl -s http://localhost:8080/VAADIN/generated/jar-resources/<component>Connector.ts | grep -c <method>` should be `0`, and a data update should log no `jar-resources` TypeError.
- **`ERR_PNPM_MINIMUM_RELEASE_AGE_VIOLATION`** can fail the install on bleeding-edge lines (`main`/`*-SNAPSHOT`) whose `@vaadin/*` nightlies are < 24h old and trip pnpm's supply-chain guard. `CI=true` doesn't fix it — reproduce on the latest released maintenance branch (§8), or ask the user for the local override.
- **Old branches may not compile under a modern JDK.** Older lines pin an old `maven-compiler-plugin` (e.g. `3.1`) that crashes on Java 17/21 — tell-tale is a `BUILD FAILURE` in a *shared* module (e.g. `vaadin-flow-components-test-util`) with `Cannot read the array length because "arr$" is null`. Remedy: reproduce on the **newest maintenance line that still shows the bug**. Only if truly version-specific, point Maven at a matching JDK via `JAVA_HOME=/path/to/jdkNN mvn …` — ask which JDK rather than guessing.
- The server does **not** hot-reload Java — restart after editing the View.
- If port 8080 is already in use, stop and ask the user.

## 8. Maintenance branches

Branches are named `<major>.<minor>` (e.g. `24.10`, `25.1`). The branch for "up to 24.10.x" is `24.10`.

**Pick the newest line that still reproduces — not necessarily the reported minor.** A bug filed against `24.5` almost always still reproduces up the `24.x` line, and a newer branch builds cleanly under a modern JDK whereas the oldest (≲ 24.6) fail to compile under Java 17/21 (§7). Start at the latest line of the reported major; drop to an older minor only if it doesn't reproduce there (then you've also learned the upper bound). For Flow, try `24.10` before `24.5`.

Safety — never clobber the user's work:

1. Record the starting branch: `git -C <ROOT> rev-parse --abbrev-ref HEAD`.
2. Require a clean tree. If `git -C <ROOT> status --porcelain` is non-empty, stop and ask — do not stash or discard.
3. Fetch and check out: `git -C <ROOT> fetch origin <major>.<minor> && git -C <ROOT> checkout <major>.<minor>`.
4. **Force the frontend to regenerate** — `git checkout` doesn't clean the git-ignored generated frontend, so moving between lines mandates the "Force a clean regeneration" command in §7.

A **"version-specific" verdict needs evidence at both ends**: reproduce on the affected branch **and** confirm it does *not* reproduce on the current checkout. If it doesn't reproduce even on the affected branch, say so.

## 9. Stop it cleanly

```bash
cd "<ROOT>" && mvn jetty:stop -pl vaadin-<component>-flow-parent/vaadin-<component>-flow-integration-tests
```

Do **not** kill the background Maven task to stop Jetty — that orphans the forked Jetty JVM holding port 8080.
