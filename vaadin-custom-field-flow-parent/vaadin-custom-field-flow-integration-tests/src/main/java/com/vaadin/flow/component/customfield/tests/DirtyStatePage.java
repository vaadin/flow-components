package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-custom-field/dirty-state")
public class DirtyStatePage
        extends AbstractDirtyStatePage<CustomField<String>> {
    private class NameField extends CustomField<String> {
        @Override
        protected String generateModelValue() {
            return "";
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
        }
    };

    @Override
    protected CustomField<String> createTestField() {
        return new NameField();
    }
}
