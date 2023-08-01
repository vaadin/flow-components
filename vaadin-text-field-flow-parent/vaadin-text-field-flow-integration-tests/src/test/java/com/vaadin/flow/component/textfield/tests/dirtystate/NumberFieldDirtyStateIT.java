package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-number-field/dirty-state")
public class NumberFieldDirtyStateIT
        extends AbstractDirtyStateIT<NumberFieldElement> {
    @Override
    protected NumberFieldElement getTestField() {
        return $(NumberFieldElement.class).first();
    }
}
