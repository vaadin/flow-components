package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-checkbox/dirty-state")
public class CheckboxDirtyStatePage extends AbstractDirtyStatePage<Checkbox> {
    @Override
    protected Checkbox createTestField() {
        return new Checkbox();
    }
}
