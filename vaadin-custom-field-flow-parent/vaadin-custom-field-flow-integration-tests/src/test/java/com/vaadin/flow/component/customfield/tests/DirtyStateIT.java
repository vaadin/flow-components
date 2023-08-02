package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.testbench.CustomFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-custom-field/dirty-state")
public class DirtyStateIT extends AbstractDirtyStateIT<CustomFieldElement> {
    @Override
    protected CustomFieldElement getTestField() {
        return $(CustomFieldElement.class).first();
    }
}
