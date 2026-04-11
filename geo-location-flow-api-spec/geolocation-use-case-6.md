# Use Case 6 — Trade off precision, freshness, and battery

Different parts of the same application have different tolerances.
The PR 23527 API expresses this through a single `GeolocationOptions`
record that maps 1:1 onto W3C `PositionOptions`. Every call to
`Geolocation.get` or `Geolocation.track` may take its own options, so
two call sites on the same page can legitimately disagree.

```java
public record GeolocationOptions(
        Boolean enableHighAccuracy,
        Integer timeout,        // milliseconds
        Integer maximumAge)     // milliseconds
```

A `null` field means "use the browser default" (`false`, `Infinity`,
`0` respectively).

## Example: Four call sites in a single application

```java
public class LocationSites {

    // A — News site: a cached city-level reading is fine.
    //     Up to 5 minutes old, give up fast.
    public static void showLocalHeadlines(NewsService news) {
        Geolocation.get(
                new GeolocationOptions(null, 5000, 300_000),
                pos -> news.loadHeadlinesAt(pos),
                err -> { /* ignore — the user can pick a city manually */ });
    }

    // B — Turn-by-turn navigation: most accurate, continuous.
    public static Geolocation startNavigation(Component view) {
        return Geolocation.track(view,
                new GeolocationOptions(true, null, null));
    }

    // C — Check in at a venue: must be fresh, no cached positions accepted.
    public static void checkIn(CheckInService service) {
        Geolocation.get(
                new GeolocationOptions(true, 10_000, 0),
                pos -> service.checkIn(pos),
                err -> Notification.show("Could not verify your location: "
                        + err.message()));
    }

    // D — Address form: give up quickly so the user can type instead.
    public static void prefillAddress(AddressForm form) {
        Geolocation.get(
                new GeolocationOptions(null, 3000, null),
                pos -> form.prefillFrom(pos),
                err -> {
                    // Let the user type the address manually.
                });
    }
}
```

## How the PR API covers UC6

- **Per-call options.** Both `get` and `track` accept a
  `GeolocationOptions` argument, so there is no shared configuration.
  The four examples above can coexist in the same view without
  interfering with one another.
- **Nullable fields express "use the browser default".** A
  `GeolocationOptions(null, null, null)` — equivalent to
  `new GeolocationOptions()` (the zero-arg constructor) — behaves
  identically to calling the browser API with no options.
- **`@JsonInclude(NON_NULL)`** on the record means null fields are
  omitted when the options travel over the wire, so the browser sees
  exactly the keys the caller set.

## What each field does

| Field | Maps to | Default (when `null`) | Effect when set |
|---|---|---|---|
| `enableHighAccuracy` | W3C `enableHighAccuracy` | `false` | Asks the browser for the best possible position, typically using GPS. Slower and more battery-intensive. |
| `timeout` (ms) | W3C `timeout` | `Infinity` | Maximum wait before the browser reports `TIMEOUT`. |
| `maximumAge` (ms) | W3C `maximumAge` | `0` | Maximum age of a cached reading. `0` means "no cached reading accepted"; a larger value lets stale readings satisfy the request instantly. |

## `Integer` vs `Duration`

The PR stays millisecond-integer because W3C `PositionOptions` is
millisecond-integer and the wire format is millisecond-integer. In
Java code, `Duration` is usually more ergonomic, so applications
that use `Duration` often can add a tiny helper:

```java
public final class GeolocationOptionsBuilder {

    public static GeolocationOptions of(boolean highAccuracy,
                                        Duration timeout,
                                        Duration maximumAge) {
        return new GeolocationOptions(
                highAccuracy,
                timeout == null ? null : Math.toIntExact(timeout.toMillis()),
                maximumAge == null ? null : Math.toIntExact(maximumAge.toMillis()));
    }
}
```

This is deliberately left to the application — the core record
keeps the same shape as the W3C dictionary.

## Combining options with UC2 and UC3

`GeolocationOptions` is orthogonal to everything else, so it composes
freely with the tracking-signal pattern from UC2 and the auto-fetch
pattern from UC3:

```java
// A watched, continuously refreshing, high-accuracy navigator.
Geolocation nav = Geolocation.track(this,
        new GeolocationOptions(true, null, null));

// An auto-fetching news panel that returns a cached city-level
// position instantly on return visits.
@Override
protected void onAttach(AttachEvent event) {
    super.onAttach(event);
    Geolocation.get(
            new GeolocationOptions(null, 5000, 900_000), // 15 min cache
            pos -> populateLocalHeadlines(pos),
            null);
}
```
