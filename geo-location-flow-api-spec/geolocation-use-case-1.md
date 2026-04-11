# Use Case 1 — Show information relevant to where I am right now

The user clicks a button, the browser asks for permission, and the
application reacts to the captured position by showing content tailored
to where the user currently is. This is the canonical shape and the
**primary** use case.

With the PR 23527 API, the Flow side contributes `Geolocation.get`; the
application composes a plain Vaadin `Button` with that static call.

## Example: "Find stores near me"

```java
@Route("stores")
@PageTitle("Find a restaurant")
public class StoreFinderView extends VerticalLayout {

    private final Button locate = new Button("Use my location");
    private final Div results = new Div();
    private final Paragraph message = new Paragraph();

    public StoreFinderView(StoreService stores) {
        locate.setIcon(VaadinIcon.MAP_MARKER.create());
        locate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        locate.addClickListener(e -> Geolocation.get(
                pos -> {
                    message.setText("");
                    results.removeAll();
                    stores.findNearest(
                                    pos.coords().latitude(),
                                    pos.coords().longitude(),
                                    3)
                            .forEach(store -> results.add(renderStore(store)));
                },
                err -> {
                    results.removeAll();
                    message.setText(switch (err.code()) {
                        case GeolocationError.PERMISSION_DENIED ->
                                "Location permission denied. "
                                        + "Please enter a postcode instead.";
                        case GeolocationError.TIMEOUT ->
                                "Location request timed out. Please try again.";
                        default -> "Could not determine your location.";
                    });
                }));

        add(new H2("Restaurants near you"), locate, message, results);
    }

    private Component renderStore(Store store) {
        return new Div(
                new H3(store.name()),
                new Paragraph(store.address()),
                new Paragraph("%.1f km away".formatted(store.distanceKm())));
    }
}
```

## How the PR API covers UC1

- **Single click → single callback.** `Geolocation.get(onSuccess, onError)`
  is the two-argument form; the success callback gets a typed
  `GeolocationPosition`, the error callback gets a `GeolocationError`,
  and both run on the UI thread so they can update components
  directly.
- **Typed position**. `pos.coords().latitude()` and
  `pos.coords().longitude()` are plain Java `double` accessors on
  records — no JSON, no string parsing.
- **Error codes are the W3C numeric codes** exposed as
  `public static final int` constants on `GeolocationError`. The
  `switch` is readable without reaching for an enum.

## What the application owns

Unlike the earlier "component with a button built in" design, the PR
API does *not* provide a button. Every view that needs a location
trigger owns its own `Button` (or any other trigger — a keyboard
shortcut, a context menu item, a click on a map) and calls
`Geolocation.get` from the handler. This is one extra line of code
per view but keeps the Flow API free of opinions about rendering.
