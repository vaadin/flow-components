package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Page for testing DatePicker validation constraints
 */
@Route("vaadin-date-picker/constraint-validation")
public class DatePickerConstraintValidationPage extends Div {

    public static final String MIN_DATE_BUTTON = "min-date-button";
    public static final String MAX_DATE_BUTTON = "max-date-button";
    public static final String REQUIRED_BUTTON = "required-button";

    public static final LocalDate CONSTRAINT_DATE_VALUE = LocalDate.of(2022, 1,
            1);
    public static final String SERVER_VALIDITY_STATE_BUTTON = "server-validity-state-button";
    public static final String VALIDITY_STATE = "validity-state";

    public DatePickerConstraintValidationPage() {
        var field = new DatePicker("Select date");
        field.setLocale(Locale.US);

        var required = addButton("required", REQUIRED_BUTTON,
                e -> field.setRequired(true));
        var minDate = addButton("min date", MIN_DATE_BUTTON,
                e -> field.setMin(CONSTRAINT_DATE_VALUE));
        var maxDate = addButton("max date", MAX_DATE_BUTTON,
                e -> field.setMax(CONSTRAINT_DATE_VALUE));

        var validityState = new Div();
        validityState.setId(VALIDITY_STATE);

        var retrieveValidityState = addButton("server validity state",
                SERVER_VALIDITY_STATE_BUTTON,
                e -> validityState.setText(String.valueOf(field.isInvalid())));

        add(field, required, minDate, maxDate,
                new Div(retrieveValidityState, validityState));
    }

    private NativeButton addButton(String label, String id,
            ComponentEventListener<ClickEvent<NativeButton>> clickListener) {
        var button = new NativeButton(label, clickListener);
        button.setId(id);
        return button;
    }
}
