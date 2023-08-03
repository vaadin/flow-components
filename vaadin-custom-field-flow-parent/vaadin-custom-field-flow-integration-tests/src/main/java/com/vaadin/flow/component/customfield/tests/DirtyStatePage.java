package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-custom-field/dirty-state")
public class DirtyStatePage
        extends AbstractDirtyStatePage<CustomField<String>> {
    private static class TestCustomField extends CustomField<String> {
        private TextField field1 = new TextField();
        private TextField field2 = new TextField();

        TestCustomField() {
            add(field1, field2);
        }

        @Override
        protected String generateModelValue() {
            return field1.getValue() + "," + field2.getValue();
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            String[] parts = newPresentationValue.split(",");
            field1.setValue(parts[1]);
            field2.setValue(parts[2]);
        }
    };

    @Override
    protected CustomField<String> createTestField() {
        return new TestCustomField();
    }
}
