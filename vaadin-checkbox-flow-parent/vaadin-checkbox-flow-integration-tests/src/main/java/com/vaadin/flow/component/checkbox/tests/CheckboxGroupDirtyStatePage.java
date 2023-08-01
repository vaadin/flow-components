package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-checkbox-group/dirty-state")
public class CheckboxGroupDirtyStatePage extends AbstractDirtyStatePage<CheckboxGroup<String>> {
    @Override
    protected CheckboxGroup<String> createTestField() {
        return new CheckboxGroup<>();
    }
}
