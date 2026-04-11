# Use Case 3 — Automatically use my location when I come back

The user has already granted the site location permission on a previous
visit. On every subsequent visit the local-content panel should appear
pre-populated, without an extra click. If permission has not been
granted yet (first visit, revoked, private window), the application
must fall back to the explicit flow from UC1 — not pop an unexpected
prompt on page load.

`Geolocation.queryPermission(callback)` returns the current browser
permission state as a `GeolocationPermission` enum. Gating
`Geolocation.get` on `GRANTED` is the complete UC3 recipe in Java.

## Example: News portal with a "local headlines" panel

```java
@Route("")
@PageTitle("News")
public class HomeView extends VerticalLayout {

    private final Div localHeadlines = new Div();
    private final Button locate = new Button("Use my location");
    private final NewsService news;

    public HomeView(NewsService news) {
        this.news = news;
        locate.addClickListener(e -> fetchAndPopulate());
        add(new H1("Today's headlines"), localHeadlines,
                new Paragraph("Don't see local headlines? Click below:"),
                locate);
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        Geolocation.queryPermission(state -> {
            if (state == GeolocationPermission.GRANTED) {
                fetchAndPopulate();
            }
            // DENIED / PROMPT / UNKNOWN → do nothing; the user will
            // click the explicit button (UC1).
        });
    }

    private void fetchAndPopulate() {
        Geolocation.get(
                new GeolocationOptions(null, 5000, 300_000),
                pos -> populateLocalHeadlines(news, pos),
                err -> { /* stay quiet — the user has the button */ });
    }
}
```

## How the Flow API covers UC3

- **`Geolocation.queryPermission(callback)`** is a single-line query
  of the browser's Permissions API. The callback runs on the UI
  thread so it can call `Geolocation.get` directly.
- **Only `GRANTED` triggers a browser call.** First-time visitors
  (`PROMPT`), previously-denied visitors (`DENIED`), and browsers
  that cannot report state (`UNKNOWN`) never see an unexpected
  permission dialog. This is exactly what UC3 calls for.
- **Returning-user case is a one-line branch.** Once `GRANTED` has
  been confirmed, `Geolocation.get` runs silently and the local
  panel populates before the user notices.

## Safari limitation

Safari does not implement `navigator.permissions.query({name: 'geolocation'})`
— it throws a `TypeError` on unknown permission names. The Flow
wrapper catches that and returns `GeolocationPermission.UNKNOWN`, so
**Safari users never see the `GRANTED` branch** and auto-fetch never
fires for them. They fall through to the explicit-click flow (UC1),
which is the safest degradation.

This is a browser limitation with no known workaround. Firefox and
Chromium-based browsers return the correct state.

## Reacting to later permission changes

`queryPermission` is a one-shot query. To react to the user
re-enabling or revoking location mid-session, call `queryPermission`
on every attach (the code above already does this) or, for views
that stay mounted for a long time, poll on a timer. A reactive
`Signal<GeolocationPermission>` tracking `permissionchange` events
was considered but is not part of the API — Firefox's
`permissionchange` is unreliable and Safari never fires it, so a
signal-based variant would have inconsistent semantics across
browsers.
