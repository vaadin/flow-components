package com.vaadin.flow.component.select.tests;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-select/dirty-state")
public class DirtyStateIT
        extends AbstractDirtyStateIT<SelectElement> {
    @Override
    protected SelectElement getTestField() {
        return $(SelectElement.class).first();
    }
}
