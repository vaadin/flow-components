# Use Case 5 — Use detailed position data, not just latitude and longitude

Some applications need everything the browser reports: accuracy,
altitude, altitude accuracy, heading, speed, and the reading timestamp.

## Example: Cycling dashboard

```java
@Route("ride/dashboard")
@PageTitle("Ride")
public class RideDashboardView extends VerticalLayout {

    private final GeoLocation tracker = new GeoLocation("Start ride");
    private final Span latLng = new Span();
    private final Span accuracyLabel = new Span();
    private final Span altitudeLabel = new Span();
    private final Span speedLabel = new Span();
    private final Span headingLabel = new Span();
    private final Span takenAtLabel = new Span();

    public RideDashboardView() {
        tracker.setWatch(true);
        tracker.setHighAccuracy(true);
        tracker.setMinimumAccuracy(25); // reject fixes > 25 m

        tracker.addLocationListener(event -> event.getPosition().ifPresent(this::render));

        add(new H2("Ride dashboard"), tracker,
                row("Position", latLng),
                row("Accuracy", accuracyLabel),
                row("Altitude", altitudeLabel),
                row("Speed", speedLabel),
                row("Heading", headingLabel),
                row("Reading taken", takenAtLabel));
    }

    private void render(GeoLocationPosition p) {
        latLng.setText("%.5f, %.5f".formatted(p.getLatitude(), p.getLongitude()));
        accuracyLabel.setText("± %.0f m".formatted(p.getAccuracy()));

        altitudeLabel.setText(p.getAltitude()
                .map(a -> "%.0f m".formatted(a))
                .orElse("—"));

        speedLabel.setText(p.getSpeed()
                .map(mps -> "%.1f km/h".formatted(mps * 3.6))
                .orElse("—"));

        headingLabel.setText(p.getHeading()
                .map(deg -> "%.0f°".formatted(deg))
                .orElse("—"));

        takenAtLabel.setText(DateTimeFormatter.ISO_INSTANT.format(p.getTimestamp()));
    }

    private Component row(String label, Component value) {
        return new HorizontalLayout(new Span(label + ":"), value);
    }
}
```

## Why this shape works for UC5

- **All fields are available and typed.** `latitude`, `longitude`,
  `accuracy`, and `timestamp` are always present; `altitude`,
  `altitudeAccuracy`, `heading`, and `speed` are `Optional<Double>`,
  because the W3C spec lets them be missing. A desktop Chrome user
  typically has no `speed` or `heading` — `Optional` lets the view
  handle this by design instead of via null checks.
- **`Instant` for the timestamp.** Every Flow developer already uses
  `java.time` for dates. A long-milliseconds timestamp would be
  strictly worse.
- **`setMinimumAccuracy(25)` rejects bad fixes on the client.** A
  brief GPS glitch never reaches the `LocationEvent`, is never
  counted in the ride distance, and is never stored as the
  component's value. The server-side view never needs its own
  accuracy filter.

## Server-side persistence of detailed readings

`GeoLocationPosition` is serializable and implements `equals` /
`hashCode`, so it can be stored directly in a bean or collection:

```java
public record RideSample(UUID rideId, GeoLocationPosition position) {}

List<RideSample> samples = new ArrayList<>();

tracker.addLocationListener(event -> event.getPosition()
        .ifPresent(p -> samples.add(new RideSample(currentRideId, p))));
```

For a field-biology data-collection app, the same pattern records each
measurement together with the reading timestamp and accuracy, so
analysts can later filter on `getAccuracy()` to discard low-confidence
samples.
