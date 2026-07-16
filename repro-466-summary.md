<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced (the core "loses filtering" symptom; the report bundles several sub-issues — see Notes)
- **Hypothesis tested:** The bug is the Crud auto-generated filter row disappearing, triggered by calling `crud.getGrid().setColumns(...)` after construction, observable as zero filter fields (`crud-role="Search"`) in the grid header.
- **Regression?:** not a regression (broken since introduction — `setColumns` has always rebuilt the columns after the filter row is set up)
- **Branch:** `repro/466` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ `main` (343bc14f16, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** `![Filter row present in default Crud, gone after setColumns](https://raw.githubusercontent.com/vaadin/flow-components/42460402f1eaebc7c83f64260f078fd4edd7bd5a/repro-466.png)`

## Observed behavior

Minimal pair, same bean and data provider:

| Crud | columns | filter fields (`crud-role="Search"`) |
| --- | --- | --- |
| default (control) | 4 (id, firstName, lastName, edit) | **3** — filter row present |
| after `getGrid().setColumns("firstName","lastName")` | 2 | **0** — filter row empty |

The default Crud shows a "Filter" input under each column; the Crud reconfigured with `setColumns(...)` shows an empty header band with no filter inputs (and also loses the edit column and toolbar). Console clean apart from the favicon 404.

## Expected behavior

Reordering or selecting columns with `crud.getGrid().setColumns(...)` should not remove the auto-generated filter row; filtering should stay available.

## Steps to reproduce

1. Open `http://localhost:8080/repro-466`.
2. Compare the two grids: the default one has a "Filter" field per column; the `setColumns` one has none.

## Reproduction

How to run: start the server (`CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-crud-flow-parent/vaadin-crud-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-466`
- **Scaffold:** `vaadin-crud-flow-parent/vaadin-crud-flow-integration-tests/src/main/java/com/vaadin/flow/component/crud/tests/Repro466View.java`

```java
Crud<Person> crud = new Crud<>(Person.class, Helper.createPersonEditor());
crud.getGrid().setColumns("firstName", "lastName"); // wipes the auto-generated filter row
crud.setDataProvider(new PersonCrudDataProvider());
```

## Root cause (suspected)

`CrudGrid.setupFiltering()` runs once in the constructor, iterating the auto-generated columns and attaching a filter `TextField` to each column's header cell. `Grid.setColumns(...)` removes those columns and creates new ones, so the filter row's cells lose their components and no filter fields exist for the new columns:

https://github.com/vaadin/flow-components/blob/343bc14f161208728e5acafc9a3571930494f4e3/vaadin-crud-flow-parent/vaadin-crud-flow/src/main/java/com/vaadin/flow/component/crud/CrudGrid.java#L72-L97

## Notes

- This issue bundles several distinct sub-claims from 2020; only the primary, deterministic one — filtering lost after `setColumns` — was reproduced here. The others, which need the reporter's `Employee`/`Department` (nested bean) entities, were **not** separately reproduced:
  - nested property `department.departmentName` rendering as a hashcode in auto-generated columns;
  - filtering not working with `CrudServiceDataProvider` (from the `a-vaadin-helper` add-on, not part of this repo);
  - sort throwing for a nested property with a custom data provider.
- Fix archaeology: **fixing PR: none found** for the filtering-lost-after-`setColumns` symptom; it is unchanged on main. Related later fixes touched only filter/sorter aria-labels (#2610, #9608), not this behavior.
- The reporter's linked repro project tag (`Avec112/crud@2020-11-30_issue_#466`) was not fetchable via the API; the reproduction was built from the code in the issue body.
