package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/validation/basic")
public class DatePickerValidationBasicPage extends AbstractValidationPage {
    public static final String ATTACH_FIELD_BUTTON = "attach-field-button";
    public static final String DETACH_FIELD_BUTTON = "detach-field-button";

    public static final String REQUIRED_BUTTON = "required-button";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";

    public DatePickerValidationBasicPage() {
        super();

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            field.setRequiredIndicatorVisible(true);
        }));

        add(createInput(MIN_INPUT, "Set min date", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            field.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date", event -> {
            LocalDate value = LocalDate.parse(event.getValue());
            field.setMax(value);
        }));

        addAttachDetachControls();
    }

    protected void addAttachDetachControls() {
        NativeButton attachButton = createButton(ATTACH_FIELD_BUTTON,
                "Attach field", event -> add(field));
        NativeButton detachButton = createButton(DETACH_FIELD_BUTTON,
                "Detach field", event -> add(field));

        add(new Div(attachButton, detachButton));
    }
}
