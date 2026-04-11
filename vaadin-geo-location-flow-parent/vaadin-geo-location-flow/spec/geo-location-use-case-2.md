# Use Case 2 — Continuously follow me while I am doing something

The application wants a stream of positions — not a single snapshot —
while an activity is in progress. Tracking must stop when the activity
ends, when the user leaves the page, or when the user explicitly opts
out.

## Example: Cycling workout view

```java
@Route("ride")
@PageTitle("Record ride")
public class RideView extends VerticalLayout {

    private final GeoLocation tracker = new GeoLocation("Start ride");
    private final Span distanceLabel = new Span("0.00 km");
    private final Span speedLabel = new Span("— km/h");

    private GeoLocationPosition previous;
    private double totalMetres;

    public RideView() {
        tracker.setHighAccuracy(true);
        tracker.setMinimumAccuracy(25); // drop wild fixes > 25 m
        tracker.addLocationListener(this::onSample);

        Button toggle = new Button("Start", e -> toggleTracking());
        add(new H2("Ride"), tracker, toggle,
                new HorizontalLayout(new Span("Distance:"), distanceLabel),
                new HorizontalLayout(new Span("Speed:"), speedLabel));
    }

    private void toggleTracking() {
        if (tracker.isWatching()) {
            tracker.stopWatching();
        } else {
            previous = null;
            totalMetres = 0;
            tracker.startWatching();
        }
    }

    private void onSample(LocationEvent event) {
        GeoLocationPosition p = event.getPosition().orElse(null);
        if (p == null) {
            return; // UC4 covers error handling
        }
        if (previous != null) {
            totalMetres += haversine(previous, p);
            distanceLabel.setText("%.2f km".formatted(totalMetres / 1000));
        }
        previous = p;

        p.getSpeed().ifPresent(mps ->
                speedLabel.setText("%.1f km/h".formatted(mps * 3.6)));
    }
}
```

## Why this works with minimal code

- **`startWatching()` / `stopWatching()` on the component itself.** The
  developer does not write any client-side JS, does not manage
  `watchPosition` / `clearWatch`, and does not have to worry about
  unloading.
- **Detach cancels the watch automatically.** When the user navigates
  away from `RideView`, `GeoLocation` is detached, the client-side
  `disconnectedCallback` calls `clearWatch`, and no background tracking
  survives the page transition. The UC2 requirement "tracking must stop
  when the user leaves the page" is satisfied for free.
- **`minimumAccuracy` filters bad fixes on the client** so a brief bad
  GPS reading never reaches the server and cannot add a kilometre-long
  zig-zag to the route.
- **Optional fields are `Optional<Double>`** — `speed` on a desktop
  browser is almost always empty, and the code can `ifPresent` it
  cleanly without a null check.

## Alternative: declarative watch

If the tracker is always watching whenever it is visible (e.g. a
dedicated "tracking screen" the user navigates to), skip the toggle
button and set `watch` declaratively:

```java
GeoLocation tracker = new GeoLocation("Tracking…");
tracker.setWatch(true);
tracker.setHighAccuracy(true);
```

The first click starts the watch, subsequent navigation away cancels
it.
