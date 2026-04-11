# Use Case 2 — Continuously follow me while I am doing something

The application wants a stream of positions — not a single snapshot —
while an activity is in progress. Tracking must stop when the activity
ends, when the user leaves the page, or when the user explicitly opts
out.

`Geolocation.track(owner)` returns a handle whose `state()` is a
`Signal<GeolocationState>` that the view consumes with
`ComponentEffect.effect(owner, ...)`. The browser watch is tied to
`owner`'s lifecycle, so navigation away from the view cancels it
automatically. A `stop()` method on the handle also cancels the watch
explicitly, for the "Stop ride" case where the user ends tracking
without leaving the view.

## Example: Cycling workout view

```java
@Route("ride")
@PageTitle("Record ride")
public class RideView extends VerticalLayout {

    private final Span distanceLabel = new Span("0.00 km");
    private final Span speedLabel = new Span("— km/h");

    private Geolocation tracker;      // set when the user starts the ride
    private GeolocationPosition previous;
    private double totalMetres;

    public RideView() {
        Button toggle = new Button("Start", e -> toggleTracking());
        add(new H2("Ride"), toggle,
                new HorizontalLayout(new Span("Distance:"), distanceLabel),
                new HorizontalLayout(new Span("Speed:"), speedLabel));
    }

    private void toggleTracking() {
        if (tracker == null) {
            previous = null;
            totalMetres = 0;

            tracker = Geolocation.track(this,
                    GeolocationOptions.builder().highAccuracy(true).build());

            ComponentEffect.effect(this, () -> {
                switch (tracker.state().get()) {
                    case GeolocationState.Pending p -> {
                        // waiting for the first fix
                    }
                    case GeolocationPosition pos -> onSample(pos);
                    case GeolocationError err -> Notification.show(
                            "Tracking failed: " + err.message());
                }
            });
        } else {
            tracker.stop();   // cancels the browser watch
            tracker = null;
        }
    }

    private void onSample(GeolocationPosition pos) {
        // UC5 — drop low-confidence fixes in the callback itself.
        if (pos.coords().accuracy() > 25) {
            return;
        }
        if (previous != null) {
            totalMetres += haversine(previous, pos);
            distanceLabel.setText("%.2f km".formatted(totalMetres / 1000));
        }
        previous = pos;

        Double mps = pos.coords().speed();
        speedLabel.setText(mps == null
                ? "— km/h"
                : "%.1f km/h".formatted(mps * 3.6));
    }
}
```

## How the PR API covers UC2

- **`Geolocation.track(this, options)`** starts the browser `watchPosition`
  and ties it to the view. The `Geolocation` handle holds a
  `Signal<GeolocationState>`.
- **`ComponentEffect.effect(owner, ...)`** re-runs every time the
  state changes, so the switch over `GeolocationState` is the only
  place that reacts to new samples. No manual listener registration.
- **Sealed `GeolocationState`** makes the switch exhaustive at compile
  time. Adding a new state later would break every existing consumer
  — a feature for keeping call sites honest.
- **Detach cancels the browser watch.** When the user navigates away
  from `RideView`, the detach listener installed by
  `Geolocation.track` fires `window.Vaadin.Flow.geolocation.clearWatch`
  on the client. UC2's "tracking must stop when the user leaves the
  page" is satisfied without any view code.
- **`tracker.stop()`** cancels the watch explicitly when the user
  taps "Stop" without leaving the view. It is idempotent and runs
  the same teardown path as the detach listener, so calling it
  twice — or calling it on a handle whose owner has already
  detached — is safe.
- **`minimumAccuracy` is an application-side filter.** The API does
  not ship a threshold option. The `if (pos.coords().accuracy() > 25)`
  line in `onSample` is the full equivalent — UC5's "brief bad GPS
  fix should not add a kilometre-long zig-zag" is still
  straightforward.

## Why there is no `AutoCloseable` / `try-with-resources`

The obvious "wrap the handle in `try-with-resources`" idiom does not
actually work for `Geolocation.track`: the try block exits as soon as
the effect is registered, long before any samples arrive. Real usage
always stores the handle in a field and calls `stop()` from an event
listener or lets the detach listener cancel it.

If a scoped cleanup is genuinely needed (e.g. inside a background
task handler), `try` / `finally` expresses it directly:

```java
Geolocation geo = Geolocation.track(this, options);
try {
    // ... code that waits for the tracker to reach a stop condition ...
} finally {
    geo.stop();
}
```

This is the same shape `AutoCloseable` would give, without the
redundant method name.

## Alternative: a whole view that exists only while tracking

If the user navigates to `RideView` specifically to start tracking
and leaves it to stop, there is no need for a toggle — start the
tracker in the constructor and let navigation do the rest:

```java
@Route("ride/tracking")
public class TrackingView extends VerticalLayout {

    public TrackingView() {
        Geolocation geo = Geolocation.track(this,
                GeolocationOptions.builder().highAccuracy(true).build());

        ComponentEffect.effect(this, () -> {
            switch (geo.state().get()) {
                case GeolocationState.Pending p -> {}
                case GeolocationPosition pos -> record(pos);
                case GeolocationError err -> showError(err);
            }
        });
    }
}
```
