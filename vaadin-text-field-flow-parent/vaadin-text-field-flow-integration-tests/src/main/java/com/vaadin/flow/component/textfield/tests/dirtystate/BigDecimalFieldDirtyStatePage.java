package com.vaadin.flow.component.textfield.tests.dirtystate;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-big-decimal-field/dirty-state")
public class BigDecimalFieldDirtyStatePage
        extends AbstractDirtyStatePage<BigDecimalField> {
    @Override
    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }
}
