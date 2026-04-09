# Use Case 1: Basic Navigation

Manual construction of a breadcrumb trail with string paths.

## What needs to work

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.addItem(new BreadcrumbItem("Home", "/"));
breadcrumb.addItem(new BreadcrumbItem("Products", "/products"));
breadcrumb.addItem(new BreadcrumbItem("Widgets", "/products/widgets"));
breadcrumb.addItem(new BreadcrumbItem("Sprocket"));
```

## Implementation needed

### `BreadcrumbItem`

New class. Tag: `vaadin-breadcrumb-item`.

```java
@Tag("vaadin-breadcrumb-item")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.2.0-alpha6")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb-item.js")
public class BreadcrumbItem extends Component implements HasText, HasEnabled, HasPrefix {

    public BreadcrumbItem() {
    }

    public BreadcrumbItem(String text) {
        setText(text);
    }

    public BreadcrumbItem(String text, String path) {
        setText(text);
        setPath(path);
    }

    public void setPath(String path) {
        getElement().setProperty("path", path);
    }

    public Optional<String> getPath() {
        String path = getElement().getProperty("path");
        return Optional.ofNullable(path);
    }
}
```

Key details:
- `path` maps to the web component's `path` property via `Element.setProperty()`
- `HasText` provides `setText()`/`getText()` — maps to the default slot text content
- `HasEnabled` provides `setEnabled()`/`isEnabled()` — maps to the `disabled` attribute (inverted)
- `HasPrefix` provides `setPrefixComponent()`/`getPrefixComponent()` — maps to the `prefix` slot
- The web component handles `aria-current="page"` automatically when `path` is absent

### `Breadcrumb`

Update the existing skeleton class.

```java
@Tag("vaadin-breadcrumb")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.2.0-alpha6")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb.js")
public class Breadcrumb extends Component implements HasSize, HasAriaLabel {

    public Breadcrumb() {
    }

    public Breadcrumb(BreadcrumbItem... items) {
        addItem(items);
    }

    public void addItem(BreadcrumbItem... items) {
        for (BreadcrumbItem item : items) {
            getElement().appendChild(item.getElement());
        }
    }

    public void addItemAsFirst(BreadcrumbItem item) {
        getElement().insertChild(0, item.getElement());
    }

    public void addItemAtIndex(int index, BreadcrumbItem item) {
        getElement().insertChild(index, item.getElement());
    }

    public void remove(BreadcrumbItem... items) {
        for (BreadcrumbItem item : items) {
            getElement().removeChild(item.getElement());
        }
    }

    public void removeAll() {
        getElement().removeAllChildren();
    }
}
```

Key details:
- Items are added as child elements in the default slot
- `HasAriaLabel` maps to the web component's `label` property (used as `aria-label` on the navigation landmark)
- No `items` property synchronization needed — this use case uses only the child component (slot) API

### Unit tests needed

- Create `BreadcrumbItem` with text only → `getText()` returns text, `getPath()` returns empty
- Create `BreadcrumbItem` with text and path → both accessible
- `setPath(null)` clears the path
- `addItem()` adds children to the element
- `removeAll()` clears all children
- `addItemAsFirst()` / `addItemAtIndex()` insert at correct positions
- `Breadcrumb(BreadcrumbItem...)` constructor adds all items
- `HasEnabled` works (setEnabled false → disabled attribute)
- `HasPrefix` works (setPrefixComponent → slot="prefix")

### Integration test view

A page with a static breadcrumb trail showing `Home > Products > Widgets > Sprocket`, where the first three items are clickable links and the last is plain text.
