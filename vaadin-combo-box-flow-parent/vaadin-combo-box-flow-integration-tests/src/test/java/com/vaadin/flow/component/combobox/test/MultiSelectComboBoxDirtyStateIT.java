package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-multi-select-combo-box/dirty-state")
public class MultiSelectComboBoxDirtyStateIT
        extends AbstractDirtyStateIT<MultiSelectComboBoxElement> {
    @Override
    protected MultiSelectComboBoxElement getTestField() {
        return $(MultiSelectComboBoxElement.class).first();
    }
}
