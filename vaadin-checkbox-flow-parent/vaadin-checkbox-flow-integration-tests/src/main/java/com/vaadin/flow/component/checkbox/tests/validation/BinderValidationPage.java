/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox-group/validation/binder")
public class BinderValidationPage
        extends AbstractValidationPage<CheckboxGroup<String>> {

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";

    public static class Bean {
        private Set<String> property;

        public Set<String> getProperty() {
            return property;
        }

        public void setProperty(Set<String> property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    public BinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems(List.of("foo", "bar", "baz"));

        return checkboxGroup;
    }
}
