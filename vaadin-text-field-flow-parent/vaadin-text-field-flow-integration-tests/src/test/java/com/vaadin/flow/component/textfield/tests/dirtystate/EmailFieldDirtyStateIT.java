package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-email-field/dirty-state")
public class EmailFieldDirtyStateIT
        extends AbstractDirtyStateIT<EmailFieldElement> {
    @Override
    protected EmailFieldElement getTestField() {
        return $(EmailFieldElement.class).first();
    }
}
