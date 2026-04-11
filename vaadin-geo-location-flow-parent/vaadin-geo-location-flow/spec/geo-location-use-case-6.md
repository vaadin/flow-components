# Use Case 6 — Trade off precision, freshness, and battery

Different parts of the same application have different tolerances.
Three settings on `GeoLocation` map 1:1 onto the W3C `PositionOptions`
dictionary: `highAccuracy`, `maximumAge`, and `timeout`. Every setting
is per-instance — two `GeoLocation` buttons on the same view can
legitimately disagree.

## Example: Four buttons in a single application

```java
// A — News site: a cached city-level reading is fine.
//     Up to 5 minutes old, give up fast.
GeoLocation localHeadlines = new GeoLocation("Show local headlines");
localHeadlines.setMaximumAge(Duration.ofMinutes(5));
localHeadlines.setTimeout(Duration.ofSeconds(5));

// B — Turn-by-turn navigation: most accurate position, continuous.
GeoLocation navigation = new GeoLocation("Start navigation");
navigation.setHighAccuracy(true);
navigation.setWatch(true);

// C — Check in at a venue: must be fresh, no cached positions accepted.
GeoLocation checkIn = new GeoLocation("Check in here");
checkIn.setMaximumAge(Duration.ZERO);
checkIn.setHighAccuracy(true);
checkIn.setTimeout(Duration.ofSeconds(10));

// D — Address form: give up quickly so the user can type instead.
GeoLocation useMyAddress = new GeoLocation("Use my current address");
useMyAddress.setTimeout(Duration.ofSeconds(3));
```

## Why `Duration` instead of `long`

- **No unit confusion.** `Duration.ofMinutes(5)` is obviously five
  minutes. `5 * 60 * 1000` is not obviously anything. The Flow wrapper
  converts to the web component's millisecond format internally.
- **`Duration.ZERO` expresses "no cached reading accepted"** without a
  magic `0`. `Duration.ofSeconds(0)` is also valid and means the same.
- **Pairs with `Instant`** from UC5 for a consistent `java.time`-based
  API.

## Why per-instance, not global

Each `GeoLocation` button stores its own configuration, so the four
buttons above can be on the same page and the news-site button is not
forced to wait 10 seconds because the check-in button set a longer
timeout somewhere else. This is exactly what UC6 calls for:

> Different parts of the same application may legitimately want
> different trade-offs, so these choices cannot be a global setting.

## What each setting does

| Setting | Maps to | Default | Effect when set |
|---|---|---|---|
| `setHighAccuracy(true)` | W3C `enableHighAccuracy` | `false` | Asks the browser for the best possible position, typically using GPS. Slower and more battery-intensive. |
| `setMaximumAge(Duration)` | W3C `maximumAge` | `Duration.ZERO` | Maximum age of a cached reading the browser may return. `Duration.ZERO` disables caching; a large value lets stale readings satisfy the request instantly. |
| `setTimeout(Duration)` | W3C `timeout` | `null` (no timeout) | How long to wait before giving up. After elapsing, a `LocationEvent` fires with `GeoLocationErrorCode.TIMEOUT`. |

## Combining with UC2 and UC3

The three settings compose freely with `watch` and `autoLocate`:

```java
// A watched, continuously refreshing, high-accuracy navigator.
navigation.setWatch(true);
navigation.setHighAccuracy(true);

// An auto-locating news panel that returns a cached city-level
// position instantly on return visits.
GeoLocation newsPanel = new GeoLocation("Local news");
newsPanel.setAutoLocate(true);
newsPanel.setMaximumAge(Duration.ofMinutes(15));
```
