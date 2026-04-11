# Use Case 4 — Handle users who say "no" or can't share a location

Permission denials, failed fixes, timeouts, and "location is not
available at all" are the normal path for a significant share of
users. Every application that uses location has to cope with these
situations gracefully — a blank page is not acceptable.

Three Flow methods cover the three distinct UC4 paths:

1. **`Geolocation.isSupported(callback)`** — "API not usable at all"
   (insecure origin, no `navigator.geolocation`, Permissions-Policy
   block). Applications should hide the location control entirely
   when this returns `false`, because no click will ever succeed.
2. **`Geolocation.queryPermission(callback)`** — "previously denied"
   (returns `DENIED`). Applications pre-explain why the button is
   doing nothing and how to re-enable location.
3. **`err.errorCode()` in the `onError` callback** — "this request
   failed". Returns a `GeolocationErrorCode` enum so the `switch` is
   exhaustive.

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

        // 1. "API not usable at all" → hide the button entirely.
        Geolocation.isSupported(supported -> {
            if (!supported) {
                locate.setVisible(false);
                postcode.setVisible(true);
                hint.setText("Enter a postcode to find nearby stores.");
            }
        });

        // 2. "Previously denied" → pre-explain before the user
        //    even tries clicking.
        Geolocation.queryPermission(state -> {
            if (state == GeolocationPermission.DENIED) {
                hint.setText("Location is blocked for this site. Click the "
                        + "padlock in the address bar to re-enable, or "
                        + "enter a postcode below.");
                postcode.setVisible(true);
            }
        });

        // 3. "This request failed" → exhaustive switch over the enum.
        locate.addClickListener(e -> Geolocation.get(
                pos -> {
                    postcode.setVisible(false);
                    hint.setText("");
                    showStoresFor(pos, stores);
                },
                err -> {
                    postcode.setVisible(true);
                    hint.setText(switch (err.errorCode()) {
                        case PERMISSION_DENIED ->
                                "Location not shared. Please enter a postcode below.";
                        case POSITION_UNAVAILABLE ->
                                "We couldn't determine your location. "
                                        + "Please enter a postcode.";
                        case TIMEOUT ->
                                "Location took too long. Please enter a "
                                        + "postcode or try again.";
                        case null ->
                                // unknown future W3C code
                                "Location unavailable (" + err.message() + "). "
                                        + "Please enter a postcode.";
                    });
                }));

        postcode.addValueChangeListener(e ->
                showStoresForPostcode(e.getValue(), stores));

        add(new H2("Find a store"), locate, hint, postcode, results);
    }
}
```

## How the Flow API covers UC4

- **`isSupported` separates "can never work" from "failed this
  time".** When it returns `false`, the view hides the button
  entirely; when it returns `true`, an error in the `onError`
  callback means "retry is possible". The two situations need
  different UI and the two methods make the distinction explicit.
- **`queryPermission` surfaces `DENIED` so the application can
  pre-explain.** A returning user whose previous denial is blocking
  every click sees the help text the moment the view loads,
  instead of clicking a button that silently does nothing.
- **`err.errorCode()` is a `GeolocationErrorCode` enum**, so the
  `switch` is exhaustive. `case null` handles any unknown future
  W3C code — the code still compiles if the W3C spec is ever
  extended, and the `null` arm shows a reasonable fallback message.
- **Browser message is forwarded.** `err.message()` is whatever the
  browser provided, in case the application wants to surface it
  directly.

## Safari limitation

`Geolocation.queryPermission` returns `GeolocationPermission.UNKNOWN`
on Safari regardless of the user's actual setting, because Safari
does not support `permissions.query({name: 'geolocation'})`. This
means **the "previously denied" pre-explain branch does not fire on
Safari** — Safari users see the unadorned button and only discover
the denial when they click it. The subsequent `onError` callback
with `GeolocationErrorCode.PERMISSION_DENIED` still fires correctly,
so the user gets the same information just one click later. There
is no browser-side workaround.

`Geolocation.isSupported` works reliably in every supported browser,
including Safari.

## Note on the timeout path

When `GeolocationErrorCode.TIMEOUT` fires, the callback runs once
and returns. The button is still a regular `Button`, so the user
can click it again to issue a fresh request — no reset logic is
needed. This is consistent with every other one-shot click flow in
Flow.
