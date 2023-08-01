package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-email-field/dirty-state")
public class EmailFieldDirtyStatePage extends AbstractDirtyStatePage<EmailField> {
    @Override
    protected EmailField createTestField() {
        return new EmailField();
    }
}
