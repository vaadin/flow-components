# Use Case 4 — Handle users who say "no" or can't share a location

Permission denials, failed fixes, timeouts, and "location is not
available at all" are the normal path for a significant share of
users. Every application that uses location has to cope with these
situations gracefully — a blank page is not acceptable.

With the PR 23527 API, error handling lives in the `onError` callback
of `Geolocation.get` (for one-shot requests) or the
`GeolocationError` branch of the state signal (for tracking). Error
codes are the raw W3C numerics (`PERMISSION_DENIED=1`,
`POSITION_UNAVAILABLE=2`, `TIMEOUT=3`) exposed as constants on
`GeolocationError`.

## Example: Store finder with manual postcode fallback

```java
@Route("stores")
@PageTitle("Find a store")
public class StoreFinderView extends VerticalLayout {

    private final Button locate = new Button("Find stores near me");
    private final TextField postcode = new TextField("Postcode");
    private final Paragraph hint = new Paragraph();
    private final Div results = new Div();

    public StoreFinderView(StoreService stores) {
        postcode.setVisible(false);
        hint.addClassName(LumoUtility.TextColor.SECONDARY);

        // Case A — "API not usable at all" (insecure origin, no
        // navigator.geolocation, Permissions-Policy block). The PR
        // API does not surface this, so we check it ourselves via
        // executeJs once on attach.
        checkApiUsable().thenAccept(usable -> {
            if (!usable) {
                locate.setVisible(false);
                postcode.setVisible(true);
                hint.setText("Enter a postcode to find nearby stores.");
            }
        });

        locate.addClickListener(e -> Geolocation.get(
                pos -> {
                    postcode.setVisible(false);
                    hint.setText("");
                    showStoresFor(pos, stores);
                },
                err -> {
                    // Case B–D — permission denied at click time,
                    // position unavailable, or timeout. One listener,
                    // one switch.
                    postcode.setVisible(true);
                    hint.setText(switch (err.code()) {
                        case GeolocationError.PERMISSION_DENIED ->
                                "Location not shared. Please enter a postcode below.";
                        case GeolocationError.POSITION_UNAVAILABLE ->
                                "We couldn't determine your location. "
                                        + "Please enter a postcode.";
                        case GeolocationError.TIMEOUT ->
                                "Location took too long. Please enter a "
                                        + "postcode or try again.";
                        default -> "Location unavailable. "
                                + "Please enter a postcode.";
                    });
                }));

        postcode.addValueChangeListener(e ->
                showStoresForPostcode(e.getValue(), stores));

        add(new H2("Find a store"), locate, hint, postcode, results);
    }

    private CompletableFuture<Boolean> checkApiUsable() {
        return UI.getCurrent().getPage()
                .executeJs(
                        "return (typeof navigator.geolocation !== 'undefined') "
                                + "&& window.isSecureContext")
                .toCompletableFuture(Boolean.class);
    }
}
```

## How the PR API covers UC4

- **Single error callback.** The `onError` lambda of
  `Geolocation.get(onSuccess, onError)` is the single place that
  handles permission denials, position failures, and timeouts. No
  second listener to forget to register.
- **W3C numeric codes as constants.** `GeolocationError.PERMISSION_DENIED`,
  `POSITION_UNAVAILABLE`, and `TIMEOUT` are `public static final int`
  on the record, so the `switch` is readable without an enum. The
  spec does not wrap them in a Java enum because the PR keeps the
  Java shape identical to the wire shape.
- **Browser message is forwarded.** `err.message()` is whatever the
  browser provided, for cases where the application wants to surface
  it directly.

## Gaps compared with the earlier "button component" spec

The earlier spec had two Flow-specific features that the PR does
not have. Each is still achievable in a few lines of application
code.

| Gap | Why the PR omits it | How the application fills it |
|---|---|---|
| `isUnavailable()` — "API is not usable at all in this context" | Not exposed; PR is a thin W3C wrapper | `executeJs` check for `typeof navigator.geolocation !== 'undefined' && window.isSecureContext`, as in `checkApiUsable()` above |
| `getPermission()` — pre-explain why the feature is blocked to previously-denied users | Not exposed; no Permissions API bridge | `executeJs` snippet from UC3's permission-gated variant, reading `navigator.permissions.query({name:'geolocation'}).state` |
| `PermissionChangeEvent` — react when the user re-enables location from site settings | Not exposed | Add an `executeJs` listener for `permissions.query().onchange`, forward to server via a custom event |

None of these gaps change the core UC4 flow — the `switch` over
`GeolocationError.code()` handles the three W3C error codes directly,
and the application-side `checkApiUsable()` helper handles the
"can never work" case in a few lines.

## Note on the timeout path

When `GeolocationError.TIMEOUT` fires, the callback runs once and
returns. The button is still a regular `Button`, so the user can
click it again to issue a fresh request — no reset logic is needed.
This is consistent with every other one-shot click flow in Flow.
