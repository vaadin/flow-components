# Use Case 6 — Trade off precision, freshness, and battery

Different parts of the same application have different tolerances.
The Flow API expresses this through a single `GeolocationOptions`
record that maps 1:1 onto W3C `PositionOptions`. Every call to
`Geolocation.get` or `Geolocation.track` may take its own options, so
two call sites on the same page can legitimately disagree.

The record has three nullable fields; a `null` field means "use the
browser default" (`false`, `Infinity`, `0` respectively). The
recommended way to construct an options value is
`GeolocationOptions.builder()`, which labels each setting at the
call site and accepts `java.time.Duration` for the time-based fields:

```java
GeolocationOptions opts = GeolocationOptions.builder()
        .highAccuracy(true)
        .timeout(Duration.ofSeconds(10))
        .maximumAge(Duration.ZERO)
        .build();
```

Only the setters that are actually invoked affect the built options;
the rest stay `null` and fall through to the browser defaults.

## Example: Four call sites in a single application

```java
public class LocationSites {

    // A — News site: a cached city-level reading is fine.
    //     Up to 5 minutes old, give up fast.
    public static void showLocalHeadlines(NewsService news) {
        Geolocation.get(
                GeolocationOptions.builder()
                        .timeout(Duration.ofSeconds(5))
                        .maximumAge(Duration.ofMinutes(5))
                        .build(),
                pos -> news.loadHeadlinesAt(pos),
                err -> { /* ignore — the user can pick a city manually */ });
    }

    // B — Turn-by-turn navigation: most accurate, continuous.
    public static Geolocation startNavigation(Component view) {
        return Geolocation.track(view,
                GeolocationOptions.builder().highAccuracy(true).build());
    }

    // C — Check in at a venue: must be fresh, no cached positions accepted.
    public static void checkIn(CheckInService service) {
        Geolocation.get(
                GeolocationOptions.builder()
                        .highAccuracy(true)
                        .timeout(Duration.ofSeconds(10))
                        .maximumAge(Duration.ZERO)
                        .build(),
                pos -> service.checkIn(pos),
                err -> Notification.show("Could not verify your location: "
                        + err.message()));
    }

    // D — Address form: give up quickly so the user can type instead.
    public static void prefillAddress(AddressForm form) {
        Geolocation.get(
                GeolocationOptions.builder()
                        .timeout(Duration.ofSeconds(3))
                        .build(),
                pos -> form.prefillFrom(pos),
                err -> {
                    // Let the user type the address manually.
                });
    }
}
```

## How the Flow API covers UC6

- **Per-call options.** Both `get` and `track` accept a
  `GeolocationOptions` argument, so there is no shared configuration.
  The four examples above can coexist in the same view without
  interfering with one another.
- **Builder with `Duration` inputs.** `GeolocationOptions.builder()`
  accepts `java.time.Duration` for the time-based fields and
  converts to the record's millisecond `Integer` shape internally,
  so application code never has to do unit conversion by hand.
- **Unset setters stay `null`.** Any setter the caller does not
  invoke leaves that field at its default. `@JsonInclude(NON_NULL)`
  on the record means these defaults are omitted on the wire, so
  the browser sees exactly the keys the caller set.

## What each field does

| Field | Builder setter | Maps to | Default (when unset) | Effect when set |
|---|---|---|---|---|
| `enableHighAccuracy` | `.highAccuracy(boolean)` | W3C `enableHighAccuracy` | `false` | Asks the browser for the best possible position, typically using GPS. Slower and more battery-intensive. |
| `timeout` | `.timeout(Duration)` | W3C `timeout` | `Infinity` | Maximum wait before the browser reports `TIMEOUT`. |
| `maximumAge` | `.maximumAge(Duration)` | W3C `maximumAge` | `0` | Maximum age of a cached reading. `Duration.ZERO` means "no cached reading accepted"; a larger value lets stale readings satisfy the request instantly. |

## Direct record construction

The record's canonical constructor
`new GeolocationOptions(Boolean enableHighAccuracy, Integer timeout, Integer maximumAge)`
is also public and accepts raw millisecond `Integer` values. Use it
when:

- You already have millisecond values on hand (e.g. from a config file).
- You are deserialising `GeolocationOptions` from JSON (Jackson calls
  the canonical constructor).

For hand-written application code, the builder is the recommended
path because `new GeolocationOptions(null, 3000, null)` is
unreadable without flipping to the record definition.

## Combining options with UC2 and UC3

`GeolocationOptions` is orthogonal to everything else, so it composes
freely with the tracking-signal pattern from UC2 and the auto-fetch
pattern from UC3:

```java
// A watched, continuously refreshing, high-accuracy navigator.
Geolocation nav = Geolocation.track(this,
        GeolocationOptions.builder().highAccuracy(true).build());

// An auto-fetching news panel that returns a cached city-level
// position instantly on return visits.
@Override
protected void onAttach(AttachEvent event) {
    super.onAttach(event);
    Geolocation.get(
            GeolocationOptions.builder()
                    .timeout(Duration.ofSeconds(5))
                    .maximumAge(Duration.ofMinutes(15))
                    .build(),
            pos -> populateLocalHeadlines(pos),
            null);
}
```
