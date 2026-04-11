# Use Case 4 — Handle users who say "no" or can't share a location

Permission denials, failed fixes, timeouts, and "location is not
available at all" are the normal path for a significant share of users.
The application must recognise each case and offer a sensible
alternative — a blank page is not acceptable.

## Example: Store finder with manual postcode fallback

```java
@Route("stores")
@PageTitle("Find a store")
public class StoreFinderView extends VerticalLayout {

    private final GeoLocation locate = new GeoLocation("Find stores near me");
    private final TextField postcode = new TextField("Postcode");
    private final Paragraph hint = new Paragraph();
    private final Div results = new Div();

    public StoreFinderView(StoreService stores) {
        postcode.setVisible(false);
        hint.addClassName(LumoUtility.TextColor.SECONDARY);

        // Case A: the Geolocation API is not usable at all in this context
        // (insecure origin, Permissions-Policy block, unsupported browser).
        // Hide the button entirely — no click will ever succeed — and show
        // the manual postcode fallback from the start.
        if (locate.isUnavailable()) {
            locate.setVisible(false);
            postcode.setVisible(true);
            hint.setText("Enter a postcode to find nearby stores.");
        }

        // Case B: the user previously denied permission. Show help text
        // up front so returning users understand why the button does
        // nothing when they click it.
        if (locate.getPermission() == GeoPermissionState.DENIED) {
            hint.setText("Location is blocked for this site. Click the "
                    + "padlock in the address bar to re-enable, or enter "
                    + "a postcode below.");
            postcode.setVisible(true);
        }

        locate.addLocationListener(event -> {
            if (event.isSuccess()) {
                postcode.setVisible(false);
                hint.setText("");
                showStoresFor(event.getPosition().get(), stores);
                return;
            }
            // Case C–E: permission denied at click time, position
            // unavailable, or timeout. In each case, reveal the manual
            // fallback and show a targeted message.
            GeoLocationError error = event.getError().get();
            postcode.setVisible(true);
            hint.setText(switch (error.getCode()) {
                case PERMISSION_DENIED ->
                        "Location not shared. Please enter a postcode below.";
                case POSITION_UNAVAILABLE ->
                        "We couldn't determine your location. Please enter a postcode.";
                case TIMEOUT ->
                        "Location took too long. Please enter a postcode or try again.";
                default -> "Location unavailable. Please enter a postcode.";
            });
        });

        postcode.addValueChangeListener(e -> showStoresForPostcode(e.getValue(), stores));

        add(new H2("Find a store"), locate, hint, postcode, results);
    }
}
```

## Why the Flow API models UC4 cleanly

- **Error path uses the same event.** One `addLocationListener` covers
  UC1's success path and UC4's failure path. No second listener that
  the developer could forget to register — historically the most
  common bug with the raw Geolocation API.
- **Named error codes.** `switch` on `GeoLocationErrorCode` is
  exhaustive and refactor-safe. Application code never handles the
  raw numeric codes the browser exposes.
- **`isUnavailable()` is separate from an error on a click.** A
  request that *fails* with `PERMISSION_DENIED` is still a successful
  call whose outcome was "the user said no". A context where the API
  is not usable at all (insecure origin, policy block) is different —
  the component surfaces this via `isUnavailable()` so the application
  can hide the control up front, rather than leaving a button whose
  every click is guaranteed to fail.
- **`getPermission()` lets the application pre-explain.** Returning
  `DENIED` users get a help message before they even try clicking, so
  they are not left wondering why nothing happened.
- **`PermissionChangeEvent` for live updates.** If the user
  re-enables location from the browser's site settings while the view
  is visible, a `PermissionChangeEvent` fires and the view can react
  without a reload.

## Note on the timeout path

The web component resets the button to its idle state after the timeout
fires, so re-clicking it issues a fresh request. The Flow application
does not need to call `clear()` or reset anything manually.
