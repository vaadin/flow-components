# Use Case 2 — Continuously follow me while I am doing something

The application wants a stream of positions — not a single snapshot —
while an activity is in progress. Tracking must stop when the activity
ends, when the user leaves the page, or when the user explicitly opts
out.

With the PR 23527 API, `Geolocation.track(owner)` returns a handle
whose `state()` is a `Signal<GeolocationState>` that the view consumes
with `ComponentEffect.effect(owner, ...)`. The browser watch is tied
to `owner`'s lifecycle, so navigation away from the view cancels it
automatically.

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
                    new GeolocationOptions(true, null, null));

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
        }
        // Detaching the view is the only way to cancel the browser
        // watch in this version of the API — see "Stopping tracking"
        // below.
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
- **`minimumAccuracy` is an application-side filter.** The PR does
  not ship a threshold option. The `if (pos.coords().accuracy() > 25)`
  line in `onSample` is the full equivalent — UC5's "brief bad GPS
  fix should not add a kilometre-long zig-zag" is still
  straightforward.

## Stopping tracking without leaving the view

There is **no `tracker.stop()` method** on the PR API — tracking stops
only when the owner component detaches. To implement a "Stop ride"
button that the user can click without navigating away, the view has
to detach the owner or install the tracker on a disposable subcomponent:

```java
private Div trackerHost;         // a throwaway child component
private Geolocation tracker;

private void startTracking() {
    trackerHost = new Div();
    add(trackerHost);
    tracker = Geolocation.track(trackerHost, ...);
    ComponentEffect.effect(trackerHost, () -> { /* react to tracker.state() */ });
}

private void stopTracking() {
    remove(trackerHost);   // detaches trackerHost → cancels the browser watch
    trackerHost = null;
    tracker = null;
}
```

This is slightly more ceremony than the earlier "button with a
`stopWatching()` method" design, but it is a direct consequence of the
PR's decision to tie tracking to component lifecycle rather than to
a separately-managed subscription.

## Alternative: a whole view that exists only while tracking

If the user navigates to `RideView` specifically to start tracking
and leaves it to stop, there is no need for a toggle — start the
tracker in the constructor and let navigation do the rest:

```java
@Route("ride/tracking")
public class TrackingView extends VerticalLayout {

    public TrackingView() {
        Geolocation geo = Geolocation.track(this,
                new GeolocationOptions(true, null, null));

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
