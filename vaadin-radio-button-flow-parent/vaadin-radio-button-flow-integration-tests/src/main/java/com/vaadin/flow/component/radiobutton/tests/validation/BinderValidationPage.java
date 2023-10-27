package com.vaadin.flow.component.radiobutton.tests.validation;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;

@Route("vaadin-radio-button-group/validation/binder")
public class BinderValidationPage
        extends AbstractValidationPage<RadioButtonGroup<String>> {

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
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });
    }

    @Override
    protected RadioButtonGroup<String> createTestField() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setItems(List.of("foo", "bar", "baz"));

        return radioButtonGroup;
    }
}
