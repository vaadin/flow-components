package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-radio-button-group/dirty-state")
public class RadioButtonGroupDirtyStateIT
        extends AbstractDirtyStateIT<RadioButtonGroupElement> {
    @Override
    protected RadioButtonGroupElement getTestField() {
        return $(RadioButtonGroupElement.class).first();
    }
}
