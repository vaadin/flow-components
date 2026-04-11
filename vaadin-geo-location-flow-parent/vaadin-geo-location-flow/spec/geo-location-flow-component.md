# Vaadin Geo Location Flow Component

`GeoLocation` is a button that asks the browser — with the user's consent —
for the user's current position. It wraps the browser's Geolocation API in
a regular Flow component: the server receives location readings as
strongly-typed Java objects via a `LocationEvent`, and the component can
participate in a form as a `HasValue<GeoLocationPosition>` field.

The component does not display a map, it does not reverse-geocode, it does
not store history. It is the bridge between "where am I" and "what should
the application do with it".

> ⚠️ The component is experimental. The Flow wrapper enables
> `window.Vaadin.featureFlags.geoLocationComponent = true` automatically on
> attach, so Flow applications don't need to configure it manually.

## Usage Examples

### 1. Basic — one-time location request on click (UC1)

The canonical shape. Drop a `GeoLocation` button into a view, listen for
the location event, and react to the captured position. This covers the
most common scenario: "tell me what's around me right now".

```java
GeoLocation locate = new GeoLocation("Use my location");
locate.setPrefixComponent(VaadinIcon.MAP_MARKER.create());

locate.addLocationListener(event -> {
    event.getPosition().ifPresentOrElse(
            position -> showNearestStores(position.getLatitude(),
                    position.getLongitude()),
            () -> showManualPostcodeEntry(event.getError().orElseThrow()));
});

add(locate);
```

A `LocationEvent` is fired once per click. Its `getPosition()` and
`getError()` are mutually exclusive — exactly one is present.

### 2. Continuous tracking (UC2)

Setting `watch` turns the button into a tracker: after the first click,
the browser keeps reporting fresh positions and `LocationEvent` fires on
every update. Tracking stops automatically when the component is detached,
so no stray background watches leak between views.

```java
GeoLocation tracker = new GeoLocation("Start tracking");
tracker.setWatch(true);
tracker.setHighAccuracy(true);

tracker.addLocationListener(event ->
        event.getPosition().ifPresent(this::recordWorkoutSample));
```

Programmatic equivalent (e.g. toggling from a different button):

```java
tracker.startWatching();
// ...later...
tracker.stopWatching();
```

The `watching` state is reflected back to the server, and
`tracker.isWatching()` returns the current state.

### 3. Auto-locate on return visit (UC3)

`setAutoLocate(true)` causes the component to attempt a single request
automatically when it is attached — but **only** if the browser reports
that permission has already been granted. If permission is `PROMPT` or
`DENIED`, the component stays idle and waits for an explicit click. The
browser's permission dialog is therefore never triggered by an auto-locate
pass.

```java
GeoLocation locate = new GeoLocation("Use my location");
locate.setAutoLocate(true);
locate.addLocationListener(event ->
        event.getPosition().ifPresent(this::showLocalHeadlines));
```

`autoLocate` and `watch` can be combined to mean "silently start
tracking on return visits".

### 4. Handling denial, failure, and unavailability (UC4)

The error path is a first-class part of the same `LocationEvent`. The
Flow component also exposes two higher-level signals:

- `isUnavailable()` — the Geolocation API cannot be used at all
  (insecure origin, blocked by Permissions-Policy, unsupported browser).
  An unavailable component is inert and must be hidden by the
  application, because no click will ever succeed.
- `getPermission()` — cached `GeoPermissionState` (`GRANTED`, `DENIED`,
  `PROMPT`, `UNKNOWN`) so the application can tell the user *why*
  location is not working and how to re-enable it.

```java
GeoLocation locate = new GeoLocation("Find stores near me");
TextField postcode = new TextField("Postcode");
postcode.setVisible(false);

if (locate.isUnavailable()) {
    locate.setVisible(false);
    postcode.setVisible(true);
}

locate.addLocationListener(event -> {
    if (event.getPosition().isPresent()) {
        showNearbyStores(event.getPosition().get());
        return;
    }
    GeoLocationError error = event.getError().orElseThrow();
    postcode.setVisible(true);
    switch (error.getCode()) {
        case PERMISSION_DENIED -> showNotice(
                "Location blocked. Click the padlock in the address bar "
                        + "to re-enable.");
        case POSITION_UNAVAILABLE -> showNotice(
                "We couldn't determine your location.");
        case TIMEOUT -> showNotice("Location request timed out.");
        default -> showNotice(error.getMessage());
    }
});
```

Error codes are exposed as a stable `GeoLocationErrorCode` enum, not
opaque numeric codes, so the mapping is obvious and exhaustive `switch`
statements remain valid if new codes are added later.

### 5. Reading detailed position data (UC5)

The full `GeoLocationPosition` value carries everything the browser
reported: latitude, longitude, accuracy, altitude, altitude accuracy,
heading, speed, and the reading timestamp.

```java
GeoLocation cycle = new GeoLocation("Record ride");
cycle.setWatch(true);
cycle.setHighAccuracy(true);

cycle.addLocationListener(event -> event.getPosition().ifPresent(p ->
        updateDashboard(
                p.getLatitude(), p.getLongitude(),
                p.getAltitude(),               // Optional<Double>
                p.getAccuracy(),               // metres
                p.getAltitudeAccuracy(),       // Optional<Double>
                p.getHeading(),                // Optional<Double>
                p.getSpeed(),                  // Optional<Double>
                p.getTimestamp())));           // Instant
```

Optional fields that the browser may omit (`altitude`, `heading`,
`speed`, `altitudeAccuracy`) return `Optional<Double>`. `latitude`,
`longitude`, `accuracy`, and `timestamp` are always present.

To ignore readings that are too imprecise, set `minimumAccuracy`:

```java
// Reject readings worse than 25 metres.
cycle.setMinimumAccuracy(25);
```

Bad readings are then reported as
`GeoLocationErrorCode.MINIMUM_ACCURACY_NOT_MET` instead of polluting the
route with a zig-zag.

### 6. Tuning precision, freshness, and battery per instance (UC6)

Three settings map 1:1 onto the W3C `PositionOptions` dictionary:
`highAccuracy`, `maximumAge`, and `timeout`. They are per-instance — two
`GeoLocation` buttons on the same view may legitimately disagree, because
different features have different tolerances.

`Duration` is used for the time-based properties, because it is the
natural Java type and avoids unit confusion.

```java
// News site: a cached city-level reading is fine.
GeoLocation newsLocate = new GeoLocation("Show local headlines");
newsLocate.setMaximumAge(Duration.ofMinutes(5));
newsLocate.setTimeout(Duration.ofSeconds(5));

// Navigation: most accurate position, continuously updated.
GeoLocation navLocate = new GeoLocation("Start navigation");
navLocate.setWatch(true);
navLocate.setHighAccuracy(true);

// Check-in: must be fresh, no cached positions accepted.
GeoLocation checkIn = new GeoLocation("Check in here");
checkIn.setMaximumAge(Duration.ZERO);
checkIn.setHighAccuracy(true);
checkIn.setTimeout(Duration.ofSeconds(10));

// Address form: give up quickly.
GeoLocation addressLocate = new GeoLocation("Use my address");
addressLocate.setTimeout(Duration.ofSeconds(3));
```

### 7. Location as a form field (UC7)

`GeoLocation` is a `HasValue<GeoLocationPosition>` field. It participates
in `Binder` and `FormLayout` exactly like a `TextField` or a
`DatePicker`. The value is a `GeoLocationPosition` (or `null` when no
position has been captured yet). `setRequired(true)` prevents the form
from submitting until a position has been captured, and `setMinimumAccuracy`
from example 5 doubles as a validation rule — a reading that misses the
threshold is never accepted as a value.

```java
public class ReportForm extends FormLayout {

    private final TextField description = new TextField("Description");
    private final GeoLocation location = new GeoLocation("Pin my location");
    private final Button submit = new Button("Report");

    private final Binder<PotholeReport> binder = new Binder<>(PotholeReport.class);

    public ReportForm() {
        description.setRequired(true);
        location.setRequired(true);
        location.setHighAccuracy(true);
        location.setMinimumAccuracy(50); // metres

        binder.forField(description).asRequired()
                .bind(PotholeReport::getDescription, PotholeReport::setDescription);
        binder.forField(location).asRequired()
                .bind(PotholeReport::getPosition, PotholeReport::setPosition);

        submit.addClickListener(e -> {
            if (binder.writeBeanIfValid(new PotholeReport())) {
                service.report(binder.getBean());
            }
        });

        add(description, location, submit);
    }
}
```

- `HasValue.getValue()` returns the currently captured
  `GeoLocationPosition`, or `null` when none has been captured.
- `setValue(position)` is useful for restoring a previously captured
  position on form edit. Setting it manually does not trigger a
  browser request.
- `addValueChangeListener` fires whenever the captured position
  changes, including when a user clicks the button and gets a new fix.
- A required `GeoLocation` is invalid while its value is `null`.
  `Binder`'s `asRequired()` enforces this in the usual Flow way.
- Resetting the form (or calling `clear()`) resets the value, error, and
  invalid state so the form can be reused.

---

### Key Design Decisions

1. **`GeoLocation` is a Flow field, not just a button wrapper.** The
   Flow component is a `HasValue<GeoLocationPosition>`: its value is the
   most recent captured position (or `null`), and every UC7 scenario
   falls out of that — `Binder`, `asRequired()`, value change listeners,
   and form reset work without any component-specific code on the
   application side. The web component's serialized
   `"latitude,longitude"` value string is an internal client-side
   detail that the Flow API does not surface.

2. **Rich value type: `GeoLocationPosition`.** Instead of forcing
   developers to parse a `"lat,lng"` string (or work with a browser DOM
   type that does not exist on the server), the Flow component maps the
   browser's `GeolocationPosition` to an immutable Java value object
   with typed accessors. Always-present fields (`latitude`, `longitude`,
   `accuracy`, `timestamp`) are plain `double` / `Instant`; browser-optional
   fields (`altitude`, `altitudeAccuracy`, `heading`, `speed`) are
   `Optional<Double>` — a direct reflection of the W3C API's nullability
   and exactly what UC5 needs.

3. **`Instant` for the reading timestamp, `Duration` for config.** These
   are the natural Java types and eliminate the "is this milliseconds or
   seconds?" class of bugs. The wire format is still milliseconds (the
   web component's `timeout`/`maximum-age`/`timestamp` are all ms), but
   the translation happens inside the Flow wrapper.

4. **Single `LocationEvent` with `Optional<Position>` and `Optional<Error>`.**
   Mirrors the web component decision: one listener handles success and
   failure in one place, so UC1 and UC4 share a single control-flow
   branch. An alternative with separate `PositionEvent` /
   `LocationErrorEvent` events was considered and rejected — it is the
   most common source of "forgot to handle denial" bugs in plain
   Geolocation API code.

5. **Named error codes via a `GeoLocationErrorCode` enum.** `switch`
   statements on the enum are exhaustive, refactor-safe, and allow the
   component to add new codes (`INSECURE_CONTEXT`, `UNSUPPORTED`,
   `MINIMUM_ACCURACY_NOT_MET`) without breaking existing listeners.

6. **`isUnavailable()` is distinct from an error on a request.** Some
   UC4 scenarios (insecure origin, Permissions-Policy block, unsupported
   browser) mean the feature can *never* work in the current context —
   the correct UI is to hide the button, not to show "try again". Those
   scenarios are exposed via `isUnavailable()` + an
   `UnavailableChangeEvent` (rarely fires, but observable) rather than
   being bundled into `LocationEvent`, so application code can easily
   branch "hide the control" vs. "show a retry button".

7. **`getPermission()` is an enum, not a boolean.** UC4's "tell
   previously-denied users *why* the feature isn't working" needs to
   distinguish `DENIED` from `PROMPT` from `UNKNOWN`. A boolean would
   lose that distinction. The value is kept in sync with the browser's
   Permissions API, and a `PermissionChangeEvent` lets the application
   react if the user re-enables location in another tab.

8. **`watch` is both a property and a pair of methods.** `setWatch(true)`
   / `setWatch(false)` is the declarative form, matching the web
   component's attribute, and is the form that pairs naturally with
   `Binder` / property binding. `startWatching()` / `stopWatching()` /
   `isWatching()` is the imperative form for code that toggles tracking
   from a different control (e.g. a separate stop button). Detaching
   the component always cancels an active watch, so UC2's "tracking
   must stop when the user leaves the page" needs no application code.

9. **`setAutoLocate(true)` is never surprising.** It only triggers a
   request when `getPermission() == GRANTED`. The component never
   triggers the browser's permission dialog from an auto-locate pass —
   only an explicit click does that. This is what makes UC3 compatible
   with UC4's "must not pop an unexpected prompt on page load".

10. **`minimumAccuracy` is in the component, not the application.**
    UC5 and UC7 both describe "too bad to accept". Keeping the
    threshold check on the component collapses the logic into one
    place: a rejected reading never reaches the `LocationEvent` as a
    position, is never stored as the component value, and therefore is
    never accepted by `Binder`. A single `setMinimumAccuracy(double)`
    call is enough to enforce "submit only when we have a good-enough
    location", with no custom listener code.

11. **`requestLocation()` returns void and uses the listener, not a
    `CompletableFuture`.** Flow components already have a well-established
    listener pattern. A future-based API would compete with the existing
    `addLocationListener` subscription that UC2 (watch mode) depends on
    — there is no single future that represents "all the positions from
    a watch". Keeping a single path (listener) keeps the API coherent.

12. **`HasPrefix` / `HasSuffix` / `HasText`.** The component is
    visually a button, so it reuses the same slot interfaces as
    `Button`. A developer who already knows `Button` can put an icon on
    a `GeoLocation` button without reading any extra documentation.

13. **`HasThemeVariant<GeoLocationVariant>`.** The web component's
    theme variants mirror `vaadin-button` exactly (`PRIMARY`,
    `TERTIARY`, `ERROR`, `SMALL`, `LARGE`). The same enum shape is
    exposed on the Flow side.

14. **Feature flag auto-enabled on attach.** The web component is gated
    by `window.Vaadin.featureFlags.geoLocationComponent = true`; the
    Flow wrapper sets this flag via `Element.executeJs` in the attach
    handler, so Flow applications need only add the Maven dependency.
    The experimental status is documented on the class Javadoc.

15. **Router-agnostic.** The component does not know about the
    application's routes, does not fetch tiles, does not render a map,
    and does not reverse-geocode. These are explicitly out of scope.

---

## Implementation

### Classes

**`GeoLocation`** — Main component class

```java
@Tag("vaadin-geo-location")
@NpmPackage(value = "@vaadin/geo-location", version = "...")
@JsModule("@vaadin/geo-location/src/vaadin-geo-location.js")
public class GeoLocation
        extends AbstractField<GeoLocation, GeoLocationPosition>
        implements Focusable<GeoLocation>, HasAriaLabel, HasEnabled,
                HasPrefix, HasSize, HasStyle, HasSuffix, HasText,
                HasThemeVariant<GeoLocationVariant>, HasTooltip,
                HasValidationProperties, HasValidator<GeoLocationPosition>
```

`AbstractField<GeoLocation, GeoLocationPosition>` provides the
`HasValue` machinery with `null` as the empty value.

| Constructor | Parameters | Description |
|---|---|---|
| `GeoLocation()` | — | Creates an empty button with no label. |
| `GeoLocation(String label)` | `String label` | Creates a button with the given text label. |
| `GeoLocation(String label, ComponentEventListener<LocationEvent> listener)` | `String label, ComponentEventListener<LocationEvent> listener` | Creates a button with a label and an immediately-registered location listener. |

#### Acquisition / lifecycle

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `requestLocation` | — | `void` | Programmatically triggers a single location request, equivalent to a user click. Fires the same `LocationEvent` as a click. Does nothing if the component is disabled or unavailable. |
| `startWatching` | — | `void` | Starts continuous position updates. Equivalent to `setWatch(true)`. |
| `stopWatching` | — | `void` | Stops continuous position updates. Equivalent to `setWatch(false)`. |
| `isWatching` | — | `boolean` | Returns `true` if a watch is currently active on the client. |
| `clear` | — | `void` | Resets the value to `null`, clears the current error, and resets the `invalid` state. Does not change the `watch`, `autoLocate`, or configuration properties. |

#### Watch / auto-locate

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `setWatch` | `boolean watch` | `void` | Enables or disables continuous position updates. When enabled, a `LocationEvent` fires on every position update reported by the browser. |
| `isWatch` | — | `boolean` | Returns whether continuous-watch mode is enabled. |
| `setAutoLocate` | `boolean autoLocate` | `void` | When `true`, the component attempts a single location request on attach — **only** if `getPermission() == GRANTED`. Never triggers the browser's permission dialog. |
| `isAutoLocate` | — | `boolean` | Returns whether auto-locate is enabled. |

#### Position options (W3C `PositionOptions`)

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `setHighAccuracy` | `boolean highAccuracy` | `void` | When `true`, requests the most accurate position the device can produce (GPS), at the cost of speed and battery. Maps to W3C `enableHighAccuracy`. Default `false`. |
| `isHighAccuracy` | — | `boolean` | Returns whether high-accuracy mode is enabled. |
| `setMaximumAge` | `Duration maximumAge` | `void` | Maximum age of a cached position that the browser may return. `Duration.ZERO` means "no cached position accepted". `null` is treated as zero. Default zero. Maps to W3C `maximumAge` (milliseconds). |
| `getMaximumAge` | — | `Duration` | Returns the configured maximum age. |
| `setTimeout` | `Duration timeout` | `void` | How long to wait for a position before giving up. `null` means "no timeout". After elapsing, the `LocationEvent` is fired with `GeoLocationErrorCode.TIMEOUT`. Maps to W3C `timeout` (milliseconds). |
| `getTimeout` | — | `Duration` | Returns the configured timeout, or `null` if no timeout is set. |
| `setMinimumAccuracy` | `double metres` | `void` | If set, readings with `accuracy > metres` are rejected with `GeoLocationErrorCode.MINIMUM_ACCURACY_NOT_MET` instead of being surfaced as a position. Pass `Double.POSITIVE_INFINITY` (the default) to disable the check. |
| `getMinimumAccuracy` | — | `double` | Returns the configured threshold in metres. |

#### Permission / availability

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `getPermission` | — | `GeoPermissionState` | Returns the cached permission state (`GRANTED`, `DENIED`, `PROMPT`, `UNKNOWN`). Kept in sync with `navigator.permissions.query({name: 'geolocation'})`. |
| `isUnavailable` | — | `boolean` | Returns `true` when the Geolocation API is not usable at all in the current context (no `navigator.geolocation`, insecure origin, blocked by Permissions-Policy). An unavailable component is inert and must be hidden by the application. |
| `getState` | — | `GeoLocationState` | Returns the current lifecycle state (`IDLE`, `REQUESTING`, `SUCCESS`, `ERROR`, `UNAVAILABLE`). |
| `getLastError` | — | `Optional<GeoLocationError>` | Returns the most recent error, if any. Empty on success or when idle. |

#### Label / content

Content is managed through the standard `HasText`, `HasPrefix`, and
`HasSuffix` interfaces:

```java
GeoLocation locate = new GeoLocation();
locate.setText("Use my location");
locate.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
```

#### Form-field properties

Inherited from `AbstractField` / `HasValidationProperties`:

| Method | Returns | Description |
|---|---|---|
| `getValue()` | `GeoLocationPosition` | The most recent captured position, or `null`. |
| `setValue(GeoLocationPosition)` | `void` | Restores a previously captured position without a browser request. |
| `addValueChangeListener(...)` | `Registration` | Fires on every value change. |
| `setRequired(boolean)` | `void` | Required-indicator styling and invalid-when-empty semantics. |
| `isRequired()` | `boolean` |   |
| `setInvalid(boolean)` | `void` |   |
| `isInvalid()` | `boolean` |   |
| `setErrorMessage(String)` | `void` |   |
| `validate()` | `void` | Standard Flow field validation. |

#### Events

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `addLocationListener` | `ComponentEventListener<LocationEvent>` | `Registration` | Fired once per location attempt (one-shot or watched), whether success or failure. |
| `addPermissionChangeListener` | `ComponentEventListener<PermissionChangeEvent>` | `Registration` | Fired when the browser reports a change in geolocation permission state. |
| `addUnavailableChangeListener` | `ComponentEventListener<UnavailableChangeEvent>` | `Registration` | Fired when `isUnavailable()` toggles. Rare in practice. |

---

**`GeoLocationPosition`** — Immutable value object for a captured position

Corresponds to the browser's `GeolocationPosition`. Implements
`Serializable`; can be stored in a session or a bean.

| Method | Returns | Description |
|---|---|---|
| `getLatitude` | `double` | Latitude in decimal degrees (WGS 84). |
| `getLongitude` | `double` | Longitude in decimal degrees (WGS 84). |
| `getAccuracy` | `double` | Accuracy of the latitude/longitude reading, in metres. Always present. |
| `getAltitude` | `Optional<Double>` | Altitude in metres above WGS 84, if the browser reports it. |
| `getAltitudeAccuracy` | `Optional<Double>` | Accuracy of the altitude reading, in metres. |
| `getHeading` | `Optional<Double>` | Direction of travel in degrees clockwise from true north, if the browser reports it. |
| `getSpeed` | `Optional<Double>` | Ground speed in metres per second, if the browser reports it. |
| `getTimestamp` | `Instant` | When the reading was taken. |

| Constructor | Parameters | Description |
|---|---|---|
| `GeoLocationPosition(double latitude, double longitude, double accuracy, Instant timestamp)` | Required fields only. Optional fields are empty. | Main constructor for restoring a persisted position. |
| `GeoLocationPosition(double latitude, double longitude, double accuracy, Double altitude, Double altitudeAccuracy, Double heading, Double speed, Instant timestamp)` | All fields; pass `null` for any optional field the caller does not know. | Full constructor. |

Also implements `equals`, `hashCode`, `toString`.

---

**`GeoLocationError`** — Immutable value object for a failed request

| Method | Returns | Description |
|---|---|---|
| `getCode` | `GeoLocationErrorCode` | Stable, named error code. |
| `getMessage` | `String` | Human-readable description from the browser (may be empty). |

---

**`GeoLocationErrorCode`** — Error code enum

```java
public enum GeoLocationErrorCode {
    PERMISSION_DENIED,
    POSITION_UNAVAILABLE,
    TIMEOUT,
    MINIMUM_ACCURACY_NOT_MET,
    INSECURE_CONTEXT,
    UNSUPPORTED
}
```

Named codes mirror the web component's stable string codes. The raw
browser numeric codes (1, 2, 3) never appear in Flow application code.

---

**`GeoPermissionState`** — Permission state enum

```java
public enum GeoPermissionState {
    GRANTED,
    DENIED,
    PROMPT,
    UNKNOWN
}
```

Mirrors the Permissions API's `PermissionState` plus an extra
`UNKNOWN` value for browsers that cannot report the state.

---

**`GeoLocationState`** — Lifecycle state enum

```java
public enum GeoLocationState {
    IDLE,
    REQUESTING,
    SUCCESS,
    ERROR,
    UNAVAILABLE
}
```

---

**`LocationEvent`** — Fired for every location attempt

Extends `ComponentEvent<GeoLocation>`.

| Method | Returns | Description |
|---|---|---|
| `getPosition` | `Optional<GeoLocationPosition>` | The captured position, or empty on failure. |
| `getError` | `Optional<GeoLocationError>` | The error, or empty on success. |
| `isSuccess` | `boolean` | Convenience: `getPosition().isPresent()`. |

Exactly one of `getPosition()` and `getError()` is non-empty per event.

---

**`PermissionChangeEvent`** — Fired when the browser's permission state changes

Extends `ComponentEvent<GeoLocation>`.

| Method | Returns | Description |
|---|---|---|
| `getPermission` | `GeoPermissionState` | The new permission state. |

---

**`UnavailableChangeEvent`** — Fired when `isUnavailable()` toggles

Extends `ComponentEvent<GeoLocation>`.

| Method | Returns | Description |
|---|---|---|
| `isUnavailable` | `boolean` | Whether the component is now unavailable. |

---

**`GeoLocationVariant`** — Theme variant enum

Implements `ThemeVariant`. Mirrors `ButtonVariant` exactly:

```java
public enum GeoLocationVariant implements ThemeVariant {
    LUMO_PRIMARY("primary"),
    LUMO_TERTIARY("tertiary"),
    LUMO_ERROR("error"),
    LUMO_SMALL("small"),
    LUMO_LARGE("large");
    // ...
}
```

---

### Behavior notes

- **Feature flag.** `GeoLocation` sets
  `window.Vaadin.featureFlags.geoLocationComponent = true` via
  `Element.executeJs` in its attach handler. This means Flow applications
  do not need to enable the flag manually.
- **Attach / detach.** Registering the client-side listeners for the
  `location`, `permission-changed`, `position-changed`, and
  `unavailable-changed` events is done in the attach handler and torn
  down in the detach handler. A watch active on the client when the
  component is detached is cancelled by the web component's own
  `disconnectedCallback`, so UC2's "tracking must stop when the user
  leaves the page" works with no Flow-side code.
- **Permission changes from other tabs.** If the user re-enables
  geolocation in a different tab, the browser fires a
  `permissionchange` event that the web component propagates as
  `permission-changed`; the Flow component forwards that as a
  `PermissionChangeEvent`. Auto-locate does NOT re-fire because of such
  a change — only the next attach re-evaluates `autoLocate`.
- **Value change.** When a location arrives on the client, the
  component's `position` is synchronized to the server, the Flow
  `AbstractField` value is updated, and `ValueChangeEvent` listeners
  fire. `LocationEvent` fires before the value change event, so
  handlers that need to see both can use either.
- **Rejected-accuracy readings.** A reading that violates
  `minimumAccuracy` is converted (on the client) to a
  `MINIMUM_ACCURACY_NOT_MET` error. It never reaches the Flow value and
  never triggers a value change — a required + minimumAccuracy
  `GeoLocation` stays invalid.
- **Disabled / unavailable.** The inner button is rendered with
  `aria-disabled="true"` in both cases; the Flow `isEnabled()` /
  `isUnavailable()` pair distinguishes "the developer disabled it" from
  "the API cannot be used here".
- **`setValue(null)` is valid** and is equivalent to `clear()` for the
  purposes of form reset, but does not reset the `invalid` flag —
  `clear()` does both.

---

### Accessibility

- The component renders as a button with proper keyboard semantics
  (Enter/Space activation, focus ring, disabled), handled by the web
  component through its mixin chain.
- `setAriaLabel(String)` (from `HasAriaLabel`) sets an accessible
  label when the visible label is an icon-only button.
- `HasTooltip` provides accessible tooltip support via
  `setTooltipText`.
- `HasValidationProperties` wires the standard `aria-invalid` /
  `aria-required` state attributes that every Flow form component
  already exposes.
- Error announcements are deliberately left to the application — a
  `LocationEvent` listener can update a live region of the application's
  choice. Baking in a default live region would be wrong for many
  contexts.

---

### CSS custom properties (reference only)

Theming is done via the CSS custom properties defined by the web
component, which inherit from `<vaadin-button>`. Flow apps theme the
component by writing CSS against these properties — they are not exposed
as Java methods:

- `--vaadin-geo-location-background`
- `--vaadin-geo-location-text-color`
- `--vaadin-geo-location-border-color`
- `--vaadin-geo-location-border-width`
- `--vaadin-geo-location-border-radius`
- `--vaadin-geo-location-padding`
- `--vaadin-geo-location-gap`
- `--vaadin-geo-location-height`
- `--vaadin-geo-location-font-size`
- `--vaadin-geo-location-font-weight`

---

### Features considered but omitted

- **`CompletableFuture<GeoLocationPosition> requestLocation()`.** Would
  collide with watch mode, where there is no single future that
  represents "all the positions from a watch". The listener pattern
  covers both modes uniformly.
- **`HasComponents` / arbitrary children.** The web component only
  exposes `prefix`, `suffix`, and a default label slot. `HasPrefix`,
  `HasSuffix`, and `HasText` are exactly the right set — arbitrary
  children are not a valid use case.
- **`GeoLocationI18n`.** No use case requires localizing component-side
  text: all visible text (the button label, fallback messages) is
  under application control already. Can be added later as a
  non-breaking addition if translation of error messages is needed.
- **Reverse-geocoding / map helpers / history.** Out of scope. The
  application composes `GeoLocation` with `vaadin-map` or its own
  geocoding service from a `LocationEvent` listener.

---

### Coverage check: use cases → API

| Use case | How the Flow API covers it |
|---|---|
| UC1 — Show info relevant to where I am now | `new GeoLocation("Use my location")` + `addLocationListener`. One listener handles both success and failure paths. |
| UC2 — Continuously follow me | `setWatch(true)` (or `startWatching()`). `LocationEvent` fires on every update. Detach cancels the watch automatically. |
| UC3 — Auto-use location on return | `setAutoLocate(true)`. Only fires when `getPermission() == GRANTED`, so returning users see no prompt and first-time users are not surprised. |
| UC4 — Handle denial / unavailability | `LocationEvent.getError()` with `GeoLocationErrorCode` enum; `isUnavailable()` for "can never work" scenarios; `getPermission()` for "why is this not working" messaging; `PermissionChangeEvent` for live updates. |
| UC5 — Detailed position data | `GeoLocationPosition` exposes latitude, longitude, accuracy, altitude, altitudeAccuracy, heading, speed, and timestamp. `setMinimumAccuracy` rejects bad readings automatically. |
| UC6 — Per-instance trade-offs | `setHighAccuracy`, `setMaximumAge(Duration)`, `setTimeout(Duration)` — each per instance. |
| UC7 — Location as a form field | `HasValue<GeoLocationPosition>`, `setRequired(true)`, `Binder.forField(...).asRequired().bind(...)`. `clear()` resets on form reset. `setMinimumAccuracy` enforces "refuse to submit imprecise positions". |
