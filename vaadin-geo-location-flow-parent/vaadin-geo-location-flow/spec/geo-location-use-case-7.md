# Use Case 7 — Capture the user's location as part of a form

Location is a field inside a larger form: alongside a description and a
photo, the user also pins their current coordinates, and the whole
thing is submitted together. The location field must behave like any
other field: it participates in `Binder`, can be required, validates,
and resets with the rest of the form.

## Example: "Report a pothole" form

```java
public class PotholeReport {
    private String description;
    private GeoLocationPosition position;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public GeoLocationPosition getPosition() { return position; }
    public void setPosition(GeoLocationPosition position) { this.position = position; }
}
```

```java
@Route("report")
@PageTitle("Report a pothole")
public class PotholeReportView extends VerticalLayout {

    private final TextArea description = new TextArea("Description");
    private final GeoLocation location = new GeoLocation("Pin my location");
    private final Button submit = new Button("Submit");
    private final Button reset = new Button("Reset");

    private final Binder<PotholeReport> binder = new Binder<>(PotholeReport.class);

    public PotholeReportView(PotholeService service) {
        description.setRequired(true);
        description.setWidthFull();

        location.setRequired(true);
        location.setHighAccuracy(true);
        location.setMinimumAccuracy(50); // metres
        location.setPrefixComponent(VaadinIcon.MAP_MARKER.create());

        binder.forField(description).asRequired("Describe the problem")
                .bind(PotholeReport::getDescription, PotholeReport::setDescription);
        binder.forField(location).asRequired("A location is required")
                .bind(PotholeReport::getPosition, PotholeReport::setPosition);

        binder.addStatusChangeListener(e -> submit.setEnabled(binder.isValid()));
        submit.addClickListener(e -> {
            PotholeReport report = new PotholeReport();
            if (binder.writeBeanIfValid(report)) {
                service.report(report);
                Notification.show("Thank you, report submitted.");
                binder.readBean(new PotholeReport());
                location.clear();
            }
        });
        reset.addClickListener(e -> {
            binder.readBean(new PotholeReport());
            location.clear();
        });

        add(description, location, new HorizontalLayout(submit, reset));
    }
}
```

## Why this works out of the box

- **`GeoLocation` is a `HasValue<GeoLocationPosition>`.** Every other
  Flow field API that already works with `Binder`, `FormLayout`, and
  `ComponentValueChangeEvent` works here without a special case. A
  developer who has used `TextField` before does not need to learn
  anything new to put a location into a form.
- **Rich value type, not a string.** `PotholeReport.position` is a
  strongly-typed `GeoLocationPosition` — not a `"lat,lng"` string the
  server has to parse, not a pair of loose `double` fields that can be
  swapped by accident. `getLatitude()` / `getLongitude()` are direct
  method calls on the value.
- **`setRequired(true)` + `Binder.asRequired()` is enough.** The form
  is invalid while no position has been captured; the submit button is
  disabled via the `StatusChangeListener`; the user sees the same
  required-indicator styling as every other Vaadin field.
- **`setMinimumAccuracy(50)` is the validation.** A reading that is
  too imprecise (e.g. from a stationary desktop indoors) never becomes
  the component's value — it is reported to the client listener as
  `MINIMUM_ACCURACY_NOT_MET` instead. No custom `Validator` is needed,
  and the form stays invalid until the user captures a sufficiently
  precise position.
- **`clear()` resets the field.** Form reset restores an empty report
  bean and calls `location.clear()` to wipe the captured value, any
  leftover error, and the invalid state. The form is ready for the
  next submission.

## Restoring a previously captured position (edit form)

For an edit scenario where the position has already been captured and
stored, `setValue(...)` (from `HasValue`) restores it without a
browser request:

```java
PotholeReport existing = service.findById(reportId);
binder.readBean(existing);
// location.getValue() == existing.position()
```

The `GeoLocation` button is still clickable — the user can re-capture
their location if they moved since the original reading.

## Extra validation

`Binder` validators compose with the component's built-in
`minimumAccuracy` check. For example, "only accept reports inside the
city boundary":

```java
binder.forField(location).asRequired("A location is required")
        .withValidator(p -> cityBoundary.contains(p.getLatitude(), p.getLongitude()),
                "This location is outside the city we cover")
        .bind(PotholeReport::getPosition, PotholeReport::setPosition);
```

The component-level `minimumAccuracy` and the `Binder`-level custom
validators run together — the application gets both without any
bespoke wiring.
