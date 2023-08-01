package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-text-field/dirty-state")
public class TextFieldDirtyStateIT
        extends AbstractDirtyStateIT<TextFieldElement> {
    @Override
    protected TextFieldElement getTestField() {
        return $(TextFieldElement.class).first();
    }
}
