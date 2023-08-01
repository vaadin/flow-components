package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-password-field/dirty-state")
public class PasswordFieldDirtyStateIT
        extends AbstractDirtyStateIT<PasswordFieldElement> {
    @Override
    protected PasswordFieldElement getTestField() {
        return $(PasswordFieldElement.class).first();
    }
}
