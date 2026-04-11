# Use Case 5 — Use detailed position data, not just latitude and longitude

Some applications need everything the browser reports: accuracy,
altitude, altitude accuracy, heading, speed, and the reading timestamp.

With the PR 23527 API, all of these are accessors on
`GeolocationCoordinates` (plus `timestamp()` on `GeolocationPosition`).
Optional fields are nullable `Double`; required fields are `double` or
`long`.

## Example: Cycling dashboard

```java
@Route("ride/dashboard")
@PageTitle("Ride")
public class RideDashboardView extends VerticalLayout {

    private final Span latLng = new Span();
    private final Span accuracyLabel = new Span();
    private final Span altitudeLabel = new Span();
    private final Span speedLabel = new Span();
    private final Span headingLabel = new Span();
    private final Span takenAtLabel = new Span();

    public RideDashboardView() {
        Geolocation geo = Geolocation.track(this,
                GeolocationOptions.builder().highAccuracy(true).build());

        ComponentEffect.effect(this, () -> {
            switch (geo.state().get()) {
                case GeolocationState.Pending p -> {}
                case GeolocationPosition pos -> {
                    // UC5 — drop fixes that are too imprecise. The PR
                    // API does not expose a minimumAccuracy option, so
                    // the view filters in the effect itself.
                    if (pos.coords().accuracy() > 25) {
                        return;
                    }
                    render(pos);
                }
                case GeolocationError err ->
                        Notification.show("Tracking failed: " + err.message());
            }
        });

        add(new H2("Ride dashboard"),
                row("Position", latLng),
                row("Accuracy", accuracyLabel),
                row("Altitude", altitudeLabel),
                row("Speed", speedLabel),
                row("Heading", headingLabel),
                row("Reading taken", takenAtLabel));
    }

    private void render(GeolocationPosition pos) {
        GeolocationCoordinates c = pos.coords();

        latLng.setText("%.5f, %.5f".formatted(c.latitude(), c.longitude()));
        accuracyLabel.setText("± %.0f m".formatted(c.accuracy()));
        altitudeLabel.setText(c.altitude() == null
                ? "—" : "%.0f m".formatted(c.altitude()));
        speedLabel.setText(c.speed() == null
                ? "—" : "%.1f km/h".formatted(c.speed() * 3.6));
        headingLabel.setText(c.heading() == null
                ? "—" : "%.0f°".formatted(c.heading()));

        takenAtLabel.setText(DateTimeFormatter.ISO_INSTANT.format(
                Instant.ofEpochMilli(pos.timestamp())));
    }

    private Component row(String label, Component value) {
        return new HorizontalLayout(new Span(label + ":"), value);
    }
}
```

## How the PR API covers UC5

- **All W3C fields present on `GeolocationCoordinates`.**
  `latitude()`, `longitude()`, `accuracy()` are primitive `double`
  (always present). `altitude()`, `altitudeAccuracy()`, `heading()`,
  `speed()` are boxed `Double` that may be `null` — exactly matching
  the W3C spec's optionality.
- **Null checks instead of `Optional`.** A desktop Chrome user
  typically has no `speed` or `heading`, so every access site checks
  for `null`. The PR chose `Double` over `Optional<Double>` to
  Jackson-round-trip cleanly and to match the wire shape.
- **Timestamp is `long` epoch millis.** `Instant.ofEpochMilli(
  pos.timestamp())` converts to `java.time` at the point of use.
  Callers that want an `Instant`-typed API can add their own helper
  method; the core record stays aligned with the W3C `DOMTimeStamp`.

## Server-side persistence

All the records are `Serializable` and have structural `equals` /
`hashCode` / `toString` (records give this for free), so they can be
stored in a bean or collection directly:

```java
public record RideSample(UUID rideId, GeolocationPosition position) {}

List<RideSample> samples = new ArrayList<>();

ComponentEffect.effect(this, () -> {
    if (geo.state().get() instanceof GeolocationPosition p
            && p.coords().accuracy() <= 25) {
        samples.add(new RideSample(currentRideId, p));
    }
});
```

Storing `GeolocationPosition` directly preserves the timestamp,
accuracy, and any browser-optional fields. A field-biology data
collection app can then filter on `accuracy()` when the analyst later
loads the samples.

## Note on the missing `minimumAccuracy` filter

The earlier "button component" spec had a `setMinimumAccuracy(metres)`
method that rejected bad readings automatically — they never reached
the server. The PR API has no such hook, so the filter lives in the
application effect/callback (the single `if` line in the example
above). This is functionally equivalent but incurs one server round
trip per rejected reading, which matters only for very high
update rates.
