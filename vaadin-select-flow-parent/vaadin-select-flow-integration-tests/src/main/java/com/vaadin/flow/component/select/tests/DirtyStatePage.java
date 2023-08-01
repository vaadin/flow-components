package com.vaadin.flow.component.select.tests;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-select/dirty-state")
public class DirtyStatePage extends AbstractDirtyStatePage<Select<String>> {
    @Override
    protected Select<String> createTestField() {
        return new Select<>();
    }
}
