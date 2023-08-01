package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-password-field/dirty-state")
public class PasswordFieldDirtyStatePage extends AbstractDirtyStatePage<PasswordField> {
    @Override
    protected PasswordField createTestField() {
        return new PasswordField();
    }
}
