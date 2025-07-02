package com.vaadin.flow.component.customfield.validation;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Input;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<CustomField<String>, String> {
    class TestCustomField extends CustomField<String> {
        private Input input = new Input();

        public TestCustomField() {
            add(input);
        }

        @Override
        protected String generateModelValue() {
            return input.getValue();
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            input.setValue(newPresentationValue);
        }
    };

    @Override
    protected CustomField<String> createTestField() {
        return new TestCustomField();
    }
}
