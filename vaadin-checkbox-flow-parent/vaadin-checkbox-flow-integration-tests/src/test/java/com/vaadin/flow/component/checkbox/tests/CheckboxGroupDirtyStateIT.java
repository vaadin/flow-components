package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-checkbox-group/dirty-state")
public class CheckboxGroupDirtyStateIT
        extends AbstractDirtyStateIT<CheckboxGroupElement> {
    @Override
    protected CheckboxGroupElement getTestField() {
        return $(CheckboxGroupElement.class).first();
    }
}
