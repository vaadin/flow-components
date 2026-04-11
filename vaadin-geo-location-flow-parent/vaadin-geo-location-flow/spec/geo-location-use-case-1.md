# Use Case 1 — Show information relevant to where I am right now

The user clicks a button, the browser asks for permission, and the
application reacts to the captured position by showing content tailored
to where the user currently is. This is the canonical shape and the
**primary** use case — it should be the easiest to implement.

## Example: "Find stores near me"

A restaurant-chain view with a single button and a list of nearby
locations.

```java
@Route("stores")
@PageTitle("Find a restaurant")
public class StoreFinderView extends VerticalLayout {

    private final GeoLocation locate = new GeoLocation("Use my location");
    private final Div results = new Div();
    private final Paragraph message = new Paragraph();

    public StoreFinderView(StoreService stores) {
        locate.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locate.addThemeVariants(GeoLocationVariant.LUMO_PRIMARY);

        locate.addLocationListener(event -> {
            if (event.isSuccess()) {
                GeoLocationPosition position = event.getPosition().get();
                message.setText("");
                results.removeAll();
                stores.findNearest(position.getLatitude(),
                                position.getLongitude(), 3)
                        .forEach(store -> results.add(renderStore(store)));
            } else {
                GeoLocationError error = event.getError().get();
                results.removeAll();
                message.setText(switch (error.getCode()) {
                    case PERMISSION_DENIED ->
                            "Location permission denied. Please enter a postcode instead.";
                    case TIMEOUT ->
                            "Location request timed out. Please try again.";
                    default -> "Could not determine your location.";
                });
            }
        });

        add(new H2("Restaurants near you"), locate, message, results);
    }

    private Component renderStore(Store store) {
        return new Div(new H3(store.name()),
                new Paragraph(store.address()),
                new Paragraph("%.1f km away".formatted(store.distanceKm())));
    }
}
```

## Why this is the easiest path

- **One line to create the component** (`new GeoLocation("Use my location")`).
- **One listener** handles both success and failure. No separate error
  event, no separate "location unavailable" path.
- **The position arrives as a typed Java object**, so the service call
  uses `position.getLatitude()` / `getLongitude()` directly — no parsing
  of a `"lat,lng"` string, no DOM types on the server.
- **The button activates itself.** The developer does not need to wire
  up a click listener or call `navigator.geolocation` manually — the
  component's activation triggers the request.

## Notes

- Activating the button again later yields a fresh reading. The
  component does not cache the previous position on the server unless
  `setMaximumAge` is set.
- A brand new `GeoLocation` defaults to a single one-shot request per
  click, which is exactly what this use case needs. `watch` and
  `autoLocate` are both off by default.
