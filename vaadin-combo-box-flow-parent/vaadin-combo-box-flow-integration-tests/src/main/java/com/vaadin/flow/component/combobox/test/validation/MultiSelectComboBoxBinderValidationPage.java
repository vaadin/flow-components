package com.vaadin.flow.component.combobox.test.validation;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;
import java.util.Set;

@Route("vaadin-multi-select-combo-box/validation/binder")
public class MultiSelectComboBoxBinderValidationPage
        extends AbstractValidationPage<MultiSelectComboBox<String>> {
    public static final String ENABLE_CUSTOM_VALUE_BUTTON = "enable-custom-value-button";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";

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

    private Set<String> expectedValue;

    public MultiSelectComboBoxBinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");

        add(createButton(ENABLE_CUSTOM_VALUE_BUTTON, "Enable custom values",
                event -> {
                    testField.setAllowCustomValue(true);
                    testField.addCustomValueSetListener(e -> {
                        testField.select(e.getDetail());
                    });
                }));

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected value", event -> {
            expectedValue = Set.of(event.getValue());
        }));
    }

    @Override
    protected MultiSelectComboBox<String> createTestField() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(List.of("foo", "bar", "baz"));

        return comboBox;
    }
}
