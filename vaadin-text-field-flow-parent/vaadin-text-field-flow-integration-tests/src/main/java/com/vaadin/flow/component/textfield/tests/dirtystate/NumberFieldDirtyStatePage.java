package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-number-field/dirty-state")
public class NumberFieldDirtyStatePage extends AbstractDirtyStatePage<NumberField> {
    @Override
    protected NumberField createTestField() {
        return new NumberField();
    }
}
