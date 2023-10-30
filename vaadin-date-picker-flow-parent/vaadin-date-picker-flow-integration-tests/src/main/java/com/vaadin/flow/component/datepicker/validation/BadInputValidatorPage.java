package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-date-picker/validation/bad-input-validator")
public class BadInputValidatorPage extends AbstractValidationPage<DatePicker> {
    public static class Bean {
        private LocalDate property;

        public LocalDate getProperty() {
            return property;
        }

        public void setProperty(LocalDate property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    public BadInputValidatorPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField)
            .withValidator(new DatePicker.BadInputValidator("Invalid input"))
            .asRequired("Required")
            .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });
    }

    @Override
    protected DatePicker createTestField() {
        return new DatePicker();
    }
}
