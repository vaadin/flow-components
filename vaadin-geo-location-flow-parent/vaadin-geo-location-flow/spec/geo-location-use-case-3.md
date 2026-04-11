# Use Case 3 — Automatically use my location when I come back

The user has already granted the site location permission on a previous
visit. On every subsequent visit the local-content panel should appear
pre-populated, without an extra click. If permission has not been
granted yet (first visit, revoked, private window), the application
must fall back to the explicit flow from UC1 — not pop an unexpected
prompt on page load.

## Example: News portal with a "local headlines" panel

```java
@Route("")
@PageTitle("News")
public class HomeView extends VerticalLayout {

    private final GeoLocation locate = new GeoLocation("Use my location");
    private final Div localHeadlines = new Div();

    public HomeView(NewsService news) {
        locate.setAutoLocate(true);
        locate.setMaximumAge(Duration.ofMinutes(5));
        locate.setTimeout(Duration.ofSeconds(5));

        locate.addLocationListener(event -> event.getPosition().ifPresent(p -> {
            localHeadlines.removeAll();
            news.localHeadlinesFor(p.getLatitude(), p.getLongitude())
                    .forEach(story -> localHeadlines.add(renderStory(story)));
        }));

        add(new H1("Today's headlines"),
                localHeadlines,
                new Paragraph("Don't see local headlines? Click below:"),
                locate);
    }
}
```

## Why `autoLocate` is the right mechanism

- **No prompt on cold load.** The Flow component's `autoLocate` flag
  only triggers a request when `getPermission() == GRANTED`. A
  first-time visitor whose permission is `PROMPT` sees nothing happen
  until they click the button, and a previously-denied visitor is
  never nagged.
- **Falls back to UC1 cleanly.** The same `GeoLocation` is still a
  clickable button; if auto-locate does not fire, clicking it produces
  the explicit flow from UC1. No separate "explicit mode" component
  is needed.
- **Zero extra code on the application side.** The single line
  `locate.setAutoLocate(true)` is the entire difference from UC1.

## Avoiding stale state on navigation

On each attach (including after a browser tab switch and back), the
component re-evaluates `autoLocate`. If the user granted permission in
another tab in the meantime, returning to the Flow view will pick it
up on the next attach — no page reload is needed. To react immediately
to permission changes while the view is visible, listen for permission
changes as well:

```java
locate.addPermissionChangeListener(event -> {
    if (event.getPermission() == GeoPermissionState.GRANTED
            && locate.getValue() == null) {
        locate.requestLocation();
    }
});
```

This is optional and only makes sense for long-lived views.
