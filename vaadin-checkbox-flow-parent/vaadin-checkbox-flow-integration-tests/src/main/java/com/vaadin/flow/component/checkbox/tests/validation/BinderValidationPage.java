package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.Arrays;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox-group/validation/binder")
public class BinderValidationPage
        extends AbstractValidationPage<CheckboxGroup<String>> {

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";

    public static class Bean {
        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    public BinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .bind("property");
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems(Arrays.asList("foo", "bar", "baz"));

        return checkboxGroup;
    }
}
