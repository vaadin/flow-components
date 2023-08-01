package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-integer-field/dirty-state")
public class IntegerFieldDirtyStatePage
        extends AbstractDirtyStatePage<IntegerField> {
    @Override
    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
