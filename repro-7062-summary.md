> [!WARNING]
> Automated reproduction by the Claude Code `repro` skill. Needs human verification.

**Reproduced** on `24.10` (24.10-SNAPSHOT, `@vaadin/grid` 24.10.10). Fixed on `main` by vaadin/web-components#10443 (25.0.0, not backported to 24.x). Not a regression.

**Root cause**: In `vaadin-grid` 24.x, `_renderColumnTree()` re-creates the cells of the body rows that already exist when the first column arrives. It updates the row parts but never calls `_toggleDetailsCell()`. The details cells of those rows stay visible, even though `detailsOpened` is false. Only the rows rendered before the columns arrive are affected, which here are the first 3. vaadin/web-components#10443 changed `_renderColumnTree()` to run the full `__updateRow()`, which also toggles the details cell. That PR targeted CSS parts, so this fix was a side effect.

https://github.com/vaadin/web-components/blob/e0df05b282cba861120700bd049842bf94247d5c/packages/grid/src/vaadin-grid-mixin.js#L703-L711

Reproduction: branch [`repro/7062`](https://github.com/vaadin/flow-components/tree/repro/7062), route `/repro-7062`.

<details>
<summary>Details</summary>

| Case | `24.10` | `main` |
| --- | --- | --- |
| Columns, items and details renderer added before attach | details hidden | details hidden |
| Same setup on an attached empty grid (reporter's button) | rows 0 to 2: details cell shown (36px), rows 3 and 4: hidden | details hidden |
| Reporter's button clicked again | details hidden | details hidden |
| 24.10, `_renderColumnTree()` patched in the browser to call `_toggleDetailsCell()` per row | details hidden | |

The last row checks the mechanism on 24.10 without a rebuild. The fix in 25.0 was not bisected.

</details>
