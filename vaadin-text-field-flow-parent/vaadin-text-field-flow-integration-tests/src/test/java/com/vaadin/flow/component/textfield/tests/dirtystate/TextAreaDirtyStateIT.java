package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-text-area/dirty-state")
public class TextAreaDirtyStateIT
        extends AbstractDirtyStateIT<TextAreaElement> {
    @Override
    protected TextAreaElement getTestField() {
        return $(TextAreaElement.class).first();
    }
}
