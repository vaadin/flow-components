package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-integer-field/dirty-state")
public class IntegerFieldDirtyStateIT
        extends AbstractDirtyStateIT<IntegerFieldElement> {
    @Override
    protected IntegerFieldElement getTestField() {
        return $(IntegerFieldElement.class).first();
    }
}
