package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-date-picker/validation/error-message")
public class ErrorMessagePage extends AbstractValidationPage<DatePicker> {
    public ErrorMessagePage() {
        super();

        testField.setI18n(
                new DatePickerI18n().setRequiredErrorMessage("Date is required")
                        .setBadInputErrorMessage("Date has invalid format")
                        .setMinErrorMessage("Date is too small")
                        .setMaxErrorMessage("Date is too big"));

        testField.setRequired(true);

        testField.setMin(LocalDate.now().minusDays(2), context -> {
            return "Date must be after " + testField.getMin();
        });

        testField.setMax(LocalDate.now().plusDays(2), context -> {
            return "Date must be before " + testField.getMax();
        });
    }

    @Override
    protected DatePicker createTestField() {
        return new DatePicker();
    }
}
