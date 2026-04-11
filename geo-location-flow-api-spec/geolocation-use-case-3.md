# Use Case 3 — Automatically use my location when I come back

The user has already granted the site location permission on a previous
visit. On every subsequent visit the local-content panel should appear
pre-populated, without an extra click. If permission has not been
granted yet (first visit, revoked, private window), the application
must fall back to the explicit flow from UC1 — not pop an unexpected
prompt on page load.

With the PR 23527 API, "auto-fetch on view load" is straightforward:
call `Geolocation.get` from `onAttach`. The trickier part — **"never
prompt on cold load when permission is not already granted"** — is
not covered by the API; the application has to query the browser's
Permissions API itself (via `executeJs`) before calling
`Geolocation.get`.

## Example: News portal with a "local headlines" panel

### Naive version: auto-fetch always (may prompt first-time visitors)

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
        // Will silently succeed for returning users whose permission is
        // already 'granted'. Will prompt first-time visitors — see the
        // "permission-gated" version below to avoid that.
        fetchAndPopulate();
    }

    private void fetchAndPopulate() {
        Geolocation.get(
                new GeolocationOptions(null, 5000, 300_000),
                pos -> populateLocalHeadlines(news, pos),
                err -> { /* stay quiet — the user will click the button */ });
    }
}
```

### Permission-gated version (matches UC3's "no surprise prompt")

Use a tiny `executeJs` snippet to ask the browser's Permissions API
whether we already have consent, and only call `Geolocation.get` if
we do. Everything else is identical.

```java
@Override
protected void onAttach(AttachEvent event) {
    super.onAttach(event);

    event.getUI().getPage().executeJs(
                    "return navigator.permissions"
                            + " ? navigator.permissions.query({name:'geolocation'})"
                            + "       .then(r => r.state)"
                            + " : 'unknown'")
            .toCompletableFuture(String.class)
            .thenAccept(state -> event.getUI().access(() -> {
                if ("granted".equals(state)) {
                    fetchAndPopulate();
                }
                // state == 'prompt' / 'denied' / 'unknown' → do nothing;
                // the user will click the explicit button (UC1).
            }));
}
```

## How the PR API covers UC3

- **`Geolocation.get` is callable from any UI-thread code**, including
  `onAttach`. There is no "auto mode" flag — the application decides
  when to call it.
- **Returning-user case is one line.** Inside an `onAttach` that has
  already confirmed permission is granted, `Geolocation.get` runs
  silently and the local panel populates before the user notices.
- **UC3's no-prompt-on-cold-load requirement needs extra code.** The
  PR does not expose the Permissions API, so the application queries
  it directly with `executeJs`. The snippet above is the complete
  glue; once a helper like `Geolocation.currentPermission(ui)` is
  available in a future revision, this becomes a one-liner, but for
  now it is a few lines of JS in the application.

## What this looks like compared to the earlier "button component" spec

The earlier spec had `setAutoLocate(true)` on the component, which
internally called the Permissions API and auto-gated. The PR API
pushes that decision into the application for two reasons: it keeps
the Flow surface minimal, and it lets applications make their own
trade-offs (e.g. some apps legitimately *want* to prompt on first
load, and a one-size-fits-all `autoLocate` flag would have to pick a
default). The trade-off is that UC3 moves from "one method call" to
"one method call + a few lines of `executeJs`".
