# Vaadin Flow Geolocation API

`Geolocation` is a framework-level Java API that wraps the browser's W3C
Geolocation API. It lives in `flow-server`
(`com.vaadin.flow.component.geolocation`) and has four entry points:

- **`Geolocation.get(...)`** — one-shot position request with callbacks.
- **`Geolocation.track(Component owner)`** — continuous tracking whose
  state is exposed as a reactive `Signal<GeolocationState>` and is
  automatically tied to the owner component's lifecycle. The returned
  handle is `AutoCloseable`, so tracking can also be stopped
  explicitly via `stop()` without detaching the owner.
- **`Geolocation.isSupported(callback)`** — async feature detection
  (`navigator.geolocation` present, secure origin, not blocked by
  Permissions-Policy).
- **`Geolocation.queryPermission(callback)`** — async query of the
  browser's Permissions API, returning `GRANTED` / `DENIED` / `PROMPT`
  / `UNKNOWN`.

It is **not a UI component**. The application decides what to render —
a button, an icon, an auto-loaded map — and calls into `Geolocation`
from its own event handlers. The Flow API carries the W3C shape across
the wire, converts the browser result into typed Java records, ties
the browser watch to the owner component so it is cleaned up on
detach, and exposes the small number of browser-capability checks that
every real application needs.

> Source: [vaadin/flow#23527](https://github.com/vaadin/flow/pull/23527)
> (`flow-server/src/main/java/com/vaadin/flow/component/geolocation/`).
>
> **Additions over the base PR:** `Geolocation.stop()` /
> `AutoCloseable`, `Geolocation.isSupported(callback)`,
> `Geolocation.queryPermission(callback)`, `GeolocationPermission`
> enum, and a `GeolocationErrorCode` enum derived from the numeric
> error codes. Rationale for each is in the "Key Design Decisions"
> section below.

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
    private Geolocation tracker;

    public RideView() {
        Button toggle = new Button("Start", e -> toggleTracking());
        add(toggle, distance);
    }

    private void toggleTracking() {
        if (tracker == null) {
            tracker = Geolocation.track(this,
                    new GeolocationOptions(true, null, null));

            ComponentEffect.effect(this, () -> {
                switch (tracker.state().get()) {
                    case GeolocationState.Pending p -> {
                        // waiting for the first fix
                    }
                    case GeolocationPosition pos ->
                            updateDistance(pos);
                    case GeolocationError err ->
                            Notification.show(
                                    "Tracking failed: " + err.message());
                }
            });
        } else {
            tracker.stop();   // cancels the watch without detaching the view
            tracker = null;
        }
    }
}
```

- The watch is cancelled automatically when `this` detaches, so no
  background tracking survives a navigation. This is what makes UC2's
  "tracking must stop when the user leaves the page" work without any
  cleanup code in the view.
- **`tracker.stop()`** cancels the browser watch explicitly, for the
  "Stop ride" case where the user wants to end tracking without
  leaving the view. The method is idempotent — calling it twice, or
  calling it on a handle whose owner has already detached, is safe.
  `Geolocation` also implements `AutoCloseable`, so a handle can be
  used with `try-with-resources` in short-lived scopes:
  ```java
  try (Geolocation geo = Geolocation.track(this, options)) {
      // geo.stop() is called automatically when this block exits
  }
  ```
- `GeolocationState` is a sealed interface with exactly three permitted
  subtypes, so the `switch` is exhaustive — adding a future state
  would be a compile error on every existing switch, keeping callers
  honest.
- The state signal can also be read imperatively with `.get()` /
  `.peek()` outside an effect when the caller just needs a snapshot.

### 3. Auto-fetch on view load, gated on permission (UC3)

UC3's key requirement is *"no surprise prompt on cold load"*: only
auto-fetch if the user has already granted permission on a previous
visit. `Geolocation.queryPermission(callback)` returns the current
browser permission state as a `GeolocationPermission` enum
(`GRANTED` / `DENIED` / `PROMPT` / `UNKNOWN`), so the view can gate
`Geolocation.get` on it.

```java
public class HomeView extends VerticalLayout {

    private final Div localHeadlines = new Div();
    private final Button locate = new Button("Use my location");
    private final NewsService news;

    public HomeView(NewsService news) {
        this.news = news;
        locate.addClickListener(e -> fetchAndPopulate());
        add(new H1("Today's headlines"), localHeadlines,
                new Paragraph("Don't see local headlines? Click below:"),
                locate);
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        Geolocation.queryPermission(state -> {
            if (state == GeolocationPermission.GRANTED) {
                fetchAndPopulate();
            }
            // DENIED / PROMPT / UNKNOWN → do nothing; the user will
            // click the explicit button (UC1).
        });
    }

    private void fetchAndPopulate() {
        Geolocation.get(
                new GeolocationOptions(null, 5000, 300_000),
                pos -> populateLocalHeadlines(news, pos),
                err -> { /* stay quiet — the user has the button */ });
    }
}
```

- **The `GRANTED` branch is the only path that may trigger a browser
  call.** First-time visitors (`PROMPT`) and previously-denied
  visitors (`DENIED`) see the view without any permission dialog
  opening. This is exactly what UC3 calls for: "no extra click if
  permission is already in place, no surprise prompt on cold load".
- **Safari limitation:** Safari does not implement
  `navigator.permissions.query({name: 'geolocation'})` and always
  returns `UNKNOWN`. On Safari, the `GRANTED` branch is never taken,
  so auto-fetch silently reverts to UC1 (explicit click). This is the
  safest degradation — Safari users never get auto-fetch, but they
  also never get an unexpected prompt. There is no browser-side
  workaround. Firefox and Chromium return the correct state.
- `queryPermission` runs on the UI thread, so the callback can update
  components directly (no `ui.access()` needed).

### 4. Handling denial, failure, and unavailability (UC4)

UC4 has three distinct failure paths and the Flow API separates each
into its own mechanism so applications can respond correctly:

1. **"API not usable at all in this context"** — `Geolocation.isSupported(
   callback)` returns `false` when the browser has no
   `navigator.geolocation`, is on an insecure origin, or is inside an
   iframe where Permissions-Policy blocks geolocation. The application
   hides the control entirely.
2. **"Request failed on this click"** — the `onError` callback
   receives a `GeolocationError`. The `errorCode()` accessor returns
   a `GeolocationErrorCode` enum, so `switch` is exhaustive.
3. **"Previously denied — tell the user how to re-enable"** —
   `Geolocation.queryPermission(callback)` returns `DENIED` so the
   view can pre-explain.

```java
public class StoreFinderView extends VerticalLayout {

    private final Button locate = new Button("Find stores near me");
    private final TextField postcode = new TextField("Postcode");
    private final Paragraph hint = new Paragraph();

    public StoreFinderView(StoreService stores) {
        postcode.setVisible(false);

        // 1. Hide the button entirely where location can never work.
        Geolocation.isSupported(supported -> {
            if (!supported) {
                locate.setVisible(false);
                postcode.setVisible(true);
                hint.setText("Enter a postcode to find nearby stores.");
            }
        });

        // 3. Pre-explain for users who previously denied permission.
        Geolocation.queryPermission(state -> {
            if (state == GeolocationPermission.DENIED) {
                hint.setText("Location is blocked for this site. Click the "
                        + "padlock in the address bar to re-enable, or "
                        + "enter a postcode below.");
                postcode.setVisible(true);
            }
        });

        // 2. One listener, one exhaustive switch for the error path.
        locate.addClickListener(e -> Geolocation.get(
                pos -> showStoresFor(pos, stores),
                err -> {
                    postcode.setVisible(true);
                    hint.setText(switch (err.errorCode()) {
                        case PERMISSION_DENIED ->
                                "Location not shared. Please enter a postcode.";
                        case POSITION_UNAVAILABLE ->
                                "We couldn't determine your location.";
                        case TIMEOUT ->
                                "Location request timed out. Please try again.";
                        case null ->
                                // unknown future W3C code
                                err.message();
                    });
                }));

        add(new H2("Find a store"), locate, hint, postcode);
    }
}
```

- **`GeolocationErrorCode`** is an enum with three values —
  `PERMISSION_DENIED`, `POSITION_UNAVAILABLE`, `TIMEOUT`. It is
  derived from `GeolocationError.code()` via `errorCode()`, and
  returns `null` for any unknown future numeric code so the raw
  `int code()` accessor can still be consulted. Exhaustive `switch`
  on the enum is safe: the `case null` arm handles the "unknown
  code" case without losing the compile-time check.
- **`isSupported` works in all browsers.** It is plain feature
  detection: `'geolocation' in navigator`, `window.isSecureContext`,
  and (where available) `document.featurePolicy.allowsFeature(
  'geolocation')`.
- **`queryPermission` has a Safari limitation** — it returns
  `UNKNOWN` on Safari regardless of the user's actual setting,
  because Safari does not implement `permissions.query({name:
  'geolocation'})`. On Safari the UC4 "pre-explain why it's blocked"
  path will not fire. The application still works correctly: Safari
  users see the unadorned button, click it, and get the browser's
  own prompt or denial. The only thing missing is the targeted help
  text, which is a browser limitation with no workaround.

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

### Key Design Decisions

The first nine decisions are inherited from PR 23527. Decisions 10–13
are additions made after reviewing the PR against the use cases; each
one is small but closes a gap where applications would otherwise fall
back to raw `executeJs`.

1. **Thin wrapper over W3C, not a UI component.** `Geolocation` lives
   in `flow-server` as a framework-level utility, not as a
   `vaadin-*-flow` component module. The browser Geolocation API is a
   data source, not a widget — the application decides what to
   render, and the Flow API only carries the W3C shape across the
   wire. This is the single biggest shape difference from the earlier
   "button component" spec.

2. **Two primary entry points — `get` and `track`.** The W3C API has
   three operations (`getCurrentPosition`, `watchPosition`,
   `clearWatch`); the Flow API collapses them into two Java entry
   points and makes `clearWatch` implicit (tied to the owner
   component's detach, or to an explicit `stop()`).

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

6. **`GeolocationError` carries both the numeric code and an enum
   accessor.** The record keeps `int code` and the three
   `public static final int` constants — matching the W3C wire shape
   and staying Jackson-round-trippable — but also exposes
   `errorCode()` returning a `GeolocationErrorCode` enum. Exhaustive
   `switch` on the enum is the recommended usage. Unknown future
   codes surface as `errorCode() == null`, so the raw `code()`
   accessor remains available without a second API.

7. **Optional coord fields are boxed `Double`, not `Optional<Double>`.**
   `altitude`, `altitudeAccuracy`, `heading`, `speed` may be missing
   from the browser's response, and the API represents that as
   `null`. Records plus `null` work cleanly with Jackson's default
   serialization; `Optional` does not.

8. **Timestamps are `long` milliseconds since epoch.** The W3C
   `DOMTimeStamp` is a long, the Flow wire format is a long, and the
   API keeps it a long on the Java side. Callers that want an
   `Instant` use `Instant.ofEpochMilli(pos.timestamp())` at the point
   of use.

9. **Options are nullable record fields.** `GeolocationOptions` has
   three fields (`Boolean enableHighAccuracy`, `Integer timeout`,
   `Integer maximumAge`) and `null` means "use the browser default".
   This matches W3C `PositionOptions` semantics and composes with
   `@JsonInclude(NON_NULL)` to omit defaults on the wire.

10. **`Geolocation` is `AutoCloseable` with an explicit `stop()`.**
    Tying the browser watch to the owner component's detach covers
    the common case (UC2: "stop when the user leaves the page"), but
    it does not cover "Stop" buttons in a view the user hasn't
    navigated away from. An explicit `stop()` method, plus
    `AutoCloseable` for `try-with-resources`, closes that gap. The
    method is idempotent and safe to call on an already-detached
    owner, so application code never has to track state.

11. **`Geolocation.queryPermission(callback)` returns a
    `GeolocationPermission` enum.** Without a Java-side permission
    query, UC3 is unachievable — every application that wants
    "auto-fetch on return visit without nagging first-time visitors"
    has to drop down to raw `executeJs`. Exposing the query as a
    static method keeps the Flow API thin but removes the boilerplate
    from every caller. The enum has an explicit `UNKNOWN` state so
    that callers have a correct fall-through branch in every browser.
    - *Safari limitation:* Safari does not implement
      `permissions.query` for `geolocation` and the method will
      always return `UNKNOWN` there. This is a browser limitation
      with no known workaround. The `GRANTED` branch simply never
      fires on Safari, which causes applications to fall back to
      the explicit click flow — the safest degradation.
    - *Reactive variant considered:* a `Signal<GeolocationPermission>`
      that tracks `permissionchange` events was considered and
      deferred. Firefox's `permissionchange` is unreliable and
      Safari does not fire it at all, so a signal would have
      inconsistent semantics across browsers. A future addition
      can layer a signal on top of `queryPermission` without
      breaking the static API.

12. **`Geolocation.isSupported(callback)` is a separate method.**
    "The feature is not usable in this context at all" is a
    different decision from "this request failed" — applications
    should hide controls entirely when `isSupported` returns
    `false`, and show a retry affordance when a request fails. The
    two concerns live in two methods so each has a clear semantic.
    `isSupported` is purely feature detection
    (`'geolocation' in navigator`, `isSecureContext`, and
    `featurePolicy.allowsFeature('geolocation')`) and works reliably
    in every supported browser.

13. **`GeolocationErrorCode` is additive, not a replacement.** The
    record still exposes `int code` and the numeric constants. Code
    that prefers the enum uses `errorCode()`; code that wants the
    raw W3C value uses `code()`. This avoids the "two names for one
    thing" trap and keeps wire-level fidelity. Calling
    `errorCode()` on an unknown future code returns `null`, which
    pattern-matches cleanly as `case null -> ...` in the
    recommended `switch` idiom.

14. **No form-field integration.** `Geolocation` is not a
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
public class Geolocation implements Serializable, AutoCloseable {
    private Geolocation() { ... }

    // --- One-shot ---

    public static void get(SerializableConsumer<GeolocationPosition> onSuccess);
    public static void get(GeolocationOptions options,
                           SerializableConsumer<GeolocationPosition> onSuccess);
    public static void get(SerializableConsumer<GeolocationPosition> onSuccess,
                           SerializableConsumer<GeolocationError> onError);
    public static void get(GeolocationOptions options,
                           SerializableConsumer<GeolocationPosition> onSuccess,
                           SerializableConsumer<GeolocationError> onError);

    // --- Tracking ---

    public static Geolocation track(Component owner);
    public static Geolocation track(Component owner, GeolocationOptions options);

    public Signal<GeolocationState> state();
    public void stop();
    public boolean isActive();
    @Override public void close();   // equivalent to stop()

    // --- Capability checks ---

    public static void isSupported(SerializableConsumer<Boolean> callback);
    public static void queryPermission(
            SerializableConsumer<GeolocationPermission> callback);
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
| `isSupported` | `SerializableConsumer<Boolean> callback` | `void` | Async feature detection. Calls back with `true` when `navigator.geolocation` is present, the origin is secure, and Permissions-Policy does not block geolocation. Works in every supported browser. |
| `queryPermission` | `SerializableConsumer<GeolocationPermission> callback` | `void` | Async query of the browser's Permissions API. Returns `GRANTED`, `DENIED`, `PROMPT`, or `UNKNOWN`. **Safari always returns `UNKNOWN`** because Safari does not support `permissions.query({name: 'geolocation'})`. |

| Instance method | Returns | Description |
|---|---|---|
| `state` | `Signal<GeolocationState>` | Read-only signal. Starts as `GeolocationState.Pending`; transitions to `GeolocationPosition` or `GeolocationError` as the browser reports. Consume via `ComponentEffect.effect(owner, ...)` or `.get()` / `.peek()`. |
| `stop` | `void` | Cancels the underlying browser watch and tears down the DOM listeners. Idempotent — safe to call multiple times and safe to call on a handle whose owner has already detached. After `stop()`, `state()` stops receiving updates; the last state value remains readable. Has no effect on handles returned from `get(...)` (one-shot requests have no watch to cancel). |
| `isActive` | `boolean` | Returns `true` while the browser watch is running. `false` after `stop()` has been called or after the owner has detached. |
| `close` | `void` | Equivalent to `stop()`. Enables `try-with-resources`. |

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

    /**
     * Returns the error code as a {@link GeolocationErrorCode} enum,
     * or {@code null} if the browser reports an unknown future code.
     */
    public GeolocationErrorCode errorCode() {
        return GeolocationErrorCode.fromCode(code);
    }
}
```

`code()` is the raw W3C numeric error code, kept for wire fidelity
and for the `public static final int` constants. `errorCode()` is the
enum-typed accessor for idiomatic Java `switch`. The two accessors
expose the same information; callers pick whichever suits them.

---

**`GeolocationErrorCode`** — Enum derived from `GeolocationError.code()`

```java
public enum GeolocationErrorCode {
    PERMISSION_DENIED(GeolocationError.PERMISSION_DENIED),
    POSITION_UNAVAILABLE(GeolocationError.POSITION_UNAVAILABLE),
    TIMEOUT(GeolocationError.TIMEOUT);

    private final int code;

    GeolocationErrorCode(int code) { this.code = code; }

    public int code() { return code; }

    /**
     * Returns the enum constant for the given W3C numeric code, or
     * {@code null} if the code is unknown.
     */
    public static GeolocationErrorCode fromCode(int code) {
        for (GeolocationErrorCode c : values()) {
            if (c.code == code) {
                return c;
            }
        }
        return null;
    }
}
```

Recommended usage in `switch`:

```java
switch (err.errorCode()) {
    case PERMISSION_DENIED -> showPermissionHelp();
    case POSITION_UNAVAILABLE -> showRetry();
    case TIMEOUT -> showRetry();
    case null -> showGenericError(err.message()); // unknown future code
}
```

`case null` keeps the `switch` exhaustive at compile time even
though `errorCode()` can legitimately return `null` for an unknown
future browser code. If the Geolocation spec is ever extended with a
new numeric code, existing callers continue to compile; the
`case null` arm handles the new code until the application is
updated.

---

**`GeolocationPermission`** — Enum returned by `queryPermission`

```java
public enum GeolocationPermission {
    /** The user has granted geolocation permission for this origin. */
    GRANTED,
    /** The user has denied geolocation permission for this origin. */
    DENIED,
    /** Permission has not been requested yet — a browser prompt will
     *  appear on the next call to {@code Geolocation.get} or
     *  {@code Geolocation.track}. */
    PROMPT,
    /**
     * The browser cannot report the permission state. Applications
     * should treat this as "do not auto-fetch; wait for explicit
     * user action". Safari always returns this value because it
     * does not support {@code permissions.query({name:'geolocation'})}.
     */
    UNKNOWN
}
```

Use `GRANTED` to gate auto-fetch on return visits (UC3) and `DENIED`
to pre-explain why a previously-denied user's clicks are being
rejected (UC4). Treat `PROMPT` and `UNKNOWN` as "do nothing
automatic; wait for an explicit user action".

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

Two TypeScript sources in `flow-client`:

- `flow-client/src/main/frontend/Geolocation.ts` — exposes:
  - `window.Vaadin.Flow.geolocation.get(options)` — one-shot
  - `window.Vaadin.Flow.geolocation.watch(host, options, watchKey)` — start watch
  - `window.Vaadin.Flow.geolocation.clearWatch(watchKey)` — stop watch
  - `window.Vaadin.Flow.geolocation.isSupported()` — feature detection
  - `window.Vaadin.Flow.geolocation.queryPermission()` — Permissions API wrapper
- `flow-client/src/main/frontend/Flow.ts` — wires the geolocation
  helper into the `window.Vaadin.Flow` namespace.

Applications do not call these directly; they are invoked from the
corresponding `Geolocation` static methods via `executeJs`, and from
the detach listener / `stop()` path registered by `Geolocation.track`.

**`isSupported` implementation.** Returns `true` when:
```javascript
typeof navigator !== 'undefined'
    && 'geolocation' in navigator
    && window.isSecureContext
    && (!('featurePolicy' in document)
        || document.featurePolicy.allowsFeature('geolocation'))
```
The `featurePolicy` check is only performed when the (deprecated but
still widely implemented) `document.featurePolicy` interface is
present. When the newer `document.permissionsPolicy` is available, it
is preferred.

**`queryPermission` implementation.** Uses
`navigator.permissions.query({name: 'geolocation'})` where available
and maps the result:
- `'granted'` → `GeolocationPermission.GRANTED`
- `'denied'` → `GeolocationPermission.DENIED`
- `'prompt'` → `GeolocationPermission.PROMPT`
- any other state, exception, missing `navigator.permissions`, or
  Safari's `TypeError` on unknown permission name →
  `GeolocationPermission.UNKNOWN`

The promise rejection from Safari's `permissions.query` is caught
client-side and translated into `UNKNOWN`; it never surfaces as an
error on the Java side.

---

### Lifecycle and cleanup

- `get`, `isSupported`, and `queryPermission` use
  `ui.getElement().executeJs(...)` and resolve exactly once. No
  server state is kept.
- `track(owner)`:
  1. Registers a `vaadin-geolocation-position` DOM listener on
     `owner.getElement()` that updates the signal.
  2. Registers a `vaadin-geolocation-error` DOM listener likewise.
  3. Calls `window.Vaadin.Flow.geolocation.watch(this, options, key)`
     on the owner element to start the browser watch.
  4. Registers a detach listener that calls
     `window.Vaadin.Flow.geolocation.clearWatch(key)` and removes
     both DOM listeners.
- `stop()` / `close()` run the same teardown path as the detach
  listener (and is safe to call before the detach listener fires —
  the detach listener becomes a no-op). The handle's internal
  `isActive` flag flips to `false`; further `stop()` calls are
  no-ops.
- The client uses `addEventDetail().allowInert()` so watch events are
  delivered even when the owner element is temporarily inert.

---

### Testing

The PR includes `GeolocationTest.java` covering Jackson round-trips
for every record and `GeolocationView.java` / `GeolocationIT.java` as
integration tests that mock `navigator.geolocation` so CI runs do not
need real permissions. These live in `flow-tests/test-root-context/`.

---

### Features considered but NOT included

These are features from the earlier "button component" spec that are
deliberately out of scope. Each is still achievable in application
code, with the trade-off noted.

| Feature | Why it's out | How the application can do it |
|---|---|---|
| Button / slotted icon / prefix-suffix content | Not a UI component | Use `Button` + `VaadinIcon` and call `Geolocation.get` from the click handler |
| `HasValue<GeolocationPosition>` and `Binder` support | Form semantics belong to the form, not the sensor | Store the position in a bean field; a tiny submit-state guard does the rest |
| `required` / `invalid` / `validate()` | Same reason | As above |
| `setAutoLocate(true)` component flag | Covered by `queryPermission` + `onAttach` — one method call instead of a hidden flag | `Geolocation.queryPermission(state -> { if (state == GRANTED) Geolocation.get(...); });` |
| `minimumAccuracy` (reject bad fixes) | Kept out of the wrapper; one `if` covers it | `if (pos.coords().accuracy() > 25) return;` in the callback or effect |
| Reactive `Signal<GeolocationPermission>` tracking `permissionchange` | Firefox fires `permissionchange` unreliably; Safari never fires it. Signal semantics would differ across browsers | Call `queryPermission` on every attach, or poll on a timer for long-lived views |
| `Duration` / `Instant` types on options/timestamps | Kept as `long` / `int` ms to match W3C and the wire shape | `Instant.ofEpochMilli(pos.timestamp())`; `(int) Duration.ofSeconds(5).toMillis()` |

---

### Coverage check: use cases → Flow API

| Use case | How the Flow API covers it | Remaining gaps |
|---|---|---|
| UC1 — One-shot on click | `Geolocation.get(onSuccess, onError)` from a click handler. Exhaustive `switch` on `err.errorCode()`. | None. |
| UC2 — Continuous tracking | `Geolocation.track(owner)` + `ComponentEffect.effect` on `state()`. Detach cancels the browser watch automatically; `stop()` / `AutoCloseable` cancels it from application code without detaching. | None. |
| UC3 — Auto-use on return visit | `Geolocation.queryPermission` from `onAttach` → `Geolocation.get` only when `GRANTED`. No surprise prompts on cold load. | **Safari:** always returns `UNKNOWN`, so auto-fetch never fires and Safari users fall back to UC1. Browser limitation with no workaround. |
| UC4 — Denial / failure / timeout / unavailable | `Geolocation.isSupported` hides the control when the API is unusable; `Geolocation.queryPermission` surfaces `DENIED` so the application can pre-explain; the `onError` callback with `GeolocationErrorCode` enum handles request failures. | **Safari** returns `UNKNOWN` from `queryPermission`, so targeted "previously-denied" help text cannot be shown on Safari. Error-path handling itself is fully covered. |
| UC5 — Detailed position data | `GeolocationCoordinates` exposes all W3C fields; timestamp is `long` epoch ms, convertible to `Instant`. | `minimumAccuracy` filter lives in the application — one `if` in the callback. No functional gap. |
| UC6 — Precision / freshness / battery | `GeolocationOptions` record, per-call. | Millisecond `Integer` instead of `Duration` — stylistic, not functional. |
| UC7 — Location as a form field | Application owns a `Button` + `GeolocationPosition` field + `refreshSubmitState` guard. | No `HasValue` / `Binder` integration — by design. A higher-level `GeolocationField` component could be added in a separate module if it turns out to be commonly needed. |
