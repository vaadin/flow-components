package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox/validation/binder")
public class CheckboxBinderValidationPage
        extends AbstractValidationPage<Checkbox> {

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";

    public static class Bean {
        private Boolean property;

        public Boolean getProperty() {
            return property;
        }

        public void setProperty(Boolean property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    public CheckboxBinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });
    }

    @Override
    protected Checkbox createTestField() {
        return new Checkbox("Checkbox");
    }
}
