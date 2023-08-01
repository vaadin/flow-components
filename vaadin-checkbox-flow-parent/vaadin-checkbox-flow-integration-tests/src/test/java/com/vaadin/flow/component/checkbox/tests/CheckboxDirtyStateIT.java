package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-checkbox/dirty-state")
public class CheckboxDirtyStateIT
        extends AbstractDirtyStateIT<CheckboxElement> {
    @Override
    protected CheckboxElement getTestField() {
        return $(CheckboxElement.class).first();
    }
}
