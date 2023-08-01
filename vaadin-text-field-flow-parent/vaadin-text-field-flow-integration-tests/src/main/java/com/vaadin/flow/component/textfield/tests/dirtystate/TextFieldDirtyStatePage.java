package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-text-field/dirty-state")
public class TextFieldDirtyStatePage extends AbstractDirtyStatePage<TextField> {
    @Override
    protected TextField createTestField() {
        return new TextField();
    }
}
