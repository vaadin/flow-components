package com.vaadin.flow.component.textfield.tests.validation;

import java.time.LocalDate;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-integer-field/validation/bad-input-validator")
public class BadInputValidatorPage extends AbstractValidationPage<IntegerField> {

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
                .withValidator(new IntegerField.BadInputValidator("Invalid input"))
                .asRequired("Required")
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });
    }

    @Override
    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
