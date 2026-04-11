# Use Case 7 — Capture the user's location as part of a form

Location is a field inside a larger form: alongside a description and
a photo, the user also pins their current coordinates, and the whole
thing is submitted together. The location field must behave like any
other field: it participates in the form, can be required, validates,
and resets with the rest of the form.

With the PR 23527 API, `Geolocation` is **not** a form field — there
is no `HasValue<GeolocationPosition>`, no `Binder` hook, and no
built-in `required` / `reset` semantics. The form owns the location
through a regular `Button` + a `GeolocationPosition` field on the
bean, plus a few lines of glue to gate the submit button on "a
position has been captured".

## Example: "Report a pothole" form

### The bean

```java
public class PotholeReport {
    private String description;
    private GeolocationPosition position;

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public GeolocationPosition getPosition() { return position; }
    public void setPosition(GeolocationPosition position) {
        this.position = position;
    }
}
```

### The form

```java
@Route("report")
@PageTitle("Report a pothole")
public class PotholeReportView extends VerticalLayout {

    private final TextArea description = new TextArea("Description");
    private final Button pin = new Button("Pin my location");
    private final Span pinLabel = new Span("No location pinned yet");
    private final Button submit = new Button("Submit");
    private final Button reset = new Button("Reset");

    private final Binder<PotholeReport> binder = new Binder<>(PotholeReport.class);

    public PotholeReportView(PotholeService service) {
        description.setRequired(true);
        description.setWidthFull();

        pin.setIcon(VaadinIcon.MAP_MARKER.create());
        pin.addClickListener(e -> Geolocation.get(
                GeolocationOptions.builder()
                        .highAccuracy(true)
                        .timeout(Duration.ofSeconds(10))
                        .maximumAge(Duration.ZERO)
                        .build(),
                pos -> {
                    // Application-side validation (UC7's "refuse imprecise
                    // positions"): reject readings worse than 50 m.
                    if (pos.coords().accuracy() > 50) {
                        Notification.show(
                                "Location is too imprecise, please try again.");
                        return;
                    }
                    PotholeReport bean = binder.getBean();
                    if (bean == null) {
                        bean = new PotholeReport();
                        binder.setBean(bean);
                    }
                    bean.setPosition(pos);
                    pinLabel.setText(
                            "Pinned at %.5f, %.5f (±%.0f m)".formatted(
                                    pos.coords().latitude(),
                                    pos.coords().longitude(),
                                    pos.coords().accuracy()));
                    refreshSubmitState();
                },
                err -> Notification.show(
                        "Could not pin location: " + err.message())));

        binder.setBean(new PotholeReport());
        binder.forField(description).asRequired("Describe the problem")
                .bind(PotholeReport::getDescription,
                        PotholeReport::setDescription);
        binder.addValueChangeListener(e -> refreshSubmitState());

        submit.addClickListener(e -> {
            PotholeReport bean = binder.getBean();
            if (binder.validate().isOk() && bean.getPosition() != null) {
                service.report(bean);
                Notification.show("Thank you, report submitted.");
                resetForm();
            }
        });
        reset.addClickListener(e -> resetForm());

        add(description, pin, pinLabel,
                new HorizontalLayout(submit, reset));
        refreshSubmitState();
    }

    private void refreshSubmitState() {
        PotholeReport bean = binder.getBean();
        submit.setEnabled(
                bean != null
                        && bean.getPosition() != null
                        && binder.isValid());
    }

    private void resetForm() {
        binder.setBean(new PotholeReport());
        pinLabel.setText("No location pinned yet");
        refreshSubmitState();
    }
}
```

## How the PR API covers UC7

- **`Geolocation.get` captures the reading on click.** The success
  callback stores the `GeolocationPosition` on the form bean and
  triggers `refreshSubmitState`, which enables the submit button only
  when a position is present and the rest of the form is valid.
- **Minimum-accuracy validation is a single `if`.** UC7's "refuse to
  submit imprecise positions" is three lines in the callback. No
  component-level option is needed.
- **`Binder` handles the non-location fields normally.** The
  description field is bound via `asRequired()`; only the location
  field needs custom glue because it is not a `HasValue`.
- **Form reset is a helper method.** Calling `binder.setBean(new
  PotholeReport())` clears the bound fields; the location helper
  also resets the `pinLabel` and re-evaluates the submit button.

## Gaps compared with the earlier "button component" spec

| Gap | Why the PR omits it | How the application fills it |
|---|---|---|
| `HasValue<GeolocationPosition>` | `Geolocation` is not a component; form-field semantics belong on a form field, not the sensor | Store the position in a bean field and gate the submit button on `bean.getPosition() != null` |
| `Binder.forField(location).bind(...)` | Same reason | Bind only the other fields with `Binder`; wire the location via a tiny click callback + `refreshSubmitState` |
| `setRequired(true)` / `setInvalid(...)` | Same reason | `refreshSubmitState` enables/disables the submit button directly |
| `clear()` that resets the captured value + error | Same reason | The view's `resetForm` method |
| `setMinimumAccuracy(metres)` | Not in the PR API | One `if (pos.coords().accuracy() > 50)` in the success callback |

None of these gaps change the *behaviour* the user sees — every UC7
requirement is still satisfied — but each one moves a responsibility
from the framework into the form class. The trade-off is a shorter
Flow API at the cost of a dozen lines of glue in each form that
captures a location.

## Restoring a previously captured position (edit form)

For an edit scenario where the position has already been captured
and stored, populate the bean's `position` directly — there is no
browser interaction involved:

```java
PotholeReport existing = service.findById(reportId);
binder.setBean(existing);
if (existing.getPosition() != null) {
    GeolocationCoordinates c = existing.getPosition().coords();
    pinLabel.setText(
            "Pinned at %.5f, %.5f (±%.0f m)".formatted(
                    c.latitude(), c.longitude(), c.accuracy()));
}
refreshSubmitState();
```

The user can still click the "Pin my location" button to re-capture
the position if they moved since the original reading.

## Custom server-side validation

Application-level business rules (e.g. "only accept reports inside
the city boundary") can be enforced either in the click callback or
in a submit-time check:

```java
submit.addClickListener(e -> {
    PotholeReport bean = binder.getBean();
    if (!binder.validate().isOk()) return;
    GeolocationPosition p = bean.getPosition();
    if (p == null) {
        Notification.show("Please pin your location.");
        return;
    }
    if (!cityBoundary.contains(p.coords().latitude(),
            p.coords().longitude())) {
        Notification.show("This location is outside the city we cover.");
        return;
    }
    service.report(bean);
    resetForm();
});
```
