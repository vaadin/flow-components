# Use Case 6: Limiting Depth

Breadcrumb shows only the first N levels of the hierarchy.

## What needs to work

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setAutoGeneration(true);
breadcrumb.setMaxItems(2);
```

Navigating to `/departments/engineering/frontend` produces: `Departments > Engineering`

## Prerequisites

Use case 3 (auto-generation) must be implemented first.

## Implementation needed

### `Breadcrumb` — maxItems field

```java
private int maxItems = 0; // 0 means unlimited

public void setMaxItems(int maxItems) {
    if (maxItems < 0) {
        throw new IllegalArgumentException("maxItems must be >= 0");
    }
    this.maxItems = maxItems;
}

public int getMaxItems() {
    return maxItems;
}
```

### `Breadcrumb.generateItems` — truncate items

After filtering by root (use case 5) and before calling the customizer:

```java
// Limit to maxItems
if (maxItems > 0 && items.size() > maxItems) {
    items = items.subList(0, maxItems);
}
```

Key details:
- `maxItems = 0` means unlimited (default).
- Truncation keeps the first N items (top levels of the hierarchy). The last visible item becomes the "current page" (path cleared).
- This interacts with root scoping: scoping happens first, then depth limiting is applied to the remaining items.
- The truncated last item gets its path cleared (current page behavior), even though it's not actually the deepest page in the URL. This is correct — the breadcrumb shows "where you are" in the limited view.

### Unit tests needed

- `setMaxItems(2)` with 4 generated items → only first 2 shown
- `setMaxItems(0)` → all items shown (unlimited)
- `setMaxItems(N)` where N > item count → all items shown
- Negative value → `IllegalArgumentException`
- Last item after truncation has no path
- Interaction with `setRootPath`: scoping first, then depth limit

### Integration test view

A breadcrumb with `maxItems(2)` in a layout with a three-level hierarchy. Navigating to the deepest level shows only the top two levels. A button toggles `maxItems` to verify dynamic changes.
