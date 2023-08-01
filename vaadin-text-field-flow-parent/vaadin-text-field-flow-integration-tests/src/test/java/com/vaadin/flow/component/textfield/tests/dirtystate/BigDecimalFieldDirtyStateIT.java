package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-big-decimal-field/dirty-state")
public class BigDecimalFieldDirtyStateIT
        extends AbstractDirtyStateIT<BigDecimalFieldElement> {
    @Override
    protected BigDecimalFieldElement getTestField() {
        return $(BigDecimalFieldElement.class).first();
    }
}
