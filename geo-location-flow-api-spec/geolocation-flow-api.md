# Vaadin Flow Geolocation API

`Geolocation` is a framework-level Java API that wraps the browser's W3C
Geolocation API. It lives in `flow-server`
(`com.vaadin.flow.component.geolocation`) and has two entry points:

- **`Geolocation.get(...)`** — one-shot position request with callbacks.
- **`Geolocation.track(Component owner)`** — continuous tracking whose
  state is exposed as a reactive `Signal<GeolocationState>` and is
  automatically tied to the owner component's lifecycle.

It is **not a UI component**. The application decides what to render —
a button, an icon, an auto-loaded map — and calls into `Geolocation`
from its own event handlers. The Flow API only carries the W3C shape
across the wire, converts the browser result into typed Java records,
and (for tracking) ties the browser watch to the owner component so it
is cleaned up on detach.

> Source: [vaadin/flow#23527](https://github.com/vaadin/flow/pull/23527)
> (`flow-server/src/main/java/com/vaadin/flow/component/geolocation/`).

## Usage Examples

### 1. One-shot request on user action (UC1)

The canonical shape for "the user clicks something and we react to
their current position". The click handler calls `Geolocation.get`; the
success callback runs in the UI thread, so it can update components
directly.

```java
Button locate = new Button("Use my location");
locate.addClickListener(e -> Geolocation.get(
        pos -> {
            double lat = pos.coords().latitude();
            double lon = pos.coords().longitude();
            showNearestStores(lat, lon);
        },
        err -> showManualPostcodeEntry(err)));
```

- `Geolocation.get(onSuccess, onError)` is the two-argument overload
  that covers both branches; UC4's error handling lives in the second
  callback.
- The `onError` parameter is optional — the one-argument
  `Geolocation.get(onSuccess)` silently ignores errors and is
  appropriate when the application does not need to react to failures.
- Both callbacks run on the UI thread, so no `ui.access()` is needed.

### 2. Continuous tracking via a reactive signal (UC2)

`Geolocation.track(owner)` starts a browser `watchPosition` whose
lifecycle is tied to `owner`. The returned `Geolocation` handle exposes
a `Signal<GeolocationState>` that starts as `Pending`, transitions to
`GeolocationPosition` on every successful reading, and transitions to
`GeolocationError` on failure. `ComponentEffect.effect(owner, ...)` is
the idiomatic way to react to state changes — the effect re-runs
automatically whenever the signal value changes.

```java
public class RideView extends VerticalLayout {

    private final Span distance = new Span("0.00 km");

    public RideView() {
        Geolocation geo = Geolocation.track(this,
                new GeolocationOptions(true, null, null)); // high accuracy

        ComponentEffect.effect(this, () -> {
            switch (geo.state().get()) {
                case GeolocationState.Pending p -> {
                    // waiting for the first fix
                }
                case GeolocationPosition pos ->
                        updateDistance(pos);
                case GeolocationError err ->
                        Notification.show("Tracking failed: " + err.message());
            }
        });

        add(distance);
    }
}
```

- The watch is cancelled automatically when `this` detaches, so no
  background tracking survives a navigation. This is what makes UC2's
  "tracking must stop when the user leaves the page" work without any
  cleanup code in the view.
- `GeolocationState` is a sealed interface with exactly three permitted
  subtypes, so the `switch` is exhaustive — adding a future state
  would be a compile error on every existing switch, keeping callers
  honest.
- The state signal can also be read imperatively with `.get()` /
  `.peek()` outside an effect when the caller just needs a snapshot.

### 3. Auto-fetch on view load (UC3)

`Geolocation.get` can be called from `onAttach` (or any lifecycle hook
that runs in the UI thread) to request the position as soon as the
view appears, without a button click. The browser's own permission
cache decides whether this triggers a prompt: a user who previously
granted permission on this origin gets an instant result; a user who
has not will see the browser prompt.

```java
public class HomeView extends VerticalLayout {

    private final Div localHeadlines = new Div();

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        Geolocation.get(
                new GeolocationOptions(null, 5000, 300_000), // 5s timeout, 5min cache
                pos -> populateLocalHeadlines(pos),
                err -> {
                    // fall back to the explicit flow from UC1
                });
    }
}
```

- **Note on UC3's "never prompt on cold load" requirement:** the PR's
  API does not expose the browser's Permissions API, so it cannot gate
  an auto-fetch on `permission === 'granted'` in Java. The
  application must decide — e.g. only call `Geolocation.get` from
  `onAttach` after it has separately checked the permission state via
  its own `executeJs` call, or accept that a first-time visitor sees a
  browser prompt. This is a deliberate trade-off: the API is a thin
  wrapper over the browser's Geolocation API, not a higher-level
  permission manager.

### 4. Handling denial, failure, and timeout (UC4)

All error handling is through the `onError` callback of `get` or the
`GeolocationError` branch of the tracking state signal. Errors are
mapped 1:1 from the browser's `PositionError` — the `code` is the W3C
numeric code (`PERMISSION_DENIED=1`, `POSITION_UNAVAILABLE=2`,
`TIMEOUT=3`), and the `message` is the browser's human-readable text.

```java
Geolocation.get(
        pos -> showStores(pos),
        err -> {
            String userMessage = switch (err.code()) {
                case GeolocationError.PERMISSION_DENIED ->
                        "Location blocked. Click the padlock in the address "
                                + "bar to re-enable, or enter a postcode below.";
                case GeolocationError.POSITION_UNAVAILABLE ->
                        "We couldn't determine your location. Enter a postcode.";
                case GeolocationError.TIMEOUT ->
                        "Location request timed out. Please try again.";
                default -> err.message();
            };
            showFallback(userMessage);
        });
```

- `GeolocationError.PERMISSION_DENIED`, `POSITION_UNAVAILABLE`, and
  `TIMEOUT` are `public static final int` constants on the record.
  They are the **raw W3C numeric codes**, not an enum, because the PR
  deliberately stays a thin wrapper over the browser API.
- **Note on "API not usable at all":** the PR does not expose an
  equivalent of the web component spec's `unavailable` state. In a
  browser where `navigator.geolocation` is missing or blocked by
  Permissions-Policy, `Geolocation.get` will simply never invoke
  either callback (or will invoke `onError` with a numeric browser
  error, depending on the browser). Applications that need to tell
  "this can't possibly work" apart from "this attempt failed" have to
  either (a) check `window.isSecureContext` /
  `typeof navigator.geolocation` via `executeJs` up-front, or (b)
  layer a timeout on top and treat "no callback within N seconds" as
  "unavailable".

### 5. Reading detailed position data (UC5)

`GeolocationPosition.coords()` returns a `GeolocationCoordinates`
record with every field the browser provides: latitude, longitude,
accuracy (always present), and altitude, altitudeAccuracy, heading,
speed (which the browser may omit — represented as `Double` that may
be `null`). The reading timestamp is `long timestamp()` on
`GeolocationPosition`, in milliseconds since the Unix epoch.

```java
Geolocation.get(pos -> {
    GeolocationCoordinates c = pos.coords();
    double lat = c.latitude();
    double lon = c.longitude();
    double accuracyMetres = c.accuracy();
    Double altitude = c.altitude();                 // may be null
    Double altitudeAccuracy = c.altitudeAccuracy(); // may be null
    Double heading = c.heading();                   // may be null
    Double speed = c.speed();                       // metres/second, may be null
    long takenAtEpochMillis = pos.timestamp();

    updateDashboard(lat, lon, accuracyMetres, altitude, heading, speed,
            Instant.ofEpochMilli(takenAtEpochMillis));
});
```

- Optional fields are boxed `Double` (nullable), not `Optional<Double>`.
  This matches the Jackson round-trip of the W3C shape, where missing
  fields deserialize to `null`.
- **Note on "reject bad fixes"**: the PR does not expose a
  `minimumAccuracy` filter. Applications that want to drop
  low-confidence readings (UC5's "a brief bad GPS fix should not add a
  kilometre-long zig-zag") filter inside the callback or effect —
  e.g. `if (pos.coords().accuracy() > 25) return;`.

### 6. Tuning precision, freshness, and battery (UC6)

`GeolocationOptions` is a record with three nullable fields that map
1:1 onto W3C `PositionOptions`. A `null` field means "use the browser
default".

```java
public record GeolocationOptions(
        Boolean enableHighAccuracy,
        Integer timeout,        // milliseconds
        Integer maximumAge)     // milliseconds
```

Examples covering UC6's four tolerances:

```java
// A — News site: cached city-level reading is fine.
GeolocationOptions news = new GeolocationOptions(null, 5000, 300_000);

// B — Turn-by-turn navigation: most accurate, continuous.
GeolocationOptions nav = new GeolocationOptions(true, null, null);
Geolocation geo = Geolocation.track(navView, nav);

// C — Check-in: must be fresh.
GeolocationOptions checkIn = new GeolocationOptions(true, 10_000, 0);

// D — Address form: give up quickly.
GeolocationOptions address = new GeolocationOptions(null, 3000, null);
Geolocation.get(address, pos -> prefillAddress(pos));
```

- Each call to `get` or `track` may pass its own `GeolocationOptions`,
  so two parts of the same application can legitimately disagree.
- `null` on any field means "use the browser default" (`false` for
  `enableHighAccuracy`, `Infinity` for `timeout`, `0` for
  `maximumAge`).
- **Note on `Integer` vs `Duration`:** the PR uses `Integer timeout`
  and `Integer maximumAge` in milliseconds because it is a thin
  wrapper over W3C `PositionOptions`. Callers that prefer
  `java.time.Duration` can wrap the constructor in a helper of their
  own (`new GeolocationOptions(true, (int) d.toMillis(), ...)`), but
  the core record stays millisecond-based.

### 7. Capturing a location as part of a form (UC7)

`Geolocation` is not a form field. To capture a position as part of a
form, the application owns a "Pin my location" button and a
`GeolocationPosition` field on its form bean. The button's click
handler calls `Geolocation.get` and stores the result on the bean; a
boolean guard stops the form from submitting until a position is
present.

```java
public class PotholeReportForm extends FormLayout {

    private final TextField description = new TextField("Description");
    private final Button pin = new Button("Pin my location");
    private final Span pinLabel = new Span("No location pinned yet");
    private final Button submit = new Button("Report");

    private GeolocationPosition pinned;

    public PotholeReportForm(PotholeService service) {
        description.setRequired(true);
        submit.setEnabled(false);

        pin.addClickListener(e -> Geolocation.get(
                new GeolocationOptions(true, 10_000, 0),
                pos -> {
                    if (pos.coords().accuracy() > 50) {
                        Notification.show(
                                "Location too imprecise, please try again.");
                        return;
                    }
                    pinned = pos;
                    pinLabel.setText(
                            "Location pinned at %.5f, %.5f (±%.0fm)".formatted(
                                    pos.coords().latitude(),
                                    pos.coords().longitude(),
                                    pos.coords().accuracy()));
                    refreshSubmitState();
                },
                err -> Notification.show(
                        "Could not pin location: " + err.message())));

        description.addValueChangeListener(e -> refreshSubmitState());
        submit.addClickListener(e -> service.report(
                description.getValue(), pinned));

        add(description, pin, pinLabel, submit);
    }

    private void refreshSubmitState() {
        submit.setEnabled(!description.isEmpty() && pinned != null);
    }
}
```

- **Note on form field integration:** the PR's API does not provide
  `HasValue<GeolocationPosition>`, `Binder` integration, a
  `required` flag, or a reset hook. UC7's "behave like any other
  field" is the application's responsibility — typically a few lines
  of glue code like `refreshSubmitState` above.
- Minimum-accuracy validation (UC7's "refuse to submit imprecise
  positions") is a single `if` in the success callback.

---

### Key Design Decisions (inherited from PR 23527)

1. **Thin wrapper over W3C, not a UI component.** The PR places
   `Geolocation` in `flow-server` as a framework-level utility, not as
   a `vaadin-*-flow` component module. The reasoning is that the
   browser Geolocation API is a data source, not a widget — the
   application decides what to render, and the Flow API only carries
   the W3C shape across the wire. This is the single biggest shape
   difference from the earlier "button component" spec.

2. **Two entry points — `get` and `track` — nothing else.** The W3C
   API has three operations (`getCurrentPosition`, `watchPosition`,
   `clearWatch`); the PR collapses them into two Java entry points and
   makes `clearWatch` implicit (tied to the owner component's detach).

3. **`track` requires an owner component.** `Geolocation.track(owner)`
   takes a `Component` and registers a detach listener that cancels
   the underlying browser watch. This is the mechanism that prevents
   background watches from leaking between views — UC2's key
   requirement — without any manual cleanup in application code.

4. **State is a sealed interface + reactive signal.** The tracking
   handle's `state()` returns a `Signal<GeolocationState>`, and
   `GeolocationState` is a sealed interface permitting exactly
   `Pending`, `GeolocationPosition`, and `GeolocationError`. Consumers
   use `switch` pattern matching, which is exhaustive at compile time.
   `ComponentEffect.effect(owner, ...)` re-runs automatically on state
   changes, so the view does not need to register event listeners
   manually.

5. **`get` uses callbacks, not a signal.** A one-shot request has one
   answer, not a stream, so a signal would be overkill. The
   `get(onSuccess, onError)` callback pair maps directly to the two
   W3C promise branches, and the callbacks run on the UI thread.

6. **Errors are records with W3C numeric codes.** `GeolocationError`
   is `record(int code, String message)` and exposes
   `PERMISSION_DENIED = 1`, `POSITION_UNAVAILABLE = 2`, `TIMEOUT = 3`
   as `public static final int` constants. No enum and no named
   `GeolocationErrorCode` type — the PR keeps the Java shape identical
   to the wire shape, which is also what Jackson can round-trip
   without a custom (de)serializer.

7. **Optional coord fields are boxed `Double`, not `Optional<Double>`.**
   `altitude`, `altitudeAccuracy`, `heading`, `speed` may be missing
   from the browser's response, and the PR represents that as `null`.
   Records plus `null` work cleanly with Jackson's default
   serialization; `Optional` does not.

8. **Timestamps are `long` milliseconds since epoch.** The W3C
   `DOMTimeStamp` is a long, the Flow wire format is a long, and the
   PR keeps it a long on the Java side. Callers that want an `Instant`
   use `Instant.ofEpochMilli(pos.timestamp())` at the point of use.

9. **Options are nullable record fields.** `GeolocationOptions` has
   three fields (`Boolean enableHighAccuracy`, `Integer timeout`,
   `Integer maximumAge`) and `null` means "use the browser default".
   This matches W3C `PositionOptions` semantics and composes with
   `@JsonInclude(NON_NULL)` to omit defaults on the wire.

10. **No permission, no availability, no auto-locate, no minimum
    accuracy.** These higher-level features are out of scope for the
    thin wrapper. Applications that need them build them on top of
    `Geolocation.get` and `executeJs` in a few lines each — see the
    "Note on..." callouts in the examples above.

11. **No form-field integration.** `Geolocation` is not a
    `HasValue<GeolocationPosition>`. UC7 is covered by an application
    pattern (a button that owns a `GeolocationPosition` field and a
    submit-state guard), not by a framework hook. This keeps the
    surface small and avoids baking form assumptions into the wrapper.

---

## Implementation

All classes live in package `com.vaadin.flow.component.geolocation` in
`flow-server`. They are plain Java — no `@Tag`, no `@NpmPackage`, no
web component. The client-side helpers (`window.Vaadin.Flow.geolocation.get`,
`watch`, `clearWatch`) are shipped in `flow-client` as
`Geolocation.ts` and hooked into `Flow.ts`.

### Classes

**`Geolocation`** — Utility class with static entry points and a
tracking handle

```java
public class Geolocation implements Serializable {
    private Geolocation() { ... }

    public static void get(SerializableConsumer<GeolocationPosition> onSuccess);
    public static void get(GeolocationOptions options,
                           SerializableConsumer<GeolocationPosition> onSuccess);
    public static void get(SerializableConsumer<GeolocationPosition> onSuccess,
                           SerializableConsumer<GeolocationError> onError);
    public static void get(GeolocationOptions options,
                           SerializableConsumer<GeolocationPosition> onSuccess,
                           SerializableConsumer<GeolocationError> onError);

    public static Geolocation track(Component owner);
    public static Geolocation track(Component owner, GeolocationOptions options);

    public Signal<GeolocationState> state();
}
```

| Static method | Parameters | Returns | Description |
|---|---|---|---|
| `get` | `SerializableConsumer<GeolocationPosition> onSuccess` | `void` | One-shot request. Errors are silently ignored. Must be called from a UI thread. |
| `get` | `GeolocationOptions options, SerializableConsumer<GeolocationPosition> onSuccess` | `void` | As above, with options. `null` options means browser defaults. |
| `get` | `SerializableConsumer<GeolocationPosition> onSuccess, SerializableConsumer<GeolocationError> onError` | `void` | One-shot with explicit error callback. `onError` may be `null`. |
| `get` | `GeolocationOptions options, SerializableConsumer<GeolocationPosition> onSuccess, SerializableConsumer<GeolocationError> onError` | `void` | Full form. All callbacks run on the UI thread. |
| `track` | `Component owner` | `Geolocation` | Starts continuous tracking tied to `owner`'s lifecycle. Returns a handle whose `state()` signal reports progress. |
| `track` | `Component owner, GeolocationOptions options` | `Geolocation` | As above with options. |

| Instance method | Returns | Description |
|---|---|---|
| `state` | `Signal<GeolocationState>` | Read-only signal. Starts as `GeolocationState.Pending`; transitions to `GeolocationPosition` or `GeolocationError` as the browser reports. Consume via `ComponentEffect.effect(owner, ...)` or `.get()` / `.peek()`. |

---

**`GeolocationState`** — Sealed interface

```java
public sealed interface GeolocationState extends Serializable
        permits GeolocationState.Pending, GeolocationPosition, GeolocationError {

    record Pending() implements GeolocationState {}
}
```

Exactly three subtypes are permitted:
- `GeolocationState.Pending` — initial state before the first browser response.
- `GeolocationPosition` — a successful fix.
- `GeolocationError` — the browser reported an error.

Pattern-match with `switch`:

```java
switch (geo.state().get()) {
    case GeolocationState.Pending p -> {}
    case GeolocationPosition pos -> showFix(pos);
    case GeolocationError err -> showError(err);
}
```

---

**`GeolocationPosition`** — A successful reading

```java
public record GeolocationPosition(GeolocationCoordinates coords, long timestamp)
        implements GeolocationState {}
```

| Field | Type | Description |
|---|---|---|
| `coords` | `GeolocationCoordinates` | The latitude/longitude/accuracy/etc. fields. |
| `timestamp` | `long` | Milliseconds since the Unix epoch. Convert with `Instant.ofEpochMilli(...)` when an `Instant` is needed. |

---

**`GeolocationCoordinates`** — Latitude/longitude and related fields

```java
public record GeolocationCoordinates(
        double latitude,
        double longitude,
        double accuracy,
        Double altitude,
        Double altitudeAccuracy,
        Double heading,
        Double speed) implements Serializable {}
```

| Field | Type | Description |
|---|---|---|
| `latitude` | `double` | Decimal degrees. Always present. |
| `longitude` | `double` | Decimal degrees. Always present. |
| `accuracy` | `double` | 1-sigma accuracy of `latitude`/`longitude` in metres. Always present. |
| `altitude` | `Double` | Metres above the WGS 84 ellipsoid. `null` if the browser does not report it. |
| `altitudeAccuracy` | `Double` | Metres. `null` if the browser does not report altitude accuracy. |
| `heading` | `Double` | Direction of travel in degrees clockwise from true north. `null` if the browser does not report it. |
| `speed` | `Double` | Metres per second. `null` if the browser does not report speed. |

---

**`GeolocationError`** — A failed reading

```java
public record GeolocationError(int code, String message)
        implements GeolocationState {

    public static final int PERMISSION_DENIED = 1;
    public static final int POSITION_UNAVAILABLE = 2;
    public static final int TIMEOUT = 3;
}
```

`code` is the W3C numeric error code; the three constants are
provided for readability.

---

**`GeolocationOptions`** — W3C `PositionOptions` equivalent

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeolocationOptions(
        Boolean enableHighAccuracy,
        Integer timeout,
        Integer maximumAge) implements Serializable {

    public GeolocationOptions() { this(null, null, null); }
}
```

| Field | Type | Default (when `null`) | Description |
|---|---|---|---|
| `enableHighAccuracy` | `Boolean` | `false` | When `true`, requests GPS-backed high-accuracy position. |
| `timeout` | `Integer` | `Infinity` | Maximum wait, in milliseconds, before the browser reports `TIMEOUT`. |
| `maximumAge` | `Integer` | `0` | Maximum age of a cached reading, in milliseconds. `0` means "no cached reading accepted". |

---

### Client-side helpers

The PR ships two TypeScript sources in `flow-client`:

- `flow-client/src/main/frontend/Geolocation.ts` — exposes
  `window.Vaadin.Flow.geolocation.get(options)`,
  `window.Vaadin.Flow.geolocation.watch(host, options, watchKey)`,
  and `window.Vaadin.Flow.geolocation.clearWatch(watchKey)`.
- `flow-client/src/main/frontend/Flow.ts` — wires the geolocation
  helper into the `window.Vaadin.Flow` namespace.

Applications do not call these directly; they are invoked from
`Geolocation.get` (via `executeJs`) and from the detach listener
registered by `Geolocation.track`.

---

### Lifecycle and cleanup

- `get` uses `ui.getElement().executeJs(...)` and resolves exactly
  once. No server state is kept.
- `track(owner)`:
  1. Registers a `vaadin-geolocation-position` DOM listener on
     `owner.getElement()` that updates the signal.
  2. Registers a `vaadin-geolocation-error` DOM listener likewise.
  3. Calls `window.Vaadin.Flow.geolocation.watch(this, options, key)`
     on the owner element to start the browser watch.
  4. Registers a detach listener that calls
     `window.Vaadin.Flow.geolocation.clearWatch(key)` and removes
     both DOM listeners.
- The client uses `addEventDetail().allowInert()` so watch events are
  delivered even when the owner element is temporarily inert.

---

### Testing

The PR includes `GeolocationTest.java` covering Jackson round-trips
for every record and `GeolocationView.java` / `GeolocationIT.java` as
integration tests that mock `navigator.geolocation` so CI runs do not
need real permissions. These live in `flow-tests/test-root-context/`.

---

### Features considered but NOT in this PR

These are features from the earlier "button component" spec that the
PR's thin-wrapper shape deliberately leaves out. Each is still
achievable in application code, with the trade-off noted.

| Feature | Why it's out | How the application can do it |
|---|---|---|
| Button / slotted icon / prefix-suffix content | Not a UI component | Use `Button` + `VaadinIcon` and call `Geolocation.get` from the click handler |
| `HasValue<GeolocationPosition>` and `Binder` support | Form semantics belong to the form, not the sensor | Store the position in a bean field; a tiny submit-state guard does the rest |
| `required` / `invalid` / `validate()` | Same reason | As above |
| Auto-locate ("only if permission granted") | The PR does not expose the Permissions API | Call `executeJs` to check `navigator.permissions.query({name: 'geolocation'})`, then gate `Geolocation.get` on the result |
| `unavailable` (insecure origin / Permissions-Policy block) | Not exposed | Check `window.isSecureContext` / `typeof navigator.geolocation` via `executeJs` up-front |
| `minimumAccuracy` (reject bad fixes) | Kept out of the wrapper | One `if` in the callback or effect: `if (pos.coords().accuracy() > 25) return;` |
| Named `GeolocationErrorCode` enum | Kept as W3C numeric codes | Compare against `GeolocationError.PERMISSION_DENIED` etc. |
| `Duration` / `Instant` types | Kept as `long`/`int` ms to match W3C and wire shape | `Instant.ofEpochMilli(pos.timestamp())`; `(int) Duration.ofSeconds(5).toMillis()` |

---

### Coverage check: use cases → PR API

| Use case | How the PR API covers it | Gaps (vs. UC prose) |
|---|---|---|
| UC1 — One-shot on click | `Geolocation.get(onSuccess, onError)` from a click handler. | None. |
| UC2 — Continuous tracking | `Geolocation.track(owner)` + `ComponentEffect.effect` on `state()`. Detach cancels the browser watch automatically. | None. |
| UC3 — Auto-use on return visit | Call `Geolocation.get` from `onAttach`. | No built-in "only if already granted" gate — application must check permission itself to avoid unexpected prompts on cold load. |
| UC4 — Denial / failure / timeout | `onError` callback / `GeolocationError` state branch, with W3C numeric codes. | No `unavailable` signal for "API not usable at all" — application checks `isSecureContext` / `navigator.geolocation` itself. |
| UC5 — Detailed position data | `GeolocationCoordinates` exposes all W3C fields; timestamp is `long` epoch ms. | No `minimumAccuracy` filter — application filters in the callback/effect. |
| UC6 — Precision / freshness / battery | `GeolocationOptions` record, per-call. | None (millisecond ints instead of `Duration`, but functionally equivalent). |
| UC7 — Location as a form field | Application owns a button, stores the result in a bean field, gates submit on non-null. | No `HasValue` / `Binder` integration — application wires it up in a few lines. |
