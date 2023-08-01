package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-combo-box/dirty-state")
public class ComboBoxDirtyStateIT
        extends AbstractDirtyStateIT<ComboBoxElement> {
    @Override
    protected ComboBoxElement getTestField() {
        return $(ComboBoxElement.class).first();
    }
}
